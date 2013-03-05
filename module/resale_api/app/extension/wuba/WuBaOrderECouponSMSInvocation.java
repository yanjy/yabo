package extension.wuba;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.accounts.AccountType;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import util.extension.ExtensionResult;

/**
 * 实现重定义58团购的订单券短信内容。
 */
public class WuBaOrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("【58团】")
                .append(StringUtils.isNotEmpty(context.goods.title) ? context.goods.title : context.goods.shortName)
                .append("由58合作商家【一百券】提供,一百券号")
                .append(context.couponInfo)
                .append(context.notes)
                .append("有效期至").append(context.expiredDate)
                .append("客服4007895858");
        context.setSmsContent(sb.toString());
        return ExtensionResult.SUCCESS;
    }

    @Override
    public boolean match(OrderECouponSMSContext context) {
        return (AccountType.RESALER.equals(context.order.userType) && context.order.getResaler().loginName.equals
                (Resaler.WUBA_LOGIN_NAME));
    }
}
