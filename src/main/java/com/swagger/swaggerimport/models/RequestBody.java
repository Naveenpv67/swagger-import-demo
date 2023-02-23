package com.swagger.swaggerimport.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestBody {

	private String contentType;
	private String mode;
	private Object payload;
}
