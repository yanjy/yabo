package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.consumer.Address;
import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;


@Entity
@Table(name = "orders")
public class Orders extends Model {
    @ManyToOne
    public User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<OrderItems> orderItems;

    @Column(name = "order_no")
    public String orderNumber;

    public OrderStatus status;

    public BigDecimal amount;

    @Column(name = "account_pay")
    public BigDecimal accountPay;

    @Column(name = "discount_pay")
    public BigDecimal discountPay;

    @Column(name = "need_pay")
    public BigDecimal needPay;

    @Column(name = "buyer_phone")
    public String buyerPhone;

    @Column(name = "buyer_mobile")
    public String buyerMobile;

    public String remark;

    @Column(name = "pay_method")
    public String payMethod;

    @Column(name = "pay_request_id")
    public Long payRequestId;

    @Column(name = "receiver_phone")
    public String receiverPhone;

    @Column(name = "receiver_mobile")
    public String receiverMobile;

    @Column(name = "receiver_address")
    public String receiverAddress;

    @Column(name = "receiver_name")
    public String receiverName;

    @Column(name = "paid_at")
    public Date paidAt;
    public String postcode;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Column(name = "lock_version")
    public int lockVersion;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "delivery_no")
    public String deliveryNo;
    @Column(name = "delivery_type")
    public int deliveryType;
    /**
     * 成交开始时间
     */
    @Transient
    public Date createdAtBegin;
    /**
     * 成交开始时间
     */
    @Transient
    public Date createdAtEnd;
    /**
     * 退款开始时间
     */
    @Transient
    @Temporal(TemporalType.DATE)
    public Date refundAtBegin;
    /**
     * 退款开始时间
     */
    @Transient
    @Temporal(TemporalType.DATE)
    public String refundAtEnd;

    public Orders() {
    }

    public Orders(User user, Address address) {
        this.user = user;
        this.status = OrderStatus.UNPAID;
        this.deleted = DeletedStatus.UN_DELETED;
        this.orderNumber = OrdersNumber.generateOrderNumber();
        this.orderItems = new ArrayList();
        this.paidAt = null;
        this.amount = new BigDecimal(0);
        this.accountPay = new BigDecimal(0);
        this.needPay = new BigDecimal(0);
        this.discountPay = new BigDecimal(0);

        this.lockVersion = 0;

        this.createdAt = new Date();
        this.updatedAt = new Date();
        if (address != null) {
            this.receiverAddress = address.getFullAddress();
            this.receiverMobile = address.mobile;
            this.receiverName = address.name;
            this.receiverPhone = address.getPhone();
            this.postcode = address.postcode;
        }
    }

    public Orders(User user, long goodsId, long number, Address address, String mobile) throws NotEnoughInventoryException {
        this(user, address);

        Goods goods = Goods.findById(goodsId);
        checkInventory(goods, number);
        if (goods.salePrice.compareTo(new BigDecimal(0)) > 0) {
            this.amount = goods.salePrice.multiply(new BigDecimal(number));
            if (goods.materialType == MaterialType.REAL) {
                this.amount = this.amount.add(new BigDecimal(5));
            }
            //todo 目前没考虑支付优惠
            this.needPay = amount;
        }
        this.receiverMobile = mobile;

        OrderItems orderItems = new OrderItems(this, goods, number);
        this.orderItems.add(orderItems);
    }

    public Orders(User user, List<Cart> cartList, Address address) throws NotEnoughInventoryException {
        this(user, address);

        this.amount = Cart.amount(cartList);
        for (Cart cart : cartList) {
            if (cart.goods.materialType == MaterialType.REAL) {
                this.amount = this.amount.add(new BigDecimal(5));
                break;
            }
        }
        this.needPay = amount;

        for (Cart cart : cartList) {
            if (cart.number <= 0) {
                continue;
            }
            checkInventory(cart.goods, cart.number);
            OrderItems orderItems = new OrderItems(this, cart.goods, cart.number);
            this.orderItems.add(orderItems);
        }
    }

    /**
     * 生成订单编号.
     *
     * @return 订单编号
     */
    public static String generateOrderNumber() {
        int random = new Random().nextInt() % 10000;
        return DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS") + random;
    }

    public void checkInventory(Goods goods, long number) throws NotEnoughInventoryException {
        if (goods.baseSale < number) {
            throw new NotEnoughInventoryException();
        }
    }

	/**
	 * 订单查询
	 *
	 * @param orders 订单信息
     * @param companyId 商户ID
	 * @return List
	 */
	public static List query(Orders orders,String companyId) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct o from Orders o, OrderItems oi  WHERE oi member of o.orderItems");

		Map<String, Object> params = new HashMap<String, Object>();
		sql.append(" and oi.goods.companyId = :companyId");
		params.put("companyId", companyId);
		if (orders.createdAtBegin != null) {
			sql.append(" and o.createdAt >= :createdAtBegin");
			params.put("createdAtBegin", orders.createdAtBegin);
		}
		if (orders.createdAtEnd != null) {
			sql.append(" and o.createdAt <= :createdAtEnd");
			params.put("createdAtEnd", orders.createdAtEnd);
		}
        if (orders.refundAtBegin != null) {
            sql.append(" and o.paidAt >= :refundAtBegin");
            params.put("refundAtBegin", orders.refundAtBegin);
        }
        if (orders.refundAtEnd != null) {
            sql.append(" and o.paidAt <= :refundAtEnd");
            params.put("refundAtEnd", orders.refundAtEnd);
        }
		if (orders.status != null) {
			sql.append(" and o.status = :status");
			params.put("status",orders.status);
		}
		if (orders.deliveryType != 0) {
			sql.append(" and o.deliveryType = :deliveryType");
			params.put("deliveryType", orders.deliveryType);
		}
		if (StringUtils.isNotBlank(orders.payMethod)) {
			sql.append(" and o.payMethod = :payMethod");
			params.put("payMethod", orders.payMethod);
		}

        sql.append(" order by o.createdAt desc");
		Query orderQery = entityManager.createQuery(sql.toString());
		for(Map.Entry<String,Object> entry : params.entrySet()){
			orderQery.setParameter(entry.getKey(), entry.getValue());
		}

		return orderQery.getResultList();

	}

	/**
	 * 商家券号列表
	 *
	 * @return
	 */
	public static List queryCoupons() {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
        sql.append("select distinct o from Orders o join o.ECoupons e ") ;
        Query orderQery = entityManager.createQuery(sql.toString());
		return  orderQery.getResultList();
	}

    /**
     * 会员中心 券号列表
     *
     * @return
     */
    public static List userTicketsQuery(Long id, String createdAt, String status) {
        EntityManager entityManager = play.db.jpa.JPA.em();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.order_no,c.name,d.discount_sn,a.created_at,c.expired_bg_on,c.expired_ed_on,d.status FROM discount d");
        sql.append(" LEFT JOIN orders a ON a.id = d.order_id ");
        sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
        sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
        sql.append(" WHERE a.user_id=" + id);
        if (createdAt != null && !"".equals(createdAt)) {
            sql.append(" and a.created_at <=" + createdAt);
        }
        if (status != null && !"".equals(status)) {
            sql.append(" and a.status ='" + status + "'");
        }
        sql.append(" ORDER BY a.created_at DESC");
        return entityManager.createNativeQuery(sql.toString()).getResultList();
    }

    public void createAndUpdateInventory(User user, String cookieIdentity) {
        save();
        for (OrderItems orderItem : orderItems) {
            orderItem.goods.baseSale -= orderItem.number;
            orderItem.save();
        }
        Cart.clear(user, cookieIdentity);

    }

}
