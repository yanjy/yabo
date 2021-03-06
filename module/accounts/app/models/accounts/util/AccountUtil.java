package models.accounts.util;


import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountNotFoundException;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceFlag;
import models.accounts.AccountStatus;
import models.accounts.AccountType;
import models.accounts.BalanceNotEnoughException;
import models.accounts.SettlementStatus;
import models.accounts.TradeType;
import models.operator.Operator;
import models.resale.Resaler;
import models.resale.ResalerCreditable;
import play.Logger;

import java.math.BigDecimal;

/**
 * 账户资金变动流水工具.
 *
 * @author likang
 *         Date: 12-3-7
 */

public class AccountUtil {
    public static final String PARTNER_ALIPAY = "alipay";
    public static final String PARTNER_TENPAY = "tenpay";
    public static final String PARTNER_99BILL = "99bill";
    public static final String PARTNER_TESTPAY = "testpay";
    public static final String PARTNER_SINAPAY = "sina";

    /**
     * 一百券收款账户.
     *
     * @return
     */
    public static Account getUhuilaAccount(Operator operator) {
        return getCreditableAccount(Account.UHUILA_COMMISSION, AccountType.PLATFORM, operator);
    }

    /**
     * 平台收款账户.
     *
     * @return
     */
    public static Account getPlatformIncomingAccount(Operator operator) {
        return getCreditableAccount(Account.PLATFORM_INCOMING, AccountType.PLATFORM, operator);
    }

    public static Account getPlatformCommissionAccount(Operator operator) {
        return getCreditableAccount(Account.PLATFORM_COMMISSION, AccountType.PLATFORM, operator);
    }


    public static Account getPlatformWithdrawAccount(Operator operator) {
        return getAccount(Account.PLATFORM_WITHDRAW, AccountType.PLATFORM, operator);
    }


    public static Account getFinancingIncomingAccount(Operator operator) {
        return getCreditableAccount(Account.FINANCING_INCOMING, AccountType.PLATFORM, operator);
    }


    public static Account getPromotionAccount(Operator operator) {
        return getCreditableAccount(Account.PROMOTION, AccountType.PLATFORM, operator);
    }


    public static Account getPaymentPartnerAccount(String partner, Operator operator) {
        switch (partner) {
            case PARTNER_ALIPAY:
                return getCreditableAccount(Account.PARTNER_ALIPAY, AccountType.PLATFORM);
            case PARTNER_TENPAY:
                return getCreditableAccount(Account.PARTNER_TENPAY, AccountType.PLATFORM);
            case PARTNER_99BILL:
                return getCreditableAccount(Account.PARTNER_KUAIQIAN, AccountType.PLATFORM);
            case PARTNER_SINAPAY:
                return getCreditableAccount(Account.PARTNER_SINA, AccountType.PLATFORM);
            case PARTNER_TESTPAY:
                return getCreditableAccount(Account.PARTNER_TENPAY, AccountType.PLATFORM);
            default:
                return null;
        }
    }

    public static boolean accountExist(long uid, AccountType type) {
        return Account.find("byUidAndAccountType", uid, type).first() != null;
    }


    /**
     * 将门店对应的帐号的状态设置为CANCEL
     */
    public static boolean cancelAccount(long uid, AccountType type) {
        Account account = getAccount(uid, type);
        if (account == null) {
            return false;
        }
        account.status = AccountStatus.CANCEL;
        account.save();
        return true;
    }


    public static Account getConsumerAccount(long uid) {
        return getAccount(uid, AccountType.CONSUMER, false);
    }


    public static Account getResalerAccount(Resaler resaler) {
        if (resaler.creditable == ResalerCreditable.YES) {
            return getAccount(resaler.id, AccountType.RESALER, true, resaler.operator);
        }
        return getAccount(resaler.id, AccountType.RESALER, false, resaler.operator);
    }

    public static Account getResalerAccount(long uid) {
        Resaler resaler = Resaler.findById(uid);
        if (resaler == null) {
            return null;
        }
        return getResalerAccount(resaler);
    }

    public static Account getSupplierAccount(long supplierId, Operator operator) {
        return getAccount(supplierId, AccountType.SUPPLIER, true, operator);
    }

    public static Account getShopAccount(Long shopId, Operator operator) {
        return getAccount(shopId, AccountType.SHOP, true, operator);
    }

    /**
     * 获取不可欠款账户,若不存在则新建
     * 当账户余额小于0时,将抛出BalanceNotEnoughException
     *
     * @param uid  用户ID
     * @param type 用户类型
     * @return 不可欠款账户
     */
    public static Account getAccount(long uid, AccountType type) {
        return getAccount(uid, type, Operator.defaultOperator());
    }

    public static Account getAccount(long uid, AccountType type, Operator operator) {
        return getAccount(uid, type, false, operator);
    }

    /**
     * 获取可欠款账户,若不存在则新建
     * 账户余额可以小于0
     *
     * @param uid  用户ID
     * @param type 用户类型
     * @return 可欠款账户
     */
    public static Account getCreditableAccount(long uid, AccountType type) {
        return getAccount(uid, type, true);
    }

    public static Account getAccount(long uid, AccountType type, boolean creditable) {
        return getAccount(uid, type, creditable, Operator.defaultOperator());
    }


    public static Account getCreditableAccount(long uid, AccountType type, Operator operator) {
        return getAccount(uid, type, true, operator);
    }

