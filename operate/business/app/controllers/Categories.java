package controllers;

import java.util.List;
import models.sales.Category;
import models.sales.CategorySerializer;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.operate.cas.SecureCAS;

/**
 * 商品分类的控制器.
 * <p/>
 * User: sujie
 * Date: 3/12/12
 * Time: 10:42 AM
 */
@With({SecureCAS.class, OperateRbac.class})
@ActiveNavigation("goods_index")
public class Categories extends Controller {

    public static void showSubs(Long id) {
        List<Category> categories = Category.findByParent(id);

        renderJSON(categories, new CategorySerializer());
    }

}
