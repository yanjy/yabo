package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.WithdrawBill;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-31
 * Time: 上午9:38
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("crm_app")
public class OperateCRM extends Controller {


    public static void index(String phone, CRMCondition condition, Long userId, Long consultId, String consultStatus) {

        if (condition != null)
            if (condition.userId != null)
                userId = condition.userId;

        int times = 0;

        User user = null;
        Address address = null;
        String loginName = null;
        ConsultRecord consult = null;

        MemberCallBind bind = new MemberCallBind();

        List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();

        if (userList == null || userList.size() <= 0) {
            userList = User.find("mobile=?", phone).fetch();


            for (int i = 0; i < userList.size(); i++) {
                bind.userId = userList.get(i).id;
                bind.phone = phone;
                bind.loginName = userList.get(i).loginName;
                bind.save();
            }

        } else {

        }


        String moreSearch = "";
        List<ConsultRecord> consultContent = null;

        consultContent = ConsultRecord.find("deleted=? and phone=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();
        if (StringUtils.isNotBlank(consultStatus)) {
            if (consultStatus.equals("finish"))
                consultContent = ConsultRecord.find("deleted=? and phone=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();
            if (consultStatus.equals("tempSave")) {
                consultContent = null;
                consult = ConsultRecord.find("id=?", consultId).first();
            }
        }
        String currentOperator = OperateRbac.currentUser().loginName;

        if (condition == null) {
            condition = new CRMCondition();
            if (userList.size() > 0) {
                user = userList.get(0);
                address = ConsultCondition.findAddressByCondition(user);
                condition.userId = userList.get(0).getId();
            }

        } else {


            if (userId != null)
                user = User.find("id=?", userId).first();
            if (user == null) {
                if (condition.userId != null)
                    user = User.find("id=?", condition.userId).first();
                else if (userList.size() > 0)
                    user = userList.get(0);
            }
            address = ConsultCondition.findAddressByCondition(user);
        }


        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        else if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;
        List<User> searchUserList = null;
        HashMap<Long, Address> addressMap = null;

        if (condition.searchUser != null || condition.searchOrderCoupon != null) {
            searchUserList = ConsultCondition.findSearchUserByCondition(condition);
            addressMap = new HashMap<>();
            for (User u : searchUserList) {
                addressMap.put(u.id, ConsultCondition.findAddressByCondition(u));
            }
        }

        if (condition.searchUser == null)
            if (phone != null) {
                condition.searchUser = phone;

            }


        List<Order> orderList = ConsultCondition.findOrderByCondition(condition);

        List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);

        List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

        if (userId != null) {
            user = User.find("id=?", userId).first();
            loginName = user.loginName;
        }
        long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
        long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
        long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);


        times++;
        if (condition.searchUser == null && user != null) {
            userId = user.id;
        }


        List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();

        if (userId != null)
            condition.userId = userId;

        render(couponCallBindList, addressMap, searchUserList, userId, address, user, userList, orderList, condition, eCoupons, consultContent, phone,
                currentOperator, moreSearch, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize, consultId, consult);
    }


    public static void tempSave(CRMCondition condition, Long consultId, ConsultRecord consult, User user, String phone, Long userId, String consultStatus) {
        System.out.println("condition" + condition.searchUser);
        String tempPhone = consult.phone;
        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;
        if (consultId != null)
            consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.text = tempText;
            consult.phone = tempPhone;
            consult.consultType = tempConsultType;
        }
//        CRMCondition condition = new CRMCondition();
        if (consult != null)
            if (StringUtils.isBlank(consult.text))
                Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            String currentOperator = OperateRbac.currentUser().loginName;

            List<ConsultRecord> consultContent = ConsultRecord.find("deleted=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

            if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {
                if (user != null) {
                    user = User.find("id=?", user.id).first();
                    Address address = ConsultCondition.findAddressByCondition(user);
                }

                List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
                List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
                List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

                long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
                long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
                long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

                if (userId != null)
                    user = User.find("id=?", userId).first();

                List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
                List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();


                render("OperateCRM/index.html", withdrawBillSize, orderListSize, eCouponsSize, withdrawBill, orderList, couponCallBindList, eCoupons, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);


            }

            if (userId != null)
                user = User.find("id=?", userId).first();

            List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
            consultStatus = "tempSave";
            if (userId != null)
                condition.userId = userId;
            List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
            List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
            List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

            long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
            long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
            long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

            consultContent = null;
            List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
            render("OperateCRM/index.html", couponCallBindList, userId, orderList, eCoupons, withdrawBill, orderListSize, eCouponsSize, withdrawBillSize, consultStatus, consult, consultContent, currentOperator, phone, condition, consultId, userList, user);
        }
        if (consult != null) {
            consult.deleted = DeletedStatus.UN_DELETED;
            consult.createdBy = OperateRbac.currentUser().loginName;
            consult.userType = models.accounts.AccountType.CONSUMER;
            if (user != null)
                if (user.id != null)
                    consult.userId = user.id;
            consult.phone = phone;
            if (user != null)
                consult.loginName = user.loginName;

            consult.create();
            consult.save();
        }
        consultStatus = "tempSave";

