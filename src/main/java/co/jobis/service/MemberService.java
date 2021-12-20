package co.jobis.service;

import co.jobis.dto.MemberDTO;
import com.google.gson.JsonObject;

public interface MemberService {
    public JsonObject signUp (MemberDTO memberDTO) throws Exception ;
}
