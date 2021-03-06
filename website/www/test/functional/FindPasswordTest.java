/**
 *
 */
package functional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import util.DateHelper;

/**
 * @author wangjia
 * @date 2012-7-31 下午1:14:50
 */
public class FindPasswordTest extends FunctionalTest {
    UserInfo userInfo;
    User user;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        user.mobile = "15618096151";
        user.save();

        Cache.clear();
    }

    protected void login() {
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testSendMessageCode() {
        Response response = GET("/send-message_code?from=15618096151");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK 200
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("1", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testCheckMobileMobileNotExist() {
        Response response = GET("/check-mobile");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK 200
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("3", getContent(response)); // 浏览器相应
    }


    @Test
    public void testCheckMobileValidCodeWrong() {
        Response response = GET("/check-mobile?mobile=15618096151&validCode=123");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK 200
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("1", response.out.toString()); // 浏览器相应
    }


    @Test
    public void testCheckMobile() {
        Cache.set("mobile_", "15618096151", "30mn");
        Response response = GET("/check-mobile?mobile=15618096151&validCode=123456");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK 200
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("0", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testUpdatePassword() {

        Map<String, String> passwordParams = new HashMap<>();
        passwordParams.put("password", "123");
        passwordParams.put("confirmPassword", "123");
        Response response = POST("/reset-password", passwordParams);
        assertEquals("-1", response.out.toString()); // 浏览器相应
    }


    @Test
    public void testUpdatePasswordMobileBlank() {
        Map<String, String> passwordParams = new HashMap<>();
        passwordParams.put("mobile", "15618096151");
        passwordParams.put("password", "123");
        passwordParams.put("confirmPassword", "123");
        Response response = POST("/reset-password", passwordParams);
        assertEquals("1", response.out.toString()); // 浏览器相应
    }


    @Test
    public void testCheckMobilePhoneWrong() {
        Cache.set("mobile_", "123", "30mn");
        Response response = GET("/check-mobile?mobile=15618096151&validCode=123456");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK 200
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("2", response.out.toString()); // 浏览器相应

    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/forget-password");
        assertStatus(200, response);
        assertContentMatch("请选择找回密码的方式", response);
    }

    @Test
    public void testResetPassword() {
        user.passwordToken = "12345";
        user.sendMailAt = DateHelper.beforeDays(new Date(), 1);
        user.save();
        user.refresh();
        Http.Response response = GET("/reset-password?mobile=" + user.mobile + "&token=" + user.passwordToken);
        assertStatus(200, response);
        assertEquals(false, renderArgs("isExpired"));
    }

    @Test
    public void testFindByEmailOrMobile() {
        Http.Response response = GET("/find-password?from=email");
        assertStatus(200, response);
        assertContentMatch("请填写您的注册邮箱", response);
        response = GET("/find-password?from=mobile");
        assertStatus(200, response);
        assertContentMatch("输注册手机", response);
    }

    @Test
    public void testSendEmailSuccess() {
        Http.Response response = GET("/send-email/success");
        assertStatus(200, response);
        assertContentMatch("找回密码-邮件发送成功", response);
    }


}
