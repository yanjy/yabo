package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.order.Cart;
import play.data.binding.As;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.List;

/**
 * 购物车控制器，提供http接口对购物车进行增删该查
 *
 * @author likang
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Carts extends Controller {
    public static final int TOP_LIMIT = 5;

    /**
     * 购物车主界面
     */
    public static void index() {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);
        render(carts);
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId   商品ID
     * @param increment 购物车中商品数增量，
     *                  若购物车中无此商品，则新建条目
     *                  若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void order(long goodsId, int increment) {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if (goods == null) {
            error(500, "no such goods: " + goodsId);
            return;
        }
        if (user == null && cookie == null) {
            error(500, "can not identity current user");
            return;
        }

        Cart.order(user, cookieValue, goods, increment);

        ok();
    }

    /**
     * 在顶部展示所有购物车内容
     */
    public static void tops() {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> cartList = Cart.findAll(user, cookieValue);
        int count = 0;
        for (Cart cart : cartList) {
            count += cart.number;
        }
        if (cartList.size() <= 5) {
            renderArgs.put("carts", cartList);
        } else {
            ValuePaginator<Cart> carts = new ValuePaginator<>(cartList);
            carts.setPageNumber(1);
            carts.setPageSize(TOP_LIMIT);
            renderArgs.put("carts", carts);
        }

        renderArgs.put("count", count);
        render();
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsIds 商品列表
     */
    public static void delete(@As(",") List<Long> goodsIds) {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        if (user == null && cookie == null) {
            error(500, "can not identity current user");
            return;
        }
        if (goodsIds == null || goodsIds.size() == 0) {
            error(500, "no goods specified");
            return;
        }

        Cart.delete(user, cookieValue, goodsIds);

        ok();
    }
}
