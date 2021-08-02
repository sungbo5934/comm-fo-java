package com.ssb.comm.model;

import com.ssb.comm.annotation.ColunmField;

import lombok.Data;

@Data
public class CommBaseModel {
	
	@ColunmField(colunmNm = "member_key")
	private String commMemberId;

	@ColunmField(colunmNm = "memberNm")
	private String commMemberNm;
	
	@ColunmField(colunmNm = "phone")
	private String commMemberPhon;

	
	
}
