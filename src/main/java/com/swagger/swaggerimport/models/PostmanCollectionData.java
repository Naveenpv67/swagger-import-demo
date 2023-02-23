package com.swagger.swaggerimport.models;

import java.util.List;

import lombok.Data;

@Data
public class PostmanCollectionData {
	
	private String collectionName;
	private String appId;
	private List<Request> requests;

}
