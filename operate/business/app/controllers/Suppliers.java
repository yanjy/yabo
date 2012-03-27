package controllers;

import com.uhuila.common.util.FileUploadUtil;
import controllers.modules.cas.SecureCAS;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 商户管理的控制器.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 3:13 PM
 */
@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("suppliers_index")
public class Suppliers extends Controller {
    private static final String ADMIN_ROLE = "admin";

    public static void index() {
        List<Supplier> suppliers = Supplier.findUnDeleted();
        render(suppliers);
    }

    @ActiveNavigation("suppliers_add")
    public static void add() {
        render();
    }

    /**
     * 创建商户，同时创建商户的一个管理员.
     *
     * @param supplier
     * @param image
     * @param admin
     */
    @ActiveNavigation("suppliers_add")
    public static void create(@Valid Supplier supplier, File image, @Valid SupplierUser admin) {
        checkImage(image);
        initAdmin(admin);
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                System.out.println("validation.errorsMap().get(key):" + validation.errorsMap().get(key));
            }
            render("Suppliers/add.html");
        }

        supplier.create();
        try {
            uploadImagePath(image, supplier);
            supplier.save();
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        admin.create(supplier.id);

        index();
    }

    private static void initAdmin(SupplierUser admin) {
        admin.roles = new HashSet<>();
        admin.roles.add(SupplierRole.findByKey(ADMIN_ROLE));
    }

    private static void checkImage(File image) {
        if (image == null) {
            return;
        }
        //检查目录
        File uploadDir = new File(UploadFiles.ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            Validation.addError("supplier.image", "validation.write");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            Validation.addError("supplier.image", "validation.write");
        }

        if (image.length() > UploadFiles.MAX_SIZE) {
            Validation.addError("supplier.image", "validation.maxFileSize");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
        String fileExt = image.getName().substring(image.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            Validation.addError("supplier.image", "validation.invalidType", StringUtils.join(fileTypes, ','));
        }
    }

    private static void uploadImagePath(File image, Supplier supplier) throws IOException {
        if (image == null || image.getName() == null) {
            return;
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(image, supplier.id, UploadFiles.ROOT_PATH);
        supplier.logo = absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    public static void edit(long id, Supplier supplier, SupplierUser admin) {
        if (supplier == null || supplier.id == null) {
            supplier = Supplier.findById(id);
            admin = SupplierUser.findAdmin(id, "admin");
        }
        render(supplier, admin);
    }

    public static void update(@Valid Supplier supplier, File image, @Valid SupplierUser admin, String password) {

        EntityManager entityManager = JPA.em();
        entityManager.merge(admin);

        SupplierUser uuu = SupplierUser.findById(admin.id);
        System.out.println("uuu.mobile:" + uuu.mobile);

        if (Validation.hasError("admin.encryptedPassword") && Validation.hasError("admin")
                && Validation.errors().size() == 2) {
            Validation.clear();
            System.out.println("validation.errorsMap().size():" + validation.errorsMap().size());
        }
        if (Validation.hasErrors()) {
            render("Suppliers/edit.html");
        }
        try {
            uploadImagePath(image, supplier);
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        supplier.update();
        if (admin.id == null) {
            admin.create(supplier.id);
        } else {
            admin.update();
        }
        index();
    }

    public static void freeze(long id) {
        Supplier.freeze(id);
        index();
    }

    public static void unfreeze(long id) {
        Supplier.unfreeze(id);
        index();
    }

    public static void delete(long id) {
        System.out.println("id:" + id);
        Supplier.delete(id);
        ok();
    }
}
