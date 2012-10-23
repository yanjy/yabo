package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.order.ECoupon;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-23
 * Time: 下午3:34
 * To change this template use File | Settings | File Templates.
 */
public class BatchFreezeCouponsTest extends FunctionalTest {
    ECoupon coupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        coupon = FactoryBoy.create(ECoupon.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/coupons/batchfreeze/index");
        assertStatus(200, response);
    }

    @Test
    public void testImportFreezeCoupons() {
        String temp = coupon.eCouponSn;
        Map<String, String> itemParams = new HashMap<>();
        itemParams.put("couponsFreezed", temp);
        Http.Response response = POST("/coupons/batchfreeze/import", itemParams);
        assertStatus(200, response);
        Set<ECoupon> couponSet = (Set<ECoupon>) renderArgs("unUsedCouponsList");
        assertEquals(1, couponSet.size());
        assertEquals(coupon.eCouponSn, couponSet.iterator().next().eCouponSn);
    }


    @Test
    public void testBatchFreezeCoupons() {
        assertEquals(0, coupon.isFreeze);
        Map<String, String> itemParams = new HashMap<>();
        itemParams.put("couponsFreezedId", coupon.id.toString());
        Http.Response response = POST("/coupons/batchfreeze/doing", itemParams);
        assertStatus(200, response);
        Set<ECoupon> couponSet = (Set<ECoupon>) renderArgs("unUsedCouponsList");
        assertEquals(1, couponSet.size());
        assertEquals(coupon.eCouponSn, couponSet.iterator().next().eCouponSn);
        assertEquals(coupon.id, couponSet.iterator().next().id);
        coupon.refresh();
        assertEquals(1, coupon.isFreeze);
    }


}