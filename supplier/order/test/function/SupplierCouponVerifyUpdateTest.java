package function;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

import com.uhuila.common.util.DateUtil;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;

/**
 * 门店验证测试
 * @author tanglq
 */
public class SupplierCouponVerifyUpdateTest extends FunctionalTest {
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
    public void 验证一张券() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", coupon.eCouponSn);
        Http.Response response = POST("/coupons/update", params);

        assertContentMatch("0", response);
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertSMSContentMatch("【一百券】您尾号" + coupon.getLastCode(4)
                + "的券号于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166",
                msg.getContent());

    }

    @Test
    public void 无效店ID() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", "99999999");
        params.put("eCouponSn", "0000000000");
        Http.Response response = POST("/coupons/update", params);

        assertContentMatch("1", response);
    }
    
    @Test
    public void 验证无效券() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", "0000000000");
        Http.Response response = POST("/coupons/update", params);

        assertContentMatch("err", response);
    }

    @Test
    public void 消费275元时验证多张券返回250元已消费券() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", coupon.eCouponSn);
        params.put("amount", "275");
        Http.Response response = POST("/coupons/update", params);
        assertIsOk(response);
        
        BigDecimal consumedAmount = (BigDecimal)renderArgs("consumedAmount");
        assertNotNull(consumedAmount);
        assertEquals(new BigDecimal("250.00").setScale(2), consumedAmount.setScale(2));

        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(3, ecoupons.size());
        
        
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertSMSContentMatch("【一百券】您尾号" + coupon.getLastCode(4)
                + "共3张券(总面值250元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166",
                msg.getContent());

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