package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.util.DateUtil;
import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvRoomType;
import models.order.*;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.classloading.enhancers.LocalvariablesNamesEnhancer;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * User: yan
 * Date: 13-3-22
 * Time: 下午4:36
 */
@With({SecureCAS.class})
@TargetOAuth(OAuthType.SINA)
public class WebSinaVouchers extends Controller {

    /**
     * 从新浪微博入口，展示页面
     *
     * @param productId 产品ID
     */
    @SkipCAS
    public static void showProduct(String productId, String source) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Collection<Shop> shops = goods.getShopList();


        String templatePath = "WebSinaVouchers/showProduct" + StringUtils.capitalize(StringUtils.trimToEmpty(source)) + ".html";
        renderTemplate(templatePath, goods, shops, productId);
    }

    /**
     * 下单页面
     *
     * @param productId 商品ID
     */
    public static void showOrder(String productId) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null || goods.status != GoodsStatus.ONSALE) {
            error("no goods!");
        }

        User user = SecureCAS.getUser();

        //判断是否ktv商户，读取价格等信息
        initKtvPage(productId, goods, user);

        render(goods, productId, user);
    }

    private static void initKtvPage(String productId, Goods goods, User user) {
        if (goods.getSupplierProperty(Supplier.KTV_SUPPLIER)) {
            Collection<Shop> shops = goods.getShopList();
            if (shops.size() == 0) {
                error("no shop found");
            }
            Shop shop = shops.iterator().next();
            Long shopId = shop.id;
            renderTemplate("WebSinaVouchers/ktvorder.html", goods, productId, shopId, user);
        }
    }

    /**
     * 创建订单
     */
    public static void order(String productId, Long buyCount, String phone, Date scheduledDay, String source) {
        User user = SecureCAS.getUser();
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Validation.required("phone", phone);
        Validation.match("phone", phone, "^1\\d{10}$");
        String pageUrl = "ktvorder.html";
        //ktv商户不需要验证购买数量
        if (!goods.getSupplierProperty(Supplier.KTV_SUPPLIER)) {
            pageUrl = "showOrder.html";
            Validation.required("buyCount", buyCount);
        }

        if (Validation.hasErrors()) {
            //判断是否ktv商户，读取价格等信息
            renderArgs.put("phone", phone);
            initKtvPage(productId, goods, user);
            render("WebSinaVouchers/" + pageUrl, goods, productId);
        }
        Resaler resaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);
        if (resaler == null) {
            error("not found this resaler!");
        }
        //创建订单
        Order order = Order.createConsumeOrder(user, resaler).save();
        OrderItems orderItems = null;
        try {
            //页面根据包厢ID,取得该时间段的价格信息
            if (goods.getSupplierProperty(Supplier.KTV_SUPPLIER)) {
                Collection<Shop> shops = goods.getShopList();
                Shop shop = shops.iterator().next();
                for (String key : request.params.all().keySet()) {
                    if (key.startsWith("roomId")) {
                        String[] values = request.params.getAll(key);
                        String[] scheduledTimes = values[0].split(",");
                        Long roomId = Long.valueOf(key.substring("roomId".length()));
                        BigDecimal salePrice = BigDecimal.ZERO;
                        for (String scheduledTime : scheduledTimes) {
                            KtvRoom ktvRoom = KtvRoom.findById(roomId);
                            List<KtvRoomOrderInfo> scheduledRoomList = KtvRoomOrderInfo.findScheduledInfos(scheduledDay, shop, ktvRoom, scheduledTime);
                            if (scheduledRoomList.size() > 0) {
                                error("该包厢已被他人预定！");
                            }
                            KtvPriceSchedule ktvPriceSchedule = KtvPriceSchedule.findPrice(scheduledDay, scheduledTime, ktvRoom.roomType);
                            salePrice = salePrice.add(ktvPriceSchedule.price);
                            new KtvRoomOrderInfo(goods, null, ktvRoom, ktvRoom.roomType, scheduledDay, scheduledTime).save();
                        }
                        //eCoupon.originalPrice=eCoupon.salePrice*(goods.originalPrice/goods.salePrice)
                        orderItems = order.addOrderItem(goods, 1L, phone, salePrice, salePrice);
                        orderItems.outerGoodsNo = productId;
                        orderItems.originalPrice = salePrice.multiply(goods.originalPrice.divide(goods.salePrice,RoundingMode.FLOOR)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        orderItems.save();

                        List<KtvRoomOrderInfo> ktvRoomOrderInfoList = KtvRoomOrderInfo.findByOrderItem(orderItems);
                        for (KtvRoomOrderInfo orderInfo : ktvRoomOrderInfoList) {
                            orderInfo.orderItem = orderItems;
                            orderInfo.save();
                        }
                    }
                }

            } else {
                orderItems = order.addOrderItem(goods, buyCount, phone, goods.getResalePrice(), goods.getResalePrice());
                orderItems.outerGoodsNo = productId;
                orderItems.save();
            }

        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory is not enough!");
            error("库存不足！goodsId=" + goods.id);
        }

        order.deliveryType = DeliveryType.SMS;
        order.generateOrderDescription();
        order.discountPay = order.needPay;
        order.accountPay = BigDecimal.ZERO;
        order.save();

        user.updateMobile(phone);

        PaymentSource paymentSource = PaymentSource.findByCode("sina");
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);

        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress, source);

        PaymentJournal.savePayRequestJournal(
                order.orderNumber,
                order.description,
                order.discountPay.toString(),
                paymentSource.paymentCode,
                paymentSource.subPaymentCode,
                request.remoteAddress,
                form);
        render(form);

    }

    /**
     * 检查是否从手机或除PC以外的设备访问页面
     *
     * @return
     */
    private static boolean isMobile() {
//        Http.Header headAgent = request.headers.get("user-agent");
//        String userAgent = headAgent.value();
//        for (String mobile : MOBILE_SPECIFIC_SUBSTRING) {
//            if (userAgent.contains(mobile)
//                    || userAgent.contains(mobile.toUpperCase())
//                    || userAgent.contains(mobile.toLowerCase())) {
//                return true;
//            }
//        }
//
//        return false;
        return true;
    }

    @Before
    private static void mobileCheck() {
        String url = request.url;
        if (isMobile()) {
            if (!url.startsWith(WEIBO_WAP)) {
                redirect(WEIBO_WAP + url.substring(6));
            }
        }
    }


    static final String WEIBO_WAP = "/weibo/wap";

    static final String[] MOBILE_SPECIFIC_SUBSTRING = {
            "iPad", "iPhone", "Android", "MIDP", "Opera Mobi",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile",
            "MSIEMobile", "Windows Phone", "HTC", "LG",
            "MOT", "Nokia", "Symbian", "Fennec",
            "Maemo", "Tear", "Midori", "armv",
            "Windows CE", "WindowsCE", "Smartphone", "240x320",
            "176x220", "320x320", "160x160", "webOS",
            "Palm", "Sagem", "Samsung", "SGH",
            "SIE", "SonyEricsson", "MMP", "UCWEB"};
}
