package com.ssb.comm.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CommResponseConstant {
	
	SUCCESS(200, "Success"),
	AUTHORIZE(403, "No Permission"),
	ERROR(500, "Error");
	
	@Getter
	private int resultCode;
	@Getter
	private String resultMsg;
	
}
