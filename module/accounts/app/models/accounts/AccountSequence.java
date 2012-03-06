package models.accounts;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户资金变动流水
 *
 * User: likang
 */
@Entity
@Table(name = "account_sequence")
public class AccountSequence extends Model {

    @Column(name = "serial_number")
    public String serialNumber;

    @ManyToOne
    public Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "sequence_flag")
    public AccountSequenceFlag sequenceFlag;    //账务变动方向：来账，往账

    @Enumerated(EnumType.STRING)
    @Column(name = "sequence_type")
    public AccountSequenceType sequenceType;    //资金变动类型

    @Column(name = "pre_amount")
    public BigDecimal preAmount;                //变动前金额

    public BigDecimal amount;                   //变动后金额

    @Column(name = "cash_amount")
    public BigDecimal cashAmount;               //可提现发生额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount;             //不可提现发生额

    @Column(name = "ref_sn_id")
    public Long referenceSerialId;              //关联交易流水ID

    @Column(name = "order_id")
    public Long orderId;                        //冗余订单ID

    @Column(name = "created_at")
    public Date createdAt;                      //创建时间

    public String remark;                       //备注

    public AccountSequence(Account account, AccountSequenceFlag sequenceFlag, AccountSequenceType sequenceType,
                           BigDecimal preAmount, BigDecimal amount, BigDecimal cashAmount, BigDecimal uncashAmount,
                           long referenceSerialId){

        this.account = account;
        this.sequenceFlag = sequenceFlag;
        this.sequenceType = sequenceType;
        this.preAmount = preAmount;
        this.amount = amount;
        this.cashAmount = cashAmount;
        this.uncashAmount = uncashAmount;
        this.referenceSerialId = referenceSerialId;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.remark = null;

    }

}
