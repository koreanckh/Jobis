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
     * ??????????????? ?????? ?????????.
     * if ????????? ????????? ????????? : ???????????? ?????? Y
     * else : ???????????? ?????? N
     *
     * @param memberDTO : ???????????? ??????(?????? ?????? DTO ??????)
     * @return Json ?????? ??????. message(String), success(boolean)
     * @throws Exception
     */
    public String signUp (MemberDTO memberDTO) throws Exception {
        JsonObject result = new JsonObject();

        // ?????????????????? ???,????????? ???????????? ???????????? ????????? ??????
        String[] tmpRegno = memberDTO.getRegNo().split("-");

        // DTO --> Entity
        MemberEntity me = new MemberEntity(
                memberDTO.getUserId()                             // id
                , Sha256.encrypt(memberDTO.getPassword())         // password
                , memberDTO.getName()                             // ??????
                , tmpRegno[0]                                     // ?????????????????? ?????????
                , Sha256.encrypt(tmpRegno[1]));                   // ?????????????????? ?????????

        if(!isDuplicated(me.getUserId())) {
            // if ????????? ???????????? ????????? ?????? Y
            memberRepository.save(me);
            result.addProperty("message", "????????? ?????????????????????.");
            result.addProperty("succes", true);

        } else {
            // else ???????????? ???????????? ?????? N
            result.addProperty("message", "???????????? ?????????????????????.");
            result.addProperty("succes", false);
        }

        return new Gson().toJson(result);
    }

    /**
     * ????????? ?????? ??????
     *
     * @param userId ?????? ?????? ?????????
     * @return
     */
    public boolean isDuplicated(String userId) {
        return memberRepository.findById(userId).isPresent();
    }

    public MemberEntity findByUserId(String userId) throws Exception{
        return memberRepository.findByUserId(userId);
    }

    /**
     * ????????? + token ??????
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

        // ????????? ?????? ??????(?????????, ????????????)
        Optional<MemberEntity> memberEntity = memberRepository.findByUserIdAndPassword(memberDTO.getUserId(), Sha256.encrypt(memberDTO.getPassword()));


        if (memberEntity.isPresent()){
            // ?????? ??????

            String token = jwtTokenUtil.generateToken(memberDTO);

            Cookie jwtToken = new Cookie("refresh_token", token);
            jwtToken.setHttpOnly(true);
            jwtToken.setSecure(true);
            jwtToken.setPath("/");
            jwtToken.setMaxAge(3600);
            res.addCookie(jwtToken);

            // ???????????? ????????? ??????
            TokenEntity tokenEntity = TokenEntity.builder().key(memberDTO.userId).value(token).expireDate(jwtTokenUtil.getExpirationDateFromToken(token)).updateDate(new Date()).build();
            tokenRepository.save(tokenEntity);
            jwtTokenUtil.setUserAuthentication(req, memberDTO.userId);

            result.addProperty("token", token);         //????????? ????????? ????????? ???????????????, ????????? ????????? ????????? ?????? ???????????? ??????.
            result.addProperty("success", true);

        } else {
            result.addProperty("success", false);

        }

        return gson.toJson(result);
    }

    /**
     * ??? ?????? ????????????
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
     * ?????? ????????? api ??????
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

        // ????????? api ??????
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


        // api?????? ????????? ???????????? DB ??????
        Map<String, Object> bodyMap = resMap.getBody();
        for(String key : bodyMap.keySet()) {
            // jsonList ?????? ??????
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
                                .incomeDetail(s001TmpMap.get("????????????"))
                                .incomeTotal(Integer.valueOf(s001TmpMap.get("????????????")))
                                .workStartDate(sdf1.parse(s001TmpMap.get("???????????????")))
                                .companyName(s001TmpMap.get("?????????"))
                                .name(s001TmpMap.get("??????"))
                                .regNo(s001TmpMap.get("??????????????????"))
                                .paymentDate(sdf1.parse(s001TmpMap.get("?????????")))
                                .workEndDate(sdf1.parse(s001TmpMap.get("???????????????")))
                                .incomeType(s001TmpMap.get("????????????"))
                                .companyRegNo(s001TmpMap.get("?????????????????????"))
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
                                .incomeType(s002TmpMap.get("????????????"))
                                .paymentTotal(Integer.valueOf(s002TmpMap.get("???????????????")))
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
     * ????????? ??????
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

        // ?????? ?????? ????????????
        String jwt = jwtTokenUtil.getJwtFromRequest(req);
        String userId = jwtTokenUtil.getUserIdFromToken(jwt);

        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody();

        String name = String.valueOf(claims.get("name"));

        // ????????? ?????? ????????? ??????
        Scrap001Entity scrap001Entity = scrap001Repository.findByUserId(userId);
        Scrap002Entity scrap002Entity = scrap002Repository.findByUserId(userId);

        // ??????, ?????????, ????????? ??????
        if(ObjectUtils.isNotEmpty(scrap001Entity) && ObjectUtils.isNotEmpty(scrap002Entity)) {
            int taxCreditLimit = 0;    // ??????????????????
            int deductible = 0;        // ?????????
            int refundTax = 0;         // ?????????


            // ?????? ????????????
            int userIncomeTotal = scrap001Entity.getIncomeTotal();

            // ????????????
            int userPaymentTotal = scrap002Entity.getPaymentTotal();

            // ???????????? ?????? ??????
            if(userIncomeTotal <= 33000000) {
                taxCreditLimit = 740000;

            } else if (33000000 < userIncomeTotal && userIncomeTotal <= 70000000) {
                taxCreditLimit = (int) (740000 - (userIncomeTotal - 33000000) * 0.008);
                taxCreditLimit = Math.max(taxCreditLimit, 660000);

            } else {
                taxCreditLimit = 660000 - (userIncomeTotal - 70000000) / 2;
                taxCreditLimit = Math.max(taxCreditLimit, 500000);

            }


            // ????????? ??????
            if(userPaymentTotal <= 1300000) {
                deductible = (int) (userPaymentTotal * 0.55);
            } else {
                deductible = (int) (715000 - (1300000 - userPaymentTotal) * 0.3);
            }


            // ????????? ??????
            refundTax = Math.min(taxCreditLimit, deductible);

            result.put("??????", name);
            result.put("??????", String.valueOf(taxCreditLimit));
            result.put("?????????", String.valueOf(deductible));
            result.put("?????????", String.valueOf(refundTax));

            return new Gson().toJson(result);
        } else {

            return null;
        }
    }
}
