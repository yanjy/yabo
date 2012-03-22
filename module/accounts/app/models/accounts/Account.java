package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Account extends Model {
    public long uid;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;        //账户类型

    public BigDecimal amount;       //总金额

    @Enumerated(EnumType.STRING)
    public AccountStatus status;

    @Column(name = "create_at")
    public Date createdAt;

    public static final long PLATFORM_INCOMING     = 1L;   //券平台收款账户
    public static final long PLATFORM_COMMISSION   = 2L;   //券平台佣金账户
    public static final long UHUILA_COMMISSION     = 3L;   //优惠啦佣金账户


    public Account(long uid, AccountType type){
        this.uid = uid;
        this.accountType = type;
        this.amount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
    }
    

}
