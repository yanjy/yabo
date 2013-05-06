package models.ktv;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.sales.Shop;
import org.apache.commons.lang.time.DateUtils;
import play.db.jpa.Model;
import util.DateHelper;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: tanglq
 * Date: 13-4-10
 * Time: 上午11:57
 */
@Entity
@Table(name = "ktv_room_order_info")
public class KtvRoomOrderInfo extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    public Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    public OrderItems orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    public Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    public KtvRoomType ktvRoomType;

    @Enumerated(EnumType.STRING)
    public KtvOrderStatus status;
    /**
     * 预定日期
     */
    @Column(name = "scheduled_day")
    public Date scheduledDay;

    /**
     * 预定时间
     */
    @Column(name = "scheduled_time")
    public String scheduledTime;

    /**
     * 锁定时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 取消时间||成交时间
     */
    @Column(name = "deal_at")
    public Date dealAt;

    public KtvRoomOrderInfo(Goods goods, OrderItems orderItem, KtvRoomType ktvRoomType, Date scheduledDay, String scheduledTime) {
        this.goods = goods;
        this.orderItem = orderItem;
        this.ktvRoomType = ktvRoomType;
        this.scheduledDay = scheduledDay;
        this.scheduledTime = scheduledTime;
        this.status = KtvOrderStatus.LOCK;
        this.createdAt = new Date();

    }

    /*
    public static List<KtvRoomOrderInfo> findScheduledInfos(Date scheduleDay, Shop shop) {
        return KtvRoom.find("select k from KtvRoomOrderInfo k join k.goods.shops s where k.scheduledDay = ? and s.id=? and (k.status = ? or (k.status=?  and k.createdAt >= ?))",
                DateUtils.truncate(scheduleDay, Calendar.DATE), shop.id, KtvOrderStatus.DEAL, KtvOrderStatus.LOCK, DateUtils.addMinutes(new Date(), -10)).fetch();
    }
    */

    public static List<KtvRoomOrderInfo> findScheduled(Date scheduledDay, Shop shop) {
        scheduledDay = DateUtils.truncate(scheduledDay, Calendar.DATE);
        Date tenMinutesAgo = DateUtils.addMinutes(new Date(), -10);

        return KtvRoomOrderInfo.find("select o from KtvRoomOrderInfo o where o.shop = ? and o.scheduledDay = ? " +
                "and (o.status = ? or (o.status = ? and o.createdAt >= ?))",
                shop, scheduledDay, KtvOrderStatus.DEAL, KtvOrderStatus.LOCK, tenMinutesAgo).fetch();
    }

    /**
     * 取消订单
     */
    public void cancelKtvRoom() {
        if (this.status == KtvOrderStatus.LOCK) {
            this.status = KtvOrderStatus.CANCELED;
            this.orderItem.status = OrderStatus.CANCELED;
            this.orderItem.save();
            this.dealAt = new Date();
            this.save();
        }
    }

    /**
     * 成交新订单
     */
    public void dealKtvRoom() {
        this.status = KtvOrderStatus.DEAL;
        this.orderItem.status = OrderStatus.PAID;
        this.orderItem.save();
        this.dealAt = new Date();
        this.save();
    }

    /**
     * 查找10分钟前锁定的订单
     *
     * @param order
     * @return
     */
    public static List<KtvRoomOrderInfo> findByOrder(Order order) {
        return KtvRoomOrderInfo.find("status=? and orderItem.order = ? and createdAt <= ?", KtvOrderStatus.LOCK, order, DateUtils.addMinutes(new Date(), -10)).fetch();
    }

    /**
     * 查出该orderItem对应的room
     */
    public static List<KtvRoomOrderInfo> findByOrderItem(OrderItems orderItem) {
        return KtvRoomOrderInfo.find("status=? and orderItem=?", KtvOrderStatus.LOCK, orderItem).fetch();
    }

    /**
     * 查找10分钟内的锁定Room或出售的Room
     */
    /*
    public static List<KtvRoomOrderInfo> findScheduledInfos(Date scheduledDay, Shop shop, KtvRoom ktvRoom, String scheduledTime) {
        return KtvRoom.find("select k from KtvRoomOrderInfo k join k.goods.shops s where k.scheduledDay = ? and s.id=? and k.ktvRoom=? and k.scheduledTime =? and " +
                "(k.status = ? or (k.status=?  and k.createdAt >= ?))",
                DateUtils.truncate(scheduledDay, Calendar.DATE), shop.id, ktvRoom, scheduledTime,
                KtvOrderStatus.DEAL, KtvOrderStatus.LOCK, DateUtils.addMinutes(new Date(), -10)).fetch();

    }
    */

    public static String getRoomOrderTime(List<KtvRoomOrderInfo> ktvRoomOrderInfoList) {
        List<Integer> orderTimeList = new ArrayList<>();
        for (KtvRoomOrderInfo ktvRoomOrderInfo : ktvRoomOrderInfoList) {
            orderTimeList.add(Integer.parseInt(ktvRoomOrderInfo.scheduledTime.substring(0, 2)));

        }
        Collections.sort(orderTimeList);

        int lastTime = -2;
        List<Integer[]> result = new ArrayList<>();
        for (int t : orderTimeList) {
            if (t != lastTime + 1) {
                result.add(new Integer[]{t, t + 1});
            } else {
                result.get(result.size() - 1)[1] = t + 1;
            }
            lastTime = t;
        }

        StringBuilder resultStr = new StringBuilder();
        for (Integer[] pair : result) {
            resultStr.append(pair[0]).append(":00-").append(pair[1]).append(":00;");
        }
        return resultStr.toString();
    }
}
