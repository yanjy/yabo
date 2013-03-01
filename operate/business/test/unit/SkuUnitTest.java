package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.sales.Brand;
import models.sales.Sku;
import models.sales.SkuCondition;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-27
 * Time: 下午5:37
 */
public class SkuUnitTest extends UnitTest {
    Sku sku;

    @Before
    public void setUp() {
        FactoryBoy.delete(Sku.class);
        sku = FactoryBoy.create(Sku.class);
    }

    @Test
    public void test_Create() {
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplier.code = "010001";
        supplier.sequenceCode = "0001";
        supplier.supplierCategory = sku.supplierCategory;
        supplier.save();
        sku1.supplierId = supplier.id;
        sku1.brand = FactoryBoy.lastOrCreate(Brand.class);
        sku1.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);
    }

    @Test
    public void test_Create_CodeIs999() {
        sku.sequenceCode = "999";
        sku.code = "S010001999";
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplier.code = "010001";
        supplier.sequenceCode = "0001";
        supplier.supplierCategory = sku.supplierCategory;
        supplier.save();
        sku1.supplierId = supplier.id;
        sku1.brand = FactoryBoy.lastOrCreate(Brand.class);
        sku1.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);
    }

    @Test
    public void test_Create_CodeIs99() {
        sku.sequenceCode = "99";
        sku.code = "S01000199";
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplier.code = "010001";
        supplier.sequenceCode = "0001";
        supplier.supplierCategory = sku.supplierCategory;
        supplier.save();
        sku1.supplierId = supplier.id;
        sku1.brand = FactoryBoy.lastOrCreate(Brand.class);
        sku1.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);

    }

    @Test
    public void testUpdate() {
        assertEquals("S01000101", sku.code);
        sku.code = "002";
        Sku.update(sku.id, sku);
        sku.refresh();
        assertEquals("002", sku.code);

    }

    @Test
    public void testDelete() {
        assertEquals(DeletedStatus.UN_DELETED, sku.deleted);
        Sku.delete(sku.id);
        assertEquals(DeletedStatus.DELETED, sku.deleted);

        SkuCondition condition = new SkuCondition();
        List<Sku> skuList = Sku.findByCondition(condition, 1, 15);
        assertEquals(0, skuList.size());
    }

    @Test
    public  void testIndex() {
        SkuCondition condition = new SkuCondition();
        List<Sku> skuList = Sku.findByCondition(condition, 1, 15);
        assertEquals(1, skuList.size());
    }


}