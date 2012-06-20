package models.job;

import com.uhuila.common.util.DateUtil;
import models.order.CancelUnpaidOrders;
import models.order.Order;
import models.order.OrderStatus;
import play.jobs.Job;
import play.jobs.On;

import javax.persistence.Query;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-20
 * Time: 上午11:09
 */
@On("0 0 0 * * ?")  //每天凌晨执行,十天后自动取消未付款的订单
public class CancleUnPaidOrderJob extends Job {

    @Override
    public void doJob() throws ParseException {

        String sql = "select o from Order o where o.orderNumber not in (select c.orderNumber from CancelUnpaidOrders " +
                "c ) and o.status =:status and (o.createdAt > :createdAtBegin and o.createdAt <=:createdAtEnd " +
                ") order by o.id";
        Query query = Order.em().createQuery(sql);
        query.setParameter("status", OrderStatus.UNPAID);
        query.setParameter("createdAtBegin", DateUtil.getBeginExpiredDate(9));
        query.setParameter("createdAtEnd", DateUtil.getEndExpiredDate(10));
        query.setFirstResult(0);
        query.setMaxResults(200);

        Order order = null;
        List<Order> orderList = query.getResultList();
        Iterator<Order> it = orderList.iterator();
        while (it.hasNext()) {
            order = it.next();
            if (order.status == OrderStatus.UNPAID) {
                //取消订单并且增加库存和减少销量
                order.cancelAndUpdateOrder();
                new CancelUnpaidOrders(order.orderNumber, order.userType, order.userId).save();
            }
            query = Order.em().createQuery(sql);
            query.setParameter("status", OrderStatus.UNPAID);
            query.setParameter("createdAtBegin", DateUtil.getBeginExpiredDate(9));
            query.setParameter("createdAtEnd", DateUtil.getEndExpiredDate(10));
            query.setFirstResult(0);
            query.setMaxResults(200);
            orderList = query.getResultList();
            it = orderList.iterator();
        }


    }

}
