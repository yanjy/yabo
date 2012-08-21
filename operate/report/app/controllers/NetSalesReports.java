package controllers;

import models.SalesOrderItemReport;
import models.SalesOrderItemReportCondition;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-21
 * Time: 上午9:42
 */
@With(OperateRbac.class)
public class NetSalesReports extends Controller {

    private static final int PAGE_SIZE = 30;

    /**
     * 查询销售税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("net_sales_reports")
    public static void index(SalesOrderItemReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesOrderItemReportCondition();
        }

        // 查询出所有结果
        List<SalesOrderItemReport> resultList = SalesOrderItemReport.getNetSales(condition);
        // 分页
        ValuePaginator<SalesOrderItemReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        // 汇总
        SalesOrderItemReport summary = SalesOrderItemReport.getNetSummary(resultList);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(reportPage, summary, condition, supplierList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
