package models.order;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;

import models.accounts.AccountType;
import models.consumer.User;

import org.apache.commons.lang.StringUtils;

import com.uhuila.common.constants.DeletedStatus;

public class CouponsCondition {
    public Map<String, Object> couponsMap = new HashMap<>();

    /**
     * @return orderBySql 排序字段
     */
    public String getOrderByExpress() {
        String orderBySql = "e.createdAt desc";

        return orderBySql;
    }

    /**
     * 券查询条件
     *
     * @param user           用户信息
     * @param createdAtBegin 开始日
     * @param createdAtEnd   结束日
     * @param status         状态
     * @param goodsName      商品名称
     * @return sql 查询条件
     */
    public String getFilter(Long userId, AccountType accountType, Date createdAtBegin, Date createdAtEnd,
                            ECouponStatus status, String goodsName, String orderNumber, String phone) {
        StringBuilder sql = new StringBuilder();
        sql.append(" 1=1 ");
        if (userId != null && accountType != null) {
            sql.append(" and e.order.userId = :userId and e.order.userType = :userType");
            couponsMap.put("userId", userId);
            couponsMap.put("userType", accountType);
        }

        if (createdAtBegin != null) {
            sql.append(" and e.createdAt >= :createdAtBegin");
            couponsMap.put("createdAtBegin", createdAtBegin);
        }

        if (createdAtEnd != null) {
            sql.append(" and e.createdAt <= :createdAtEnd");
            couponsMap.put("createdAtEnd", createdAtEnd);
        }

        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and e.goods.name like :name");
            couponsMap.put("name", "%" + goodsName + "%");
        }

        if (status != null) {
            sql.append(" and e.status = :status");
            couponsMap.put("status", status);
        }

        if(orderNumber != null){
            sql.append(" and e.order.orderNumber like :orderNumber");
            couponsMap.put("orderNumber", "%" + orderNumber + "%");
        }
        
        if(StringUtils.isNotBlank(phone)){
            sql.append(" and e.orderItems.phone like :phone");
            couponsMap.put("phone", "%" + phone + "%");
        }

        return sql.toString();
    }
        
}
