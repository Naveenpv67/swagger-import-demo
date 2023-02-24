package com.swagger.swaggerimport.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swagger.swaggerimport.processor.SwaggerParserExample;

@RestController
public class SampleRestController {
	
	@Autowired
	private SwaggerParserExample swaggerParserExample;
	
	@PostMapping(value = "/swagger")
	public ResponseEntity<Object> importSwagger(@RequestPart("uploadFile") MultipartFile uploadFile) throws Exception {
	
		return ResponseEntity.ok().body(swaggerParserExample.extractRequestsFromOpenAPI(uploadFile));
	}
}