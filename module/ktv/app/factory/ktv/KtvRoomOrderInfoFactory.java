package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvRoomType;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.Shop;
import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * User: yan
 * Date: 13-4-16
 * Time: 下午4:41
 */
public class KtvRoomOrderInfoFactory extends ModelFactory<KtvRoomOrderInfo> {

    @Override
    public KtvRoomOrderInfo define() {
        KtvRoomOrderInfo roomOrderInfo = new KtvRoomOrderInfo(FactoryBoy.lastOrCreate(Goods.class),
                FactoryBoy.create(OrderItems.class),
                KtvRoomType.MIDDLE, new Date(), 9);
        roomOrderInfo.scheduledDay = DateUtils.truncate(new Date(), Calendar.DATE);
        roomOrderInfo.status = KtvOrderStatus.LOCK;
        roomOrderInfo.shop = FactoryBoy.lastOrCreate(Shop.class);
        roomOrderInfo.createdAt = new Date();
        return roomOrderInfo;
    }

    @Factory(name = "time1")
    public KtvRoomOrderInfo roomOrderWithT1(KtvRoomOrderInfo orderInfo) {
        orderInfo.scheduledTime = 10;
        return orderInfo;
    }

    @Factory(name = "time2")
    public KtvRoomOrderInfo roomOrderWithT2(KtvRoomOrderInfo orderInfo) {
        orderInfo.scheduledTime = 12;
        return orderInfo;
    }

    @Factory(name = "time3")
    public KtvRoomOrderInfo roomOrderWithT3(KtvRoomOrderInfo orderInfo) {
        orderInfo.scheduledTime = 11;
        return orderInfo;
    }

    @Factory(name = "time4")
    public KtvRoomOrderInfo roomOrderWithT4(KtvRoomOrderInfo orderInfo) {
        orderInfo.scheduledTime = 15;
        return orderInfo;
    }
}
