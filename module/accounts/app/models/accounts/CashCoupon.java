package models.accounts;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import play.db.jpa.Model;

import com.uhuila.common.constants.DeletedStatus;
import play.modules.paginate.JPAExtPaginator;

/**
 * 现金券
 * @author tanglq
 *
 */
@Entity
@Table(name="cash_coupons")
public class CashCoupon extends Model {
	
	/**
	 * 券名称（批次名)
	 * 说明此次发券的用途等.
	 * 多个现金券名称可以相同，如：国庆特惠券
	 */
	public String name;
	
	/**
	 * 序列号.
	 * 保存为英文大写和数字。
	 * 
	 * 录入时，编辑可以指定一个前缀，然后生成时用前缀加数字序号即可.
	 * 可以这样做：
	 *      DecimalFormat myFormatter = new DecimalFormat("00000");
     *      String serialNo = prefix + myFormatter.format(22);
	 */
	@Column(name="serial_no", unique = true)
	public String serialNo;
	
	@Transient
	public String prefix;
	
	/**
	 * 充值码.
	 * 伪随机生成的15位数字
	 */
	@Column(name="charge_code", unique = true)
	public String chargeCode;

	/**
	 * 面值.
	 * 充值时用面值充入不可提现余额.
	 */
	@Column(name="face_value")
	public BigDecimal faceValue;
	
	/**
	 * 被充值用户的ID.
	 */
	@Column(name="user_id")
	public Long userId;
	
	@Column(name="created_at")
	public Date createdAt;
	
	/**
	 * 创建者ID，为运营后台登录ID.
	 */
    @Column(name = "operator_id")
	public Long operatorId;

    @Transient
    public String operatorName;
	
	@Version
	@Column(name="lock_version")
	public int lockVersion;
	
	/**
	 * 充值时间
	 */
	@Column(name="charged_at")
	public Date chargedAt;

	/**
	 * 删除状态。
	 * 没有被充值的记录才可被删除，删除后不可再被使用.
	 */
    @Enumerated
	public DeletedStatus deleted;

    public CashCoupon(){
        lockVersion = 0;
        deleted = DeletedStatus.UN_DELETED;
        createdAt = new Date();
    }

    public static JPAExtPaginator<CashCoupon> findByCondition(
            CashCouponCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<CashCoupon> page = new JPAExtPaginator<>(
                null, null, CashCoupon.class, condition.getFilter(), condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

}
