package jobs.dadong;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.dadong.DadongProduct;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.libs.WS;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.common.SafeParse;
import util.ws.WebServiceRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 同步大东商品，返回同步商品数。
 * <p/>
 * 使用Job作为异步请求机制.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:24
 */
public class DadongProductsSyncRequest {
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String ORGAN_CODE = Play.configuration.getProperty("dadong.origin.code", "shanghaishihui_201301145784");
    public static String URL = Play.configuration.getProperty("dadong.url", "http://www.ddrtty.net/bjskiService.action");

    public static Integer syncProducts() {

        Supplier dadong = Supplier.findByDomainName("dadong");

        List<DadongProduct> dadongProductList = new ArrayList<>();

        Template template = TemplateLoader.load("app/xml/template/dadong/GetProductRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("organCode", ORGAN_CODE);
        int pageIndex = 0;
        boolean nextLoop = true;
        do {
            args.put("pageIndex", pageIndex);
            String xml = template.render(args);
            Map<String, Object> params = new HashMap<>();
            params.put("xml", xml);
            try {
                String document = WebServiceRequest.url(URL).type("thirdtuan.dadong.GetProducts")
                        .encoding("GB2312").params(params).addKeyword(String.valueOf(pageIndex))
                        .postString();

                List<String> products = DadongXmlNodePath.selectNodes("products", document);

                if (products == null || products.size() == 0) {
                    Logger.info("找不到products");
                    break;
                }
                for (String node : products) {
                    String productId = DadongXmlNodePath.selectText("product_id", node);
                    DadongProduct product = new DadongProduct();
                    product.productId = SafeParse.toLong(productId);
                    product.province = DadongXmlNodePath.selectText("province", node);
                    product.city = DadongXmlNodePath.selectText("city", node);
                    product.aqeg = DadongXmlNodePath.selectText("aqeg", node);
                    product.category = DadongXmlNodePath.selectText("category", node);
                    product.productName = DadongXmlNodePath.selectText("product_name", node);
                    product.faceValue = SafeParse.toBigDecimal(DadongXmlNodePath.selectText("product_faceValue", node));
                    product.webValue = SafeParse.toBigDecimal(DadongXmlNodePath.selectText("product_webValue", node));
                    product.platformValue = SafeParse.toBigDecimal(DadongXmlNodePath.selectText("product_platformValue", node));
                    product.ticketExplain = DadongXmlNodePath.selectText("product_ticketExplain", node);
                    product.address = DadongXmlNodePath.selectText("product_address", node);
                    product.imageUrl = DadongXmlNodePath.selectText("product_image", node);
                    product.introduction = DadongXmlNodePath.selectText("product_introduction", node);
                    product.expireTime = SafeParse.toDate(DadongXmlNodePath.selectText("product_expireTime", node));

                    Logger.info("product:" + product.toString());
                    dadongProductList.add(product);
                }
            } catch (Exception e) {
                e.printStackTrace();
                nextLoop = false;
            }
            pageIndex++;

        } while (nextLoop);

        List<Goods> goodsList = new ArrayList<>();

        for (DadongProduct product : dadongProductList) {
            Goods goods = Goods.find("bySupplierGoodsId", product.productId).first();
            if (goods != null) {
                continue;
            }
            Goods g = createGoods(dadong, product);
            goodsList.add(g);
        }

        return goodsList.size();
    }

    /**
     * 创建一百券商品
     *
     * @param product 大东商品
     */
    private static Goods createGoods(Supplier dadong, DadongProduct product) {
        Goods goods = new Goods();
        Brand brand = Brand.find("bySupplier", dadong).first();

        Shop shop = createShop(dadong, product);
        if (shop == null) {
            Logger.error("dadong create goods failed: can not find the area: %s", product.address);
            //return;
        } else {
            goods.shops = new HashSet<Shop>();
            goods.shops.add(shop);
        }

        Category category = Category.find("name like '旅游票务%'").first();
        if (category == null) {
            Logger.error("dadong find category failed: 旅游票务");
            //return;
        } else {
            goods.categories = new HashSet<>();
            goods.categories.add(category);
        }

        goods.createdAt = new Date();
        goods.createdBy = dadong.fullName;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.setDetails(product.ticketExplain);
        goods.effectiveAt = new Date();
        goods.beginOnSaleAt = new Date();
        goods.endOnSaleAt = product.expireTime;
        goods.expireAt = product.expireTime;
        goods.faceValue = product.faceValue;
        goods.salePrice = product.webValue;
        goods.originalPrice = product.platformValue;
        goods.status = GoodsStatus.APPLY;
        goods.cumulativeStocks = 9999L;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.updatedAt = new Date();
        goods.couponType = GoodsCouponType.GENERATE;
        goods.promoterPrice = BigDecimal.ZERO;
        goods.isAllShop = false;

        // 大东票务不可退款
        goods.noRefund = Boolean.TRUE;

        goods.setDiscount(goods.salePrice.multiply(BigDecimal.TEN).divide(goods.faceValue, RoundingMode.FLOOR).setScale(2, RoundingMode.FLOOR));
        goods.resaleAddPrice = BigDecimal.ZERO;
        goods.materialType = MaterialType.ELECTRONIC;
        goods.virtualBaseSaleCount = 0l;

        goods.name = product.productName;
        goods.shortName = product.productName;
        goods.title = goods.shortName;
        //详情
        goods.setExhibition(product.introduction);
        goods.setPrompt("请修改提示信息以给消费者更完整的参考");

        goods.supplierGoodsId = product.productId;
        goods.supplierId = dadong.id;

        goods.brand = brand;
        String imageUrl = product.imageUrl;
        if (!StringUtils.isBlank(imageUrl) && !Play.runingInTestMode()) {
            InputStream is = WS.url(imageUrl).get().getStream();
            try {
                File file = File.createTempFile("dadong", "." + FilenameUtils.getExtension(imageUrl));
                IO.write(is, file);
                goods.imagePath = uploadFile(file);
            } catch (IOException e) {
                Logger.error("upload file error:", e);
            }
        }
        goods.save();
        return goods;
    }

    private static Shop createShop(Supplier dadong, DadongProduct product) {
        Area area = Area.find("byName", product.aqeg).first();
        Shop shop = new Shop();
        if (area != null) {
            shop.areaId = area.id;
            shop.cityId = area.id;
            shop.name = area.name;
        }
        shop.supplierId = dadong.id;
        shop.transport = product.province + " " + product.city + " " + product.aqeg;
        shop.address = product.address;
        return shop.save();
    }

    private static String uploadFile(File file) {
        String targetFilePath = null;
        try {
            targetFilePath = FileUploadUtil.storeImage(file, ROOT_PATH);
        } catch (IOException e) {
            return null;
        }
        return targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
    }
}
