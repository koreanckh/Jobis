package co.jobis.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SCRAP001")
@Data
@SequenceGenerator(
        name = "Scrap001Seq_Gen",
        sequenceName = "Scrap001Seq",
        initialValue = 1,
        allocationSize = 1
)
public class Scrap001Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Scrap001Seq_Gen")
    int idx;
    String userId;
    String incomeDetail;    // 소득내역
    int incomeTotal; // 총지급액
    Date workStartDate;     // 업무시작일
    String companyName;     // 기업명
    String name;            // 이름
    String regNo;           // 주민등록번호
    Date paymentDate;       // 지급일
    Date workEndDate;       // 업무종료
    String incomeType;      // 소득구분
    String companyRegNo;    // 사업자등록번호

    String errMsg;
    String company;
    String svcCd;

    String appVer;
    String hostNm;
    Date workResDt;
    Date workReqDt;

    @Builder
    public Scrap001Entity(int idx, String userId, String incomeDetail, int incomeTotal, Date workStartDate, String companyName, String name, String regNo, Date paymentDate, Date workEndDate, String incomeType, String companyRegNo, String errMsg, String company, String svcCd, String appVer, String hostNm, Date workResDt, Date workReqDt) {
        this.idx = idx;
        this.userId = userId;
        this.incomeDetail = incomeDetail;
        this.incomeTotal = incomeTotal;
        this.workStartDate = workStartDate;
        this.companyName = companyName;
        this.name = name;
        this.regNo = regNo;
        this.paymentDate = paymentDate;
        this.workEndDate = workEndDate;
        this.incomeType = incomeType;
        this.companyRegNo = companyRegNo;
        this.errMsg = errMsg;
        this.company = company;
        this.svcCd = svcCd;
        this.appVer = appVer;
        this.hostNm = hostNm;
        this.workResDt = workResDt;
        this.workReqDt = workReqDt;
    }

    @Builder

    public Scrap001Entity() {}
}