        index(phone, null, userId, consultId, consultStatus);

    }

    public static void save(Long consultId, ConsultRecord consult, User user, String phone, Long userId, String consultStatus) {

        System.out.println("save userId" + userId);

        if (userId != null)
            user = User.findById(userId);

        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;

        consult = ConsultRecord.findById(consultId);
        consult.text = tempText;

        consult.consultType = tempConsultType;
        CRMCondition condition = new CRMCondition();

        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {


            String currentOperator = OperateRbac.currentUser().loginName;

            List<ConsultRecord> consultContent = ConsultRecord.find("deleted=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

            if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {

                user = User.find("id=?", user.id).first();
                Address address = ConsultCondition.findAddressByCondition(user);
                List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
                List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
                List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

                long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
                long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
                long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);
                if (userId != null)
                    user = User.find("id=?", userId).first();

                List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();


                List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
                render("OperateCRM/index.html", couponCallBindList, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);
            }

            if (userId != null)
                user = User.find("id=?", userId).first();

            List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
            if (userId != null)
                condition.userId = userId;
            List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
            List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
            List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

            long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
            long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
            long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

            List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
            render("OperateCRM/index.html", couponCallBindList, consult, userId, orderList, eCoupons, withdrawBill, orderListSize, eCouponsSize, withdrawBillSize, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);


        }

        consult.deleted = DeletedStatus.UN_DELETED;
        consult.createdBy = OperateRbac.currentUser().loginName;
        consult.userType = models.accounts.AccountType.CONSUMER;
        if (user != null)
            if (user.id != null)
                consult.userId = user.id;
        consult.phone = phone;
        if (user != null)
            consult.loginName = user.loginName;

        consult.create();
        consult.save();
        consultStatus = "finish";


        getPhone();

    }


    /**
     * 删除指定咨询
     *
     * @param id 商品ID
     */
    public static void delete(Long id, String phone) {

        ConsultRecord consult = ConsultRecord.findById(id);


        ConsultRecord.delete(id);

        index(phone, null, null, null, null);
    }

    public static void edit(Long id) {

        ConsultRecord consult = ConsultRecord.findById(id);
        render(consult, id);
    }


    public static void update(Long id, @Valid ConsultRecord consult, String phone) {


        ConsultRecord oldConsult = ConsultRecord.findById(id);


        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            render("OperateCRM/edit.html", consult, id);
        }
        ConsultRecord.update(id, consult);

        index(phone, null, null, null, null);
    }

    public static void getPhone() {
        render();
    }


    public static void jumpIndex(String phone, CRMCondition condition, Long userId, Long consultId, String consultStatus) {
        ConsultRecord consult = new ConsultRecord();
        consult.deleted = DeletedStatus.UN_DELETED;
        consult.save();
        consultId = consult.id;
        consultStatus = "finish";
        index(phone, condition, userId, consultId, consultStatus);
    }

    public static void bind(String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();

        render(phone, coupon, userId, consultId, consult);
    }

    public static void saveBind(String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
        CouponCallBind couponBind = new CouponCallBind();
        ECoupon coupon = ECoupon.find("id=?", couponId).first();

        couponBind.eCouponSn = coupon.eCouponSn;
        couponBind.phone = phone;
        couponBind.userId = userId;
        couponBind.couponId = coupon.id;
        couponBind.consultId = consultId;
        couponBind.save();
        couponBind = CouponCallBind.find("couponId=?", couponId).first();
        consult = ConsultRecord.find("id=?", consultId).first();
        if (consult != null) {
            if (consult.couponCallBindList == null) {
                consult.couponCallBindList = new ArrayList<>();
            }


            consult.couponCallBindList.add(couponBind);
            consult.save();
        }


        render(consult);

    }


    public static void abandon(Long consultId, String phone) {
        ConsultRecord consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();

        }

        getPhone();
    }

    public static void bindCouponDetails(String phone, Long couponId, Long userId, Long consultId) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        render(phone, coupon, userId, consultId);
    }

    public static void callCenter(String phone) {
        jumpIndex(phone, null, null, null, null);

    }

    public static void bindSearchUser(String phone, Long userId) {

        MemberCallBind bind = new MemberCallBind();
        User user = User.findById(userId);
        bind.phone = phone;
        bind.userId = userId;
        bind.loginName = user.loginName;
        bind.save();
        render();
    }


}
