package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;
import util.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-3-5
 * Time: 下午3:51
 */
public class OrderECouponMessageTest extends UnitTest {
    SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);

    Order order;
    List<ECoupon> couponList;
    OrderItems orderItems;

    Resaler wuba;
    Goods goods;
    Shop shop;
    User user;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 初始化 电子券数据
        goods = FactoryBoy.create(Goods.class);
        shop = FactoryBoy.create(Shop.class);
        user = FactoryBoy.create(User.class);

        wuba = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.WUBA_LOGIN_NAME;
            }
        });
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.paidAt = DateHelper.beforeHours(1);
                target.userId = user.id;
                target.status = OrderStatus.PAID;
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems target) {
                target.phone = user.mobile;
                target.status = OrderStatus.PAID;
            }
        });
    }


    @Test
    public void 无密码单张券发送短信() {
        createNoPasswordCoupons(2);
        assertEquals(couponList.get(0).goods.title + "券号" + couponList.get(0).eCouponSn + ",截止" + dateFormat.format(couponList.get(0).expireAt) + "一百券客服4006865151",
                OrderECouponMessage.getOrderSMSMessage(couponList.get(0)));
    }

    @Test
    public void 无密码2张券发送短信() {
        createNoPasswordCoupons(2);
        String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);
        Logger.info(smsMessages[0]);
        assertEquals(couponList.get(0).goods.title + "券号" + couponList.get(0).eCouponSn + ",券号" + couponList.get(1).eCouponSn + "[共2张],截止" + dateFormat.format(couponList.get(0).expireAt) + "一百券客服4006865151",
                smsMessages[0]);
    }

    @Test
    public void 无密码30张券发送短信() {
        createNoPasswordCoupons(30);

        // 发短信时只会收到4条长短信，一条短信包括8张券
        String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);
        assertEquals(4, smsMessages.length);
    }

    @Test
    public void 无密码30张券其中15张券已验证() {
        createNoPasswordCoupons(30);

        boolean odd = true;
        for (ECoupon e : couponList) {
            if (odd) {
                e.status = ECouponStatus.CONSUMED;
                e.save();
            }
            odd = !odd;
        }

        // 重发短信时只会收到2条长短信
        String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);
        assertEquals(2, smsMessages.length);
    }

    @Test
    public void 有密码单张券发送短信() {
        createWithPasswordCoupons(2);
        assertEquals(couponList.get(0).goods.title + "券号" + couponList.get(0).eCouponSn + "密码"
                + couponList.get(0).eCouponPassword + ",截止" +
                dateFormat.format(couponList.get(1).expireAt) + "一百券客服4006865151",
                OrderECouponMessage.getOrderSMSMessage(couponList.get(0)));
    }

    @Test
    public void 有密码多张券发送短信() {
        createWithPasswordCoupons(2);
        String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);
        Logger.info(smsMessages[0]);
        assertEquals(couponList.get(0).goods.title + "券号" + couponList.get(0).eCouponSn + "密码"
                + couponList.get(0).eCouponPassword + ",券号" + couponList.get(1).eCouponSn + "密码" +
                couponList.get(1).eCouponPassword + "[共2张],截止" +
                dateFormat.format(couponList.get(1).expireAt) + "一百券客服4006865151",
                smsMessages[0]);
    }

    @Test
    public void 无密码58团单张券() {
        createNoPasswordCoupons(2);
        order.userId = wuba.id;
        order.save();
        StringBuilder sb = new StringBuilder();
        sb.append("【58团】")
                .append(couponList.get(0).goods.title)
                .append("由58合作商家【一百券】提供,一百券号").append(couponList.get(0).eCouponSn)
                .append(",有效期至").append(dateFormat.format(couponList.get(0).expireAt))
                .append("58客服4007895858");
        assertEquals(sb.toString(), OrderECouponMessage.getOrderSMSMessage(couponList.get(0)));
    }

    @Test
    public void 有密码58团多张券() {
        createWithPasswordCoupons(2);
        orderItems.order.userId = wuba.id;
        orderItems.order.save();

        String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);
        Logger.info(smsMessages[0]);
        System.out.println(smsMessages[0]);

        StringBuilder sb = new StringBuilder();
        sb.append("【58团】")
                .append(couponList.get(0).goods.title)
                .append("由58合作商家【一百券】提供,一百")
                .append("券号").append(couponList.get(0).eCouponSn)
                .append("密码").append(couponList.get(0).eCouponPassword)
                .append(",券号").append(couponList.get(1).eCouponSn)
                .append("密码").append(couponList.get(1).eCouponPassword)
                .append("[共2张]")
                .append(",有效期至").append(dateFormat.format(couponList.get(1).expireAt))
                .append("58客服4007895858");
        assertEquals(sb.toString(), smsMessages[0]);
    }


    /**
     * 创建没有密码的券列表
     */
    private void createWithPasswordCoupons(int size) {
        couponList = FactoryBoy.batchCreate(size, ECoupon.class, "password",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });
    }

    private void createNoPasswordCoupons(int size) {
        couponList = FactoryBoy.batchCreate(size, ECoupon.class, "Id",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.shop = shop;
                        target.goods = goods;
                        target.eCouponSn = "8888000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });
    }

}
