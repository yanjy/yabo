package controllers;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.uhuila.common.util.RandomNumberUtil;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.consumer.UserInfo;
import models.mail.CouponMessage;
import models.mail.MailUtil;
import models.sms.SMSUtil;
import play.cache.Cache;
import play.data.validation.Validation;
import play.libs.Images;
import play.mvc.Controller;

/**
 * 找回密码.
 * User: ynajy
 */
public class FindPassword extends Controller {
	/**
	 * 找回密码页面
	 */
	public static void index() {
		render();
	}

	/**
	 * 通过邮箱找回密码页面
	 */
	public static void findByEmail() {
		render("FindPassword/findByEmail.html");
	}

	/**
	 * 通过邮箱找回密码,并验证邮箱
	 */
	public static void checkByEmail(String email) {
		String returnFlag = User.getUser(email);
		renderJSON(returnFlag);
	}


	/**
	 * 通过手机找回密码页面
	 */
	public static void findByTel() {
		render("FindPassword/findByTel.html");
	}

	/**
	 * 通过手机找回密码页面
	 * @param mobile 手机
	 */
	public static void checkByTel(String mobile) {

		String returnFlag = User.checkMobile(mobile);
		//手机存在
		if ("2".equals(returnFlag)) {
			String validCode = RandomNumberUtil.generateSerialNumber(4);
			String comment="您的验证码是"+validCode+", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
			SMSUtil.send(comment, mobile);
			//保存手机和验证码
			Cache.set("validCode_", validCode, "10mn");
			Cache.set("mobile_", mobile, "10mn");
		}
		renderJSON(returnFlag);
	}

	/**
	 * 绑定手机
	 * 
	 * @param mobile 手机
	 */
	public static void reset(String mobile,String validCode) {
		Object objCode = Cache.get("validCode_");
		Object objMobile = Cache.get("mobile_");
		String cacheValidCode = objCode== null ?"":objCode.toString();
		String cacheMobile =  objMobile== null ?"":objMobile.toString();
		String returnFlag = User.checkMobile(mobile);
		//手机不存在
		if ("0".equals(returnFlag)) {
			renderJSON("3");
		}
		//判断验证码是否正确
		if(!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {
			renderJSON("1");
		}
		//判断手机是否正确
		if(!StringUtils.normalizeSpace(cacheMobile).equals(mobile)) {
			renderJSON("2");
		}
		Cache.delete("validCode_");
		Cache.delete("mobile_");
		renderJSON("0");
	}

	/**
	 * 绑定手机
	 * 
	 * @param mobile 手机
	 */
	public static void resetPassword() {
		String mobile = request.params.get("mobile");
		String totken = request.params.get("totken");
		render(mobile,totken);
	}

	/**
	 * 绑定手机
	 * 
	 * @param mobile 手机
	 */
	public static void updatePassword(String totken,String mobile,String password,String confirmPassword) {
		System.out.println("aaaaaaaaaaaaaaa");
		if (StringUtils.isBlank(totken) && StringUtils.isBlank(mobile) ) {
			renderJSON("-1");
		}

		//根据手机有邮箱更改密码
		User.updateFindPwd(totken, mobile,password);

		Cache.delete("mobile_");
		Cache.delete("user_email_");
		renderJSON("1");
	}

	/**
	 * 通过手机找回密码页面
	 */
	public static void success() {
		render("FindPassword/success.html");
	}
}
