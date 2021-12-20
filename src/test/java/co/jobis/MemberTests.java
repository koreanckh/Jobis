package co.jobis;

import co.jobis.controller.MemberController;
import co.jobis.dto.MemberDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberTests {

    @Autowired
    MemberController memberController;

    /**
     * 정상케이스
     */
    @Test
    void signUpTest() throws Exception {
        MemberDTO dto = new MemberDTO();
        dto.setUserId("jobis");
        dto.setPassword("Password");
        dto.setName("최광훈");
        dto.setRegNo("880701-1111111");

        memberController.signUp(dto);

//        assertEquals();
    }


}
