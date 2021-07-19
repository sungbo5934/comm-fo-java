package com.ssb.comm.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommConstant{
	
	HEADER_BEARER_SPLIT("Bearer "),
	/* 토큰 헤더 정보 */
	TOKEN_HEADER_TYP_JWT("JWT"),
	TOKEN_HEADER_ALG_HS256("HS256"),
	/* 토큰 발급 타입 */
	TOKEN_LOGIN_TYPE("L"),
	TOKEN_ACCESS_TYPE("A"),
	TOKEN_REFRESH_TYPE("R");

	@Getter
    private final String value;
	
}

