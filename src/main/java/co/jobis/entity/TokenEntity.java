package co.jobis.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Table(name = "TOKEN")
@Entity
public class TokenEntity {

    @Id
    private String key;             // key = userId
    @Size(max = 300)
    private String value;           // jwt value
    private Date expireDate;        // 만료일
    private Date updateDate;        // 업데이트일

    @Builder
    public TokenEntity(String key, String value, Date expireDate, Date updateDate) {
        this.key = key;
        this.value = value;
        this.expireDate = expireDate;
        this.updateDate = updateDate;
    }


    public TokenEntity() {}
}
