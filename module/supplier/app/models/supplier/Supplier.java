package models.supplier;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.PathUtil;
import models.admin.SupplierUser;
import models.sales.Brand;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.*;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.*;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 供应商（商户）
 * <p/>
 * author:sujie@uhuila.com
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends Model {

    private static final long serialVersionUID = 7122320609113062L;
    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";

    /**
     * 域名
     */
    @Required
    @MaxSize(100)
    @Match("^[a-zA-Z0-9\\-]{3,20}$")
    @Column(name = "domain_name")
    public String domainName;
    /**
     * 公司名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    public String fullName;

    /**
     * 公司别名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "other_name")
    public String otherName;

    /**
     * 职务
     */
    @MaxSize(100)
    public String position;

    /**
     * 负责人手机号
     */
    @Mobile
    public String mobile;

    /**
     * 负责人联系电话
     */
    @Phone
    public String phone;

    /**
     * 负责人姓名
     */
    @Column(name = "user_name")
    public String userName;

    @Column(name = "login_name")
    public String loginName;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;
    @Enumerated(EnumType.STRING)
    /**
     * 状态
     */
    public SupplierStatus status;
    /**
     * logo图片路径
     */
    public String logo;
    /**
     * 描述
     */
    public String remark;
    @Email
    public String email;
    @Email
    @Column(name = "sales_email")
    public String salesEmail;
    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "`display_order`")
    @JoinColumn(name = "supplier_id")
    public List<Brand> brands;

    public Supplier(Long id) {
        this.id = id;
    }

    @Column(name = "shop_begin_hour")
    public String shopBeginHour;

    @Column(name = "shop_end_hour")
    public String shopEndHour;

    public Supplier() {
    }


    public static final String CACHEKEY = "SUPPLIER";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }


    @Transient
    public String getSmallLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_SMALL);
    }

    @Transient
    public String getOriginalLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_ORIGINAL);
    }

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        status = SupplierStatus.NORMAL;
        createdAt = new Date();

        return super.create();
    }


    public static void update(Long id, Supplier supplier) {
        Supplier sp = findById(id);
        if (sp == null) {
            return;
        }
        if (StringUtils.isNotBlank(supplier.logo)) {
            sp.logo = supplier.logo;
        }
        sp.domainName = supplier.domainName;
        sp.fullName = supplier.fullName;
        sp.otherName = supplier.otherName;
        sp.remark = supplier.remark;
        sp.mobile = supplier.mobile;
        sp.phone = supplier.phone;
        sp.position = supplier.position;
        sp.userName = supplier.userName;
        sp.email = supplier.email;
        sp.salesEmail = supplier.salesEmail;
        sp.shopBeginHour = supplier.shopBeginHour;
        sp.shopEndHour = supplier.shopEndHour;
        sp.updatedAt = new Date();
        sp.save();
    }

    public static void delete(long id) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            return;
        }
        if (!DeletedStatus.DELETED.equals(supplier.deleted)) {
            SupplierUser supplierUser = SupplierUser.findUserByDomainName(supplier.domainName, supplier.loginName);
            if (supplierUser != null) {
                supplierUser.deleted = DeletedStatus.DELETED;
                supplierUser.save();
            }
            supplier.deleted = DeletedStatus.DELETED;
            supplier.save();
        }
    }

    public static List<Supplier> findByCondition(String otherName) {
        StringBuilder sql = new StringBuilder("deleted=?");
        List params = new ArrayList();
        params.add(DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(otherName)) {
            sql.append(" and otherName=?");
            params.add(otherName);
        }
        sql.append(" order by createdAt DESC");
        return find(sql.toString(), params.toArray()).fetch();
    }

    public static List<Supplier> findUnDeleted() {
        return find("deleted=? order by createdAt DESC", DeletedStatus.UN_DELETED).fetch();
    }

    public static void freeze(long id) {
        updateStatus(id, SupplierStatus.FREEZE);
    }

    public static void unfreeze(long id) {
        updateStatus(id, SupplierStatus.NORMAL);
    }

    private static void updateStatus(long id, SupplierStatus status) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            return;
        }
        supplier.status = status;
        supplier.save();
    }


    @Override
    public String toString() {
        return "Supplier[" + this.fullName + "@" + this.domainName + "(" + this.id + ")]";
    }

    public static Supplier findByFullName(String fullName) {
        return Supplier.find("fullName like ?", "%" + fullName + "%").first();
    }

    public static List<Supplier> findListByFullName(String fullName) {
        return find("fullName like ?", "%" + fullName + "%").fetch();
    }

    public static boolean existDomainName(String domainName) {
        return find("domainName=? and deleted=?", domainName, DeletedStatus.UN_DELETED).first() != null;
    }
}
