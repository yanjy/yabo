package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午4:12
 * To change this template use File | Settings | File Templates.
 */
public class OperateVerifyCouponsFuncTest extends FunctionalTest  {

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex(){

        Http.Response response = GET("/coupons/index");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertNotNull(renderArgs("shopList"));
        assertNotNull(renderArgs("supplierList"));

    }

//    @Test
//    public void testVerify(){
//
//        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
//        System.out.println(eCoupon.status);
//        System.out.println(eCoupon.eCouponSn);
//
//
//        Http.Response response = GET("/coupons/verify?supplierId="+eCoupon.shop.supplierId+"&shopId="+eCoupon.shop.id+"&eCouponSn="+eCoupon.eCouponSn);
//        assertIsOk(response);
//        System.out.println(response.out.toString());
//        assertContentMatch("未消费", response);
//
//
//    }
}
