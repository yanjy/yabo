package controllers;

import com.uhuila.common.util.DateUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-30
 * Time: 下午3:37
 */
@With(OperateRbac.class)
@ActiveNavigation("verify_index")
public class OperateVerifyCoupons extends Controller {

    /**
     * 验证页面
     */
    @ActiveNavigation("verify_index")
    public static void index() {
        Long supplierId = null;
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierList.size() > 0) {
            supplierId = supplierList.get(0).id;
        }

        List shopList = Shop.findShopBySupplier(supplierId);
        if (shopList.size() == 0) {
            renderArgs.put("noShop", "该商户没有添加门店信息！");
        }
        render(shopList, supplierList);

    }


    /**
     * 查询
     *
     * @param eCouponSn 券号
     */
    public static void verify(Long supplierId, Long shopId, String eCouponSn) {

        List shopList = Shop.findShopBySupplier(supplierId);
        if (Validation.hasErrors()) {
            render("/OperateVerifyCoupons/index.html", shopList, eCouponSn, supplierId);
        }

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        render("/OperateVerifyCoupons/verify.html", shopList, shopId, ecoupon);
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void update(Long shopId, Long supplierId, String eCouponSn, String shopName) {
        if (Validation.hasErrors()) {
            render("../views/SupplierCoupons/index.html", eCouponSn);
        }


        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        //根据页面录入券号查询对应信息,并产生消费交易记录
        if (eCoupon == null) {
            renderJSON("err");
        }
        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            if (!eCoupon.isBelongShop(shopId)) {
                renderJSON("1");
            }

//            eCoupon.consumeAndPayCommission(shopId, OperateRbac.currentUser(), VerifyCouponType.SHOP);
            String dateTime = DateUtil.getNowTime();
            String coupon = eCoupon.getLastCode(4);

            // 发给消费者
            SMSUtil.send("【券市场】您尾号" + coupon + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", eCoupon.orderItems.phone, eCoupon.replyCode);
        } else {
            renderJSON(eCoupon.status);
        }

        renderJSON("0");
    }
}