package com.example.mcp.config;

import com.example.mcp.service.ConnpassService;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfiguration {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(ConnpassService connpassService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(connpassService)
                .build();
    }
}
