package co.jobis.service;

import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MemberService {
    String signUp (MemberDTO memberDTO) throws Exception ;
    MemberEntity findByUserId(String userId) throws Exception;
    String login(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception;
    String me(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception;
    String scrap(MemberDTO memberDTO) throws Exception;
    String refund(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception;
}
