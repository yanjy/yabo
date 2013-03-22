package models.weixin;

import models.admin.SupplierUser;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-22
 * Time: 上午12:03
 */
public class WeixingBindSupplierUserInvocation extends WeixinInvocation {
    @Override
    public ExtensionResult execute(WeixinContext context) {
        String content = context.weixinRequest.selectTextTrim("Content");
        SupplierUser supplierUser = SupplierUser.find("idCode=?", content).first();
        if (supplierUser == null) {
            context.resultText = "没有找到身份绑定信息！";
            return ExtensionResult.SUCCESS;
        }
        supplierUser.weixinOpenId = context.weixinRequest.fromUserName;
        supplierUser.save();
        context.resultText = "身份绑定成功！" + supplierUser.userName + ", 欢迎您使用一百券商家助手！";
        return ExtensionResult.SUCCESS;
    }

    /**
     * 我们约定6位字符为身份绑定请求
     * @param context
     * @return
     */
    @Override
    public boolean match(WeixinContext context) {
        if (context.weixinRequest.msgType == WeixinMessageType.TEXT) {
            String content = context.weixinRequest.selectTextTrim("Content");
            return content.length() == 6;
        }
        return false;
    }
}
