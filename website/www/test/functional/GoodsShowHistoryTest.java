package functional;

import factory.callback.BuildCallback;
import models.consumer.Address;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.GoodsHistory;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-9
 * Time: 上午9:48
 * To change this template use File | Settings | File Templates.
 */
public class GoodsShowHistoryTest extends FunctionalTest {
    Goods goods;
    OrderItems orderItems;
    GoodsHistory goodsHistory;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods goods) {
                goods.updatedAt = new Date();
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems orderItems) {
                orderItems.createdAt = new Date();
            }
        });
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
    }


    @Test
    public void testGoodsShowHistory() {
        Http.Response response = GET("/p/" + goodsHistory.goodsId + "/h/" + goodsHistory.id + "/orderItem/" + orderItems.id);
        assertIsOk(response);
        GoodsHistory getGoodsHistory = (GoodsHistory) renderArgs("goods");
        assertEquals(goodsHistory.name, getGoodsHistory.name);

    }
}
