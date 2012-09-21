package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.CashCoupon;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import play.cache.Cache;
import play.libs.Codec;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-20
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class CashCoupons extends Controller{
    public static void index(){
        BreadcrumbList breadcrumbs = new BreadcrumbList("现金券充值", "/cash-coupon");
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String randomID = Codec.UUID();
        String action = "verify";
        render(randomID, breadcrumbs, user, account, action);
    }

    public static void verify(String couponCode, String code, String randomID){
        String errMsg = null;
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String action = "verify";
        CashCoupon coupon = null;
        String ridA = null;
        String ridB = null;

        if(Cache.get(randomID) == null || code == null
                || !((String)Cache.get(randomID)).toLowerCase().equals(code.toLowerCase())){
            errMsg = "验证码错误";
        }else {
            coupon = CashCoupon.find("byChargeCode", couponCode).first();
            if (coupon == null){
                errMsg = "现金券充值密码输入错误";
            }else if(coupon.chargedAt != null){
                errMsg = "该现金券已被使用";
            }else if(coupon.deleted == DeletedStatus.DELETED){
                errMsg = "该券无法使用";
            }else {
                action = "use";
                ridA = Codec.UUID();
                ridB = Codec.UUID();
                Cache.set(ridA, ridB, "5mn");
                Cache.set(ridB, coupon.getId(), "5mn");
            }
        }

        Cache.delete(randomID);
        randomID = Codec.UUID();
        BreadcrumbList breadcrumbs = new BreadcrumbList("现金券充值", "/cash-coupon");
        render("CashCoupons/index.html",randomID, errMsg,
                breadcrumbs, user, account, action, coupon, ridA, ridB, couponCode);
    }

    public static void useCoupon(String ridA, String ridB){
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String errMsg = null;
        String suc = null;
        String action = "verify";

        if(Cache.get(ridA) == null || Cache.get(ridB) == null || !Cache.get(ridA).equals(ridB)){
            errMsg = "验证失败";
        }
        CashCoupon coupon = CashCoupon.findById((Long)Cache.get(ridB));
        if(coupon == null){
            errMsg = "验证失败，现金券不存在";
        }else {
            suc = "充值成功";
            coupon.chargedAt = new Date();
            coupon.userId = user.getId();
            coupon.save();
            TradeBill tradeBill = TradeUtil.createPromotionChargeTrade(account, coupon.faceValue, null);
            TradeUtil.success(tradeBill, "代金券: " + coupon.name + " 充值" + coupon.faceValue + "元");
        }
        Cache.delete(ridA);
        Cache.delete(ridB);
        String randomID = Codec.UUID();
        BreadcrumbList breadcrumbs = new BreadcrumbList("现金券充值", "/cash-coupon");
        render("CashCoupons/index.html",randomID, suc, errMsg, breadcrumbs, user, account, action);
    }

}