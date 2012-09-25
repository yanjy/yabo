package controllers;

import models.order.Order;
import models.sales.ConsultRecord;
import models.sales.ConsultResultCondition;
import models.sales.ConsultType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-24
 * Time: 下午12:37
 * To change this template use File | Settings | File Templates.
 */
@With(OperateRbac.class)
@ActiveNavigation("crm_app")
public class OperateConsult extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(ConsultResultCondition condition) {
        if (condition == null) {
            condition = new ConsultResultCondition();
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<ConsultRecord> consultList = models.sales.ConsultRecord.query(condition, null, pageNumber, PAGE_SIZE);

        render(consultList,condition);

    }


    public static void consultExcelOut(ConsultResultCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new ConsultResultCondition();
        }
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "咨询内容列表_" + System.currentTimeMillis() + ".xls");
        JPAExtPaginator<ConsultRecord> consultList = ConsultRecord.query(condition, null, pageNumber, PAGE_SIZE);

        for (ConsultRecord consult : consultList) {

            if (consult.consultType == ConsultType.ORDERCONSULT) {
                consult.consultTypeInfo = "订购咨询";
            } else if (consult.consultType == ConsultType.REFUND) {
                consult.consultTypeInfo = "退款";
            } else if (consult.consultType == ConsultType.VERIFYERROR) {
                consult.consultTypeInfo = "验证错误";
            } else if (consult.consultType == ConsultType.WEBSITEBROKEN) {
                consult.consultTypeInfo = "网站报修";
            } else if (consult.consultType == ConsultType.QUESTIION) {
                consult.consultTypeInfo = "商户疑问";

            } else if (consult.consultType == ConsultType.FEEDBACK) {
                consult.consultTypeInfo = "意见反馈";

            } else if (consult.consultType == ConsultType.COMPLAINT) {
                consult.consultTypeInfo = "投诉反馈";
            }

            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
            consult.createdAtInfo = formatter.format(consult.createdAt);



        }


        render(consultList,condition);

    }
}
