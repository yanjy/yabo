package controllers;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.util.RefundUtil;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.order.Order;
import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebCAS.class})
public class MyCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的券列表
	 */
	public static void coupons(Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName) {
		User user = WebCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");
		render("MyCoupons/e_coupons.html", couponsList, breadcrumbs);
	}

	/**
	 * 申请退款
	 * 
	 * @param id 券ID
	 * @param applyNote 退款原因
	 */
	public static void applyRefund(Long id, String applyNote){

		User user = WebCAS.getUser();
		ECoupon eCoupon = ECoupon.findById(id);
		if(eCoupon == null || !eCoupon.order.user.getId().equals(user.getId())){
			renderJSON("{\"error\":\"no such eCoupon\"}");
			return;
		}
		if(!(eCoupon.status == ECouponStatus.UNCONSUMED || eCoupon.status == ECouponStatus.EXPIRED)){
			renderJSON("{\"error\":\"can not apply refund with this goods\"}");
			return;
		}

		//查找原订单信息
		Order order = eCoupon.order;
		TradeBill tradeBill = null;
		OrderItems orderItem = null;

		if(order != null){
			tradeBill = TradeBill.find("byOrderIdAndTradeStatus", order.getId(), TradeStatus.SUCCESS).first();
			orderItem = OrderItems.find("byOrderAndGoods",order, eCoupon.goods).first();
		}
		if(order == null || tradeBill == null || orderItem == null){
			renderJSON("{\"error\":\"can not get the trade bill\"}");
			return;
		}

		//创建退款流程
		RefundBill refundBill = RefundUtil.create(tradeBill, order.getId(), orderItem.getId(),
				orderItem.salePrice, applyNote);
		RefundUtil.success(refundBill);

		//更改库存
		eCoupon.goods.baseSale += 1;
		eCoupon.goods.saleCount -= 1;
		eCoupon.goods.save();

		//更改订单状态
		eCoupon.status = ECouponStatus.REFUND;
		eCoupon.save();

		renderJSON("{\"error\":\"ok\"}");
	}
}
