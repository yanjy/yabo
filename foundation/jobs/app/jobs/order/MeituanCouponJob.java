package jobs.order;

import com.google.gson.JsonObject;
import models.accounts.PaymentSource;
import models.admin.SupplierUser;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.Shop;
import models.sales.SupplierResalerShop;
import play.Logger;
import play.jobs.Every;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User: wangjia
 * Date: 13-7-24
 * Time: 上午9:43
 */
@JobDefine(title = "美团券生成", description = "处理OuterOrder中未生成的订单，生成券不发送")
@Every("1mn")
public class MeituanCouponJob extends JobWithHistory {

    @Override
    public void doJobWithHistory() {
        Logger.info("start meituan coupon job");
        List<OuterOrder> outerOrders = OuterOrder.find("(partner = ? or partner = ? or partner = ? or resaler=? or resaler=? ) and  status = ?",
                OuterOrderPartner.MT,
                OuterOrderPartner.DP,
                OuterOrderPartner.NM,
                Resaler.findApprovedByLoginName("nanjinglashou"),
                Resaler.findApprovedByLoginName("wowotuan"),
                OuterOrderStatus.ORDER_COPY).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            Logger.info("outerOrder orderId = %s", outerOrder.orderId);
            JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
            Long goodsId = jsonObject.get("goodsId").getAsLong();
            Long shopId = jsonObject.get("shopId").getAsLong();
            Goods goods = Goods.findById(goodsId);
            //生成一百券订单
            Order order = createYbqOrder(outerOrder, goods, shopId, null,
                    goods.salePrice);
            outerOrder.ybqOrder = order;
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
            Logger.info("create partner order successfully, orderId = %s", order.id);
        }
    }

    private static Order createYbqOrder(OuterOrder outerOrder, Goods goods, Long shopId,
                                        String mobile, BigDecimal salePrice) {
        Order ybqOrder = Order.createResaleOrder(outerOrder.resaler);
        ybqOrder.save();

        OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, 1L, mobile, salePrice, salePrice);
        uhuilaOrderItem.save();
        if (goods.materialType.equals(MaterialType.REAL)) {
            ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
            ybqOrder.deliveryType = DeliveryType.SMS;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        ECoupon c = ECoupon.find("byOrder", ybqOrder).first();
        c.partnerCouponId = outerOrder.orderId;
        c.save();

        Shop shop = Shop.findById(shopId);
        c.refresh();
        SupplierUser supplierUser = SupplierUser.find("byShop", shop).first();
        c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY);
        c.partner = getECouponPartner(outerOrder.partner.toString());
        c.save();

        ECouponHistoryMessage.with(c).remark("系统定时自动生成" + outerOrder.partner.partnerName() + "订单,成功后自动验证")
                .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();


        return ybqOrder;
    }

    private static ECouponPartner getECouponPartner(String partner) {
        if ("DP".equals(partner)) {
            return ECouponPartner.DP;
        }
        if ("MT".equals(partner)) {
            return ECouponPartner.MT;
        }
        if ("NM".equals(partner)) {
            return ECouponPartner.NM;
        }
        if ("LS".equals(partner)) {
            return ECouponPartner.LS;
        }
        if ("WW".equals(partner)) {
            return ECouponPartner.WW;
        }
        return null;
    }

}
