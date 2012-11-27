package functional;

import com.google.gson.Gson;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.wuba.WubaUtil;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class WubaGroupBuyTest extends FunctionalTest{
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(GoodsDeployRelation.class);
        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = "wuba";
            }
        });
        AccountUtil.getCreditableAccount(resaler.getId(), AccountType.RESALER);
    }

    @Test
    public void testNewOrder() {
        GoodsDeployRelation goodsDeployRelation = FactoryBoy.last(GoodsDeployRelation.class);


        Map<String, Object> params = new HashMap<>();
        params.put("orderId", System.currentTimeMillis());
        params.put("groupbuyIdThirdpart",goodsDeployRelation.linkId);
        params.put("mobile", "13472581853");
        params.put("prodCount", 1);
        params.put("prodPrice", 20);

        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("param", WubaUtil.encryptMessage(new Gson().toJson(params)));
        Http.Response response = POST("/api/v1/58/gb/order-add", requestParam);
        assertIsOk(response);
        assertEquals(1, Order.count());
        assertEquals(1, ECoupon.count());
    }
}