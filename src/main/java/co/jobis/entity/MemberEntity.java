package co.jobis.entity;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MEMBER")
@Data
public class MemberEntity {

    @Id
    @NotNull
    private String userId;         // 아이디

    @NotNull
    private String password;       // 비밀번호

    @NotNull
    private String name;           // 이름

    @NotNull
    private String regNo1;         // 주민등록번호 앞자리

    @NotNull
    private String regNo2;         // 주민등록번호 뒷자리

    public MemberEntity(String userId, String password, String name, String regNo1, String regNo2) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.regNo1 = regNo1;
        this.regNo2 = regNo2;
    }

    public MemberEntity() {}

}
