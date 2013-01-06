package models.sales;

import models.order.OuterOrderPartner;
import play.db.jpa.Model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p/>
 * 向第三方推送商品，保留商品更改的记录
 * User: yanjy
 * Date: 12-11-21
 * Time: 下午4:38
 */
@Entity
@Table(name = "goods_third_support")
public class GoodsThirdSupport extends Model {
    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Lob
    @Column(name = "goods_data")
    public String goodsData;          //此商品的完整信息

    @Lob
    @Column(name = "goods_supplier_info")
    public String goodsSupplierInfo;          //此对应商品门店变化的时候，更新这个字段

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    @Column(name = "created_at")
    public Date createdAt;

    public static GoodsThirdSupport generate(Goods goods, String data,OuterOrderPartner partner) {
        GoodsThirdSupport support = new GoodsThirdSupport();
        support.partner = partner;
        support.goodsData = data;
        support.goods = goods;
        support.createdAt = new Date();
        return support;
    }

    public static GoodsThirdSupport getSupportGoods(Goods goods, OuterOrderPartner partner) {
        GoodsThirdSupport support = GoodsThirdSupport.find("partner=? and goods=? order by id desc", partner, goods).first();
        return support;
    }
}
