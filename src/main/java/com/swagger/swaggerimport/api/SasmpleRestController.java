package com.swagger.swaggerimport.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swagger.swaggerimport.processor.SwaggerParserExample;

@RestController
public class SasmpleRestController {
	
	@Autowired
	private SwaggerParserExample swaggerParserExample;
	
	@PostMapping(value = "/swagger")
	public ResponseEntity<Object> importSwagger() throws Exception {
		return ResponseEntity.ok().body(swaggerParserExample.extractRequestsFromOpenAPI("E:\\Documents\\TO\\API_Testing\\Import_swagger\\petsore_swagger_v3.json"));
	}
}