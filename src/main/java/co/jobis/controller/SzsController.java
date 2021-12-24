package co.jobis.controller;

import co.jobis.dto.MemberDTO;
import co.jobis.service.impl.MemberServiceImpl;
import co.jobis.utils.jwt.JwtTokenProvider;
import co.jobis.utils.jwt.UserAuthentication;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/szs")
@Slf4j
public class SzsController {

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    /**
     * 회원가입을 위한 메서드.
     *
     * @param memberDTO : 회원가입 정보(상세 내용 DTO 참고)
     * @return Json 으로 리턴. message(String), success(boolean)
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signUp(@Valid @RequestBody MemberDTO memberDTO) throws Exception {
        log.debug("signUp start :: " + memberDTO.getUserId());

        Gson gson = new Gson();
        String result = gson.toJson(memberServiceImpl.signUp(memberDTO));

        log.debug("signUp end :: " + memberDTO.getUserId());

        return result;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(HttpServletReqeust req, HttpServletResponse res, @RequestBody MemberDTO memberDTO) throws Exception {
        log.debug("login start :: " + memberDTO.getUserId());
        Cookie jwtToken = new Cookie("refresh_token", value);
        jwtToken.setHttpOnly(true);
        jwtToken.setSecure(ture);
        jwtToken.setPath("/");
        jwtToken.setMaxAge(/*유효시간 입력*/);
        
        res.addCookie(token);
        
        memberServiceImpl.login(memberDTO);
        Authentication authentication = new UserAuthentication(memberDTO.getUserId(), null, null);
        String token = JwtTokenProvider.generateToken(authentication);
        System.out.println("토큰 :::::: " + token);
//        Response response = Response.builder().token(token).build();

        log.debug("login end :: " + memberDTO.getUserId());

        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

    @RequestMapping(value = "/scrap", method = RequestMethod.POST)
    public String scrap(@RequestBody MemberDTO memberDTO) throws Exception {
        log.debug("scrap start :: " + memberDTO.getUserId());
        String result = null;

        result = memberServiceImpl.scrap(memberDTO);

        log.debug("scrap end :: " + memberDTO.getUserId());

        return result;
    }

}
