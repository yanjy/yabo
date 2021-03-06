package models.ktv;

import com.taobao.api.ApiException;
import com.taobao.api.Constants;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.request.ItemSkuDeleteRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.request.ItemSkusGetRequest;
import com.taobao.api.request.SkusQuantityUpdateRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.taobao.api.response.ItemSkuDeleteResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import com.taobao.api.response.ItemSkusGetResponse;
import com.taobao.api.response.SkusQuantityUpdateResponse;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * User: yan
 * Date: 13-4-27
 * Time: 下午1:42
 */
public class KtvTaobaoUtil {
    // 淘宝电子凭证的secret
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    public static final long defaultCid = 50644003L;

    private static final String ACTION_ADD = "add";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_UPDATE_PRICE = "updatePrice";
    private static final String ACTION_UPDATE_QUANTITY = "updateQuantity";
    public static final int MAX_SKU_COUNT = 600;  //淘宝最大SKU数为600，不允许比600更大的值
    public static final int PRE_ORDER_HOUR = 1;    //提前预订小时数，如11:00-13:00的包厢，只能提前1小时，即10:00前预订

    /**
     * 根据某一商品，更新其在淘宝上的SKU信息.
     *
     * @param productGoods KTV商品.
     */
    public static String  updateTaobaoSkuByProductGoods(KtvProductGoods productGoods) {
        Logger.info("KtvTaobaoUtil.updateTaobaoSkuByProductGoodse method start>>>priceSchedule:" + productGoods);
        //构建新的淘宝SKU列表
        SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> localSkuMap
                = buildTaobaoSku(productGoods.shop, productGoods.product, true);

        //更新该ktv产品所对应的每一个分销渠道上的商品
        List<ResalerProduct> resalerProductList = ResalerProduct.find("byGoodsAndPartnerAndDeleted",
                productGoods.goods, OuterOrderPartner.TB, DeletedStatus.UN_DELETED).fetch();
        String error = null;
        for (ResalerProduct resalerProduct : resalerProductList) {
            if (resalerProduct.resaler == null) {
                Logger.info("ktv update sku时,resalerProduct.partnerProductId=%s的resaler.id is null,will not update this resalerProduct!", resalerProduct.partnerProductId);
                continue;
            }
            if (StringUtils.isBlank(resalerProduct.partnerProductId)) {
                continue;
            }
            String e =  updateTaobaoSkuByResalerProductAndLocalSkus(resalerProduct, localSkuMap);
            if (e != null) {
                error = error == null ? e : error + " " + e;
            }
        }
        return error;
    }

