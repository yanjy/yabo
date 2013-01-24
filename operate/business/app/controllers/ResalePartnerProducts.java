package controllers;

import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 * Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class ResalePartnerProducts extends Controller {
    public static int PAGE_SIZE = 20;

    @ActiveNavigation("resale_partner_product")
    public static void index(GoodsCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new GoodsCondition();
        }

        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        Map<String, List<ResalerProduct> > partnerProducts = new HashMap<>();
        for(models.sales.Goods goods : goodsPage.getCurrentPage()) {
            List<ResalerProduct> products = ResalerProduct.find("byGoods", goods).fetch();
            for (ResalerProduct product : products) {
                List<ResalerProduct> p = partnerProducts.get(goods.id + product.partner.toString());
                if (p == null) {
                    p = new ArrayList<>();
                    partnerProducts.put(goods.id + product.partner.toString(), p);
                }
                p.add(product);
            }
        }

        render(goodsPage, condition, supplierList, partnerProducts);
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    @ActiveNavigation("resale_partner_product")
    public static void journal(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        List<ResalerProductJournal> journals = ResalerProductJournal.find("byProduct", product).fetch();
        for(ResalerProductJournal journal : journals) {
            journal.operator = ((OperateUser)OperateUser.findById(journal.operatorId)).userName;
        }
        render(journals);
    }
    @ActiveNavigation("resale_partner_product")
    public static void journalJson(Long journalId) {
        ResalerProductJournal journal = ResalerProductJournal.findById(journalId);
        if (journal == null) {
            notFound();
        }
        render(journal);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(String partner, Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            Logger.info("goods not found");
            error("商品不存在");
        }
        List<ResalerProduct> products = ResalerProduct.find("goods = ? and partner = ? order by createdAt desc",
                goods, OuterOrderPartner.valueOf(partner.toUpperCase())).fetch();
        for (ResalerProduct product : products) {
            product.creator = ((OperateUser)OperateUser.findById(product.creatorId)).userName;
            product.lastModifier = ((OperateUser)OperateUser.findById(product.lastModifierId)).userName;
        }
        render(products);
    }

}
