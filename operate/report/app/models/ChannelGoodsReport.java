package models;

import models.accounts.AccountType;
import models.order.Order;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-25
 * Time: 上午10:16
 * To change this template use File | Settings | File Templates.
 */
public class ChannelGoodsReport {
    public Order order;
    public String loginName;
    public String userName;
    public Goods goods;
    public BigDecimal avgSalesPrice;

    /**
     * 毛利率
     */
    public BigDecimal grossMargin;
    public BigDecimal originalPrice;
    public Long buyNumber;
    public BigDecimal totalAmount;
    public String reportDate;
    public BigDecimal refundAmount;
    public BigDecimal profit;
    public BigDecimal netSalesAmount;
    public BigDecimal totalCost;
    public BigDecimal ratio;
    public BigDecimal originalAmount;

    /**
     * 渠道成本
     */
    public BigDecimal channelCost;

    public ChannelGoodsReport(Order order, Goods goods, BigDecimal originalPrice, Long buyNumber,
                              BigDecimal totalAmount, BigDecimal avgSalesPrice,
                              BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount
            , BigDecimal totalCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.goods = goods;
        this.originalPrice = originalPrice;
//        System.out.println("goods.id>>>" + goods.id);
//        System.out.println("orignalprice>>>" + originalPrice);
//        System.out.println("");
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
    }

    //from resaler
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.goods = goods;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //refund ecoupon
    public ChannelGoodsReport(Order order, BigDecimal refundAmount, Goods goods) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.refundAmount = refundAmount;
        this.goods = goods;

    }

    public ChannelGoodsReport(Order order, Long buyNumber, BigDecimal originalAmount) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public ChannelGoodsReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit) {
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> query(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                " )" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.order.userId, r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();

//        System.out.println("padiRe>>>" + paidResultList.size());

//        for (ChannelGoodsReport c : paidResultList) {
//                System.out.println("c.name>>" + c.loginName);
//                System.out.println("c.goods.name>>>" + c.goods.name);
//                System.out.println("c.profit>>>" + c.profit);
//                System.out.println("c.buyNumber>>>" + c.buyNumber);
//                System.out.println("c.totalAmount>>>" + c.totalAmount);
//                System.out.println("c.totalCost>>>" + c.totalCost);
//
//                System.out.println("");
//        }

//        for (ChannelGoodsReport c : paidResultList) {
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.goods.id>>>" + c.goods.id);
//            System.out.println("c.buy>>>" + c.buyNumber);
//            System.out.println("res.sale>>>" + c.totalAmount);
//            System.out.println("res.cost>>>" + c.totalCost);
//            System.out.println("res.gross>>>" + c.grossMargin);
//            System.out.println("");
//        }


        //from resaler
        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
        groupBy = " group by r.order.userId, r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods) " +
                " from ECoupon e ";
        groupBy = " group by e.order.userId, e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
            }
        }

        List<ChannelGoodsReport> resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);

        }
        Collections.sort(tempString);

        for (String key : tempString) {
//            System.out.println("key>>>" + key);
            resultList.add(map.get(key));
        }

//        for (ChannelGoodsReport c : resultList) {
//            System.out.println("after>>..");
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.profit>>>" + c.profit);
//            System.out.println("");
//        }

        return resultList;
    }


    /**
     * 取得按商品统计的销售记录     consumer
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> queryConsumer(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                ")" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");
        Goods goods1 = Goods.findById(619l);


//        System.out.println("");
//        System.out.println("");
//
//        System.out.println("");
//
//        System.out.println("");
//        System.out.println("");
//
//        System.out.println("goods.orignalpirce>>>" + goods1.originalPrice);


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();

//        System.out.println("padiRe>>>" + paidResultList.size());
//
//        for (ChannelGoodsReport c : paidResultList) {
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.buy>>>" + c.buyNumber);
//            System.out.println("consumer.sale>>>" + c.totalAmount);
//            System.out.println("consumer.cost>>>" + c.totalCost);
//            System.out.println("consumer.gross>>>" + c.grossMargin);
//            System.out.println("");
//        }

//        for (ChannelGoodsReport c : paidResultList) {
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.profit>>>" + c.profit);
//            System.out.println("c.buyNumber>>>" + c.buyNumber);
//            System.out.println("c.totalAmount>>>" + c.totalAmount);
//            System.out.println("c.totalCost>>>" + c.totalCost);
//
//            System.out.println("");
//        }

        //from resaler
        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
        groupBy = " group by  r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods) " +
                " from ECoupon e ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            map.put(getConsumerReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getConsumerReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(resalerItem));
            if (item == null) {
                map.put(getConsumerReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.totalCost);
            }
        }

        List resultList = new ArrayList();
        for (String key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 取得净销售的总计
     *
     * @param resultList
     * @return
     */
    public static ChannelGoodsReport getNetSummary(List<ChannelGoodsReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ChannelGoodsReport(null, 0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;

        for (ChannelGoodsReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);

            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
//            System.out.println("profit>>>>" + profit);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        return new ChannelGoodsReport(totalAmount, refundAmount, netSalesAmount, grossMargin, channelCost, profit);
    }

    public static String getReportKey(ChannelGoodsReport refoundItem) {
        if (refoundItem.order == null) {
//            System.out.println("null order.goods.id>>>>" + refoundItem.goods.id);
            return String.valueOf(refoundItem.goods.id);
        } else {
//            System.out.println("order.userId>>>>" + refoundItem.order.userId);
//            System.out.println("order.goods.id>>>>" + refoundItem.goods.id);
            return String.valueOf(refoundItem.order.userId) + String.valueOf(refoundItem.goods.id);
        }
    }

    public static String getConsumerReportKey(ChannelGoodsReport refoundItem) {
        return String.valueOf(refoundItem.goods.id);
    }

}
