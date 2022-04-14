package io.github.jihch.model;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "request")
@Data
public class CustomRequest {

	private Map<String, String> headers;
	
	private Integer connectTimeout;
	
	private Integer socketTimeout;

}
