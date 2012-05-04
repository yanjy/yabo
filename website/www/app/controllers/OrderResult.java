package controllers;


import org.apache.commons.lang.StringUtils;

import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With({SecureCAS.class, WebsiteInjector.class})
public class OrderResult extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();

	//支付宝
	public static void alipayReturn() {

		//验证通知结果
		String errorMessage = null;
		if (!paymentFlow.paymentNotify(params.all())){
			errorMessage = "对不起，暂时无法读取信息，请您稍后再试";
		}

		renderTemplate("OrderResult/index.html", errorMessage);

	}

	/**
	 * 财付通
	 */
	public static void tenpayReturn() {
		String error =  request.params.get("error");
		String errorMessage ="";
		if (StringUtils.isNotBlank(error)) {
			errorMessage = "对不起，暂时无法读取信息，请您稍后再试";
		}
		renderTemplate("OrderResult/index.html",errorMessage);

	}

}
