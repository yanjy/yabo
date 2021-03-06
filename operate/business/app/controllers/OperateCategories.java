package controllers;

import models.sales.Category;
import models.sales.CategorySerializer;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 商品分类的控制器.
 * <p/>
 * User: sujie
 * Date: 3/12/12
 * Time: 10:42 AM
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_index")
public class OperateCategories extends Controller {

    public static void showSubs(Long id) {
        List<Category> categories = Category.findByParent(id);

        renderJSON(categories, new CategorySerializer());
    }

}
