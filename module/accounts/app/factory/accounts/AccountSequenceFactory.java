package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceFlag;
import models.accounts.TradeType;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-24
 * Time: 上午11:58
 * To change this template use File | Settings | File Templates.
 */
public class AccountSequenceFactory extends ModelFactory<AccountSequence> {

    @Override
    public AccountSequence define() {
        Account account = FactoryBoy.create(Account.class);
//        User user = FactoryBoy.create(User.class);
//        account.uid = user.id;

        AccountSequence accountSequence = new AccountSequence(account, AccountSequenceFlag.VOSTRO, TradeType.PAY,
                new BigDecimal(100), BigDecimal.ZERO,
                new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, 10l);

        return accountSequence;

    }
}
