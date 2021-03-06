package models.consumer;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.cms.VoteType;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-14
 * Time: 下午1:11
 */
public class UserVoteCondition implements Serializable {
    
    private static final long serialVersionUID = 8123206386412L;
    
    public VoteType type;

    public Date createdAtBegin;
    public Date createdAtEnd;
    public String loginName;
    public String content;
    public String fullName;
    public Map<String, Object> paramsMap = new HashMap<>();

    public String getCondition() {
        StringBuilder sql = new StringBuilder();

        sql.append(" u.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

//        sql.append(" and u.vote.correctAnswer= u.answer");
        if (type != null) {
            sql.append(" and u.vote.type=:type)");
            paramsMap.put("type", type);

        }
        if (createdAtBegin != null) {
            sql.append(" and u.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and u.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (StringUtils.isNotBlank(content)) {
            sql.append(" and u.vote.content like :content");
            paramsMap.put("content", "%" + content + "%");
        }
        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and u.user.loginName like :loginName");
            paramsMap.put("loginName", "%" + loginName + "%");
        }
        if (StringUtils.isNotBlank(fullName)) {
            sql.append(" and u.user.userInfo.fullName like :fullName");
            paramsMap.put("fullName", "%" + fullName + "%");
        }
        return sql.toString();
    }

    public String getFitter() {
        StringBuilder sql = new StringBuilder("1=1");
        sql.append(" and q.visible =:visible");
        paramsMap.put("visible", true);
        if (createdAtBegin != null) {
            sql.append(" and q.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and q.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (StringUtils.isNotBlank(content)) {
            sql.append(" and q.content like :content");
            paramsMap.put("content", "%" + content + "%");
        }

        return sql.toString();
    }
}
