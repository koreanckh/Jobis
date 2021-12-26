package co.jobis;

import co.jobis.controller.SzsController;
import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;
import co.jobis.service.impl.MemberServiceImpl;
import co.jobis.utils.Sha256;
import com.google.gson.Gson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class MemberTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SzsController memberController;

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    private MemberDTO dto = new MemberDTO();
    private String token;

    /**
     * 회원가입 테스트
     */
    @Test
    @Disabled
    void signUpTest() throws Exception {
        dto.setUserId("admin");
        dto.setPassword("test1234");
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

        assertEquals(before.getName(), after.getName());

    }

    /**
     * 로그인 테스트
     *
     * @throws Exception
     */
    @Test
    void loginTest() throws Exception {
        // 필수 선행 작업
        signUpTest();

        String content = new Gson().toJson(dto);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/szs/login")
                    .content(content)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // JWT 저장
        Cookie[] cookies = mvcResult.getResponse().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh_token")) token = cookie.getValue();
        }
    }

    /**
     * 내 정보 보기 테스트
     *
     * @throws Exception
     */
    @Test
    void meTest() throws Exception {
        // 필수 선행 작업
        signUpTest();
        loginTest();

        String content = new Gson().toJson(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/szs/me")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .content(content)
                    .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

    }

    /**
     * 스크랩 테스트
     *
     * @throws Exception
     */
    @Test
    void scrapTest() throws Exception {
        // 필수 선행 작업
        signUpTest();
        loginTest();

        String content = new Gson().toJson(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/szs/scrap")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .content(content)
                    .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

    }

    /**
     * 환급금 조회 테스트
     *
     * @throws Exception
     */
    @Test
    void refundTest() throws Exception {
        // 필수 선행 작업
        signUpTest();
        loginTest();
        scrapTest();

        String content = new Gson().toJson(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/szs/refund")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .content(content)
                    .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

    }


}
