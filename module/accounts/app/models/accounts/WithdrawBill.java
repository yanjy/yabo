package models.accounts;

import com.uhuila.common.util.DateUtil;
import models.accounts.util.AccountUtil;
import models.accounts.util.SerialNumberUtil;
import models.accounts.util.TradeUtil;
import models.order.Prepayment;
import play.Logger;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 提现流水
 *
 * @author likang
 *         Date: 12-3-6
 */
@Entity
@Table(name = "withdraw_bill")
public class WithdrawBill extends Model {

    public String serialNumber;

    @ManyToOne
    public Account account;

    @Column(name = "applier")
    public String applier;

    @Required
    @Min(10)
    public BigDecimal amount;           //提现金额

    public BigDecimal fee;              //手续费

    @Column(name = "user_name")
    @Required
    public String userName;             //银行账户的开户姓名

    @Column(name = "bank_city")
    @Required
    public String bankCity;

    @Column(name = "bank_name")
    @Required
    public String bankName;

    @Column(name = "sub_bank_name")
    @Required
    public String subBankName;

    @Column(name = "card_number")
    @Required
    public String cardNumber;

    @Enumerated(EnumType.STRING)
    public WithdrawBillStatus status;

    @Column(name = "applied_at")
    public Date appliedAt;

    @Column(name = "processed_at")
    public Date processedAt;

    public String comment;

    @Column(name = "account_name")
    public String accountName;        //帐户名称，如果是商户，则为商户的短名称

    /**
     * 申请提现.
     *
     * @param applier 申请人信息
     * @param account 申请提现的账户
     */
    public boolean apply(String applier, Account account, String accountName) {
        this.applier = applier;
        this.account = account;
        this.status = WithdrawBillStatus.APPLIED;
        this.appliedAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber();
        this.accountName = accountName;
        this.save();

        try {
            AccountUtil.addBalanceWithoutSavingSequence(account.getId(), this.amount.negate(), this.amount, BigDecimal.ZERO,
                    this.getId(), "申请提现", null);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            return false;
        }

        return true;
    }

    public static WithdrawBill findByIdAndUser(Long id, Long userId, AccountType accountType) {
        return WithdrawBill.find("id = ? and account.uid = ? and account.accountType = ?",
                id, userId, accountType).first();
    }

