package co.jobis.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SCRAP002")
@Data
@SequenceGenerator(
        name = "Scrap002Seq_Gen",
        sequenceName = "Scrap002Seq",
        initialValue = 1,
        allocationSize = 1
)
public class Scrap002Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Scrap002Seq_Gen")
    int idx;
    String userId;
    String incomeType;   // 소득구분
    int paymentTotal;    // 총사용액

    String errMsg;
    String company;
    String svcCd;
    String appVer;
    String hostNm;
    Date workResDt;
    Date workReqDt;

    @Builder
    public Scrap002Entity(int idx, String userId, String incomeType, int paymentTotal, String errMsg, String company, String svcCd, String appVer, String hostNm, Date workResDt, Date workReqDt) {
        this.idx = idx;
        this.userId = userId;
        this.incomeType = incomeType;
        this.paymentTotal = paymentTotal;
        this.errMsg = errMsg;
        this.company = company;
        this.svcCd = svcCd;
        this.appVer = appVer;
        this.hostNm = hostNm;
        this.workResDt = workResDt;
        this.workReqDt = workReqDt;
    }

    public Scrap002Entity() {}
}
