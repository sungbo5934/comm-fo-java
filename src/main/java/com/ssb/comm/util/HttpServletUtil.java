package com.ssb.comm.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.ssb.comm.annotation.ColunmField;
import com.ssb.comm.constant.CommJwtConstant;
import com.ssb.comm.helper.JwtHelper;
import com.ssb.comm.model.CommBaseModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpServletUtil {

	@Autowired
	private JwtHelper jwtHelper;
	
	@Value("${jwt.login.memberKey}")
	private String memberKey;
	
	@Value("${header.authorization}")
	private String authHeader;
	
	/** 
	* 설명 : Response에 새로운 Token값을 넣어준다.
	* @methodName : setResLoginToken 
	* @author : Sungbo Sim
	* @date : 2021.08.30 
	* @param response
	* @param claims 
	*/
	public void setResLoginToken(HttpServletResponse response, Map<String, Object> claims){
		
		if(claims.get(memberKey) != null) {
			try {
				response.setHeader(authHeader, jwtHelper.createToken(CommJwtConstant.TOKEN_LOGIN_TYPE.getValue(), Collections.singletonMap(memberKey, claims.get(memberKey))));
			} catch (Exception e) {
				log.error("setResLoginToken() : " + e.getMessage());
			}
		}
		
	}
	
	/** 
	* 설명 : 자바 리플렉션을 통해 현재 유저의 대한 정보를 request에 주입한다.
	* @methodName : getCommField 
	* @author : Sungbo Sim
	* @date : 2021.08.30 
	* @param request
	* @param claims
	* @return 
	*/
	@SuppressWarnings("unchecked")
	public byte[] getCommField(HttpServletRequest request, Map<String, Object> claims) {
		
		byte[] bytes = null;
		if(claims.get(memberKey) != null) {
			try {
				InputStream in = request.getInputStream();
				String body = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
				JSONObject jsonObj = new JSONObject(body);
				Field[] fields = CommBaseModel.class.getDeclaredFields();
				for(Field field : fields) {
					if(field.isAnnotationPresent(ColunmField.class)) {
						ColunmField colunmField = field.getAnnotation(ColunmField.class);
						jsonObj.put(field.getName(), ((Map<String, Object>)claims.get(memberKey)).get(colunmField.colunmNm()));
					}
				}
				bytes = jsonObj.toString().getBytes(StandardCharsets.UTF_8);
			}catch(Exception e) {
				log.error("getCommField() : " + e.getMessage());
			}
		}
		
		return bytes;
		
	}
	
}
