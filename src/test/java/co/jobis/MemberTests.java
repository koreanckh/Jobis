package co.jobis;

import co.jobis.controller.SzsController;
import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;
import co.jobis.service.impl.MemberServiceImpl;
import co.jobis.utils.Sha256;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SzsController memberController;

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    /**
     * 정상케이스
     */
    @Test
    @Disabled
    void signUpTest() throws Exception {
        MemberDTO dto = new MemberDTO();
        dto.setUserId("jobis");
        dto.setPassword("Password");
        dto.setName("홍길동");
        dto.setRegNo("860824-1655068");

        memberController.signUp(dto);

        String[] tmpRegNo = dto.getRegNo().split("-");

        MemberEntity before = new MemberEntity(dto.getUserId()
                , Sha256.encrypt(dto.getPassword())
                , dto.getName()
                , Sha256.encrypt(tmpRegNo[0])
                , Sha256.encrypt(tmpRegNo[1]));

        MemberEntity after = memberServiceImpl.findByUserId(dto.getUserId());

        assertEquals(before, after);

    }


}
