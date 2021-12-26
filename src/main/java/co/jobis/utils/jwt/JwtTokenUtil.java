package co.jobis.utils.jwt;

import co.jobis.dto.MemberDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenUtil implements Serializable {
    private static final long serialVersionUID = -798416586417070603L;
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * jwt 토큰에서 userId 검색
     *
     * @param token
     * @return
     */
    public String getUserIdFromToken(String token) throws Exception {
        try{
            return getClaimFromToken(token, Claims::getSubject);
        }catch(Exception ex){
            throw new Exception("Exception!!");
        }
    }

    /**
     * jwt 토큰에서 날짜 만료 검색
     *
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * secret 키를 가지고 토큰에서 정보 검색
     *
     * @param token
     * @return
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * 토큰 만료 체크
     *
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token){
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * userId로 토큰생성
     *
     * @param MemberDTO
     * @return
     */
    public String generateToken(MemberDTO memberDTO){
        String[] regNo = memberDTO.getRegNo().split("-");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", memberDTO.getName());
        claims.put("userId", memberDTO.getUserId());
        claims.put("birth", regNo[0]);
        return doGenerateToken(claims, memberDTO.getUserId());
    }

    /**
     * 토큰 설정 및 생성
     *
     * @param claims
     * @param userId
     * @return
     */
    private String doGenerateToken(Map<String, Object> claims, String userId) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 토큰 검증
     *
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token) throws Exception {
        final String userId = getUserIdFromToken(token);
        System.out.println("validateToken :: " + userId);
        return!isTokenExpired(token);
    }

    /**
     * UserAuthentication 세팅
     *
     * @param request
     * @param userId
     */
    public void setUserAuthentication(HttpServletRequest request, String userId) {
        UserAuthentication authentication = new UserAuthentication(userId, null, null); //id를 인증한다.
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //기본적으로 제공한 details 세팅

        SecurityContextHolder.getContext().setAuthentication(authentication); //세션에서 계속 사용하기 위해 securityContext에 Authentication 등록
    }

    /**
     * HttpServletRequest 에서 토큰 추출
     *
     * @param request
     * @return
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

}
