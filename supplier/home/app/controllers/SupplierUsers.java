package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作员CRUD
 *
 * @author yanjy
 */

@With({SupplierRbac.class, SupplierInjector.class})
@ActiveNavigation("users")
public class SupplierUsers extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     */
    @ActiveNavigation("users")
    public static void index() {
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<SupplierRole> roles = supplierUser.roles;
        List<String> keyList = new ArrayList<String>();
        for (SupplierRole role : roles) {
            keyList.add(role.key);
        }

        if (!keyList.contains("admin")) {
            redirect("/profile");
        }

        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<SupplierUser> supplierUserList = SupplierUser.findBySupplier(supplierId);
        render(supplierUserList);
    }

    /**
     * 操作员添加页面
     */
    @ActiveNavigation("users")
    public static void add() {
        List rolesList = SupplierRole.findRoleOrderById();
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List shopList = Shop.findShopBySupplier(supplierId);
        render(rolesList, shopList);
    }

    /**
     * 创建操作员
     *
     * @param supplierUser 操作员信息
     */
    @ActiveNavigation("users")
    public static void create(@Valid SupplierUser supplierUser) {
        checkValid(null, supplierUser);
        supplierUser.create(SupplierRbac.currentUser().supplier.id);
        index();
    }

    /**
     * 逻辑删除操作员
     */
    public static void delete(Long id) {
        SupplierUser user = SupplierUser.findById(id);
        user.deleted = DeletedStatus.DELETED;
        user.save();
        index();
    }

    /**
     * 操作员编辑页面
     */
    public static void edit(Long id) {
        SupplierUser supplierUser = SupplierUser.findById(id);
        String roleIds = "";
        if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
            for (SupplierRole role : supplierUser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = SupplierRole.findRoleOrderById();
        supplierUser.roles.addAll(rolesList);
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List shopList = Shop.findShopBySupplier(supplierId);
        render(supplierUser, roleIds, rolesList, shopList);
    }

    /**
     * 操作员信息修改
     *
     * @param id           ID
     * @param supplierUser 用户信息
     */
    public static void update(Long id, @Valid SupplierUser supplierUser) {
        checkValid(id, supplierUser);
        // 更新用户信息
        SupplierUser.update(id, supplierUser);
        index();
    }

    /**
     * 验证
     *
     * @param supplierUser 操作员信息
     */
    private static void checkValid(Long id, SupplierUser supplierUser) {
        Validation.required("supplierUser.encryptedPassword", supplierUser.encryptedPassword);
        Validation.required("supplierUser.confirmPassword", supplierUser.confirmPassword);
        Validation.match("validation.jobNumber", supplierUser.jobNumber, "^[0-9]*");
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
                for (SupplierRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            supplierUser.id = id;
            if (id != null) {
                render("SupplierUsers/edit.html", supplierUser, roleIds, rolesList);
            } else {
                render("SupplierUsers/add.html", supplierUser, roleIds, rolesList);
            }
        }

    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(Long id, String loginName, String mobile, String jobNumber) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        String returnFlag = SupplierUser.checkValue(id, loginName, mobile, jobNumber,
                supplierId);
        renderJSON(returnFlag);
    }

}
