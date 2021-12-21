package co.jobis.controller;

import co.jobis.dto.MemberDTO;
import co.jobis.service.impl.MemberServiceImpl;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/szs")
public class MemberController {

    private final Logger logger = LoggerFactory.getLogger(MemberController.class);

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
        logger.debug("signUp start :: " + memberDTO.getUserId());

        Gson gson = new Gson();
        String result = gson.toJson(memberServiceImpl.signUp(memberDTO));

        logger.debug("signUp end :: " + memberDTO.getUserId());

        return result;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody MemberDTO memberDTO) throws Exception {
        logger.debug("login start :: " + memberDTO.getUserId());

        memberServiceImpl.login(memberDTO);

        logger.debug("login end :: " + memberDTO.getUserId());

        return null;
    }

}