    public static Account getAccount(long uid, AccountType type, boolean creditable, Operator operator) {
        if (operator == null) {
            Logger.error("Operator is Null!! Please check it ! uid:" + uid + " accountType:" + type + " creditable:" + creditable);
            operator = Operator.defaultOperator();
        }
        Account account = Account.find("uid=? and accountType=? and operator=? order by id", uid, type,
                operator).first();

        if (account == null) {
            synchronized (Account.class) {
                account = Account.find("uid=? and accountType=? and operator=? order by id", uid, type,
                        operator).first();
                if (account == null) {
                    account = new Account(uid, type, operator);
                    if (creditable) {
                        account.creditable = AccountCreditable.YES;
                    }
                    return account.save();
                }
            }
        }
        return account;
    }


    /*
     * 变更账户余额，同时不保存凭证相关信息.
     */
    public static Account addBalanceWithoutSavingSequence(Long accountId, BigDecimal cashAugend,
                                                          BigDecimal uncashAugend, BigDecimal promotionAugend,
                                                          Long billId, String note, Long orderId)
            throws BalanceNotEnoughException, AccountNotFoundException {
        return addBalance(accountId, cashAugend, uncashAugend, promotionAugend, billId, null, null,
                SettlementStatus.UNCLEARED, note, orderId, false, null, null);
    }

    /*
     * 变更账户余额，同时保存凭证相关信息.
     */
    public static Account addBalanceAndSaveSequence(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend,
                                                    BigDecimal promotionAugend,
                                                    Long billId, TradeType tradeType, AccountSequenceFlag sequenceFlag,
                                                    String note, Long orderId, String comment, String operatedBy)
            throws BalanceNotEnoughException, AccountNotFoundException {
        return addBalance(accountId, cashAugend, uncashAugend, promotionAugend, billId, tradeType, sequenceFlag,
                SettlementStatus.UNCLEARED, note, orderId, true, comment, operatedBy);
    }

    /*
    * 变更账户余额，同时保存凭证相关信息.
    */
    public static Account addBalanceAndSaveSequence(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend,
                                                    BigDecimal promotionAugend,
                                                    Long billId, TradeType tradeType, AccountSequenceFlag sequenceFlag,
                                                    SettlementStatus settlementStatus, String note, Long orderId)
            throws BalanceNotEnoughException, AccountNotFoundException {
        return addBalance(accountId, cashAugend, uncashAugend, promotionAugend, billId, tradeType, sequenceFlag,
                settlementStatus, note, orderId, true, null, null);
    }


    private static Account addBalance(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend,
                                      BigDecimal promotionAugend, Long billId,
                                      TradeType tradeType, AccountSequenceFlag sequenceFlag,
                                      SettlementStatus settlementStatus,
                                      String note, Long orderId, boolean saveSequence, String comment, String operatedBy)
            throws BalanceNotEnoughException, AccountNotFoundException {

        if (cashAugend == null || uncashAugend == null || promotionAugend == null) {
            throw new IllegalArgumentException("invalid augend while adding balance");
        }
        Account account = Account.findById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("can not find the specified account:" + accountId);
        }

        if (account.amount == null) {
            account.amount = BigDecimal.ZERO;
        }
        if (account.uncashAmount == null) {
            account.uncashAmount = BigDecimal.ZERO;
        }
        if (account.promotionAmount == null) {
            account.promotionAmount = BigDecimal.ZERO;
        }

        if (billId == null || (saveSequence && (tradeType == null || sequenceFlag == null))) {
            throw new IllegalArgumentException("error while add balance to account: miss parameter");
        }
        if (account.amount.add(cashAugend).compareTo(BigDecimal.ZERO) >= 0 || account.isCreditable()) {
            account.amount = account.amount.add(cashAugend);
        } else {
            throw new BalanceNotEnoughException("error while add cash to account:account(" + account + "),cashAugend:" + cashAugend + " balance not enough");
        }
        if (account.uncashAmount.add(uncashAugend).compareTo(BigDecimal.ZERO) >= 0 || account.isCreditable()) {
            account.uncashAmount = account.uncashAmount.add(uncashAugend);
        } else {
            throw new BalanceNotEnoughException("error while add uncashAmount to account: balance not enough. " +
                    "account=" + account);
        }

        if (account.promotionAmount.add(promotionAugend).compareTo(BigDecimal.ZERO) >= 0 || account.isCreditable()) {
            account.promotionAmount = account.promotionAmount.add(promotionAugend);
        } else {
            throw new BalanceNotEnoughException("error while add promotionAmount to account: balance not enough. " +
                    "account=" + account);
        }

        account.save();
        if (saveSequence) {
            accountChanged(account, cashAugend.add(uncashAugend), promotionAugend, billId, tradeType, sequenceFlag,
                    settlementStatus, note, orderId, comment, operatedBy);
        }

        return account;
    }

    private static void accountChanged(Account account, BigDecimal amount, BigDecimal promotionAmount, Long billId,
                                       TradeType tradeType, AccountSequenceFlag sequenceFlag,
                                       SettlementStatus settlementStatus, String note, Long orderId, String comment, String operatedBy) {
        if (account == null) {
            throw new IllegalArgumentException("accountChanged: account can not be null");
        }
        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,
                sequenceFlag,                                       //账务变动方向
                tradeType,                                          //变动类型
                amount,                                             //变动金额
                promotionAmount,                                    //变动活动金
                account.amount,                                     //变动后可提现余额
                account.uncashAmount,                               //变动后不可提现余额
                account.promotionAmount,                            //变动后活动金
                billId);                                            //相关流水号
        accountSequence.settlementStatus = settlementStatus == null ? SettlementStatus.UNCLEARED : settlementStatus;        //结算状态
        accountSequence.remark = note;
        accountSequence.orderId = orderId;
        accountSequence.comment = comment;
        accountSequence.operatedBy = operatedBy;
        accountSequence.save();                                     //保存账户变动信息
    }
}
