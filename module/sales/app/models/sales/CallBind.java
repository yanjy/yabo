package models.sales;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-11
 * Time: 上午10:13
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "call_bind")
public class CallBind extends Model {

    @Column(name = "email")
    @Required
    @Email
    public String loginName;

    public String phone;

    @Column(name = "user_id")
    public long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商


}
