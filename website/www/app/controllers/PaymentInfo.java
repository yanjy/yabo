package controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.payment.AliPaymentFlow;
import models.payment.BillPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With({SecureCAS.class, WebsiteInjector.class})
public class PaymentInfo extends Controller {
	private static PaymentFlow alipayPaymentFlow = new AliPaymentFlow();
	private static TenpayPaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();
	private static BillPaymentFlow billPaymentFlow = new BillPaymentFlow();

	/**
	 * 展示确认支付信息页.
	 *
	 * @param orderNumber 订单ID
	 */
	public static void index(String orderNumber) {
		//加载用户账户信息
		User user = SecureCAS.getUser();
		Account account = AccountUtil.getAccount(user.getId(), AccountType.CONSUMER);

		//加载订单信息
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);
		long goodsNumber = OrderItems.itemsNumber(order);

		List<PaymentSource> paymentSources = PaymentSource.find("order by showOrder").fetch();

		render(user, account, order, goodsNumber, paymentSources);
	}


	/**
	 * 接收用户反馈的订单的支付信息.
	 *
	 * @param orderNumber       订单ID
	 * @param useBalance        是否使用余额
	 * @param paymentSourceCode 网银代码
	 */
	public static void confirm(String  orderNumber, boolean useBalance, String paymentSourceCode) {
		User user = SecureCAS.getUser();
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);

		if (order == null){
			error(500,"no such order");
		}

		Account account = AccountUtil.getAccount(user.getId(), AccountType.CONSUMER);

		//计算使用余额支付和使用银行卡支付的金额
		BigDecimal balancePaymentAmount = BigDecimal.ZERO;
		BigDecimal ebankPaymentAmount = BigDecimal.ZERO;
		if (useBalance){
			balancePaymentAmount = account.amount.min(order.needPay);
			ebankPaymentAmount = order.needPay.subtract(balancePaymentAmount);
		}else {
			ebankPaymentAmount = order.needPay;
		}
		order.accountPay = balancePaymentAmount;
		order.discountPay = ebankPaymentAmount;

        PaymentSource paymentSource = null;
		//如果使用余额足以支付，则付款直接成功
		if (ebankPaymentAmount.compareTo(BigDecimal.ZERO) == 0){
            paymentSource = PaymentSource.getBalanceSource();
            order.payMethod = paymentSource.code;
            order.save();
			order.paid();

			render(order,paymentSource);
		}

        paymentSource = PaymentSource.find("byCode", paymentSourceCode).first();
		//无法确定支付渠道
		if(paymentSource == null){
			error(500, "can not get paymentSource");
		}
        order.payMethod = paymentSource.code;
        order.save();
		render(order, paymentSource);

	}

	/**
	 * 生成网银跳转页.
	 *
	 * @param orderNumber 订单
	 */
	public static void payIt(String orderNumber,String paymentCode){
		User user = SecureCAS.getUser();
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);

		if (order == null){
			error(500,"no such order");
		}

		String form = "";
		if ("tenpay".equals(paymentCode)) {
			try {
				form = tenpayPaymentFlow.generatetTenpayForm(order);
				//直接跳转到财付通
				redirect(form);
			} catch (UnsupportedEncodingException e) {
				error(500,"no such order");
			}
		} else if ("alipay".equals(paymentCode)) {
			form = alipayPaymentFlow.generateForm(order);
			render(form);
		} else {
			form = billPaymentFlow.generateForm(order);
			System.out.println("form========================"+form);
			render(form);
		}
	}
}

