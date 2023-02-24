package com.swagger.swaggerimport.processor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swagger.swaggerimport.models.Auth;
import com.swagger.swaggerimport.models.In;
import com.swagger.swaggerimport.models.Parameters;
import com.swagger.swaggerimport.models.Request;
import com.swagger.swaggerimport.models.RequestBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;

@Component
public class SwaggerParserExample {

	public List<Request> extractRequestsFromOpenAPI(MultipartFile multiPartFile) throws IOException {

		Path tempFilePath = Files.createTempFile(multiPartFile.getOriginalFilename(), null);
		try (InputStream inputStream = multiPartFile.getInputStream()) {
			Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
			OpenAPI openAPI = new OpenAPIV3Parser().read(tempFilePath.toString());

			// Process the file
			String baseUrl = openAPI.getServers().get(0).getUrl();
			Map<String, Schema> components = openAPI.getComponents().getSchemas();
			io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
			List<Request> requestList = new ArrayList<>();
			for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
				String path = entry.getKey();
				PathItem pathItem = entry.getValue();
				Operation get = pathItem.getGet();
				Operation post = pathItem.getPost();
				Operation put = pathItem.getPut();
				Operation delete = pathItem.getDelete();
				Operation head = pathItem.getHead();
				Operation options = pathItem.getOptions();
				Operation patch = pathItem.getPatch();
				if (get != null) {
					requestList.add(processRequest("GET", baseUrl + path, get, components));
				}
				if (post != null) {
					requestList.add(processRequest("POST", baseUrl + path, post, components));
				}
				if (put != null) {
					requestList.add(processRequest("PUT", baseUrl + path, put, components));
				}
				if (delete != null) {
					requestList.add(processRequest("DELETE", baseUrl + path, delete, components));
				}
				if (head != null) {
					requestList.add(processRequest("HEAD", baseUrl + path, head, components));
				}
				if (options != null) {
					requestList.add(processRequest("OPTIONS", baseUrl + path, options, components));
				}
				if (patch != null) {
					requestList.add(processRequest("PATCH", baseUrl + path, patch, components));
				}
			}
			return requestList;
		} finally {
			try {
				Files.deleteIfExists(tempFilePath);
			} catch (IOException e) {
				// log or handle the exception
			}
		}
	}

	private Request processRequest(String method, String url, Operation operation, Map<String, Schema> components) {
		Request request = new Request();
		List<Parameters> parameterList = new ArrayList<>();
		Auth auth = new Auth();

		String operationId = operation.getOperationId();
		List<Parameter> parameters = operation.getParameters();

		if (parameters != null) {
			for (Parameter parameter : parameters) {
				Parameters param = new Parameters();
				param.setKey(parameter.getName());
				param.setIn(In.valueOf(parameter.getIn().toUpperCase()));
				param.setEnabled(true);
				parameterList.add(param);
			}
		}

		request.setParameters(parameterList);
		request.setParameters(parameterList);
		request.setRequestName(operationId);
		request.setMethod(method);
		request.setUrl(url);

		io.swagger.v3.oas.models.parameters.RequestBody openApiRequestBody = operation.getRequestBody();
		if (openApiRequestBody != null) {
			RequestBody requestBody = new RequestBody();
			Content content = openApiRequestBody.getContent();

			for (String contentType : content.keySet()) {
				MediaType mediaType = content.get(contentType);
				Schema schema = mediaType.getSchema();

				if (schema != null) {
					Object example = getExampleFromSchema(schema, components);
					if (example != null) {
						requestBody.setPayload(example.toString());
						requestBody.setContentType(contentType);
						break;
					}
				}
			}
			request.setRequestbody(requestBody);
		}

		return request;
	}

	private Object getExampleFromSchema(Schema schema, Map<String, Schema> components) {
		if (schema.get$ref() != null) {
			String ref = schema.get$ref();
			String[] parts = ref.split("/");
			String definitionName = parts[parts.length - 1];

			Schema definitionSchema = components.get(definitionName);
			if (definitionSchema != null) {
				return getExampleFromSchema(definitionSchema, components);
			}
		} else if (schema instanceof ArraySchema) {
			ArraySchema arraySchema = (ArraySchema) schema;
			Schema itemSchema = arraySchema.getItems();

			if (itemSchema.get$ref() != null) {
				String ref = itemSchema.get$ref();
				String[] parts = ref.split("/");
				String definitionName = parts[parts.length - 1];

				Schema definitionSchema = components.get(definitionName);
				if (definitionSchema != null) {
					return Arrays.asList(getExampleFromSchema(definitionSchema, components));
				}
			} else {
				List<Object> exampleList = new ArrayList<>();
				Object itemExample = getExampleFromSchema(itemSchema, components);
				if (itemExample != null) {
					exampleList.add(itemExample);
					return exampleList;
				}
			}
		} else if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
			ObjectNode exampleNode = JsonNodeFactory.instance.objectNode();
			for (Object propertyName : schema.getProperties().keySet()) {
				Schema propertySchema = (Schema) schema.getProperties().get(propertyName);
				Object propertyExample = getExampleFromSchema(propertySchema, components);
				if (propertyExample != null) {
					exampleNode.putPOJO((String) propertyName, propertyExample);
				}
			}
			return exampleNode;
		} else if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
			return schema.getEnum().get(0);
		} else {
			String format = schema.getFormat();
			switch (schema.getType()) {
			case "string":
				if ("binary".equals(format) || "byte".equals(format)) {
					return "VEVTVA=="; // base64 encoded string
				} else if ("date".equals(format)) {
					return "2023-02-23";
				} else if ("date-time".equals(format)) {
					return "2023-02-23T01:23:45.678Z";
				} else if ("password".equals(format)) {
					return "password123";
				} else {
					return "string";
				}
			case "integer":
				if ("int32".equals(format)) {
					return 0;
				} else {
					return 0L;
				}
			case "number":
				return 0.0;
			case "boolean":
				return true;
			case "array":
				ArraySchema arraySchema = (ArraySchema) schema;
				Schema items = arraySchema.getItems();
				if (items.getType().equals("string")) {
					List<String> stringList = new ArrayList<>();
					stringList.add("string");
					return stringList;
				} else {
					return Collections.emptyList();
				}
			case "object":
				return JsonNodeFactory.instance.objectNode();
			default:
				return null;
			}
		}
		return null;
	}

}