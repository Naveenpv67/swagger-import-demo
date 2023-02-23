package com.swagger.swaggerimport.models;


import java.util.List;

import lombok.Data;

@Data
public class Request {
	
	private String method;
	private String url;
	private Auth auth;
	private List<Parameters> parameters;
	private RequestBody requestbody;
	private String requestName;
}