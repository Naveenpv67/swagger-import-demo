package com.swagger.swaggerimport.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameters {

	private boolean enabled;
	private String key;
	private String value;
	private String description;
	private boolean isParameterized;
	private Integer sequence;
	private In in;
	
}
