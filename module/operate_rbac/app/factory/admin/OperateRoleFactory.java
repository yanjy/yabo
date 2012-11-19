package factory.admin;

import factory.ModelFactory;
import models.admin.OperateRole;

import java.util.Date;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-19
 * Time: 下午1:28
 * To change this template use File | Settings | File Templates.
 */
public class OperateRoleFactory extends ModelFactory<OperateRole> {
    @Override
    public OperateRole define() {
        OperateRole role = new OperateRole();
        role.loadVersion = 1331545508967l;
        role.lockVersion = 0;
        role.createdAt = new Date();
        role.updatedAt = new Date();
        role.permissions=new HashSet<>();
        return role;
    }
}
