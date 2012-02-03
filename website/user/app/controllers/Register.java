package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import models.Registers;

import org.apache.commons.codec.digest.DigestUtils;

import play.cache.Cache;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.With;

import common.CharacterUtil;

import controllers.modules.cas.SecureCAS;

/**
 * 前台注册用户
 * 
 * @author yanjy
 *
 */
//@With(SecureCAS.class)
public class Register extends Controller{

	/**
	 * 注册页面 
	 */
	public static void register() {
		render();
	}

	/**
	 *  注册新用户
	 * 
	 * @param email
	 * @param mobile
	 * @param password
	 */
	public static void create(String email,String mobile,String password) {
		Registers r= new Registers();
		pagecheck();
		r.mobile=	mobile;
		r.email=email;
		String password_salt=CharacterUtil.getRandomString(6);
		//密码加密
		r.crypted_password=DigestUtils.md5Hex(password+password_salt);
		//正常
		r.status="1";
		//随机吗
		r.password_salt=password_salt;
		//获得本机IP
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip=addr.getHostAddress();
			r.login_ip=ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		r.save();
		index();
	}

	/**
	 * 页面信息验证
	 * 
	 * @return
	 */
	private static void pagecheck() {
		//邮箱
		String email =params.get("email");
		//手机
		String mobile =params.get("mobile");
		//密码
		String password =params.get("password");
		//确认密码
		String sure_pwd =params.get("sure_pwd");
		//验证码
		String captcha =params.get("captcha");
		//邮箱格式验证
		validation.required(email);
		if (validation.email(email) !=null) {
			validation.email(email).message(email);
		}
		validation.required(mobile);

		validation.match(mobile, "^1[3|4|5|8][0-9]\\d{4,8}$");

		validation.required(password);

		validation.required(sure_pwd);

		validation.equals(sure_pwd, password).message("两次密码输入的不一样！！");

		validation.required(captcha);
		if("dev".equals(play.Play.configuration.get("application.mode"))) {
			validation.equals(captcha.toUpperCase(), Cache.get(params.get("randomID"))).message("验证码不对，请重新输入！");
		}

		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			render("Register/register.html",params);
		}
	}

	/**
	 *注册 用户一览
	 */
	public static void index() {
		String email = params.get("email");
		String para=email==null?"":email;
		JPAQuery query=Registers.find("email like ? ", "%"+para+"%");
		List<Registers> list=query.fetch();
		render(list);
	}

	/**
	 * 验证码
	 * @param id
	 */
	public static void captcha(String randomID) {
		Images.Captcha captcha = Images.captcha();

		String code = captcha.getText("#49B07F",4);
		Cache.set(randomID, code.toUpperCase(), "30mn");
		renderBinary(captcha);
	}

	/**
	 * UUID
	 * 
	 * @return
	 */
	public static String genRandomId(){
		return Codec.UUID();
	}

}
