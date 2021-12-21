package co.jobis.service;

import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;
import com.google.gson.JsonObject;

public interface MemberService {
    JsonObject signUp (MemberDTO memberDTO) throws Exception ;
    MemberEntity findByUserId(String userId) throws Exception;
    JsonObject login(MemberDTO memberDTO) throws Exception;
}
