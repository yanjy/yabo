package models.ktv;

import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * @author likang
 *         Date: 13-4-27
 */
@Entity
@Table(name = "ktv_shop_price_schedule")
public class KtvShopPriceSchedule extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    public Shop shop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id")
    public KtvPriceSchedule schedule;

    public int roomCount;
}
