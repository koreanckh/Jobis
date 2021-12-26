package co.jobis.controller;

import co.jobis.dto.MemberDTO;
import co.jobis.service.impl.MemberServiceImpl;
import co.jobis.utils.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 *
 */
@RestController
@RequestMapping(value = "/szs")
@Slf4j
public class SzsController {

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 회원가입  controller
     *
     * @param memberDTO : 회원가입 정보(상세 내용 DTO 참고)
     * @return Json 으로 리턴. message(String), success(boolean)
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<?>  signUp(@Valid @RequestBody MemberDTO memberDTO) throws Exception {
        return ResponseEntity.ok(memberServiceImpl.signUp(memberDTO));
    }

    /**
     * 로그인 controller
     *
     * @param req
     * @param res
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<?> login(HttpServletRequest req, HttpServletResponse res, @RequestBody MemberDTO memberDTO) throws Exception {
        return ResponseEntity.ok(memberServiceImpl.login(req, res, memberDTO));
    }

    /**
     * 내정보 조회  controller
     *
     * @param req
     * @param res
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/me", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<?> me(HttpServletRequest req, HttpServletResponse res, @RequestBody MemberDTO memberDTO) throws Exception {
        return ResponseEntity.ok(memberServiceImpl.me(req, res, memberDTO));
    }

    /**
     * scrap controller
     *
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/scrap", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<?> scrap(@RequestBody MemberDTO memberDTO) throws Exception {
        return ResponseEntity.ok(memberServiceImpl.scrap(memberDTO));
    }

    /**
     * 환급금 계산 controller
     *
     * @param req
     * @param res
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/refund", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<?> refund(HttpServletRequest req, HttpServletResponse res, @RequestBody MemberDTO memberDTO) throws Exception {
        return ResponseEntity.ok(memberServiceImpl.refund(req, res, memberDTO));
    }

}
