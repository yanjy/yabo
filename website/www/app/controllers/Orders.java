package controllers;

import static play.Logger.warn;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.DeliveryType;
import models.order.DiscountCode;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderDiscount;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.MaterialType;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

/**
 * 用户订单确认控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 11:31 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class Orders extends Controller {
    public static String WWW_URL = Play.configuration.getProperty("application.baseUrl", "");

    /**
     * 预览订单.
     *
     * @param gid=37&gid=102&g37=2&g102=3
     */
    public static void index(List<Long> gid) {

        if (gid == null || gid.size() == 0) {
            error("未选择商品!");
            return;
        }
        
        Map<String, String[]> params = request.params.all();
        String items = "";
        for (Long goodsId : gid) {
            String[] numberStr = params.get("g" + goodsId);
            int number = 1;
            if (numberStr != null && numberStr.length > 0) {
                number = Integer.parseInt(numberStr[0]);
            }
            items += goodsId + "-" + number + ",";
        }
        
        DiscountCode discountCode = getDiscountCode();
        
        showOrder(items, discountCode);

        User user = SecureCAS.getUser();
        List<String> orderItems_mobiles = OrderItems.getMobiles(user);

        render(user, orderItems_mobiles);
    }

    protected static DiscountCode getDiscountCode() {
        // 折扣券
        String discountSN = request.params.get("discountSN");
        if (StringUtils.isEmpty(discountSN) && WebsiteInjector.getUserWebIdentification() != null) {
            // 访问使用的推荐码尝试作为折扣券号
            discountSN = WebsiteInjector.getUserWebIdentification().referCode;
        } else {
            renderArgs.put("discountSN", discountSN);
        }
        DiscountCode discountCode = DiscountCode.findAvaiableSN(discountSN);
        renderArgs.put("discountCode", discountCode);
        return discountCode;
    }

    private static void showOrder(String items, DiscountCode discountCode) {
        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long, Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);

        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;

        List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
        for (models.sales.Goods g : goods) {

            Integer number = itemsMap.get(g.getId());
            if (g.materialType == models.sales.MaterialType.REAL) {
                rCartList.add(new Cart(g, number));
                rCartAmount = rCartAmount.add(Order.getDiscountGoodsAmount(g, number, discountCode));
            } else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {
                eCartList.add(new Cart(g, number));
                eCartAmount = eCartAmount.add(Order.getDiscountGoodsAmount(g, number, discountCode));
            }
        }

        if (rCartList.size() == 0 && eCartList.size() == 0) {
            error("no goods specified");
            return;
        }

        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
        Address defaultAddress = Address.findDefault(SecureCAS.getUser());

        //如果有实物商品，加上运费
        if (rCartList.size() > 0) {
            rCartAmount = rCartAmount.add(Order.FREIGHT);
        }
        
        // 整单折扣，注意只折扣电子券产品，实物券不参与折扣.
        eCartAmount = Order.getDiscountTotalECartAmount(eCartAmount, discountCode);
        
        BigDecimal totalAmount = eCartAmount.add(rCartAmount);
        BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(Order.FREIGHT);

        renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        renderArgs.put("addressList", addressList);
        renderArgs.put("address", defaultAddress);
        renderArgs.put("eCartList", eCartList);
        renderArgs.put("eCartAmount", eCartAmount);
        renderArgs.put("rCartList", rCartList);
        renderArgs.put("rCartAmount", rCartAmount);
        renderArgs.put("items", items);
        renderArgs.put("querystring", request.querystring);
    }

    /**
     * 提交订单.
     */
    public static void create(String items, String mobile, String remark) {
        //如果订单中有电子券，则必须填写手机号
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long, Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);
        List<models.sales.Goods> goodsList = models.sales.Goods.findInIdList(goodsIds);
        boolean containsElectronic = containsMaterialType(goodsList, MaterialType.ELECTRONIC);
        boolean containsReal = containsMaterialType(goodsList, MaterialType.REAL);

        //电子券必须校验手机号
        if (containsElectronic) {
            Validation.required("mobile", mobile);
            Validation.match("mobile", mobile, "^1\\d{10}$");
        }

        //实物券必须校验收货地址信息
        Address defaultAddress = null;
        String receiverMobile = "";
        if (containsReal) {
            defaultAddress = Address.findDefault(SecureCAS.getUser());
            if (defaultAddress == null) {
                Validation.addError("address", "validation.required");
            } else {
                receiverMobile = defaultAddress.mobile;
            }
        }

        DiscountCode discountCode = getDiscountCode();

        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            List<String> orderItems_mobiles = OrderItems.getMobiles(user);
                 
            showOrder(items, discountCode);
            render("Orders/index.html", user, orderItems_mobiles);
        }

        //创建订单
        Order order = Order.createConsumeOrder(user.getId(), AccountType.CONSUMER);
        if (containsElectronic) {
            order.deliveryType = DeliveryType.SMS;
        } else if (containsReal) {
            order.deliveryType = DeliveryType.LOGISTICS;
        }
        
        //记录来源跟踪ID
        if (WebsiteInjector.getUserWebIdentification() != null) {
            order.webIdentificationId = WebsiteInjector.getUserWebIdentification().id;
        }

        if (defaultAddress != null) {
            order.setAddress(defaultAddress);
        }
        order.save();  //为了保存OrderDiscount，需要先保存order.

        //添加订单条目
        try {
            //计算电子商品列表和非电子商品列表
            BigDecimal eCartAmount = BigDecimal.ZERO;
            BigDecimal rCartAmount = BigDecimal.ZERO;

            for (models.sales.Goods goodsItem : goodsList) {
                Integer number = itemsMap.get(goodsItem.getId());
                if (goodsItem.materialType == models.sales.MaterialType.REAL) {
                    rCartAmount = rCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));
                } else if (goodsItem.materialType == models.sales.MaterialType.ELECTRONIC) {
                    eCartAmount = eCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));
                }        
                OrderItems orderItem = null;
                if (goodsItem.materialType == MaterialType.REAL) {
                    orderItem = order.addOrderItem(goodsItem, number, receiverMobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode
                    );
                } else {
                    orderItem = order.addOrderItem(goodsItem, number, mobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode
                    );
                }
                orderItem.save();
                
                // 保存商品折扣
                if (discountCode != null && discountCode.goods != null && discountCode.goods.id == goodsItem.id) {
                    OrderDiscount orderDiscount = new OrderDiscount();
                    orderDiscount.discountCode = discountCode;
                    orderDiscount.order = order;
                    orderDiscount.orderItem = orderItem;
                    orderDiscount.discountAmount = Order.getDiscountValueOfGoodsAmount(goodsItem, number, discountCode);
                    orderDiscount.save();
                }
            }

            // 整单折扣，注意只折扣电子券产品，实物券不参与折扣.
            if (discountCode != null && discountCode.goods == null) {
                eCartAmount = Order.getDiscountTotalECartAmount(eCartAmount, discountCode);
                order.amount = eCartAmount.add(rCartAmount);
                order.needPay = order.amount;
            
                OrderDiscount orderDiscount = new OrderDiscount();
                orderDiscount.discountCode = discountCode;
                orderDiscount.order = order;
                orderDiscount.discountAmount = Order.getDiscountValueOfTotalECartAmount(eCartAmount, discountCode);
                orderDiscount.save();
            }
        } catch (NotEnoughInventoryException e) {
            //todo 缺少库存
            Logger.error(e, "inventory not enough");
            error("inventory not enough");
        }
        order.remark = remark;

        //确认订单
        order.createAndUpdateInventory();
        
        //删除购物车中相应物品
        Cart.delete(user, cookieValue, goodsIds);

        redirect("/payment_info/" + order.orderNumber);
    }

    private static boolean containsMaterialType(List<Goods> goods, MaterialType type) {
        boolean containsElectronic = false;
        for (Goods good : goods) {
            if (type.equals(good.materialType)) {
                containsElectronic = true;
                break;
            }
        }
        return containsElectronic;
    }

    private static void parseItems(String items, List<Long> goodsIds, Map<Long, Integer> itemsMap) {
        User user = SecureCAS.getUser();
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {
            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                Integer number = Integer.parseInt(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
                    boolean isBuyFlag = Order.checkLimitNumber(user, goodsId, boughtNumber, number);
                    if (isBuyFlag) {
                        redirect(WWW_URL + "/g/" + goodsId);
                        return;
                    }
                    //取出商品的限购数量
                    Goods goods = Goods.findById(goodsId);
                    int limitNumber = 0;
                    if (goods.limitNumber != null) {
                        limitNumber = goods.limitNumber;
                    }
                    if ((limitNumber > boughtNumber) && number > (limitNumber - boughtNumber.intValue())) {
                        number = limitNumber - boughtNumber.intValue();
                    }
                    goodsIds.add(goodsId);
                    itemsMap.put(goodsId, number);
                }
            }
        }
    }

    /**
     * 判断限购数量
     *
     * @param items 商品列表
     */

    public static void checkLimitNumber(String items) {
        User user = SecureCAS.getUser();
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {
            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                Integer number = Integer.parseInt(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
                    boolean isCanBuyFlag = Order.checkLimitNumber(user, goodsId, boughtNumber, number);
                    if (isCanBuyFlag) {
                        renderJSON("1");
                    }
                }
            }
        }
        renderJSON("0");
    }
}
