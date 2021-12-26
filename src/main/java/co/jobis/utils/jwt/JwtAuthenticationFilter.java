package co.jobis.utils.jwt;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // jwt 토큰값 깨내기.
            String jwt = jwtTokenUtil.getJwtFromRequest(request);

            if (StringUtils.isNotEmpty(jwt) && jwtTokenUtil.validateToken(jwt)) {
                // 토큰에서 userId 추출.
                String userId = jwtTokenUtil.getUserIdFromToken(jwt); //jwt에서 사용자 id를 꺼낸다.
                String claims = jwtTokenUtil.getClaimFromToken(jwt, Claims::getId);

                jwtTokenUtil.setUserAuthentication(request, userId);

            } else {
                if (StringUtils.isEmpty(jwt)) {
                    request.setAttribute("unauthorization", "401 인증키 없음.");
                }
                if (jwtTokenUtil.validateToken(jwt)) {
                    request.setAttribute("unauthorization", "401-001 인증키 만료.");
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
//            throw new Exception("인증서 정보를 확인해주세요.");
        }

        filterChain.doFilter(request, response);
    }




}