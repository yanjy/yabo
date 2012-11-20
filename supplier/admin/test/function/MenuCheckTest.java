package function;

import java.util.List;

import factory.callback.BuildCallback;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.ContextedMenu;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

public class MenuCheckTest extends FunctionalTest {
    SupplierUser supplierUserAdminSales;

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
    }

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        SupplierRole roleSales = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "销售人员";
                role.key = "sales";
            }
        });


        SupplierRole roleAdmin = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "系统管理员";
                role.key = "admin";
            }
        });

        SupplierRole roleTest = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "测试角色";
                role.key = "test";
            }
        });

        SupplierRole roleEditor = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "商品编辑";
                role.key = "editor";
            }
        });

        SupplierRole roleClerk = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "店员";
                role.key = "clerk";
            }
        });

        SupplierRole roleManager = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "经理";
                role.key = "manager";
            }
        });

        SupplierRole roleAccount = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "财务";
                role.key = "account";
            }
        });

        supplierUserAdminSales = FactoryBoy.create(SupplierUser.class, "SalesAdmin");


        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @After
    public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testMenuBaseUrl() {
//		System.out.println("++++++++++++++++   user.id:" + user.id + ", name:" + user.loginName + ", supplier:" + user.supplier);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUserAdminSales.loginName);
        assertIsOk(GET("/singlefoo/user"));


        List<ContextedMenu> list = (List<ContextedMenu>) renderArgs("topMenus");

        assertTrue(list.size() > 0);
        assertTrue(list.get(0).getBaseUrl().indexOf("localhost") > 0);
    }
}
