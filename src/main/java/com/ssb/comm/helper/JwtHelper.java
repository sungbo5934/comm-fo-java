package com.ssb.comm.helper;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.ssb.comm.config.property.YamlPropertySourceFactory;
import com.ssb.comm.constant.CommJwtConstant;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@PropertySource(value = "classpath:/jwt/jwt-${spring.profiles.active}.yml", factory = YamlPropertySourceFactory.class)
public class JwtHelper {
	
	@Value("${jwt.apikey}")
	private String apiKey;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	@Value("${jwt.login.memberKey}")
	private String memberKey;
	
	@Value("${jwt.login.valid.minute}")
	private int loginTokenValidMin;
	
	@Value("${jwt.access.valid.minute}")
	private int accessTokenValidMin;
	
	@Value("${jwt.refresh.valid.day}")
	private int refreshTokenValidDay;
	
	@Value("${header.authorization}")
	private String authHeader;
	
	/** 
	* @methodName : getHeader 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @return Map<String, Object>
	* JWT Header 값 설정
	* typ : 해당 토큰의 타입
	* 
	* 해싱 	  : 가변 크기의 입력값에서 고정된 크기의 출력값을 생성해 내는 과정
	* 해시 함수 : 임의의 길이의 데이터를 고정된 길이의 데이터로 매핑하는 함수
	* 		    특정 알고리즘에 의해 해싱된 값의 크기는 항상 일정 ex) HS256 : 256bit
	* alg : 서명 생성 시 사용할 알고리즘
	* 	- HS256 : 1개의 secret key를 공유하는 알고리즘
	* 
	*/
	private Map<String, Object> getHeader() {
		
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("typ", CommJwtConstant.TOKEN_HEADER_TYP_JWT.getValue());
        headers.put("alg", CommJwtConstant.TOKEN_HEADER_ALG_HS256.getValue());
        
	    return headers;
	}
	
	
	/** 
	* @methodName : getSigninKey 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @param secretKey
	* @return Key
	* 인코딩된 SecretKey을 이용하여 Keys 객체 생성
	* 	Keys.hmacShaKeyFor() : HMAC-SHA algorithms을 이용하여 SecretKey 인스턴스를 생성
	* 	HS256 알고리즘 방식 : secretKey의 Bit 크기가 256 이상이어야함 ( Byte 길이 * 8 )
	*/
	private Key getSigninKey(String secretKey) {
	    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
	}

	/** 
	* @methodName : getLoginTokenTime 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @return
	* Login Token의 유효 시간 (분)
	*/
	public long getLoginTokenTime() {
		return 1000l * 60 * loginTokenValidMin;
	}
	
	/** 
	* @methodName : getAccessTokenTime 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @return 
	* Access Token의 유효 시간 (분)
	*/
	public long getAccessTokenTime() {
		return 1000l * 60 * accessTokenValidMin;
	}

	/** 
	* @methodName : getRefreshTokenTime 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @return 
	* Refresh Token의 유효 시간 (날)
	*/
	public long getRefreshTokenTime() {
		return 1000l * 60 *  60 * 24 * refreshTokenValidDay;
	}
	
	
	/** 
	* @methodName : getExpireTime 
	* @author : Sungbo Sim
	* @date : 2021.07.09 
	* @param type
	* @return 
	* 타입별 만료시간 date 생성
	*/
	public Date getExpireTime(String type) {
		
		Date expireTime = new Date();
		
		if(StringUtils.equals(type, CommJwtConstant.TOKEN_LOGIN_TYPE.getValue())) {
			expireTime.setTime(expireTime.getTime() + getLoginTokenTime());
		}
		
		if(StringUtils.equals(type, CommJwtConstant.TOKEN_ACCESS_TYPE.getValue())) {
			expireTime.setTime(expireTime.getTime() + getAccessTokenTime());
		}

		if(StringUtils.equals(type, CommJwtConstant.TOKEN_REFRESH_TYPE.getValue())) {
			expireTime.setTime(expireTime.getTime() + getRefreshTokenTime());
		}
		
		return expireTime;
	}
	
	/** 
	* @methodName : requestTokenChk 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @param RequestContext ctx
	* @return boolean
	* RequestContext의 request안의 Header정보를 읽어 Token 값을 검증
	* 로그인 토큰 값이라면 60분 연장된 토큰을 발급하여 로그인을 자동 연장
	* 
	* 	ClaimJwtException		: JWT 권한claim 검사가 실패했을 때
	* 	ExpiredJwtException		: 유효 기간이 지난 JWT를 수신한 경우
	* 	MalformedJwtException	: 구조적인 문제가 있는 JWT인 경우
	* 	PrematureJwtException	: 접근이 허용되기 전인 JWT가 수신된 경우
	* 	SignatureException		: 시그너처 연산이 실패하였거나, JWT의 시그너처 검증이 실패한 경우
	* 	UnsupportedJwtException	: 수신한 JWT의 형식이 애플리케이션에서 원하는 형식과 맞지 않는 경우. 예를 들어, 암호화된 JWT를 사용하는 애프리케이션에 암호화되지 않은 JWT가 전달되는 경우에 이 예외가 발생합니다.
	*/
	public boolean getTokenValid(HttpServletRequest request) {
		
		boolean tokenValid = true;
		
		try {
			log.info("new Token : " + createToken(CommJwtConstant.TOKEN_REFRESH_TYPE.getValue(), null));
			String authToken = request.getHeader(authHeader);
			Jwts.parserBuilder()
					.requireIssuer(issuer)
					.setSigningKey(getSigninKey(apiKey))
					.build()
					.parseClaimsJws(authToken);
		} catch(Exception e) {
			tokenValid = false;
			log.error("requestTokenChk() : " + e.getMessage());
		}
		
		return tokenValid;
	}
	
	
	public Map<String, Object> getTokenClaims(HttpServletRequest request) {
		
		Claims claims = null;
		try {
			String authToken = request.getHeader(authHeader);
			claims = Jwts.parserBuilder()
					.requireIssuer(issuer)
					.setSigningKey(getSigninKey(apiKey))
					.build()
					.parseClaimsJws(authToken)
					.getBody();
		} catch(Exception e) {
			log.error("requestTokenChk() : " + e.getMessage());
		}
		
		return claims;
	}
	
	/** 
	* @methodName : createToken 
	* @author : Sungbo Sim
	* @date : 2021.07.07 
	* @param type
	* @param claim
	* @return String
	* 타입에 맞는 토큰을 발급
	* 
	*   iss: 토큰 발급자(issuer)
	* 	sub: 토큰 제목(subject)
	* 	aud: 토큰 대상자(audience)
	* 	exp: 토큰 만료 시간(expiration), NumericDate 형식으로 되어 있어야 함 ex) 1480849147370
	* 	nbf: 토큰 활성 날짜(not before), 이 날이 지나기 전의 토큰은 활성화되지 않음
	* 	iat: 토큰 발급 시간(issued at), 토큰 발급 이후의 경과 시간을 알 수 있음
	* 	jti: JWT 토큰 식별자(JWT ID), 중복 방지를 위해 사용하며, 일회용 토큰(Access Token) 등에 사용
	*/
	public String createToken(String type, Map<String, Object> claim) throws Exception {
		
		return Jwts.builder()
				.setHeader(getHeader())
				.setIssuer(issuer)
				.setExpiration(getExpireTime(type))
				.setNotBefore(new Date())
				.addClaims(claim)
				.signWith(getSigninKey(apiKey), SignatureAlgorithm.HS256)
				.compact();
	}
	
}
