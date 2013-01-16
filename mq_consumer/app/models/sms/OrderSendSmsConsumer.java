package models.sms;

import models.RabbitMQConsumerWithTx;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.order.OrderStatus;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

import java.util.List;

/**
 * 发送订单的短信，如果有多个短信，会一次发掉。
 */
@OnApplicationStart(async = true)
public class OrderSendSmsConsumer extends RabbitMQConsumerWithTx<OrderECouponMessage> {
    private final String SMS_TYPE = Play.configuration.getProperty("sms.type");

    private SMSProvider smsProvider = null;

    public SMSProvider getSMSProvider(String smsType) {
        if (smsProvider == null) {
            smsProvider = SMSFactory.getSMSProvider(smsType);
        }
        return smsProvider;
    }

    @Override
    public void consumeWithTx(OrderECouponMessage message) {
        // 为保证能同步到数据库状态，先sleep一会
        try {
            Thread.sleep(800l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JPA.em().flush();  // 先强制同步hibernate缓存，避免找不到数据的情况; 如果还找不到，放异常以重试

        if (message.eCouponId != null) {
            ECoupon ecoupon = ECoupon.findById(message.eCouponId);
            if (ecoupon != null && ecoupon.status == ECouponStatus.UNCONSUMED) {
                sendECouponSMS(ecoupon, message);
            } else {
                if (ecoupon == null) {
                    Logger.info("Can NOT found ECoupon(id:" + message.eCouponId + "), retry later.");
                } else {
                    Logger.info("The ECoupon(id:" + message.eCouponId + ").status is " + ecoupon.status +
                            ", retry later.");
                }
                throw new RuntimeException("Retry later.");
            }
        } else if (message.orderItemId != null) {
            OrderItems orderItems = OrderItems.findById(message.orderItemId);
            if (orderItems != null && orderItems.order.status == OrderStatus.PAID) {
                sendOrderItemsSMS(orderItems, message);
            } else {
                if (orderItems == null) {
                    Logger.info("Can NOT found OrderItems(id:" + message.orderItemId + "), retry later.");
                } else {
                    Logger.info("The OrderItems(id:" + message.orderItemId + ").status is " + orderItems.order.status +
                            ", retry later.");
                }
                throw new RuntimeException("Retry later.");
            }
        }

    }

    private void sendOrderItemsSMS(OrderItems orderItems, OrderECouponMessage message) {
        String msg = orderItems.getOrderSMSMessage();

        if (msg == null) {
            Logger.info("OrderItems(id:" + orderItems.id + ").getOrderSMSMessage() == null, " +
                    "Will NOT Send SMS.");
            return;
        }

        try {
            List<ECoupon> ecoupons = orderItems.getECoupons();
            if (ecoupons.size() == 0) {
                Logger.info("OrderItems(id:" + orderItems.id + ").ecoupons is EMPTY, " +
                        "Will NOT Send SMS.");
                return;
            }

            getSMSProvider(SMS_TYPE).send(new SMSMessage(msg, orderItems.phone, ecoupons.get(0).replyCode));

            for (ECoupon ecoupon : ecoupons) {
                // 如果没有出现异常，则记录一下发送历史
                if (ecoupon.smsSentCount == null) {
                    ecoupon.smsSentCount = 0;
                }
                ecoupon.smsSentCount += 1;
                ecoupon.save();
                new CouponHistory(ecoupon, "MessageQ", message.remark, ecoupon.status, ecoupon.status, null).save();
            }
        } catch (SMSException e) {
            Logger.error("Sms2SenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    private void sendECouponSMS(ECoupon ecoupon, OrderECouponMessage message) {
        String msg = ecoupon.getOrderSMSMessage();
        if (msg == null) {
            Logger.info("ECoupon(id:" + ecoupon.id + ").getOrderSMSMessage() == null, Will NOT Send SMS.");
            return;
        }
        try {
            getSMSProvider(SMS_TYPE).send(new SMSMessage(msg, ecoupon.orderItems.phone, ecoupon.replyCode));
            // 如果没有出现异常，则记录一下发送历史
            if (ecoupon.smsSentCount == null) {
                ecoupon.smsSentCount = 0;
            }
            ecoupon.smsSentCount += 1;
            ecoupon.save();
            new CouponHistory(ecoupon, "MessageQ", message.remark, ecoupon.status, ecoupon.status, null).save();
        } catch (SMSException e) {
            Logger.error("Sms2SenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Class getMessageType() {
        return OrderECouponMessage.class;
    }

    @Override
    protected String queue() {
        return SMSUtil.SMS_ORDER_QUEUE;
    }

    @Override
    protected int retries() {
        return 10;  // 重试10次
    }
}
