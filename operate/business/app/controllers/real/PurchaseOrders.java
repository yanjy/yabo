package controllers.real;

import com.uhuila.common.constants.DeletedStatus;
import controllers.OperateRbac;
import models.order.PurchaseItem;
import models.order.PurchaseOrder;
import models.order.Vendor;
import models.sales.Sku;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购合同管理
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午2:53
 */
@With(OperateRbac.class)
@ActiveNavigation("purchase_orders_index")
public class PurchaseOrders extends Controller {

    @ActiveNavigation("purchase_orders_index")
    public static void index(String keyword) {
        int page = getPage();
        List<PurchaseOrder> purchaseOrderList = PurchaseOrder.findByCondition(keyword);
        render(purchaseOrderList, page, keyword);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    @ActiveNavigation("purchase_orders_add")
    public static void add() {
        List<Vendor> vendorList = Vendor.findUnDeleted();
        System.out.println(vendorList.size() + "===vendorList.size()>>");
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.deleted = DeletedStatus.DELETED;
        purchaseOrder.create();
        Long purchaseOrderId = purchaseOrder.id;
        System.out.println(purchaseOrderId + "===purchaseOrderId>>");
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder=? and deleted = ? ", purchaseOrder, DeletedStatus.UN_DELETED).fetch();
        render(vendorList, skuList, purchaseOrderId, purchaseItemList);
    }

    @ActiveNavigation("vendors_add")
    public static void create(@Valid PurchaseOrder purchaseOrder, Long purchaseOrderId) {
        System.out.println("===>>");
        if (Validation.hasErrors()) {
            System.out.println(Validation.errors() + "===Validation.errors()>>");
            List<PurchaseItem> purchaseItemList =
                    PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
            List<Sku> skuList = Sku.findShiHuiUnDeleted();
            List<Vendor> vendorList = Vendor.findUnDeleted();

            render("real/PurchaseOrders/add.html", purchaseItemList, skuList, vendorList, purchaseOrder, purchaseOrderId);
        }
        PurchaseOrder existedPurchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        existedPurchaseOrder.vendor = purchaseOrder.vendor;
        existedPurchaseOrder.invoiceType = purchaseOrder.invoiceType;
        existedPurchaseOrder.paymentType = purchaseOrder.paymentType;
        existedPurchaseOrder.signedAt = purchaseOrder.signedAt;
        existedPurchaseOrder.createdBy = OperateRbac.currentUser().userName;
        existedPurchaseOrder.deleted = DeletedStatus.UN_DELETED;
        existedPurchaseOrder.save();
        index(null);
    }

    public static void edit(Long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        List<Vendor> vendorList = Vendor.findUnDeleted();
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ", id, DeletedStatus.UN_DELETED).fetch();
        render(purchaseOrder, vendorList, purchaseItemList);
    }


    public static void updateItem(Long purchaseOrderId, @Valid PurchaseItem item, PurchaseOrder purchaseOrder) {
        System.out.println(purchaseOrder.vendor.id + "===purchaseOrder.vendor.id>>");
        if (Validation.hasErrors()) {
            System.out.println(Validation.errors() + "===Validation.errors()>>");
            List<PurchaseItem> purchaseItemList =
                    PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
            List<Sku> skuList = Sku.findShiHuiUnDeleted();
            List<Vendor> vendorList = Vendor.findUnDeleted();
            render("real/PurchaseOrders/add.html", item, purchaseOrderId, purchaseItemList, skuList, vendorList);
        }
        PurchaseOrder currentPurchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        System.out.println(purchaseOrderId + "===purchaseOrderId>>");
        System.out.println(item + "===item>>");
        System.out.println(item.count + "===update....item.count>>");
        item.sku.save();
        item.purchaseOrder = currentPurchaseOrder;
        System.out.println(item.sku.id + "===item.sku.id>>");

        item.deleted = DeletedStatus.UN_DELETED;
        item.save();
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        for (PurchaseItem item1 : purchaseItemList) {
            System.out.println(item1.count + "===item1.count>>");
        }
        List<Vendor> vendorList = Vendor.findUnDeleted();
        render("real/PurchaseOrders/add.html", purchaseItemList, skuList, vendorList, purchaseOrderId);
    }

    public static void deletePurchaseItem(Long itemId, Long purchaseOrderId) {
        System.out.println(itemId + "===itemId>>");
        System.out.println(purchaseOrderId + "===purchaseOrderId>>");
        PurchaseItem item = PurchaseItem.findById(itemId);
        if (item != null) {
            item.deleted = DeletedStatus.DELETED;
            item.save();
        }
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ?", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
        System.out.println(purchaseItemList.size() + "===purchaseItemList.size()>>");
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        List<Vendor> vendorList = Vendor.findUnDeleted();
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        render("real/PurchaseOrders/add.html", purchaseItemList, purchaseOrderId, skuList, vendorList, purchaseOrder);
    }
}