    public static String updateTaobaoSkuByResalerProductAndLocalSkus(ResalerProduct resalerProduct,
               SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> localSkuMap ) {
        Long taobaoProductId = Long.parseLong(resalerProduct.partnerProductId);
        if (localSkuMap.size() == 0) {
            return "无可用SKU信息";
        }

        List<KtvTaobaoSku> remoteSkuList = getTaobaoSku(resalerProduct);

        //找到淘宝的token
        OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        /*
        * 比较两组SKU，得出四个列表，分别是：
        * add: 应该添加到淘宝的SKU列表
        * delete: 应该删除的淘宝SKU列表
        * updatePrice: 应该更新价格的淘宝SKU列表
        * updateQuantity: 应该更新数量的淘宝SKU列表（可批量更新）
        * */
        Map<String, List<KtvTaobaoSku>> diffResult =  diffSkuBetweenLocalAndRemote(localSkuMap, remoteSkuList);

        String[] actions = new String[] {ACTION_DELETE, ACTION_ADD, ACTION_UPDATE_PRICE,ACTION_UPDATE_QUANTITY};
        for (String action: actions) {
            List<KtvTaobaoSku> skuList = diffResult.get(action);
            switch (action) {
                case ACTION_ADD:
                    for (KtvTaobaoSku sku : skuList) {
                        if (sku.getQuantity() > 0) {
                            skuList.remove(sku);
                            skuList.add(0, sku);
                            break;
                        }
                    }
                    for (KtvTaobaoSku sku : skuList) {
                        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                                resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
                        String error =  addSkuOnTaobao(sku, taobaoProductId, taobaoClient, token.accessToken);
                        if (error != null) { return error; }
                    }
                    break;
                case ACTION_DELETE:
                    for (KtvTaobaoSku sku : skuList) {
                        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                                resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
                        String error =  deleteSkuOnTaobao(sku, taobaoProductId, taobaoClient, token.accessToken);
                        if (error != null) { return error; }
                    }
                    break;
                case ACTION_UPDATE_PRICE:
                    for (KtvTaobaoSku sku : skuList) {
                        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                                resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
                        String error =  updateSkuPriceOnTaobao(sku, taobaoProductId, taobaoClient, token.accessToken);
                        if (error != null) { return error; }
                    }
                    break;
                case ACTION_UPDATE_QUANTITY:
                    TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                            resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
                    String error =  updateSkuQuantityOnTaobao(skuList, taobaoProductId, taobaoClient, token.accessToken);
                    if (error != null) { return error; }
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * 构建新的淘宝SKU列表.
     * 如果传入的 shop 和 product 有对应的 KtvProductGoods，那么返回的sku中就有goods，
     * 否则就没有,SKU中只有日期、时间价格和数量这几个信息，没有 goods 信息
     *
     * @param shop      门店
     * @param product   KTV产品
     * @param isPerfect 是否构建 Perfect Tree
     *                  如果该选项为真，则表明该树是 [Perfect Tree] (https://en.wikipedia.org/wiki/Binary_tree)
     *                  依照淘宝，对于无价格/数量信息的叶子节点，我们将其价格设置为最低，数量设置为0
     *                  <p/>
     *                  如果该选项为假，则对于无价格/数量信息的叶子节点，将忽略之，不加入到Map中
     * @return 新的淘宝SKU列表（未 save 到数据库）
     */
    public static SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> buildTaobaoSku(
            Shop shop, KtvProduct product, boolean isPerfect) {
        List<KtvTaobaoSku> taobaoSkuList = new ArrayList<>();//结果集

        Calendar calendar = Calendar.getInstance();
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        Date startDay = today;
        //当天18点到24点之后sku不更新，并删除
        int pushEndHour = Integer.parseInt( product.supplier.getProperty(Supplier.KTV_SKU_PUSH_END_HOUR, "18"));
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour >= pushEndHour) {
            startDay = DateUtils.addDays(startDay, 1);
        }

        //查出与该KTV商品有关联的、today(包含)之后的所有价格
        Query query = JPA.em().createQuery(
                "select distinct(s) from KtvPriceSchedule s join s.dateRanges r join s.shopSchedules ss where "
                + "s.product = :product and s.deleted = :deleted and r.endDay >= :startDay and ss.shop = :shop");


        query.setParameter("shop", shop);
        query.setParameter("product", product);
        query.setParameter("startDay", startDay);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        List<KtvPriceSchedule> priceScheduleList = query.getResultList();


        KtvProductGoods productGoods = KtvProductGoods.find("byShopAndProduct", shop, product).first();
        Goods goods = productGoods == null ? null : productGoods.goods;

        /**
         * 实例：
         *
         * 策略A        AAAAAA     AAAAAA           AAAAAAA
         * 策略B    BBBBBB       BBBB       BBBBB              BBBBB
         * 时间     ------------------------------------------------
         *
         * 构造一个按日期顺序排列的 策略列表的 列表
         * 每一个日期，都对应 至少一个 价格策略
         *
         * 最终我们只选取 从startDay 开始往后数，不超过14天的 日期
         */
        TreeMap<Date, Set<KtvPriceSchedule>> orderedScheduleMap = new TreeMap<>();
        for (KtvPriceSchedule schedule : priceScheduleList) {
            for (KtvDateRangePriceSchedule dateRange : schedule.dateRanges) {
                DateTime sd = new DateTime(dateRange.startDay);
                DateTime ed = new DateTime(dateRange.endDay);
                int difference = Days.daysBetween(sd, ed).getDays();
                for (int i = 0 ; i <= difference ; i++) {
                    Date d = DateUtils.addDays(dateRange.startDay, i);
                    if (d.before(startDay)) {
                        continue;
                    }
                    Set<KtvPriceSchedule> schedules = orderedScheduleMap.get(d);
                    if (schedules == null) {
                        schedules = new HashSet<>();
                        orderedScheduleMap.put(d, schedules);
                    }
                    schedules.add(schedule);
                }
            }
        }

        Set<KtvRoomType> uniqRoomTypes = new HashSet<>();//唯一 房型
        Set<Date> uniqDates = new HashSet<>();//唯一日期
        Set<String> uniqTimeRanges = new HashSet<>();//唯一时间范围

        //从startDay 开始，依次往后选择不超过dateCount个不同的日期
        int dateCount = Integer.parseInt(product.supplier.getProperty(Supplier.KTV_MAX_DEPLOY_DATE, "7"));

        List<KtvRoomOrderInfo> roomOrderInfoList = new ArrayList<>();
        Date tenMinutesAgo = DateUtils.addMinutes(new Date(), - KtvRoomOrderInfo.LOCK_MINUTE);

        for (Map.Entry<Date, Set<KtvPriceSchedule>> entry : orderedScheduleMap.entrySet()) {
            dateCount -= 1;
            if (dateCount < 0) {
                continue;
            }
            Date day = entry.getKey();
            uniqDates.add(day);
            if (uniqRoomTypes.size()*uniqDates.size()*uniqTimeRanges.size() > MAX_SKU_COUNT) {
                continue;
            }
            //查出该门店的该产品这一天已经卖出、或者被锁定的房间信息
            if (goods != null){
                roomOrderInfoList = KtvRoomOrderInfo.find(
                        "goods=? and shop=? and scheduledDay = ? and  (status =? or (status=? and createdAt >=?))",
                        goods, shop, day,  KtvOrderStatus.DEAL, KtvOrderStatus.LOCK, tenMinutesAgo).fetch();
            }

            for (KtvPriceSchedule schedule : entry.getValue()) {
                uniqRoomTypes.add(schedule.roomType);
                if (uniqRoomTypes.size()*uniqDates.size()*uniqTimeRanges.size() > MAX_SKU_COUNT) {
                    uniqRoomTypes.remove(schedule.roomType);
                    continue;
                }

                //查出门店数量
                KtvShopPriceSchedule shopPriceSchedule = KtvShopPriceSchedule.find("byShopAndSchedule", shop, schedule).first();
                //取出该房型下的时间安排
                SortedSet<Integer> startTimeArray = schedule.getStartTimesAsSet();
                for (Integer startTime : startTimeArray) {
                    //今天PRE_ORDER_HOUR小时之内的不能预订
                    if (day.compareTo(today) == 0 && startTime <= (currentHour + PRE_ORDER_HOUR)) {
                        continue;
                    }
                    //房型数 x 日期数 x 时间段数 不能超过600
                    String t = startTime + "-" + (startTime + product.duration);
                    uniqTimeRanges.add(t);
                    if (uniqRoomTypes.size()*uniqDates.size()*uniqTimeRanges.size() > MAX_SKU_COUNT) {
                        uniqTimeRanges.remove(t);
                        continue;
                    }
                    int roomCountLeft = shopPriceSchedule.roomCount;
                    //排除掉已预订的房间所占用的数量
                    for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
                        if (!orderInfo.product.id.equals(product.id)) {
                            continue;
                        }
                        if (orderInfo.roomType != schedule.roomType) {
                            continue;
                        }
                        int st = startTime < 8 ? startTime + 24 : startTime;
                        int ost = orderInfo.scheduledTime < 8 ? orderInfo.scheduledTime + 24 : orderInfo.scheduledTime;
                        if (ost < (st + product.duration) && (ost + orderInfo.product.duration) > st) {
                            roomCountLeft -= 1;
                        }
                    }
                    //如果预订满了，就不再有此SKU
                    if (roomCountLeft <= 0) {
                        continue;
                    }

                    KtvTaobaoSku sku = new KtvTaobaoSku();
                    sku.setRoomType(schedule.roomType);
                    sku.setDate(day);
                    sku.setPrice(schedule.price);
                    sku.setQuantity(roomCountLeft);
                    sku.setStartTimeAndDuration(startTime, product.duration);
                    taobaoSkuList.add(sku);
                }
            }
        }
        return skuListToMap(taobaoSkuList, isPerfect);
    }

    /**
     * 返回 以 包厢 -》 日期 -》 时长 三级形式的 树状 sku map
     *
     * @param perfect 是否构建 Perfect Tree
     *                如果该选项为真，则表明该树是 [Perfect Tree] (https://en.wikipedia.org/wiki/Binary_tree)
     *                依照淘宝，对于无价格/数量信息的叶子节点，我们将其价格设置为最低，数量设置为0
     *                <p/>
     *                如果该选项为假，则对于无价格/数量信息的叶子节点，将忽略之，不加入到Map中
     *                <p/>
     *                (发布到淘宝后台，是让淘宝后台以包厢-》时长-》日期 的形式显示的，不影响逻辑。以上面所说的三级形保存，是为了方便在我方页面显示相应信息)
     */
    public static SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> skuListToMap(
            List<KtvTaobaoSku> skuList, boolean perfect) {
        SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> result = new TreeMap<>();
        for (KtvTaobaoSku sku : skuList) {
            SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>> skuMapByRoomType = result.get(sku.getRoomType());
            if (skuMapByRoomType == null) {
                skuMapByRoomType = new TreeMap<>();
                result.put(sku.getRoomType(), skuMapByRoomType);
            }
            SortedMap<Integer, KtvTaobaoSku> skuMapByDate = skuMapByRoomType.get(sku.getDate());
            if (skuMapByDate == null) {
                skuMapByDate = new TreeMap<>();
                skuMapByRoomType.put(sku.getDate(), skuMapByDate);
            }
            skuMapByDate.put(sku.getTimeRangeCode(), sku);
        }
        if (!perfect) {
            return result;
        }

        //构建 Perfect Tree

        Set<KtvRoomType> roomTypeSet = new HashSet<>();
        Set<Date> daySet = new HashSet<>();
        Set<Integer> timeRangeCodeSet = new HashSet<>();
        BigDecimal price = null;//最低价
        for (KtvTaobaoSku sku : skuList) {
            if (sku.getQuantity() == 0) {
                continue;
            }
            roomTypeSet.add(sku.getRoomType());
            daySet.add(sku.getDate());
            timeRangeCodeSet.add(sku.getTimeRangeCode());
            if (price == null) {
                price = sku.getPrice();
            } else if (price.compareTo(sku.getPrice()) > 0) {
                price = sku.getPrice();
            }

        }
        for (KtvRoomType roomType : roomTypeSet) {
            for (Date day : daySet) {
                for (Integer timeRangeCode : timeRangeCodeSet) {
                    SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>> skuMapByRoomType = result.get(roomType);
                    if (skuMapByRoomType == null) {
                        skuMapByRoomType = new TreeMap<>();
                        result.put(roomType, skuMapByRoomType);
                    }
                    SortedMap<Integer, KtvTaobaoSku> skuMapByDate = skuMapByRoomType.get(day);
                    if (skuMapByDate == null) {
                        skuMapByDate = new TreeMap<>();
                        skuMapByRoomType.put(day, skuMapByDate);
                    }
                    if (skuMapByDate.get(timeRangeCode) == null) {
                        //填充
                        KtvTaobaoSku sku = new KtvTaobaoSku();
                        sku.setRoomType(roomType);
                        sku.setDate(day);
                        sku.setPrice(price);
                        sku.setQuantity(0);
                        int startTime = timeRangeCode/100;
                        int endTime = timeRangeCode%100;
                        if (endTime< startTime) {
                            endTime = endTime + 24;
                        }
                        sku.setStartTimeAndDuration(startTime, endTime -startTime);
                        skuMapByDate.put(timeRangeCode, sku);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param skuMap sku 树
     * @param reduce 是否输出简化列表
     *               如果该选项为真，则输出时剔除数量为0的SKU
     *               如果该选项为假，则完成输出map中的所有sku
     * @return sku 列表
     */
    public static List<KtvTaobaoSku> skuMapToList(SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> skuMap, boolean reduce) {
        List<KtvTaobaoSku> skuList = new ArrayList<>();
        for (SortedMap.Entry<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> entryA : skuMap.entrySet()) {
            for (SortedMap.Entry<Date, SortedMap<Integer, KtvTaobaoSku>> entryB : entryA.getValue().entrySet()) {
                for (Map.Entry<Integer, KtvTaobaoSku> entryC : entryB.getValue().entrySet()) {
                    KtvTaobaoSku sku = entryC.getValue();
                    if (sku.getQuantity() == 0 && reduce) {
                        continue;
                    }
                    skuList.add(sku);
                }
            }
        }
        return skuList;
    }


    /**
     * 传过来本地的完美树和远端的SKU列表
     *
     * 比较两组SKU，得出四个列表，分别是：
     * add: 应该添加到淘宝的SKU列表
     * delete: 应该删除的淘宝SKU列表
     * updatePrice: 应该更新价格的淘宝SKU列表
     * updateQuantity: 应该更新数量的淘宝SKU列表（可批量更新）
     *
     * @param localSkuMap 本地新计算出来的SKU 必须是完美树
     * @param remoteSkuList 淘宝的SKU列表
     * @return 比较结果
     */
    public static Map<String, List<KtvTaobaoSku>> diffSkuBetweenLocalAndRemote(
            SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> localSkuMap,
            List<KtvTaobaoSku> remoteSkuList ) {

        List<KtvTaobaoSku> localSkuList = skuMapToList(localSkuMap, false);
        SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> remoteSkuMap  = skuListToMap(remoteSkuList, false);

        Set<String> localSkuIdentities = new HashSet<>();
        Set<String> remoteSkuIdentities = new HashSet<>();

        for (KtvTaobaoSku sku : localSkuList) {
            localSkuIdentities.add(sku.getTaobaoOuterIid());
        }
        for (KtvTaobaoSku sku : remoteSkuList) {
            remoteSkuIdentities.add(sku.getTaobaoOuterIid());
        }

        Set<String> tobeAddedIdentities = new HashSet<String>(CollectionUtils.subtract(localSkuIdentities, remoteSkuIdentities));
        Set<String> tobeDeletedIdentities = new HashSet<String>(CollectionUtils.subtract(remoteSkuIdentities, localSkuIdentities));
        Set<String> tobeUpdatedIdentities = new HashSet<String>(CollectionUtils.intersection(localSkuIdentities, remoteSkuIdentities));

        List<KtvTaobaoSku> tobeAddedSkuList = new ArrayList<>();
        //处理添加的
        for (String identity : tobeAddedIdentities) {
            KtvTaobaoSku tmp = new KtvTaobaoSku().parseTaobaoOuterId(identity);
            KtvTaobaoSku sku = localSkuMap.get(tmp.getRoomType()).get(tmp.getDate()).get(tmp.getTimeRangeCode());
            tobeAddedSkuList.add(sku);
        }
        //处理删除的
        List<KtvTaobaoSku> tobeDeletedSkuList = new ArrayList<>();
        for (String identity : tobeDeletedIdentities) {
            KtvTaobaoSku tmp = new KtvTaobaoSku().parseTaobaoOuterId(identity);
            KtvTaobaoSku sku = remoteSkuMap.get(tmp.getRoomType()).get(tmp.getDate()).get(tmp.getTimeRangeCode());
            tobeDeletedSkuList.add(sku);
        }

        //处理更新的。分两种情况，一个列表是更新包含价格的，一个列表是只更新数量的
        List<KtvTaobaoSku> tobeUpdatedPriceSkuList = new ArrayList<>();
        List<KtvTaobaoSku> tobeUpdatedOnlyQuantityList = new ArrayList<>();

        for (String identity : tobeUpdatedIdentities) {
            KtvTaobaoSku tmp = new KtvTaobaoSku().parseTaobaoOuterId(identity);
            KtvTaobaoSku localSku = localSkuMap.get(tmp.getRoomType()).get(tmp.getDate()).get(tmp.getTimeRangeCode());
            KtvTaobaoSku remoteSku = remoteSkuMap.get(tmp.getRoomType()).get(tmp.getDate()).get(tmp.getTimeRangeCode());

            //更新包含价格
            if (localSku.getPrice().compareTo(remoteSku.getPrice()) != 0) {
                remoteSku.setPrice(localSku.getPrice());
                remoteSku.setQuantity(localSku.getQuantity());
                tobeUpdatedPriceSkuList.add(remoteSku);
                continue;
            }
            //只更新数量
            if (!localSku.getQuantity().equals(remoteSku.getQuantity())) {
                remoteSku.setQuantity(localSku.getQuantity());
                tobeUpdatedOnlyQuantityList.add(remoteSku);
            }
        }

        Map<String, List<KtvTaobaoSku>> result = new HashMap<>();
        result.put(ACTION_ADD, tobeAddedSkuList);
        result.put(ACTION_DELETE, tobeDeletedSkuList);
        result.put(ACTION_UPDATE_PRICE, tobeUpdatedPriceSkuList);
        result.put(ACTION_UPDATE_QUANTITY, tobeUpdatedOnlyQuantityList);
        return result;
    }


    public static List<KtvTaobaoSku> getTaobaoSku(ResalerProduct resalerProduct) {
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
        //找到淘宝的token
        OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        ItemSkusGetRequest request = new ItemSkusGetRequest();
        request.setNumIids(resalerProduct.partnerProductId);
        request.setFields("sku_id,quantity,price,outer_id");
        try {
            ItemSkusGetResponse response = taobaoClient.execute(request, token.accessToken);
            if (response.isSuccess()) {
                List<KtvTaobaoSku> skuList = new ArrayList<>();
                if (response.getSkus() == null) {
                    return skuList;
                }
                for (Sku sku : response.getSkus()) {
                    Logger.info("sku on taobao %s: %s;%s;%s;%s", resalerProduct.partnerProductId,
                            sku.getSkuId(), sku.getQuantity(), sku.getPrice() , sku.getOuterId());
                    KtvTaobaoSku s = new KtvTaobaoSku();
                    s.setTaobaoSkuId(sku.getSkuId());
                    s.setQuantity(sku.getQuantity().intValue());
                    s.setPrice(new BigDecimal(sku.getPrice()));
                    if ( s.parseTaobaoOuterId(sku.getOuterId()) == null){
                        Logger.error("parse taobao sku error: %s : %s", request.getNumIids(), sku.getOuterId());
                        continue;
                    }
                    skuList.add(s);
                }
                return skuList;
            }
        }catch (ApiException e) {
            Logger.error("get taobao sku failed " + resalerProduct.partnerProductId);
        }
        return null;
    }


    /**
     * 在淘宝上删除一个SKU
     */
    public static String deleteSkuOnTaobao(KtvTaobaoSku sku, long iid, TaobaoClient taobaoClient, String accessToken) {
        ItemSkuDeleteRequest deleteRequest = new ItemSkuDeleteRequest();
        deleteRequest.setNumIid(iid);
        deleteRequest.setProperties(sku.getTaobaoProperties());

        Logger.info("delete taobao sku %s : %s", iid, deleteRequest.getProperties());
        try {
            ItemSkuDeleteResponse response = taobaoClient.execute(deleteRequest, accessToken);
            if (!response.isSuccess()) {
                return "code: " + response.getErrorCode() + "; msg: " + response.getMsg() +
                        "subCode: " + response.getSubCode() + "; subMsg: " + response.getSubMsg();
            }
        } catch (ApiException e) {
            return "删除淘宝SKU时发生异常 " + iid + " : " + deleteRequest.getProperties();
        }
        return null;
    }

    /**
     * 在淘宝上更新一个SKU(包含价格)
     */
    public static String updateSkuPriceOnTaobao(KtvTaobaoSku sku, long iid, TaobaoClient taobaoClient, String accessToken) {
        ItemSkuUpdateRequest updateRequest = new ItemSkuUpdateRequest();
        updateRequest.setNumIid(iid);
        updateRequest.setProperties(sku.getTaobaoProperties());
        updateRequest.setQuantity(sku.getQuantity().longValue());
        updateRequest.setPrice(sku.getPrice().toString());
        updateRequest.setItemPrice(sku.getPrice().toString());

        Logger.info("update taobao sku price %s : %s", iid, updateRequest.getProperties());

        try {
            ItemSkuUpdateResponse response = taobaoClient.execute(updateRequest, accessToken);
            if (!response.isSuccess()) {
                return "code: " + response.getErrorCode() + "; msg: " + response.getMsg() +
                        "subCode: " + response.getSubCode() + "; subMsg: " + response.getSubMsg();
            }
        } catch (ApiException e) {
            return "更新淘宝SKU时发生异常 " + iid + " : " + updateRequest.getProperties();
        }
        return null;
    }

    /**
     * 在淘宝上批量更新SKU数量
     */
    public static String updateSkuQuantityOnTaobao(List<KtvTaobaoSku> skuList, long iid, TaobaoClient taobaoClient, String accessToken) {
        if (skuList.size() == 0) {
            return null;
        }
        List<String> quantities = new ArrayList<>(skuList.size());
        for (KtvTaobaoSku sku : skuList) {
            quantities.add(sku.getTaobaoSkuId() + ":" + sku.getQuantity());
        }

        SkusQuantityUpdateRequest updateRequest = new SkusQuantityUpdateRequest();
        updateRequest.setNumIid(iid);
        updateRequest.setType(1L);//全量更新
        updateRequest.setSkuidQuantities(StringUtils.join(quantities, ";"));

        Logger.info("batch update taobao sku price %s : %s", iid, updateRequest.getSkuidQuantities());
        try {
            SkusQuantityUpdateResponse response = taobaoClient.execute(updateRequest, accessToken);
            if (!response.isSuccess()) {
                return "code: " + response.getErrorCode() + "; msg: " + response.getMsg() +
                        "subCode: " + response.getSubCode() + "; subMsg: " + response.getSubMsg();
            }
        } catch (ApiException e) {
            return "更新淘宝SKU时发生异常 " + iid + " : " + updateRequest.getSkuidQuantities();
        }
        return null;
    }

    /**
     * 在淘宝上添加一个SKU
     */
    public static String addSkuOnTaobao(KtvTaobaoSku sku, long iid, TaobaoClient taobaoClient, String accessToken) {
        ItemSkuAddRequest addRequest = new ItemSkuAddRequest();
        addRequest.setNumIid(iid);
        addRequest.setProperties(sku.getTaobaoProperties());
        addRequest.setQuantity(sku.getQuantity().longValue());
        addRequest.setPrice(sku.getPrice().toString());
        addRequest.setOuterId(sku.getTaobaoOuterIid());

        Logger.info("add taobao sku %s : %s", iid, addRequest.getProperties());
        try {
            ItemSkuAddResponse response = taobaoClient.execute(addRequest, accessToken);
            if (!response.isSuccess()) {
                return "code: " + response.getErrorCode() + "; msg: " + response.getMsg() +
                        "subCode: " + response.getSubCode() + "; subMsg: " + response.getSubMsg();
            }
        } catch (ApiException e) {
            return "添加淘宝SKU时发生异常 " + iid + " : " + addRequest.getProperties();
        }
        return null;
    }

}
