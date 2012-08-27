package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.admin.OperateUser;
import models.consumer.User;
import models.webop.WithdrawReport;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-27
 * Time: 上午11:51
 * To change this template use File | Settings | File Templates.
 */
public class WithdrawReportUnitTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        final User u = FactoryBoy.create(User.class);
        // 初始化数据
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.tradeId = new Long(seq);
                target.account.uid = u.id;
            }
        });

    }

    @Test
    public void testInit() {
        WithdrawReport withdrawReport = new WithdrawReport(new Date(), AccountType.CONSUMER, new BigDecimal(100));
        assertNotNull(withdrawReport);
        assertEquals(new BigDecimal(100), withdrawReport.amount);
    }

    @Test
    public void testQueryWithdrawReport() {


    }


}
