package controllers;

import models.supplier.*;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * 商户合同
 * <p/>
 * User: wangjia
 * Date: 13-1-23
 * Time: 下午6:31
 */

@With(OperateRbac.class)
@ActiveNavigation("suppliers_contracts")
public class SuppliersContracts extends Controller {
    public static int PAGE_SIZE = 15;
    public static String ROOT_PATH = Play.configuration.getProperty("upload.contractpath", "");
    //    public static final String BASE_URL = Play.configuration.getProperty("uri.operate_business");
    public static String FILE_TYPES = Play.configuration.getProperty("newsImg.fileTypes", "");
    private static final String IMAGE_ROOT_GENERATED = play.Play.configuration.getProperty("image.root.generated", "/nfs/images/contract/p"); //缩略图根目录


    public static void index(SupplierContractCondition condition) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        Boolean hasViewContractPermission = ContextedPermission.hasPermission("VIEW_SUPPLIER_CONTRACT");

        int pageNumber = getPage();

        if (condition == null) {
            condition = new SupplierContractCondition();
        }
        Boolean hasManagerViewContractPermission = ContextedPermission.hasPermission("MANAGER_VIEW_SUPPLIER_CONTRACT");

        condition.hasManagerViewContractPermission = hasManagerViewContractPermission;
        condition.hasContractManagementPermission = hasContractManagementPermission;
        condition.operatorId = OperateRbac.currentUser().id;

        JPAExtPaginator<SupplierContract> contractPage = SupplierContract.findByCondition(condition, pageNumber,
                PAGE_SIZE);

        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(supplierList, contractPage, hasContractManagementPermission, hasViewContractPermission);
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void add(SupplierContract contract, SupplierContractCondition condition) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            render(supplierList, contract, condition);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void edit(Long contractId) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            SupplierContract contract = SupplierContract.findById(contractId);
            Supplier supplier = Supplier.findById(contract.supplierId);
            String supplierName = supplier.otherName;
            render(contractId, supplier, contract, supplierName);

        } else {
            index(null);
        }
    }

    public static void view(Long contractId) {
        Boolean hasViewContractPermission = ContextedPermission.hasPermission("VIEW_SUPPLIER_CONTRACT");
        if (hasViewContractPermission) {
            SupplierContract contract = SupplierContract.findById(contractId);
            Collections.sort(contract.supplierContractImagesList);

            File targetParent = new File(joinPath(IMAGE_ROOT_GENERATED, contract.supplierId.toString(), contractId.toString()));
            //检查目标目录
            if (!targetParent.exists()) {
                if (!targetParent.mkdirs()) {
                    Logger.error("can not mkdir on %s before viewing contract", targetParent.getPath());
                    error("can not mkdir on  " + targetParent.getPath() + "[before viewing contract]");
                } else {
                    Logger.info("madir %s success before viewing contract!", targetParent.getPath());
                }
            }

            render(contract);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void update(Long contractId, @Valid SupplierContract contract) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            checkExpireAt(contract);
            if (StringUtils.isBlank(contract.description)) {
                Validation.addError("contract.description", "validation.required");
            }
            SupplierContract currentContract = SupplierContract.findById(contractId);
            Supplier supplier = Supplier.findById(currentContract.supplierId);
            String supplierName = supplier.otherName;
            if (Validation.hasErrors()) {
                render("SuppliersContracts/edit.html", contractId, supplier, contract, supplierName);
            }
            contract.updatedBy = OperateRbac.currentUser().loginName;
            SupplierContract.update(contractId, contract);
            uploadContract(currentContract.supplierId, contractId);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void terminate(Long contractId) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            SupplierContract contract = SupplierContract.findById(contractId);
            contract.status = ContractStatus.TERMINATE;
            contract.save();
        }
        index(null);
    }


    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void updateDescription(Long imageId, String description) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            SupplierContractImage image = SupplierContractImage.findById(imageId);
            if (image != null) {
                image.description = description;
                image.save();
//                renderJSON("1");
//                uploadContract(image.contract.supplierId, image.contract.id);
            } else {
//                renderJSON("0");
                index(null);
            }
        } else {
            index(null);
        }
    }


    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void create(@Valid SupplierContract contract, SupplierContractCondition condition) {
        if (condition.supplierId != 0 && contract.supplierId == 0) {
            contract.supplierId = condition.supplierId;
        }
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            checkExpireAt(contract);
            checkSupplier(contract);
            if (contract.supplierId == 0 || contract.supplierId == null) {
                Validation.addError("contract.supplierId", "validation.selectExisted");
            }
            if (StringUtils.isBlank(contract.description)) {
                Validation.addError("contract.description", "validation.required");
            }
            List<Supplier> supplierList = Supplier.findUnDeleted();
            if (Validation.hasErrors()) {
                System.out.println(contract.supplierId + "===contract.supplierId>>");
                render("SuppliersContracts/add.html", supplierList, contract, condition);
            }
            Supplier supplier = Supplier.findById(contract.supplierId);
            SupplierContract newContract = new SupplierContract(supplier);
            newContract.createdBy = OperateRbac.currentUser().loginName;
            newContract.effectiveAt = contract.effectiveAt;
            newContract.expireAt = contract.expireAt;
            newContract.description = contract.description;
            newContract.create();
            newContract.save();
            uploadContract(newContract.supplierId, newContract.id);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void uploadContract(Long supplierId, Long contractId) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            SupplierContract contract = SupplierContract.findById(contractId);
            render(supplierId, contractId, contract);
        } else {
            index(null);
        }
    }

    private static void checkExpireAt(SupplierContract contract) {
        if (contract.effectiveAt == null) {
            Validation.addError("contract.effectiveAt", "validation.required");
        }
        if (contract.expireAt == null) {
            Validation.addError("contract.expireAt", "validation.required");
        }
        if (contract.effectiveAt != null && contract.expireAt != null && contract.expireAt.before(contract.effectiveAt)) {
            Validation.addError("contract.expireAt", "validation.beforeThanContractEffectiveAt");
        }
    }

    private static void checkSupplier(SupplierContract contract) {
        if (contract.supplierId == 0) {
            Validation.addError("contract.supplierId", "validation.selected");
            return;
        }
        Supplier supplier = Supplier.findById(contract.supplierId);
        if (supplier == null) {
            Validation.addError("contract.supplierId", "validation.notExisted");
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void delete(long id) {
        SupplierContract.delete(id);
        index(null);
    }

    /**
     * 删除商户合同一个图片
     *
     * @param id
     */
    public static void deleteImage(Long id) {
        SupplierContractImage images = SupplierContractImage.findById(id);
        images.delete();
        renderJSON("");
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    private static String joinPath(String... nodes) {
        StringBuilder path = new StringBuilder();
        for (String node : nodes) {
            path.append(node).append(File.separator);
        }
        if (nodes.length > 0) {
            return path.substring(0, path.length() - File.separator.length());
        } else {
            return "";
        }
    }
}