    /**
     * 提现申请被拒绝
     *
     * @param comment 拒绝理由.
     */
    public boolean reject(String comment) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return false;
        }

        try {
            AccountUtil.addBalanceWithoutSavingSequence(this.account.getId(), this.amount, this.amount.negate(), BigDecimal.ZERO,
                    this.getId(), "拒绝提现", null);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            return false;
        }

        this.comment = comment;
        this.status = WithdrawBillStatus.REJECTED;
        this.processedAt = new Date();
        this.save();
        return true;
    }

    /**
     * 同意提现操作.
     *
     * @param comment 备注.
     */
    private boolean agree(BigDecimal fee, String comment) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return false;
        }

        TradeBill tradeBill = TradeUtil.createWithdrawTrade(this.account, this.amount);
        TradeUtil.success(tradeBill, "提现成功", SettlementStatus.CLEARED);

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.save();
        return true;
    }

    /**
     * 结算操作，返回结算详细记录的笔数.
     *
     * @param fee
     * @param comment
     * @param withdrawDate
     * @return public int agree(BigDecimal fee, String comment, Date withdrawDate, Prepayment prepayment) {
    if (agree(fee, comment) && account.accountType == AccountType.SUPPLIER) { //同意提现操作
    //标记所有详细销售记录为已结算
    return AccountSequence.withdraw(account, withdrawDate, this, prepayment);
    }
    return 0;
    }
     */

    /**
     * 结算操作，返回结算详细记录的笔数.
     *
     * @param fee
     * @param comment
     * @param withdrawDate
     * @return
     */
    public int settle(BigDecimal fee, String comment, Date withdrawDate, Prepayment prepayment) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return 0;
        }

        if (prepayment == null) {
            create2TradeBill(amount, BigDecimal.ZERO);
        } else if (amount.compareTo(prepayment.getBalance()) <= 0) { //可结算金额小于或等于预付款余额时，产生一笔TradeBill，只产生预付款结算记录
            create2TradeBill(BigDecimal.ZERO, amount);
        } else {
            //如果预付款已过期
            System.out.println("prepayment:" + prepayment);
            System.out.println("withdrawDate:" + withdrawDate);
            final Date endOfPrepaymentExpireAt = DateUtil.getEndOfDay(prepayment.expireAt);

            if (withdrawDate.after(endOfPrepaymentExpireAt)) {
                BigDecimal cashSettledAmount = AccountSequence.getVostroAmount(account, endOfPrepaymentExpireAt);
                if (cashSettledAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    create2TradeBill(amount.subtract(prepayment.getBalance()), prepayment.getBalance());
                } else {
                    //获取过期时间之前的所有的未结算的收入总额。
                    BigDecimal moreAmount = AccountSequence.getVostroAmountTo(account, endOfPrepaymentExpireAt);
                    moreAmount = moreAmount.compareTo(prepayment.getBalance()) > 0 ? moreAmount.subtract(prepayment.getBalance()) : BigDecimal.ZERO;
                    create2TradeBill(cashSettledAmount.add(moreAmount), amount.subtract(cashSettledAmount).subtract(moreAmount));
                }
            } else {
                //预付款未过期
                create2TradeBill(amount.subtract(prepayment.getBalance()), prepayment.getBalance());
            }
        }

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.save();

        if (account.accountType == AccountType.SUPPLIER) { //同意提现操作
            //标记所有详细销售记录为已结算
            return AccountSequence.withdraw(account, withdrawDate, this, prepayment);
        }
        return 0;
    }

    /**
     * 结算时生成TradeBill.
     *
     * @param cashSettledAmount
     * @param prepaymentSettledAmount
     */
    private void create2TradeBill(BigDecimal cashSettledAmount, BigDecimal prepaymentSettledAmount) {
        if (cashSettledAmount.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill prepaymentTradeBill = TradeUtil.createWithdrawTrade(this.account, cashSettledAmount);
            TradeUtil.success(prepaymentTradeBill, "现金结算", SettlementStatus.CLEARED);
        }
        if (prepaymentSettledAmount.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill cashPayTradeBill = TradeUtil.createWithdrawTrade(this.account, prepaymentSettledAmount);
            TradeUtil.success(cashPayTradeBill, "预付款结算", SettlementStatus.CLEARED);
        }
    }


    /**
     * 结算操作，返回结算详细记录的笔数.
     *
     * @param fee
     * @param comment
     * @param withdrawDate
     * @return
     */
    public int agree(BigDecimal fee, String comment, Date withdrawDate) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return 0;
        }

        TradeBill tradeBill = TradeUtil.createWithdrawTrade(this.account, this.amount);
        TradeUtil.success(tradeBill, "提现成功", SettlementStatus.CLEARED);

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.save();

        if (account.accountType == AccountType.SUPPLIER) { //同意提现操作
            //标记所有详细销售记录为已结算
            return AccountSequence.withdraw(account, withdrawDate, this, null);
        }
        return 0;
    }

    public static JPAExtPaginator<WithdrawBill> findByCondition(
            WithdrawBillCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<WithdrawBill> page = new JPAExtPaginator<>(
                null, null, WithdrawBill.class, condition.getFilter(), condition.getParams());

        page.orderBy("appliedAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    /**
     * 获取已提现的总金额.
     *
     * @param account
     * @return
     */
    public static BigDecimal getWithdrawAmount(Account account) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(b.amount) from WithdrawBill b where account=:account and status=:status");
        q.setParameter("account", account);
        q.setParameter("status", WithdrawBillStatus.SUCCESS);

        return (BigDecimal) q.getSingleResult();
    }
}
