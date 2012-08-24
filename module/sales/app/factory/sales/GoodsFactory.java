package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Goods;
import models.sales.MaterialType;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;

public class GoodsFactory extends ModelFactory<Goods> {

    @Override
    public Goods define() {
        Goods goods = new Goods();
        Supplier supplier = FactoryBoy.create(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue= BigDecimal.TEN;
        goods.materialType= MaterialType.REAL;
        return goods;
    }

    @Factory(name = "SupplierId")
    public Goods defineWithSupplierId(Goods goods){
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.materialType= MaterialType.REAL;

        return goods;

    }
}
