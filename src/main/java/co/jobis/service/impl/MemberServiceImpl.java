package co.jobis.service.impl;

import co.jobis.dto.MemberDTO;
import co.jobis.entity.MemberEntity;
import co.jobis.entity.Scrap001Entity;
import co.jobis.entity.Scrap002Entity;
import co.jobis.entity.TokenEntity;
import co.jobis.repository.MemberRepository;
import co.jobis.repository.Scrap001Repository;
import co.jobis.repository.Scrap002Repository;
import co.jobis.repository.TokenRepository;
import co.jobis.service.MemberService;
import co.jobis.utils.Sha256;
import co.jobis.utils.jwt.JwtTokenUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private Scrap001Repository scrap001Repository;

    @Autowired
    private Scrap002Repository scrap002Repository;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 회원가입을 위한 메서드.
     * if 아이디 중복이 아니면 : 회원정보 저장 Y
     * else : 회원정보 저장 N
     *
     * @param memberDTO : 회원가입 정보(상세 내용 DTO 참고)
     * @return Json 으로 리턴. message(String), success(boolean)
     * @throws Exception
     */
    public String signUp (MemberDTO memberDTO) throws Exception {
        JsonObject result = new JsonObject();

        // 주민등록번호 앞,뒤자리 분리하여 뒷자리만 암호화 준비
        String[] tmpRegno = memberDTO.getRegNo().split("-");

        // DTO --> Entity
        MemberEntity me = new MemberEntity(
                memberDTO.getUserId()                             // id
                , Sha256.encrypt(memberDTO.getPassword())         // password
                , memberDTO.getName()                             // 이름
                , tmpRegno[0]                                     // 주민등록번호 앞자리
                , Sha256.encrypt(tmpRegno[1]));                   // 주민등록번호 뒷자리

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

        return new Gson().toJson(result);
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
     *
     * @param userId
     * @param password
     * @return
     * @throws Exception
     */
    @Override
    public String login(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception {
        JsonObject result = new JsonObject();
        Gson gson = new Gson();

        // 로그인 정보 조회(아이디, 비밀번호)
        Optional<MemberEntity> memberEntity = memberRepository.findByUserIdAndPassword(memberDTO.getUserId(), Sha256.encrypt(memberDTO.getPassword()));


        if (memberEntity.isPresent()){
            // 토큰 저장

            String token = jwtTokenUtil.generateToken(memberDTO);

            Cookie jwtToken = new Cookie("refresh_token", token);
            jwtToken.setHttpOnly(true);
            jwtToken.setSecure(true);
            jwtToken.setPath("/");
            jwtToken.setMaxAge(3600);
            res.addCookie(jwtToken);

            // 토큰정보 테이블 저장
            TokenEntity tokenEntity = TokenEntity.builder().key(memberDTO.userId).value(token).expireDate(jwtTokenUtil.getExpirationDateFromToken(token)).updateDate(new Date()).build();
            tokenRepository.save(tokenEntity);
            jwtTokenUtil.setUserAuthentication(req, memberDTO.userId);

            result.addProperty("token", token);         //토큰은 해더에 넣어서 리턴하지만, 테스트 편의를 위하여 리턴 데이터에 포함.
            result.addProperty("success", true);

        } else {
            result.addProperty("success", false);

        }

        return gson.toJson(result);
    }

    /**
     * 내 정보 불러오기
     *
     * @param req
     * @param res
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @Override
    public String me(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception {
        String jwt = jwtTokenUtil.getJwtFromRequest(req);
        String userId = jwtTokenUtil.getUserIdFromToken(jwt);

        return new Gson().toJson(memberRepository.findById(userId));
    }

    /**
     * 유저 스크랩 api 호출
     *
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @Override
    public String scrap(MemberDTO memberDTO) throws Exception {
        String url = "https://codetest.3o3.co.kr/scrap/";
        Map<String, Object> result = new HashMap<String, Object>();
        ResponseEntity<Map> resMap = null;
        Gson gson = new Gson();

        // 스크랩 api 호출
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("name", Collections.singletonList(memberDTO.getName()));
        params.put("regNo", Collections.singletonList(memberDTO.getRegNo()));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(params, header);

        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();

        resMap = restTemplate.postForEntity(uri.toString(), entity, Map.class);

        result.put("status", resMap.getStatusCodeValue());
        result.put("header", resMap.getHeaders());
        result.put("result", resMap.getBody());


        // api에서 얻어온 데이터로 DB 저장
        Map<String, Object> bodyMap = resMap.getBody();
        for(String key : bodyMap.keySet()) {
            // jsonList 내용 저장
            if(key.equals("jsonList")) {
                String appVer = String.valueOf(bodyMap.get("appVer"));
                String hostNm = String.valueOf(bodyMap.get("hostNm"));
                String workerResDt = String.valueOf(bodyMap.get("workerResDt"));
                String workerReqDt = String.valueOf(bodyMap.get("workerReqDt"));

                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy.MM.dd");
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

                Map tmp = (Map) bodyMap.get(key);
                String errMsg = String.valueOf(tmp.get("errMsg"));
                String company = String.valueOf(tmp.get("company"));
                String svcCd = String.valueOf(tmp.get("svcCd"));

                List scrap001List = (List) tmp.get("scrap001");
                List scrap002List = (List) tmp.get("scrap002");

                if(ObjectUtils.isNotEmpty(scrap001List)) {

                    for(Object tmpObj : scrap001List) {
                        Map<String, String> s001TmpMap = (Map)tmpObj;
                        Scrap001Entity s001Entity = Scrap001Entity.builder()
                                .userId(memberDTO.getUserId())
                                .incomeDetail(s001TmpMap.get("소득내역"))
                                .incomeTotal(Integer.valueOf(s001TmpMap.get("총지급액")))
                                .workStartDate(sdf1.parse(s001TmpMap.get("업무시작일")))
                                .companyName(s001TmpMap.get("기업명"))
                                .name(s001TmpMap.get("이름"))
                                .regNo(s001TmpMap.get("주민등록번호"))
                                .paymentDate(sdf1.parse(s001TmpMap.get("지급일")))
                                .workEndDate(sdf1.parse(s001TmpMap.get("업무종료일")))
                                .incomeType(s001TmpMap.get("소득구분"))
                                .companyRegNo(s001TmpMap.get("사업자등록번호"))
                                .errMsg(errMsg)
                                .company(company)
                                .svcCd(svcCd)
                                .appVer(appVer)
                                .hostNm(hostNm)
                                .workResDt(sdf2.parse(workerResDt))
                                .workReqDt(sdf2.parse(workerReqDt))
                                .build();
                        scrap001Repository.save(s001Entity);
                    }
                }

                if(ObjectUtils.isNotEmpty(scrap002List)) {
                    for(Object tmpObj : scrap002List) {
                        Map<String, String> s002TmpMap = (Map) tmpObj;
                        Scrap002Entity s002Entity = Scrap002Entity.builder()
                                .userId(memberDTO.getUserId())
                                .incomeType(s002TmpMap.get("소득구분"))
                                .paymentTotal(Integer.valueOf(s002TmpMap.get("총사용금액")))
                                .errMsg(errMsg)
                                .company(company)
                                .svcCd(svcCd)
                                .appVer(appVer)
                                .hostNm(hostNm)
                                .workResDt(sdf2.parse(workerResDt))
                                .workReqDt(sdf2.parse(workerReqDt))

                                .build();

                        scrap002Repository.save(s002Entity);
                    }
                }

            } else {

            }

        }

        return new Gson().toJson(result);
    }

    /**
     * 환급액 계산
     *
     * @param req
     * @param res
     * @param memberDTO
     * @return
     * @throws Exception
     */
    @Override
    public String refund(HttpServletRequest req, HttpServletResponse res, MemberDTO memberDTO) throws Exception {
        Map<String, String> result = new HashMap<>();

        // 토큰 정보 받아오기
        String jwt = jwtTokenUtil.getJwtFromRequest(req);
        String userId = jwtTokenUtil.getUserIdFromToken(jwt);

        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody();

        String name = String.valueOf(claims.get("name"));

        // 환급액 관련 테이블 조회
        Scrap001Entity scrap001Entity = scrap001Repository.findByUserId(userId);
        Scrap002Entity scrap002Entity = scrap002Repository.findByUserId(userId);

        // 한도, 공제액, 환급액 계산
        if(ObjectUtils.isNotEmpty(scrap001Entity) && ObjectUtils.isNotEmpty(scrap002Entity)) {
            int taxCreditLimit = 0;    // 세액공제한도
            int deductible = 0;        // 공제액
            int refundTax = 0;         // 환급액


            // 개인 총급여액
            int userIncomeTotal = scrap001Entity.getIncomeTotal();

            // 산출세액
            int userPaymentTotal = scrap002Entity.getPaymentTotal();

            // 세액공제 한도 계산
            if(userIncomeTotal <= 33000000) {
                taxCreditLimit = 740000;

            } else if (33000000 < userIncomeTotal && userIncomeTotal <= 70000000) {
                taxCreditLimit = (int) (740000 - (userIncomeTotal - 33000000) * 0.008);
                taxCreditLimit = Math.max(taxCreditLimit, 660000);

            } else {
                taxCreditLimit = 660000 - (userIncomeTotal - 70000000) / 2;
                taxCreditLimit = Math.max(taxCreditLimit, 500000);

            }


            // 공제액 계산
            if(userPaymentTotal <= 1300000) {
                deductible = (int) (userPaymentTotal * 0.55);
            } else {
                deductible = (int) (715000 - (1300000 - userPaymentTotal) * 0.3);
            }


            // 환급액 계산
            refundTax = Math.min(taxCreditLimit, deductible);

            result.put("이름", name);
            result.put("한도", String.valueOf(taxCreditLimit));
            result.put("공제액", String.valueOf(deductible));
            result.put("환급액", String.valueOf(refundTax));

            return new Gson().toJson(result);
        } else {

            return null;
        }
    }
}
