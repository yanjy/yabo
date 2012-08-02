package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserPoint;
import models.consumer.UserPointConfig;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
@With({SecureCAS.class, WebsiteInjector.class})
public class UserPoints extends Controller{
	
	public static int PAGE_SIZE = 15;
	
	/**
	 *积分页面
	 */
	public static void index(UserCondition condition) {
		User user = SecureCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
			condition =  new UserCondition();
		}
		JPAExtPaginator<UserPoint>  pointList = UserPoint.findUserPoints(user, condition,pageNumber, PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的积分", "/userPoint");
        System.out.println(pointList.size()+">>>>>>>>>>>>>>>");
		List<UserPointConfig> configList = UserPointConfig.findAll();
		renderArgs.put("createdAtBegin", condition.createdAtBegin);
		renderArgs.put("createdAtEnd", condition.createdAtEnd);
		renderArgs.put("pointNumber", condition.pointNumber);
		render(pointList, breadcrumbs, user, configList);
	}


}
