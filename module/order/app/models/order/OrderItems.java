package models.order;

import models.accounts.AccountType;
import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {

    private static final long serialVersionUID = 16323208753562L;

    // ====  价格列表  ====
    @Column(name = "face_value")
    public BigDecimal faceValue;        //商品面值、市场价

    @Column(name = "original_price")
    public BigDecimal originalPrice;    //供应商进货价

    @Column(name = "resaler_price")
    public BigDecimal resalerPrice;     //用户在哪个分销商平台购买的价格，用于计算分销平台的佣金

    @Column(name = "sale_price")
    public BigDecimal salePrice;        //最终成交价,对于普通分销商来说，此成交价与以上分销商价(resalerPrice)相同；
    
    /**
     * 使用折扣码后折扣的费用.
     * 
     */
    @Column(name="rebate_value")
    public BigDecimal rebateValue;
    
    /**
     * 当前订单项总费用：
     * lineValue = salePrice*buyNumber - rebateValue
     */
    @Transient
    public BigDecimal getLineValue() {
        if (rebateValue == null) {
            rebateValue = BigDecimal.ZERO;
        }
        return salePrice.multiply(new BigDecimal(buyNumber)).subtract(rebateValue);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "goods_name")
    public String goodsName;

    @Column(name = "buy_number")
    public Long buyNumber;

    public String phone;

    @Column(name = "created_at")
    public Date createdAt;

    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    @Transient
    public BigDecimal getAmount() {
        if (salePrice != null && buyNumber != null) {
            return salePrice.multiply(new BigDecimal(buyNumber));
        }
        return BigDecimal.ZERO;
    }

    public OrderItems(Order order, Goods goods, long buyNumber, String phone, BigDecimal salePrice, BigDecimal resalerPrice) {
        this.order = order;
        this.goods = goods;
        this.faceValue = goods.faceValue;
        this.originalPrice = goods.originalPrice;
        this.salePrice = salePrice;
        this.resalerPrice = resalerPrice;
        this.goodsName = goods.name;
        this.buyNumber = buyNumber;
        this.phone = phone;
        this.status = OrderStatus.UNPAID;
        this.createdAt = new Date();
    }


    public static long itemsNumber(Order order) {
        long itemsNumber = 0L;
        if (order == null) {
            return itemsNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE order = :order");
        q.setParameter("order", order);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    public static long itemsNumberElectronic(Order order) {
        long itemsNumber = 0L;
        if (order == null) {
            return itemsNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE order = :order and goods.materialType=:materialType");
        q.setParameter("order", order);
        q.setParameter("materialType", MaterialType.ELECTRONIC);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    /**
     * 取出该用户购买制定商品的数量
     *
     * @param user    用户
     * @param goodsId 商品ID
     * @return
     */
    public static long itemsNumber(User user, Long goodsId) {
        long itemsNumber = 0L;

        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE goods.id=:goodsId and " +
                "order.userId=:userId and order.userType=:userType and status=:status");
        q.setParameter("goodsId", goodsId);
        q.setParameter("userId", user.id);
        q.setParameter("userType", AccountType.CONSUMER);
        q.setParameter("status", OrderStatus.PAID);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    /**
     * 处理券号
     *
     * @return 券号
     */
    public String getEcouponSn() {
        Query query = play.db.jpa.JPA.em().createQuery(
                "select e from ECoupon e where e.order = :order and e.goods =:goods ");
        query.setParameter("order", this.order);
        query.setParameter("goods", this.goods);
        List<ECoupon> favs = query.getResultList();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : favs) {
            sn.append(e.getMaskedEcouponSn() + "\n");
        }
        return sn.toString();
    }

    /**
     * 处理券号
     *
     * @return 券号
     */
    public String getWebEcouponSn() {
        Query query = play.db.jpa.JPA.em().createQuery(
                "select e from ECoupon e where e.order = :order and e.goods =:goods ");
        query.setParameter("order", this.order);
        query.setParameter("goods", this.goods);
        List<ECoupon> favs = query.getResultList();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : favs) {
            sn.append(e.eCouponSn);
            sn.append("\n");
        }
        return sn.toString();
    }

    /**
     * 取得购买过得手机号
     *
     * @param user
     * @return
     */
    public static List<String> getMobiles(User user) {
        Query query = play.db.jpa.JPA.em().createQuery(
                "select o.phone from OrderItems o where o.order.userId = :userId and o.order.userType =:userType group by o.phone order by o.order desc ");
        query.setParameter("userId", user.id);
        query.setParameter("userType", AccountType.CONSUMER);
        query.setFirstResult(0);
        query.setMaxResults(10);
        List<String> mobileList = query.getResultList();
        return mobileList;
    }
}
