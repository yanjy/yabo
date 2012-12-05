package models;

import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
@Entity
@Table(name = "resale_sales_report")
public class ResaleSalesReport extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;
    public String loginName;
    public String userName;
    /**
     * 售出券数
     */
    public long buyNumber = 0l;

    /**
     * 退款券数
     */
    public long refundNumber = 0l;

    /**
     * 售出实物数
     */
    public long realBuyNumber = 0l;

    /**
     * 退款实物数
     */
    public long realRefundNumber = 0l;


    /**
     * 售出券金额
     */
    public BigDecimal salePrice = BigDecimal.ZERO;

    /**
     * 退款券金额
     */
    public BigDecimal refundPrice = BigDecimal.ZERO;

    /**
     * 售出实物金额
     */
    public BigDecimal realSalePrice = BigDecimal.ZERO;

    /**
     * 退款实物金额
     */
    public BigDecimal realRefundPrice = BigDecimal.ZERO;

    /**
     * 消费券数
     */
    public long consumedNumber = 0l;

    /**
     * 消费金额
     */
    public BigDecimal consumedPrice = BigDecimal.ZERO;

    /**
     * 应收款金额
     */
    public BigDecimal shouldGetPrice;
    /**
     * 已收款金额
     */
    public BigDecimal haveGetPrice;

    public Long totalNumber;
    public Long realTotalNumber;
    public BigDecimal amount;
    public BigDecimal realAmount;
    public BigDecimal totalRefundPrice;

    public ResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        if (salePrice != null) {
            this.salePrice = salePrice;
        } else {
            this.salePrice = BigDecimal.ZERO;
            System.out.println("111111111111111111111111");
        }
        this.buyNumber = buyNumber;
    }

    public ResaleSalesReport(Order order, Long buyNumber, BigDecimal salePrice) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
    }


    public ResaleSalesReport(BigDecimal consumedPrice, Order order, Long consumedNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
    }

    public ResaleSalesReport(BigDecimal refundPrice, Long refundNumber, Order order) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
    }

    public ResaleSalesReport(Long refundNumber, BigDecimal refundPrice, Order order) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.realRefundPrice = refundPrice;
        this.realRefundNumber = refundNumber;
    }


    public ResaleSalesReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.userName = "一百券";
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }

    public ResaleSalesReport(long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
                             BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.totalRefundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.shouldGetPrice = shouldGetPrice;
        this.haveGetPrice = haveGetPrice;
    }

    public ResaleSalesReport(long totalNumber, BigDecimal amount, long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
                             BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.realTotalNumber = realTotalNumber;
        this.realAmount = realAmount;
        this.totalRefundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.shouldGetPrice = shouldGetPrice;
        this.haveGetPrice = haveGetPrice;
    }


    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition) {

        //paidAt ecoupon
        String sql = "select new models.ResaleSalesReport(r.order,sum(e.salePrice),count(r.buyNumber)) from OrderItems r, ECoupon e  where e.orderItems=r ";
        String groupBy = " group by r.order.userId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.ResaleSalesReport(r.order,sum(r.salePrice),count(r.buyNumber)) from OrderItems r ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(e.salePrice),r.order,count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(e.refundPrice),count(e),r.order) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!


        Map<Long, ResaleSalesReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ResaleSalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ResaleSalesReport paidItem : sentRealResultList) {
            ResaleSalesReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                item.realSalePrice = paidItem.realSalePrice;
                item.realBuyNumber = paidItem.realBuyNumber;
            }
        }


        //merge other 2
        for (ResaleSalesReport consumedItem : consumedResultList) {
            ResaleSalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (ResaleSalesReport refundItem : refundResultList) {
            ResaleSalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }

        List resultList = new ArrayList();
        for (Long key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 消费者和分销商合计
     *
     * @param resultList
     * @return
     */

    public static ResaleSalesReport summary(List<ResaleSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ResaleSalesReport(0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long refundCount = 0l;
        long consumedCount = 0l;
        BigDecimal consumedPrice = BigDecimal.ZERO;
        long buyCount = 0l;
        long realBuyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal realAmount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        BigDecimal totRefundPrice = BigDecimal.ZERO;
        BigDecimal shouldGetPrice = BigDecimal.ZERO;
        BigDecimal haveGetPrice = BigDecimal.ZERO;
        for (ResaleSalesReport item : resultList) {

            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice);
            realBuyCount += item.realBuyNumber;
            realAmount = realAmount.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice);
            totRefundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
            refundPrice = refundPrice.add(totRefundPrice);
            refundCount += item.refundNumber;
            consumedCount += item.consumedNumber;
            System.out.println("item?????????????" + item);
            if (item.consumedPrice != null) {
                consumedPrice = consumedPrice.add(item.consumedPrice);
            }
            shouldGetPrice = amount.subtract(refundPrice);
            haveGetPrice = BigDecimal.ZERO;
        }
        return new ResaleSalesReport(buyCount, amount, realBuyCount, realAmount, refundPrice, refundCount, consumedPrice, consumedCount, shouldGetPrice, haveGetPrice);
    }

    /**
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<ResaleSalesReport> queryConsumer(ResaleSalesReportCondition condition) {
        //paidAt ecoupon
        String sql = "select new models.ResaleSalesReport(r.order,sum(e.salePrice),count(r.buyNumber)) from OrderItems r, ECoupon e  where e.orderItems=r ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER)  + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.ResaleSalesReport(r.order,sum(r.salePrice),count(r.buyNumber)) from OrderItems r ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(e.salePrice),r.order,count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + " order by sum(r.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(e.refundPrice),count(e),r.order) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.CONSUMER)  + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!
        ResaleSalesReport result = null;
        List<ResaleSalesReport> resultList = new ArrayList<>();
        if (paidResultList != null && paidResultList.size() > 0) {
            result = paidResultList.get(0);
            if (sentRealResultList != null && sentRealResultList.size() > 0) {
                result.realSalePrice = sentRealResultList.get(0).realSalePrice;
                result.realBuyNumber = sentRealResultList.get(0).realBuyNumber;
            }
            if (consumedResultList != null && consumedResultList.size() > 0) {
                result.consumedPrice = consumedResultList.get(0).consumedPrice;
                result.consumedNumber = consumedResultList.get(0).consumedNumber;
            }
            if (refundResultList != null && refundResultList.size() > 0) {
                result.refundPrice = refundResultList.get(0).refundPrice;
                result.refundPrice = refundResultList.get(0).refundPrice;
            }
            resultList.add(result);
        }


        return resultList;
    }

    private static Long getReportKey(ResaleSalesReport refoundItem) {
        return refoundItem.order.userId;
    }

}
