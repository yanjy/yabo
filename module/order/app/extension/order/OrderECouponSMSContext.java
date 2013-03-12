package extension.order;

import models.order.Order;
import models.sales.Goods;
import util.extension.InvocationContext;

/**
 * 订单券短信内容上下文.
 */
public class OrderECouponSMSContext implements InvocationContext {

    public Order order;

    public Goods goods;

    /**
     * 券内容
     */
    public String couponInfo;

    /**
     * 注意事项
     */
    public String notes;

    /**
     * 有效期截止日期
     */
    public String expiredDate;

    private String smsContent;

    public OrderECouponSMSContext(Order order, Goods goods, String couponInfo, String notes, String expiredDate) {
        this.order = order;
        this.goods = goods;
        this.couponInfo = couponInfo;
        this.notes = notes;
        this.expiredDate = expiredDate;
        this.smsContent = null;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }
}