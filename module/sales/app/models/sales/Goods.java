/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.HtmlUtil;
import com.uhuila.common.util.PathUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.supplier.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.InFuture;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.SimplePaginator;
import play.modules.solr.Solr;
import play.modules.solr.SolrEmbedded;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;
import play.modules.view_ext.annotation.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//import models.resale.ResalerLevel;

@Entity
@Table(name = "goods")
//@SolrSearchable
public class Goods extends Model {
    private static final long serialVersionUID = 7063232063912330652L;

    public static final String PREVIEW_IMG_ROOT = "/9999/9999/9999/";

    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";

    //  ========= 不同的价格列表 =======
    /**
     * 商户填写的商品市场价
     */
    @Required
    @Min(0.01)
    @Max(999999)
    @Money
    @Column(name = "face_value")
    @SolrField
    public BigDecimal faceValue;

    /**
     * 商户填写的进货价，采购价
     */
    @Required
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "original_price")
    @SolrField
    public BigDecimal originalPrice;


    /**
     * 运营人员填写的一百券网站价格
     */
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "sale_price")
    @SolrField
    public BigDecimal salePrice;

    /**
     * 分销渠道加价
     */
    @Column(name = "resale_price")
    public BigDecimal resaleAddPrice;
    /**
     * 给推荐者的返利金额
     */
    @Column(name = "promoter_price")
    @Min(0)
    @Max(5)
    @Money
    @SolrField
    public BigDecimal promoterPrice;
    /**
     * 给受邀者的返利金额
     */
    @Column(name = "invited_user_price")
    @Min(0)
    @Max(5)
    @Money
    @SolrField
    public BigDecimal invitedUserPrice;

    //  ======  价格列表结束 ==========

    /**
     * 累积进货量
     * <p/>
     * 通过管理界面增加进货量后得到累计进货量，只会越来越大，因为是累计的
     */
    public Long cumulativeStocks;

    /**
     * 虚拟销量基数
     * <p/>
     * 通过管理界面设置
     * 用户前端网站显示的销量是virtualBaseSaleCount+realSaleCount
     */
    @Column(name = "virtual_base_sale_count")
    public Long virtualBaseSaleCount;


    /**
     * 商品编号
     */
    @MaxSize(30)
    @SolrField
    public String no;
    /**
     * 商品名称
     */
    @Required
    @MaxSize(60)
    @SolrField
    public String name;

    /**
     * 商品标题（短信发送用）
     */
    @Required
    @MaxSize(60)
    @SolrField
    public String title;

    /**
     * 所属商户ID
     */
    @Column(name = "supplier_id")
    public Long supplierId;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    public Set<Shop> shops;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "category_id"), joinColumns = @JoinColumn(name = "goods_id"))
    @Required
    @SolrEmbedded
    public Set<Category> categories;

    @Transient
    private String imageSmallPath;

    @Transient
    @SolrField
    public String getCategoryIds() {
        List<Long> ids = new ArrayList<>();
        if (categories != null) {
            for (Category category : categories) {
                ids.add(category.id);
            }
        }

        return StringUtils.join(ids, " ");
    }


    @Transient
    @SolrField
    public String getParentCategoryIds() {
        List<Long> ids = new ArrayList<>();
        if (categories != null) {
            for (Category category : categories) {
                if (category != null && category.parentCategory != null) {
                    ids.add(category.parentCategory.id);
                }
            }
        }

        return StringUtils.join(ids, " ");
    }

    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    /**
     * 进货量
     */
    @Column(name = "income_goods_count")
    @SolrField
    public Long incomeGoodsCount;

    //=======================================  时间字段 ==================================

    /**
     * 最早上架时间
     */
    @Column(name = "first_onsale_at")
    @SolrField
    public Date firstOnSaleAt;
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @SolrField
    public Date createdAt;

    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    @SolrField
    public Date updatedAt;

    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    @SolrField
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    @SolrField
    public Date expireAt;

    @Column(name = "use_begin_time")
    @SolrField
    public String useBeginTime;

    @Column(name = "use_end_time")
    @SolrField
    public String useEndTime;

    @Column(name = "use_week_day")
    @SolrField
    public String useWeekDay;
    @Transient
    public String useWeekDayAll;


    private BigDecimal discount;

    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    private String details;

    /**
     * 温馨提示
     */
    @MaxSize(65000)
    @Lob
    @Required
    private String prompt;

    /**
     * 商品展示
     */
    @MaxSize(65000)
    @Lob
    @Required
    private String exhibition;
    /**
     * 售出数量
     * <p/>
     * 已作废
     */
    @Deprecated
    @Column(name = "sale_count")
    @SolrField
    public int saleCount;

    /**
     * 剩余商品数量，需要去掉.
     * <p/>
     * 已作废
     */
    @Deprecated
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;
    /**
     * 商品状态,
     */
    @Enumerated(EnumType.STRING)
    public GoodsStatus status;
    /**
     * 创建来源
     */
    @Column(name = "created_from")
    @SolrField
    public String createdFrom;
    /**
     * 创建人
     */
    @Column(name = "created_by")
    @SolrField
    public String createdBy;
    /**
     * 修改人
     */
    @Column(name = "updated_by")
    @SolrField
    public String updatedBy;
    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    @SolrField
    public DeletedStatus deleted;
    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    /**
     * 商品分组代码.
     * 用于多个商品组合成一个商品组，同一系列的发送收货短信时，使用相同的replyCode.
     */
    @Column(name = "group_code", length = 32)
    @SolrField
    public String groupCode;

    /**
     * 手工排序
     */
    @Column(name = "display_order")
    @SolrField
    public String displayOrder;

    @Required
    @ManyToOne
    @JoinColumn(name = "brand_id")
    @SolrEmbedded
    public Brand brand;

    /**
     * 限购数量
     */
    @Column(name = "limit_number")
    @SolrField
    public Integer limitNumber = 0;

    /**
     * 推荐指数.
     */
    @SolrField
    public Integer recommend = 0;

    /**
     * 优先指数.
     */
    @SolrField
    public Integer priority = 0;
    /**
     * 收藏指数.
     */
    @SolrField
    public Integer favorite = 0;

    // 以下定义用于查询条件
    @Transient
    public String salePriceBegin;
    @Transient
    public String salePriceEnd;
    @Transient
    public int saleCountBegin = -1;
    @Transient
    public int saleCountEnd = -1;

    @Transient
    @SolrEmbedded
    public GoodsStatistics statistics;

    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    @SolrField
    public MaterialType materialType;

    /**
     * SEO关键字.
     */
    @Column(name = "keywords")
    @SolrField
    public String keywords;

    @Column(name = "coupon_type")
    @Enumerated(EnumType.STRING)
    @SolrField
    public GoodsCouponType couponType;

    /**
     * 不允许发布的电子商务网站.
     * 设置后将不允许自动发布到这些电子商务网站上
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true,
            fetch = FetchType.LAZY, mappedBy = "goods")
    public Set<GoodsUnPublishedPlatform> unPublishedPlatforms;

    public static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");
    //    private static final String IMAGE_ROOT_GENERATED = Play.configuration
    //            .getProperty("image.root", "/p");
    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    /**
     * solr服务中获取到的商圈.
     */
    @Transient
    private String areaNames;


    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div", "table", "tbody", "tr", "td",
                "background-color", "width", "figure", "figcaption", "strong", "p", "dl", "dt", "dd");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("table", "style", "cellpadding", "cellspacing", "border", "bordercolor", "align");
        HTML_WHITE_TAGS.addAttributes("span", "style", "border", "align");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
                "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }


    /**
     * 获取商品所属的商户信息.
     *
     * @return
     */
    @Transient
    @SolrEmbedded
    public Supplier getSupplier() {
        if (supplierId == null) {
            return null;
        }
        return CacheHelper.getCache(CacheHelper.getCacheKey(Supplier.CACHEKEY + this.supplierId, "GOODS_SUPPLIER"), new CacheCallBack<Supplier>() {
            @Override
            public Supplier loadData() {
                return Supplier.findById(supplierId);
            }
        });
    }

    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;

    @Transient
    public Long topCategoryId;

    /**
     * 是否抽奖商品
     */
    @Column(name = "is_lottery")
    @SolrField
    public Boolean isLottery = Boolean.FALSE;
    /**
     * 是否预约商品
     */
    @Column(name = "is_order")
    @SolrField
    public Boolean isOrder = Boolean.FALSE;

    /**
     * 是否隐藏上架
     */
    @Column(name = "is_hide_onsale")
    public Boolean isHideOnsale = Boolean.FALSE;

    @Transient
    public boolean skipUpdateCache = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id")
    public List<ResalerFav> resalerFavs;

    @OneToMany(mappedBy = "goods")
    public List<GoodsImages> goodsImagesList;

    /**
     * 节省金额.
     *
     * @return
     */
    @Transient
    @SolrField
    public BigDecimal getSavePrice() {
        return originalPrice.remainder(salePrice);
    }

    /**
     * 商品详情
     *
     * @return
     */
    public String getDetails() {
        if (StringUtils.isBlank(details) || "<br />".equals(details)) {
            return "";
        }
        return Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    @Transient
    @SolrField
    public String getDetailContent() {
        return HtmlUtil.html2text(getDetails());
    }

    public void setDetails(String details) {
        this.details = Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    public void setDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) >= 0 && discount.compareTo(BigDecimal.TEN) <= 0) {
            this.discount = discount;
        } else if (discount.compareTo(BigDecimal.ZERO) < 0) {
            this.discount = BigDecimal.ZERO;
        } else if (discount.compareTo(BigDecimal.TEN) > 0) {
            this.discount = BigDecimal.TEN;
        }
    }

    @Column(name = "discount")
    //@SolrField
    public BigDecimal getDiscount() {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            return discount;
        }
        if (faceValue != null && salePrice != null && faceValue.compareTo(BigDecimal.ZERO) > 0) {
            this.discount = salePrice.divide(faceValue, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN);
            if (this.discount.compareTo(BigDecimal.TEN) >= 0) {
                this.discount = BigDecimal.TEN;
            }
        } else {
            this.discount = BigDecimal.ZERO;
        }
        return discount;
    }

    @Transient
    public String getDiscountExpress() {
        BigDecimal discount = getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return "0折";
        }
        if (discount.compareTo(BigDecimal.TEN) >= 0) {
            return "无优惠";
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return "";

        }
        DecimalFormat format = new DecimalFormat("#.#");
        return format.format(discount.doubleValue()) + "折";
    }

    @Transient
    public String getDiscountExpress1() {
        BigDecimal discount = getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        if (discount.compareTo(BigDecimal.TEN) >= 0) {
            return "";
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return "";

        }
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(discount.doubleValue());
    }

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_TINY);
    }

    /**
     * 小规格图片路径
     */
    @Transient
    //@SolrField
    public String getImageSmallPath() {
        if (StringUtils.isNotBlank(imageSmallPath)) {
            return imageSmallPath;
        }
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_SMALL);
    }

    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_MIDDLE);
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_LARGE);
    }

    @Transient
    public String getImageOriginalPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_ORIGINAL);
    }

    public String getExhibition() {
        if (StringUtils.isBlank(exhibition)) {
            return "";
        }
        return Jsoup.clean(exhibition, HTML_WHITE_TAGS);
    }

    public void setExhibition(String exhibition) {
        this.exhibition = Jsoup.clean(exhibition, HTML_WHITE_TAGS);
    }

    public String getPrompt() {
        if (StringUtils.isBlank(prompt)) {
            return "";
        }
        return Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    @Transient
    //@SolrField
    public String getPromptContent() {
        return HtmlUtil.html2text(getPrompt());
    }

    public void setPrompt(String prompt) {
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    @Transient
    public String getSafePrompt() {
        return prompt.replaceAll("&nbsp;", " ");
    }

    @Transient
    public String getSafeDetails() {
        return details.replaceAll("&nbsp;", " ");
    }

    /**
     * 得到实际的库存数量.
     */
    @Transient
    public Long getRealStocks() {
        return this.cumulativeStocks - getRealSaleCount();
    }

    /**
     * 界面上显示的销量，实际销量+虚拟销量基数
     */
    @Transient
    private Long virtualSaleCount;

    @Transient
    //@SolrField
    public Long getVirtualSaleCount() {
        if (virtualSaleCount != null && virtualSaleCount > 0) {
            return virtualSaleCount;
        }
        virtualSaleCount = (getRealSaleCount() == null ? 0 : getRealSaleCount()) + (virtualBaseSaleCount == null ? 0 : virtualBaseSaleCount);
        return virtualSaleCount;
    }

    /**
     * 得到当前实际的销售数量.
     */
    @Transient
    public Long getRealSaleCount() {
        return CacheHelper.getCache(Goods.CACHEKEY_SALECOUNT + this.id, new CacheCallBack<Long>() {
            @Override
            public Long loadData() {
                // 先找出OrderItems中的已销售数量
                long orderItemsBuyCount = OrderItems.count("goods.id=? and order.status != ?", id, OrderStatus.CANCELED);
                // 减去已退款的数量
                long ecouponRefundCount = ECoupon.count("goods.id=? and status=?", id, ECouponStatus.REFUND);

                return orderItemsBuyCount - ecouponRefundCount;
            }
        });
    }

    /**
     * 删除旧缓存以更新显示销售数量.
     */
    public void refreshSaleCount() {
        CacheHelper.delete(Goods.CACHEKEY_SALECOUNT + this.id);
    }

    /**
     * 获取商品允许发布的电子商务平T台.
     *
     * @return
     */
    @Transient
    public List<GoodsPublishedPlatformType> getPublishedPlatforms() {
        List<GoodsPublishedPlatformType> publishedPlatforms = new ArrayList<>();
        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!containsUnPublishedPlatform(type)) {
                publishedPlatforms.add(type);
            }
        }
        return publishedPlatforms;
    }

    @Transient
    //@SolrField
    public String getPublishedPlatformList() {
        List<GoodsPublishedPlatformType> platformTypeList = getPublishedPlatforms();
        return StringUtils.join(platformTypeList, " ");
    }

    public boolean containsUnPublishedPlatform(GoodsPublishedPlatformType type) {
        if (unPublishedPlatforms == null) {
            return true;
        }

        for (GoodsUnPublishedPlatform unPublishedPlatform : unPublishedPlatforms) {
            if (unPublishedPlatform != null && unPublishedPlatform.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    //@SolrField
    public GoodsStatus getStatus() {
        if (status != null && GoodsStatus.ONSALE.equals(status) &&
                (expireAt != null && expireAt.before(new Date())) || (baseSale != null && baseSale <= 0)) {
            status = GoodsStatus.OFFSALE;
        }
        return status;
    }

    /**
     * @return
     */
    @Transient
    //@SolrField
    public boolean isExpired() {
        return expireAt != null && expireAt.before(new Date());
    }

    @Transient
    // @SolrField
    public String getAreaNames() {
        if (StringUtils.isNotBlank(areaNames)) {
            return areaNames;
        }
        Collection<Shop> shopList = getShopList();
        Map<String, Object> areaMap = new HashMap<>();
        for (Shop shop : shopList) {
            Area area = Area.findAreaById(shop.areaId);
            if (area != null) {
                areaMap.put(area.id, area.name);
            }
        }
        areaNames = StringUtils.join(areaMap.values(), " ");
        return areaNames;
    }

    public String getHighLightName(String words) {
        String highLight = name;
        if (words != null) {
            String[] wordArray = words.split(" |,|;|，");
            if (wordArray != null) {
                for (String word : wordArray) {
                    highLight = highLight.replaceAll(word, "<em>" + word + "</em>");
                }
            }
        }
        return highLight;
    }

    @Transient
    public String getWwwUrl() {
        return "/p/" + id;
    }
    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        incomeGoodsCount = 0L;
        createdAt = new Date();
        if (isAllShop) {
            shops = null;
        }
        expireAt = DateUtil.getEndOfDay(expireAt);
        if (unPublishedPlatforms == null) {
            unPublishedPlatforms = new HashSet<>();
        }
        resaleAddPrice = salePrice.compareTo(originalPrice) > 0 ? salePrice.subtract(originalPrice) : BigDecimal.ZERO;
        return super.create();
    }

    public static void update(Long id, Goods goods, boolean noLevelPrices) {
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);
        if (updateGoods == null) {
            return;
        }
        updateGoods.name = goods.name;
        updateGoods.no = goods.no;
        updateGoods.effectiveAt = goods.effectiveAt;
        updateGoods.expireAt = DateUtil.getEndOfDay(goods.expireAt);
        updateGoods.faceValue = goods.faceValue;
        updateGoods.originalPrice = goods.originalPrice;
        goods.discount = null;
        updateGoods.setDiscount(goods.getDiscount());
        updateGoods.salePrice = goods.salePrice;
        updateGoods.baseSale = goods.baseSale;
        updateGoods.promoterPrice = goods.promoterPrice;
        updateGoods.invitedUserPrice = goods.invitedUserPrice;
        updateGoods.materialType = goods.materialType;
        updateGoods.topCategoryId = goods.topCategoryId;
        updateGoods.categories = goods.categories;
        updateGoods.resaleAddPrice = goods.salePrice.compareTo(goods.originalPrice) > 0 ? goods.salePrice.subtract(goods.originalPrice) : BigDecimal.ZERO;
        updateGoods.setPrompt(goods.getPrompt());
        updateGoods.setDetails(goods.getDetails());
        updateGoods.setExhibition(goods.getExhibition());
        updateGoods.updatedAt = new Date();
        updateGoods.updatedBy = goods.updatedBy;
        updateGoods.brand = goods.brand;
        updateGoods.isAllShop = goods.isAllShop;
        updateGoods.status = goods.status;
        updateGoods.keywords = goods.keywords;
        updateGoods.limitNumber = goods.limitNumber;
        updateGoods.couponType = goods.couponType;
        if (!StringUtils.isEmpty(goods.imagePath)) {
            updateGoods.imagePath = goods.imagePath;
        }
        if (goods.supplierId != null) {
            updateGoods.supplierId = goods.supplierId;
        }
        updateGoods.shops = goods.shops;
        updateGoods.title = goods.title;
        updateGoods.setPublishedPlatforms(goods.getPublishedPlatforms());
        updateGoods.useBeginTime = goods.useBeginTime;
        updateGoods.useEndTime = goods.useEndTime;
        updateGoods.useWeekDay = goods.useWeekDay;

        updateGoods.isOrder = (goods.isOrder == null) ? Boolean.FALSE : goods.isOrder;
        updateGoods.isLottery = (goods.isLottery == null) ? Boolean.FALSE : goods.isLottery;
        updateGoods.isHideOnsale = (goods.isHideOnsale == null) ? Boolean.FALSE : goods.isHideOnsale;
        updateGoods.groupCode = (StringUtils.isEmpty(goods.groupCode)) ? null : goods.groupCode.trim();
        updateGoods.save();

    }


    public static final String CACHEKEY = "SALES_GOODS";

    public static final String CACHEKEY_BASEID = "SALES_GOODS_ID";

    public static final String CACHEKEY_SALECOUNT = "SALES_GOODS_COUNT";

    @Override
    public void _save() {
        if (!this.skipUpdateCache) {
            CacheHelper.delete(CACHEKEY);
            CacheHelper.delete(CACHEKEY + this.id);
            CacheHelper.delete(CACHEKEY_SALECOUNT + this.id);
            CacheHelper.delete(CACHEKEY_BASEID + this.id);
        }
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEID + this.id);
        CacheHelper.delete(CACHEKEY_SALECOUNT + this.id);
        super._delete();
    }

    public static void update(Long id, Goods goods) {
        update(id, goods, false);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTop(int limit) {
        return find("status=? and deleted=? and baseSale >=1 and isHideOnsale is false and expireAt > ? order by priority DESC,createdAt DESC",
                GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED,
                new Date()).fetch(limit);
    }

    /**
     * 获取已上架的全部商品数量.
     *
     * @return
     */
    public static long countOnSale() {
        return count("status=? and deleted=? and baseSale >=1 and expireAt > ?", GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED, new Date());
    }


    /**
     * 根据分类获取已上架的全部商品数量.
     *
     * @param categoryId
     * @return
     */
    public static long countOnSaleByTopCategory(long categoryId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.baseSale >= 1 and g.expireAt > :now and g.id in (select g.id from g.categories c where c.parentCategory.id = :categoryId) ");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("now", new Date());
        q.setParameter("categoryId", categoryId);
        return q.getResultList().size();
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopByCategory(long categoryId, int limit) {
        return findTopByCategory(categoryId, limit, false);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopByCategory(long categoryId, int limit, boolean isRootCategory) {
        EntityManager entityManager = JPA.em();
        String categoryQueryCond = isRootCategory ? "c.parentCategory.id" : "c.id";
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.baseSale >= 1 and g.expireAt > :now and g.id in (select g.id from g.categories c where " + categoryQueryCond + " = :categoryId) " +
                "order by priority DESC,createdAt DESC");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("now", new Date());
        q.setParameter("categoryId", categoryId);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public static List<Goods> findInIdList(List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.size() == 0) {
            return new ArrayList<>();
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.id in :ids");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("ids", goodsIds);
        return q.getResultList();
    }

    public static Goods findUnDeletedById(long id) {
        return find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
    }

    public static Goods findOnSale(long id) {
        return find("id=? and deleted=? and status=? and baseSale >= 1 and expireAt > ?", id,
                DeletedStatus.UN_DELETED, GoodsStatus.ONSALE, new Date()).first();
    }


    public static List<Goods> findBySupplierId(Long supplierId) {
        return find("supplierId=? and deleted=? and isLottery=?", supplierId,
                DeletedStatus.UN_DELETED, Boolean.FALSE).fetch();
    }

    public static JPAExtPaginator<Goods> findByCondition(GoodsCondition condition,
                                                         int pageNumber, int pageSize) {

        JPAExtPaginator<Goods> goodsPage = new JPAExtPaginator<>
                ("Goods g", "g", Goods.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);
        return goodsPage;
    }


    public static List<Brand> findBrandByCondition(GoodsCondition condition) {
        return findBrandByCondition(condition, -1);
    }

    /**
     * www首页根据商品条件查询品牌.
     * 品牌按显示优先级倒序、按商户的创建时间倒序
     *
     * @param condition
     * @param limit
     * @return
     */
    public static List<Brand> findBrandByCondition(GoodsCondition condition, int limit) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select distinct g.brand from Goods g where " +
                condition.getFilter() + " and g.status='ONSALE' order by g.brand.displayOrder desc,g.brand.supplier.createdAt desc");
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    public static void delete(Long... ids) {
        for (Long id : ids) {
            models.sales.Goods goods = models.sales.Goods.findById(id);
            if (goods != null) {
                goods.deleted = DeletedStatus.DELETED;
                goods.save();
            }
        }
    }

    public static void updateStatus(GoodsStatus status, Long... ids) {
        for (Long id : ids) {
            models.sales.Goods goods = models.sales.Goods.findById(id);
            goods.status = status;

            if (status == GoodsStatus.ONSALE && goods.firstOnSaleAt == null) {
                goods.firstOnSaleAt = new Date();
            }

            goods.save();
        }
    }

    private static final String expiration = "30mn";

    /**
     * 将预览商品存入缓存.
     * <p/>
     * 商品图片移动到指定目录下，指定目录下的预览用的商品图片将通过后台crontab的方式定时删除.
     *
     * @param goods
     * @return uuid
     */
    public static String preview(Long id, Goods goods, File imageFile, String rootDir) throws IOException {
        goods.status = GoodsStatus.UNCREATED;
        if (goods.isAllShop) {
            goods.shops = new HashSet<>();

            goods.shops.addAll(Shop.findShopBySupplier(goods.supplierId));
        }
        if (id == null && imageFile == null) {
            goods.imagePath = null;
        } else if (imageFile == null || imageFile.getName() == null) {
            Goods originalGoods = Goods.findById(id);
            goods.imagePath = originalGoods.imagePath;
        } else {
            String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf("."));
            String imagePath = PREVIEW_IMG_ROOT + FileUploadUtil.generateUniqueId() + ext;
            File targetDir = new File(rootDir + PREVIEW_IMG_ROOT);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            FileUtils.moveFile(imageFile, new File(rootDir + imagePath));
            goods.imagePath = imagePath;
        }
        UUID cacheId = UUID.randomUUID();
        play.cache.Cache.set(cacheId.toString(), goods.id, expiration);
        return cacheId.toString();
    }

    /**
     * 获取商品预览对象.
     *
     * @param uuid 缓存键值
     * @return 预览商品
     */
    public static Goods getPreviewGoods(String uuid) {
        return (Goods) play.cache.Cache.get(uuid);
    }

    /**
     * 分销商查询
     *
     * @param condition  查询条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return
     */
    public static JPAExtPaginator<Goods> findByResaleCondition(Resaler resaler,
                                                               GoodsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Goods> goodsPage = new JPAExtPaginator<>
                ("Goods g", "g", Goods.class, condition.getResaleFilter(resaler),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);
        return goodsPage;
    }

    /**
     * 根据分销商等级和商品ID计算分销商现价     *
     *
     * @return resalePrice 分销商现价
     */
    @Transient
    public BigDecimal getResalePrice() {
        BigDecimal resalePrice = originalPrice.add(this.resaleAddPrice == null ? BigDecimal.ZERO : this.resaleAddPrice);
        return resalePrice;
    }

    public BigDecimal getResalerPriceOfUhuila() {
        return getResalePrice();
    }

    /**
     * 判断分销商是否已经把商品加入分销库
     *
     * @return isExist true 已经存在，false 不存在
     */
    @Transient
    public boolean isExistLibrary(Resaler resaler) {
        boolean isExist = false;
        Query query = play.db.jpa.JPA.em().createQuery(
                "select r from ResalerFav r where r.resaler = :resaler and r.goods =:goods");
        query.setParameter("resaler", resaler);
        query.setParameter("goods", this);
        List<ResalerFav> favs = query.getResultList();
        if (favs.size() > 0) {
            isExist = true;
        }


        return isExist;
    }

    @Transient
    //@SolrField
    public Collection<Shop> getShopList() {
        if (isAllShop) {
            return CacheHelper.getCache(CacheHelper.getCacheKey(Shop.CACHEKEY_SUPPLIERID + this.supplierId, "GOODS_SHOP_LIST"), new CacheCallBack<List<Shop>>() {
                @Override
                public List<Shop> loadData() {
                    return Shop.findShopBySupplier(supplierId);
                }
            });
        }
        final long goodsId = this.id;

        return CacheHelper.getCache(CacheHelper.getCacheKey(Goods.CACHEKEY_BASEID + goodsId, "GOODS_SHOPS"), new CacheCallBack<Set<Shop>>() {
            @Override
            public Set<Shop> loadData() {
                Goods goods1 = Goods.findById(goodsId);
                Set<Shop> shopSet = new HashSet<>();
                for (Shop shop : goods1.shops) {
                    if (shop.deleted == DeletedStatus.UN_DELETED) {
                        shopSet.add(shop);
                    }
                }
                return shopSet;
            }
        });
    }

    @Transient
    public List<GoodsImages> getCachedGoodsImagesList() {
        final long goodsId = this.id;
        return CacheHelper.getCache(CacheHelper.getCacheKey(GoodsImages.CACHEKEY_GOODSID + goodsId, "GOODS_IMAGES"), new CacheCallBack<List<GoodsImages>>() {
            @Override
            public List<GoodsImages> loadData() {
                Goods goods1 = Goods.findById(goodsId);
                List<GoodsImages> imagesList = new ArrayList<>();
                for (GoodsImages images : goods1.goodsImagesList) {
                    imagesList.add(images);
                }
                return imagesList;
            }
        });
    }

    /**
     * 获取最近成交的n个商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTradeRecently(int limit) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select i.goods from OrderItems i where i.goods.status = :status " +
                "and i.goods.deleted = :deleted and i.goods.baseSale >= 1 and i.goods.expireAt > :expireAt " +
                "group by i.goods order by i.createdAt DESC");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("expireAt", new Date());
        q.setMaxResults(limit);
        return q.getResultList();
    }

    /**
     * 增加商品的推荐指数.
     *
     * @param goods
     * @param like
     */
    public static void addRecommend(Goods goods, boolean like) {
        if (goods == null) {
            return;
        }
        // 因为goods可能是从缓存中读取出来的，所以需要先从数据库中读取一次
        Goods updateGoods = Goods.findById(goods.id);
        int number = 1;
        if (like) {
            number = 100;
        }
        if (updateGoods.recommend == null) {
            updateGoods.recommend = 0;
        }
        updateGoods.recommend += number;

        // 这一操作不应当更新缓存
        updateGoods.skipUpdateCache = true;
        updateGoods.save();
    }

    /**
     * 获取推荐指数高的前n个商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopRecommend(int limit) {
        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.deleted =:deleted and g.expireAt >:expireAt and g.baseSale>=1 and g.isHideOnsale is false and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        return goodsList;

    }

    /**
     * 获取同一商户前n个推荐商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopRecommendByGoods(int limit, Goods goods) {
        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.supplierId=:supplierId and g.deleted =:deleted and " +
                " g.id <> :goodsId and g.expireAt >:expireAt and g.baseSale>=1 and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("supplierId", goods.supplierId);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);

        query.setParameter("goodsId", goods.id);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        List<Goods> otherGoodsList = new ArrayList<>();
        List<Goods> newGoodsList = new ArrayList<>();
        List<Goods> doGoodsList = new ArrayList<>();
        int goodsCount = goodsList.size();
        if (goodsCount < limit) {
            otherGoodsList = findTopRecommendByCategory(limit - goodsCount, goods);
            for (Goods goods1 : otherGoodsList) {
                goodsList.add(goods1);
            }
        }

        goodsCount = goodsList.size();
        if (goodsCount < limit) {
            newGoodsList = findNewGoodsOfOthers(goods.id, limit - goodsCount);
            for (Goods goods1 : newGoodsList) {
                goodsList.add(goods1);
            }
        }
        for (Goods goods2 : goodsList) {
            doGoodsList.add(goods2);
        }
        return doGoodsList;

    }

    /**
     * 获取同一种子分类的其他商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopRecommendByCategory(final int limit, final Goods goods) {
        final Long goodsId = goods.id;
        Long categoryId = CacheHelper.getCache(CacheHelper.getCacheKey(Category.CACHEKEY + goodsId, "GOODS_CATE"), new CacheCallBack<Long>() {
            @Override
            public Long loadData() {
                Goods g = Goods.findById(goodsId);
                Long id = 0l;
                for (Category ca : g.categories) {
                    id = ca.id;
                }
                return id;
            }
        });

        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.deleted =:deleted and " +
                " g.id in (select g.id from g.categories c where c.id = :categoryId or (c.parentCategory is not null and c.parentCategory.id=:categoryId))" +
                "and g.id <> :goodsId and g.supplierId <> :supplierId and g.expireAt >:expireAt and g.baseSale>=1 and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("categoryId", categoryId);
        query.setParameter("goodsId", goods.id);
        query.setParameter("supplierId", goods.supplierId);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        return goodsList;

    }

    /**
     * 获取最新商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findNewGoods(int limit) {
        // 找出5倍需要的商品，然后手工过滤
        List<Goods> allGoods = Goods.find("status = ? and deleted = ? and baseSale >= 1 and isHideOnsale=false and expireAt > ? order by createdAt DESC",
                GoodsStatus.ONSALE, DeletedStatus.UN_DELETED, new Date()).fetch(limit * 5);
        Set<Long> supplierSet = new HashSet<>();
        List<Goods> goods = new ArrayList<>();
        for (Goods g : allGoods) {
            if (!supplierSet.contains(g.supplierId)) {
                goods.add(g);
                supplierSet.add(g.supplierId);
            }
            if (goods.size() == limit) {
                break;
            }
        }
        return goods;
    }

    /**
     * 获取除本身以外的最新商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findNewGoodsOfOthers(Long id, int limit) {
        return Goods.find(" id <> ? and status = ? and deleted = ? and baseSale >= 1 and expireAt > ? order by createdAt DESC",
                id, GoodsStatus.ONSALE, DeletedStatus.UN_DELETED, new Date()).fetch(limit);
    }

    /**
     * 取得销量前3的商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findPopGoods(int limit) {
        return Goods.find(" status = ? and deleted = ? and baseSale >= 1 and expireAt > ? order by virtualBaseSaleCount DESC",
                GoodsStatus.ONSALE, DeletedStatus.UN_DELETED, new Date()).fetch(limit);
    }

    public void setPublishedPlatforms(List<GoodsPublishedPlatformType> publishedPlatforms) {
        if (unPublishedPlatforms == null) {
            unPublishedPlatforms = new HashSet<>();
        } else {
            if (unPublishedPlatforms.size() > 0) {
                unPublishedPlatforms.clear();
            }
        }

        if (publishedPlatforms == null || publishedPlatforms.size() == 0) {
            for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(this, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
            return;
        }

        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!publishedPlatforms.contains(type)) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(this, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
        }
    }

    public boolean canPublishTo(GoodsPublishedPlatformType type) {
        return !containsUnPublishedPlatform(type);
    }

    public boolean onSale() {
        return (GoodsStatus.ONSALE.equals(status) && expireAt.after(new Date()) &&
                baseSale > 0 && DeletedStatus.UN_DELETED.equals(deleted));
    }

    public Long summaryCount() {
        final Long goodsId = this.id;
        GoodsStatistics statistics = CacheHelper.getCache(CacheHelper.getCacheKey(GoodsStatistics.CACHEKEY_GOODSID + goodsId, "GOODSSTATS"), new CacheCallBack<GoodsStatistics>() {
            @Override
            public GoodsStatistics loadData() {
                return GoodsStatistics.find("goodsId", goodsId).first();
            }
        });
        if (statistics == null) {
            return 0l;
        }
        return statistics.summaryCount;
    }

    public Set<Category> getCategories() {
        if (this.id == null) {
            return this.categories;
        }
        Goods g = Goods.findById(this.id);
        return g.categories;
    }

    public String getSupplierName() {
        String supplierName;
        Supplier supplier = getSupplier();
        if (supplier == null)
            return "";
        else
            supplierName = supplier.otherName == null ? supplier.fullName : supplier.otherName;
        return supplierName;

    }


    public static List<models.sales.Goods> getTopGoods(final long categoryId, final String tuanCategory, final String tuanNane, int limit) {
        List<models.sales.Goods> allGoods;

        List<models.sales.Goods> goodsList;
        if (categoryId == 0) {
            allGoods = models.sales.Goods.findTop(limit * 5);
            goodsList = filterTopGoods(allGoods, tuanCategory, tuanNane, limit);

        } else {
            allGoods = models.sales.Goods.findTopByCategory(categoryId, limit * 5);
            goodsList = filterTopGoods(allGoods, tuanCategory, tuanNane, limit);
        }
        return goodsList;
    }

    public static List<models.sales.Goods> filterTopGoods(List<models.sales.Goods> allGoods, final String tuanCategory, final String tuanNane, int limit) {
        List<models.sales.Goods> goodsList = new ArrayList<>();
        List<Category> noMessage = new ArrayList<Category>();
        int i = 0;
        for (models.sales.Goods g : allGoods) {
            if (g.categories != null && g.categories.size() > 0
                    && g.categories.iterator() != null && g.categories.iterator().hasNext()) {

                Category category = g.categories.iterator().next();
                if (Messages.get(tuanCategory + "." + category.id).contains(tuanCategory)) {
                    noMessage.add(category);
                } else {
                    goodsList.add(g);
                }
                if (goodsList.size() == limit) {
                    break;
                }
            }
        }
        if (noMessage.size() > 0) {
            //发送提醒邮件
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient("dev@uhuila.com");
            mailMessage.setSubject(Play.mode.isProd() ? tuanNane + "收录分类" : tuanNane + "收录分类【测试】");
            mailMessage.putParam("tuanName", tuanNane);
            mailMessage.putParam("mailCategoryList", noMessage);
            MailUtil.sendTuanCategoryMail(mailMessage);
        }
        return goodsList;
    }

    public void initDiscount(BigDecimal value) {
        discount = value;
    }

    public void createHistory(String createdFrom) {
        models.sales.GoodsHistory goodsHistory = new GoodsHistory();

        if (goodsHistory.unPublishedPlatforms == null) {
            goodsHistory.unPublishedPlatforms = new HashSet<>();
        }

        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!this.unPublishedPlatforms.contains(type)) {
                final GoodsHistoryUnPublishedPlatform goodsHistoryUnPublishedPlatform = new GoodsHistoryUnPublishedPlatform(goodsHistory, type);
                goodsHistory.unPublishedPlatforms.add(goodsHistoryUnPublishedPlatform);
            }
        }


        goodsHistory.createdFrom = createdFrom;
        goodsHistory.goodsId = this.id;
        goodsHistory.name = this.name;
        goodsHistory.no = this.no;
        goodsHistory.effectiveAt = this.effectiveAt;
        goodsHistory.expireAt = this.expireAt;
        goodsHistory.faceValue = this.faceValue;
        goodsHistory.originalPrice = this.originalPrice;
        goodsHistory.discount = this.discount;
        goodsHistory.salePrice = this.salePrice;
        goodsHistory.baseSale = this.baseSale;
        goodsHistory.promoterPrice = this.promoterPrice;
        goodsHistory.invitedUserPrice = this.invitedUserPrice;
        goodsHistory.materialType = this.materialType;
        goodsHistory.topCategoryId = this.topCategoryId;
        goodsHistory.categories = new HashSet<>();
        goodsHistory.categories.addAll(this.categories);
        goodsHistory.resaleAddPrice = this.resaleAddPrice;
        goodsHistory.prompt = this.prompt;
        goodsHistory.details = this.details;
        goodsHistory.createdAt = new Date();
        goodsHistory.createdBy = this.createdBy;
        goodsHistory.brand = this.brand;
        goodsHistory.isAllShop = this.isAllShop;
        goodsHistory.status = this.status;
        goodsHistory.keywords = this.keywords;
        goodsHistory.limitNumber = this.limitNumber;
        goodsHistory.couponType = this.couponType;
        goodsHistory.imagePath = this.imagePath;
        goodsHistory.supplierId = this.supplierId;
        if (this.shops != null) {
            goodsHistory.shops = new HashSet<>();
            goodsHistory.shops.addAll(this.shops);
        } else {
            goodsHistory.shops = null;
        }
        goodsHistory.title = this.title;
        goodsHistory.useBeginTime = this.useBeginTime;
        goodsHistory.useEndTime = this.useEndTime;
        goodsHistory.useWeekDay = this.useWeekDay;
        goodsHistory.isLottery = this.isLottery;
        goodsHistory.groupCode = this.groupCode;
        goodsHistory.save();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Goods goods = (Goods) o;

        if (id != goods.id) return false;
        if (name != null ? !name.equals(goods.name) : goods.name != null) return false;
        if (title != null ? !title.equals(goods.title) : goods.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    //------------------------------------------- 使用solr服务进行搜索的方法 (Begin) --------------------------------------
    private static final String SOLR_ID = "id";
    private static final String SOLR_GOODS_NAME = "goods.name_s";
    private static final String SOLR_GOODS_SALEPRICE = "goods.salePrice_c";
    private static final String SOLR_GOODS_FACEVALUE = "goods.faceValue_c";
    private static final String SOLR_GOODS_VIRTUALSALECOUNT = "goods.virtualSaleCount_l";
    private static final String SOLR_GOODS_AREAS = "goods.areaNames_s";
    private static final String SOLR_GOODS_IMAGESMALLPATH = "goods.imageSmallPath_s";

    /**
     * 搜索
     * 按关键词直接搜索时调用
     *
     * @param keywords   查询条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return
     */
    public static QueryResponse search(String keywords, long brandId, int pageNumber, int pageSize) {
        GoodsWebsiteCondition condition = new GoodsWebsiteCondition();
        condition.keywords = keywords;
        condition.solrOrderBy = "goods.firstOnSaleAt_d";
        condition.brandId = brandId;
        return searchFullText(condition, pageNumber, pageSize);

    }

    /**
     * 搜索.
     * 按关键词搜索时按指定字段排序时调用
     *
     * @param keywords   查询条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return
     */
    public static QueryResponse search(String keywords, String orderBy, boolean isAsc, int pageNumber, int pageSize) {
        GoodsWebsiteCondition condition = new GoodsWebsiteCondition();
        condition.keywords = keywords;
        condition.solrOrderBy = orderBy;
        condition.orderByType = isAsc ? "asc" : "desc";
        return searchFullText(condition, pageNumber, pageSize);
    }

    /**
     * 前端按条件的全文搜索.
     *
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static QueryResponse searchFullText(GoodsWebsiteCondition condition, int pageNumber, int pageSize) {
        String q = StringUtils.isNotBlank(condition.keywords) ? "text:\"" + condition.keywords + "\"" : null;
        return search(q, condition.parentCategoryId, condition.categoryId, condition.districtId, condition.areaId,
                condition.isOrder, condition.materialType, condition.brandId,
                condition.solrOrderBy, "asc".equals(condition.orderByType), pageNumber, pageSize);
    }

    /**
     * 前端按条件搜索.
     *
     * @param q                Solr的q查询字符串, 可以根据solr的查询语法自行组装查询字符串.
     * @param parentCategoryId
     * @param categoryId
     * @param districtId
     * @param areaId
     * @param orderBy
     * @param isAsc
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static QueryResponse search(String q, long parentCategoryId, long categoryId, String districtId,
                                       String areaId, Boolean isOrder, MaterialType materialType, long brandId,
                                       String orderBy, boolean isAsc, int pageNumber, int pageSize) {
        StringBuilder queryStr = new StringBuilder("goods.deleted_s:\"com.uhuila.common.constants.DeletedStatus:UN_DELETED\" AND goods.expired_b:false");
        if (StringUtils.isNotBlank(q)) {
            queryStr.append(" AND " + q);
        }
        if (categoryId > 0) {
            queryStr.append(" AND goods.categoryIds_s:" + categoryId);
        }
        if (StringUtils.isNotBlank(districtId) && !districtId.equals("0")) {
            queryStr.append(" AND shop.districtId_s:" + districtId);
        }
        if (parentCategoryId > 0) {
            queryStr.append(" AND goods.parentCategoryIds_s:" + parentCategoryId);
        }
        if (StringUtils.isNotBlank(areaId) && !areaId.equals("0")) {
            queryStr.append(" AND shop.areaId_s:" + areaId);
        }
        if (isOrder != null) {
            queryStr.append(" AND goods.isOrder_b:" + isOrder);
        }
        if (materialType != null) {
            queryStr.append(" AND goods.materialType_s:\"models.sales.MaterialType:" + materialType.name() + "\"");
        }
        if (brandId > 0) {
            queryStr.append(" AND brand.id_l:" + brandId);
        }
        System.out.println("==> queryStr:" + queryStr);

        SolrQuery query = new SolrQuery(queryStr.toString());
        query.setFields(SOLR_ID, SOLR_GOODS_NAME, SOLR_GOODS_SALEPRICE, SOLR_GOODS_FACEVALUE, SOLR_GOODS_VIRTUALSALECOUNT, SOLR_GOODS_AREAS, SOLR_GOODS_IMAGESMALLPATH);
        query.setSortField(orderBy, isAsc ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        query.setFacet(true).addFacetField("goods.categoryIds_s", "goods.parentCategoryIds_s", "shop.districtId_s", "shop.areaId_s");
        query.setHighlight(true);
        query.setStart(pageNumber * pageSize - pageSize);
        query.setRows(pageSize);
        return Solr.query(query);
    }

    /**
     * 获取最热卖商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopHotSale(int limit) {
        QueryResponse response = search(null, "goods.virtualSaleCount_l", false, 1, limit);
        return getResultList(response);
    }

    public static List<Goods> getResultList(QueryResponse response) {
        List<Goods> goodsList = new ArrayList<>();
        if (response == null) {
            return goodsList;
        }
        SolrDocumentList documentList = response.getResults();

        for (SolrDocument doc : documentList) {
            Goods goods = new Goods();
            final String docId = (String) doc.getFieldValue("id");
            goods.id = Long.parseLong(docId.substring(6, docId.length()));
            goods.name = (String) doc.getFieldValue(SOLR_GOODS_NAME);
            final String faceValue = (String) doc.getFieldValue(SOLR_GOODS_FACEVALUE);
            if (faceValue != null) {
                goods.faceValue = new BigDecimal(faceValue.substring(0, faceValue.length() - 4));
            }
            final String salePrice = (String) doc.getFieldValue(SOLR_GOODS_SALEPRICE);
            goods.salePrice = new BigDecimal(salePrice.substring(0, salePrice.length() - 4));
            goods.areaNames = (String) doc.getFieldValue(SOLR_GOODS_AREAS);
            goods.imageSmallPath = (String) doc.getFieldValue(SOLR_GOODS_IMAGESMALLPATH);
            goodsList.add(goods);
        }
        return goodsList;
    }

    public static SimplePaginator<Goods> getResultPage(QueryResponse response, int pageNumber, int pageSize) {
        final List<Goods> goodsList = getResultList(response);

        SimplePaginator<Goods> page = new SimplePaginator<>(goodsList);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setRowCount((int) response.getResults().getNumFound());
        return page;
    }

    /**
     * 从solr的返回结果中获取顶层分类统计.
     *
     * @param response
     * @return
     */
    public static List<Category> getStatisticTopCategories(QueryResponse response) {
        return getStatisticCategories(response, null);
    }

    /**
     * 获取子分类统计.
     *
     * @param response
     * @param parentCategoryId
     * @return
     */
    public static List<Category> getStatisticSubCategories(QueryResponse response, Long parentCategoryId) {
        if (parentCategoryId == null) {
            return new ArrayList<>();
        }
        return getStatisticCategories(response, parentCategoryId);
    }

    /**
     * 从solr的返回结果中获取分类统计的结果.
     *
     * @param response
     * @return
     */
    private static List<Category> getStatisticCategories(QueryResponse response, Long parentCategoryId) {
        List<Category> categoryList = new ArrayList<>();
        if (response == null) {
            return categoryList;
        }
        FacetField facetField = parentCategoryId == null ? response.getFacetField("goods.parentCategoryIds_s") : response.getFacetField("goods.categoryIds_s");
        if (facetField != null) {
            List<FacetField.Count> countList = facetField.getValues();

            for (FacetField.Count count : countList) {
                if (count.getCount() > 0) {
                    Category category = Category.findById(Long.parseLong((StringUtils.isBlank(count.getName())) ? "0" : count.getName()));

                    if ((category != null && parentCategoryId != null && category.parentCategory != null && category.parentCategory.id.equals(parentCategoryId)) ||
                            (category != null && parentCategoryId == null)) {
                        category.goodsCount = count.getCount();
                        categoryList.add(category);
                    }
                }
            }
        }
        return categoryList;
    }

    /**
     * 从solr的返回结果中获取区域的统计.
     *
     * @param response
     * @return
     */
    public static List<Area> getStatisticDistricts(QueryResponse response) {
        return getStatisticAreas(response, null);
    }

    /**
     * 从solr的返回结果中获取商圈的统计结果.
     *
     * @param response
     * @return
     */
    public static List<Area> getStatisticAreas(QueryResponse response, String districtId) {
        List<Area> areaList = new ArrayList<>();
        if (response == null) {
            return areaList;
        }
        FacetField facetField = districtId == null ? response.getFacetField("shop.districtId_s") : response.getFacetField("shop.areaId_s");
        if (facetField != null) {

            List<FacetField.Count> countList = facetField.getValues();

            for (FacetField.Count count : countList) {
                if (count.getCount() > 0) {
                    Area area = Area.findAreaById(count.getName());
                    if ((area != null && districtId != null && area.parent != null && area.parent.id.equals(districtId))
                            || (area != null && districtId == null)) {
                        area.goodsCount = count.getCount();
                        areaList.add(area);
                        System.out.println(area.name + ":          " + area.goodsCount);

                    }
                }
            }
        }

        return areaList;
    }

    public void setVirtualSaleCount(Long count) {
        this.virtualSaleCount = count;
    }
    //------------------------------------------- 使用solr服务进行搜索的方法 (End) ----------------------------------------
}
