package models.job;

import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import util.DateHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 如果ECoupon.smsSentCount为0，表明这个券从来没有发送成功过短信。
 * 通过这个Job，尝试重新发送。
 * User: tanglq
 * Date: 13-1-31
 * Time: 下午1:32
 */
@Every("5mn")
public class RetrySendFailedOrderSMSJob extends Job {
    @Override
    public void doJob() throws Exception {
        // 找出5分钟前，2小时内的券号
        List<ECoupon> eCoupons = ECoupon.find("createdAt>=? and createdAt<=? and status=? and smsSentCount=0",
                DateHelper.beforeHours(2), DateHelper.beforeMinuts(5), ECouponStatus.UNCONSUMED).fetch();

        Logger.info("RetrySendFailedOrderSMSJob 找到" + eCoupons.size() + "条没有成功发送的短信.");
        Set<OrderItems> orderItemsSet = new HashSet<>();
        for (ECoupon eCoupon : eCoupons) {
            orderItemsSet.add(eCoupon.orderItems);
        }

        for (OrderItems item : orderItemsSet) {
            Logger.info("RetrySendFailedOrderSMSJob: 重发OrderItems(" + item.id + ").");
            OrderECouponMessage.with(item).remark("默认短信没有成功，重发").sendToMQ();
        }
    }
}
