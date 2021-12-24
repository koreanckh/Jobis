package co.jobis.service.impl;

import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;
import co.jobis.repository.MemberRepository;
import co.jobis.service.MemberService;
import co.jobis.utils.Sha256;
import co.jobis.utils.jwt.JwtTokenUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService {

    private final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 회원가입을 위한 메서드.
     * if 아이디 중복이 아니면 : 회원정보 저장 Y
     * else : 회원정보 저장 N
     *
     * @param memberDTO : 회원가입 정보(상세 내용 DTO 참고)
     * @return Json 으로 리턴. message(String), success(boolean)
     * @throws Exception
     */
    public JsonObject signUp (MemberDTO memberDTO) throws Exception {
        JsonObject result = new JsonObject();

        // 주민등록번호 앞,뒤자리 분리하여 뒷자리만 암호화 준비
        String[] tmpRegno = memberDTO.getRegNo().split("-");

        // DTO --> Entity
        MemberEntity me = new MemberEntity(
                memberDTO.getUserId()                                   // id
                , Sha256.encrypt(memberDTO.getPassword())         // password
                , memberDTO.getName()                                   // 이름
                , tmpRegno[0]                                           // 주민등록번호 앞자리
                , Sha256.encrypt(tmpRegno[1]));                   // 주민등록번호 뒷자리

        logger.debug("회원가입 데이터 체크 :: " + me.toString());

        if(!isDuplicated(me.getUserId())) {
            // if 아이디 중복되지 않으면 저장 Y
            memberRepository.save(me);
            result.addProperty("message", "저장이 완료되었습니다.");
            result.addProperty("succes", true);

        } else {
            // else 아이디가 중복되면 저장 N
            result.addProperty("message", "아이디가 중복되었습니다.");
            result.addProperty("succes", false);
        }

        return result;
    }

    /**
     * 아이디 중복 체크
     *
     * @param userId 입력 받은 아이디
     * @return
     */
    public boolean isDuplicated(String userId) {
        return memberRepository.findById(userId).isPresent();
    }

    public MemberEntity findByUserId(String userId) throws Exception{
        return memberRepository.findByUserId(userId);
    }

    /**
     * 로그인 + token 발급
     * @param userId
     * @param password
     * @return
     * @throws Exception
     */
    @Override
    public JsonObject login(MemberDTO memberDTO) throws Exception {
        JsonObject result = new JsonObject();

        Optional<MemberEntity> memberEntity = memberRepository.findByUserIdAndPassword(memberDTO.getUserId(), Sha256.encrypt(memberDTO.getPassword()));

        if (memberEntity.isPresent()){

            result.addProperty("success", true);
        } else {
            result.addProperty("success", false);
        }
        return result;
    }

    @Override
    public String scrap(MemberDTO memberDTO) throws Exception {
        String url = "https://codetest.3o3.co.kr/scrap/";

        Map<String, Object> params = new HashMap<>();
        params.put("name", memberDTO.getName());
        params.put("regNo", memberDTO.getRegNo());
        
        // https://vmpo.tistory.com/27 참고!
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setConnectTimeout(5000); //타임아웃 설정 5초
//        factory.setReadTimeout(5000);//타임아웃 설정 5초
//        RestTemplate restTemplate = new RestTemplate(factory);
//
//        HttpHeaders header = new HttpHeaders();
//        HttpEntity<?> entity = new HttpEntity<>(header);
//
//        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url+"?"+"key=430156241533f1d058c603178cc3ca0e&targetDt=20120101").build();
//
//        //이 한줄의 코드로 API를 호출해 MAP타입으로 전달 받는다.
//        ResponseEntity<Map> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Map.class);
//        result.put("statusCode", resultMap.getStatusCodeValue()); //http status code를 확인
//        result.put("header", resultMap.getHeaders()); //헤더 정보 확인
//        result.put("body", resultMap.getBody()); //실제 데이터 정보 확인
 
        

        return getRequest(url, HttpMethod.POST, params);
    }

    public String getRequest(String url, HttpMethod method, Map<String, Object> params) {
        try {
            Gson gson = new Gson();
            String param = null;

            if (HttpMethod.POST.equals(method)) {
                param = gson.toJson(params);
            }


            URL uri = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setConnectTimeout(99999);
            connection.setRequestMethod(method.toString());
            connection.setRequestProperty("ContentType", "application/json");
            if (HttpMethod.POST.equals(method)) {
                connection.setDoOutput(true);
                try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                    output.writeBytes(param);
                    output.flush();
                }
            }

            int code = connection.getResponseCode();
            System.out.println(code);

            try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = input.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
