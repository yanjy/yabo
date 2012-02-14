package models.order;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import models.sales.Goods;
import models.consumer.*;

@Entity
@Table(name = "cart")
public class Cart extends Model {
    @ManyToOne
    public User user;    

    @ManyToOne
    public Goods goods;

    public int number;
    @Column(name="material_type")
    public String materialType;

    @Column(name="cookie_identity")
    public String cookieIdentity;

    @Column(name="lock_version")
    public int lockVersion;

    @Column(name="created_at")
    public Date createdAt;

    @Column(name="updated_at")
    public Date updatedAt;

    public Cart(User user, String cookieIdentity, Goods goods, int number,String materialType) {
        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods= goods;
        this.number= number;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
        this.materialType = materialType;
    }

    public static List<Cart> findECart() {
        return Cart.find("byMaterialType","e").fetch();
    }

    public static List<Cart> findRCart() {
        return Cart.find("byMaterialType","r").fetch();
    }
}
