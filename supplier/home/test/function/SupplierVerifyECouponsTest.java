package function;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.SMSMessage;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.mq.MockMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 商户后台v2的券验证功能测试.
 * <p/>
 * User: sujie
 * Date: 12/27/12
 * Time: 2:58 PM
 */
public class SupplierVerifyECouponsTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon coupon;
    SupplierUser supplierUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        coupon = FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(Account.class, "balanceAccount");

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void test_显示券验证页面() {
        supplierUser.lastShopId = this.shop.id;
        supplierUser.save();

        Http.Response response = GET("/verify");
        assertStatus(200, response);
        List<Shop> shopList = (List<Shop>) renderArgs("shopList");
        Shop shop = (Shop) renderArgs("shop");
        assertNotNull(shopList);
        assertNotNull(shop);
        assertEquals(1, shopList.size());
        assertEquals(this.shop.id, shop.id);
    }


    @Test
    public void 验证一张券() {
        Map<String, String> params = new HashMap<>();
//        params.put("shopId", shop.id.toString());
//        params.put("eCouponSn", coupon.eCouponSn);
        Http.Response response = POST("/verify/" + shop.id + "/" + coupon.eCouponSn);

        System.out.println("response:" + response);

        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch("【一百券】您尾号" + coupon.getLastCode(4)
                + "的券号于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166",
                msg.getContent());

    }

    @Test
    public void 无效店ID() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", "99999999");
        params.put("eCouponSn", "0000000000");
        Http.Response response = POST("/coupons/single-verify", params);

        assertContentMatch("1", response);
    }

    @Test
    public void 验证无效券() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", "0000000000");
        Http.Response response = POST("/coupons/single-verify", params);

        assertContentMatch("err", response);
    }

    /**
     * 使用正则匹配结果.
     *
     * @param pattern
     * @param content
     */
    public static void assertSMSContentMatch(String pattern, String content) {
        assertSMSContentLength(content);
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(content).find();
        assertTrue("The content (" + content + ") does not match '" + pattern
                + "'", ok);
    }

    protected static void assertSMSContentLength(String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(),
                content.length() <= 67);
    }
}
