package controllers;

import models.sales.SecKillGoodsCondition;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:18
 */
@With(OperateRbac.class)
@ActiveNavigation("seckill_goods_add")
public class SecKillGoodsItems extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 展示添加商品页面
     */
    public static void index(Long seckillId, SecKillGoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new SecKillGoodsCondition();

        }

        JPAExtPaginator<SecKillGoodsItem> secKillGoodsItems = SecKillGoodsItem.findByCondition(condition, seckillId, pageNumber,
                PAGE_SIZE);
        secKillGoodsItems.setBoundaryControlsEnabled(true);

        render(secKillGoodsItems, seckillId, condition);
    }

    public static void add(Long seckillId) {
        render(seckillId);
    }

    public static void create(Long seckillId, @Valid SecKillGoodsItem secKillGoodsItem) {
        if (Validation.hasErrors()) {
            render("SecKillGoodsItems/add.html", seckillId);
        }

        models.sales.SecKillGoods goods = models.sales.SecKillGoods.findById(seckillId);
        secKillGoodsItem.secKillGoods = goods;

        secKillGoodsItem.status = SecKillGoodsStatus.OFFSALE;
        secKillGoodsItem.save();

        render("SecKillGoods/index.html");
    }

    public static void edit(Long seckillId, Long id) {
        System.out.println(seckillId);
        SecKillGoodsItem secKillGoodsItem
                = SecKillGoodsItem.findById(id);

        render(secKillGoodsItem, seckillId);
    }

    public static void update(Long id, Long seckillId, @Valid SecKillGoodsItem secKillGoodsItem) {

        checkExpireAt(secKillGoodsItem);

        if (Validation.hasErrors()) {
            render("SecKillGoodsItems/edit.html", secKillGoodsItem, id);
        }

        SecKillGoodsItem.update(id, secKillGoodsItem);

        index(seckillId, null);
    }

    private static void checkExpireAt(SecKillGoodsItem goods) {
        if (goods.secKillBeginAt != null && goods.secKillEndAt != null && goods.secKillEndAt.before(goods.secKillBeginAt)) {
            Validation.addError("SecKillGoodsItem.secKillAt", "validation.beforeThanEffectiveAt");
        }

    }

    /**
     * 下架商品.
     *
     * @param id 商品ID
     */
    public static void offSale(Long seckillId, Long id) {
        SecKillGoodsItem.updateStatus(SecKillGoodsStatus.OFFSALE, id);
        index(seckillId, null);
    }

    /**
     * 上架商品.
     *
     * @param id 商品ID
     */
    public static void onSale(Long seckillId, Long id) {
        SecKillGoodsItem.updateStatus(SecKillGoodsStatus.ONSALE, id);
        index(seckillId, null);
    }
}
