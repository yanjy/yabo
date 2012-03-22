package models.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Match;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Request;

import com.uhuila.common.constants.DeletedStatus;

@Entity
@Table(name = "cusers")
public class SupplierUser extends Model {
	@Column(name = "supplier_id")
	public Long supplierId;

	@Column(name = "login_name")
	@Required
	public String loginName;

	@Required
	@Match(value="^1[3|4|5|8][0-9]\\d{4,8}$",message="手机格式不对！")
	public String mobile;

	@Column(name = "encrypted_password")
	@Required
	public String encryptedPassword;

	@Column(name = "password_salt")
	public String passwordSalt;

	@Column(name = "last_login_ip")
	public String lastLoginIP;

	@Column(name = "last_login_at")
	public Date lastLoginAt;

	@Column(name = "lock_version")
	public int lockVersion;

	@Column(name = "created_at")
	public Date createdAt;

	@Column(name = "updated_at")
	public Date updatedAt;

	/**
	 * 逻辑删除,0:未删除，1:已删除
	 */
	@Enumerated(EnumType.ORDINAL)
	public DeletedStatus deleted;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "cusers_roles", inverseJoinColumns = @JoinColumn(name
			= "role_id"), joinColumns = @JoinColumn(name = "cuser_id"))
	public Set<SupplierRole> roles;

	public static JPAExtPaginator<SupplierUser> getCuserList(String loginName,Long supplierId,
			int pageNumber, int pageSize) {
		StringBuffer sql = new StringBuffer();
		Map params= new HashMap();
		sql.append("supplierId = :supplierId");
		params.put("supplierId",supplierId);

		sql.append(" and deleted = :deleted ");
		params.put("deleted", DeletedStatus.UN_DELETED);

		if (StringUtils.isNotBlank(loginName)) {
			sql.append(" and loginName like :loginName");
			params.put("loginName",loginName+"%");
		}

		JPAExtPaginator<SupplierUser> cusersPage = new JPAExtPaginator<>("SupplierUser s", "s", 
				SupplierUser.class,sql.toString(),params).orderBy("createdAt desc");
		cusersPage.setPageNumber(pageNumber);
		cusersPage.setPageSize(pageSize);
		cusersPage.setBoundaryControlsEnabled(false);
		return cusersPage;
	}

	/**
	 * 更新操作员信息
	 * @param id ID
	 * @param cuser 用户信息
	 */
	public static void update(long id,SupplierUser cuser) {
		SupplierUser updCuser = SupplierUser.findById(id);
		updCuser.loginName=cuser.loginName;
		updCuser.mobile=cuser.mobile;
		if (StringUtils.isNotEmpty(cuser.encryptedPassword) && !cuser.encryptedPassword.equals( DigestUtils.md5Hex(updCuser.encryptedPassword))) {
			Images.Captcha captcha = Images.captcha();
			String passwordSalt =captcha.getText(6);
			//随机吗
			updCuser.passwordSalt = passwordSalt;
			updCuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword+passwordSalt);
		}
		updCuser.roles=cuser.roles;
		updCuser.lastLoginAt = new Date();
		updCuser.updatedAt = new Date();
		//获得IP
		updCuser.lastLoginIP = Request.current().remoteAddress;

		updCuser.save();

	}

	/**
	 * 判断用户名和手机是否唯一
	 *
	 * @param loginName 用户名
	 * @param mobile 手机
	 */
	public static String checkValue(Long id,String loginName, String mobile) {

		StringBuilder sq= new StringBuilder("loginName = ? ");
		List list = new ArrayList();
		list.add(loginName);
		if (id != null) {
			sq.append("and id <> ?");
			list.add(id);
		}
		List<SupplierUser> cuserList = SupplierUser.find(sq.toString(), list.toArray()).fetch();
		String returnFlag = "0";
		//用户名存在的情况
		if (cuserList.size() >0) returnFlag = "1";
		else {
			sq= new StringBuilder("mobile = ? ");
			list = new ArrayList();
			list.add( mobile);
			if (id != null) {
				sq.append("and id <> ?");
				list.add(id);
			}
			//手机存在的情况
			List<SupplierUser> mList = SupplierUser.find(sq.toString(), list.toArray()).fetch();
			if(mList.size()>0) returnFlag = "2";
		}

		return returnFlag;
	}


}
