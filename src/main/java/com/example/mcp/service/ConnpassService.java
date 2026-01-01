package com.example.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class ConnpassService {

    private final RestTemplate restTemplate;

    @Value("${connpass.api.key:}")
    private String apiKey;

    @Value("${connpass.nickname:}")
    private String nickname;

    public ConnpassService() {
        this.restTemplate = new RestTemplate();
    }

    @Tool(description = "Search for events on Connpass(IT Study Group Support Platform Connecting Engineers). You can search by keywords, event IDs, year-month, or specific dates. Returns a list of matching events with details like title, description, URL, and dates.")
    public EventListResponse searchEvents(
            @ToolParam(description = "Event ID to search for (optional)", required = false) Integer eventId,
            @ToolParam(description = "Keywords to search for in event titles and descriptions (optional). You can specify multiple keywords by separating them with a comma.", required = false) String keyword,
            @ToolParam(description = "Year and month in YYYYMM format, e.g., 202501 for January 2025 (optional)", required = false) Integer ym,
            @ToolParam(description = "Specific date in YYYYMMDD format, e.g., 20250125 for January 25, 2025 (optional)", required = false) Integer ymd,
            @ToolParam(description = "Sort order: 1 for newest first, 2 for oldest first, 3 for most participants (optional, default: 1)", required = false) Integer order,
            @ToolParam(description = "Starting position for pagination (optional, default: 1)", required = false) Integer start,
            @ToolParam(description = "Number of events to return (optional, default: 10, max: 100)", required = false) Integer count,
            @ToolParam(description = "Prefecture or 'オンライン' (online) to filter by (optional). Example: '東京都', '大阪府', 'オンライン'", required = false) String prefecture,
            @ToolParam(description = "Whether to filter for events that are joinable (have capacity and are not full) (optional)", required = false) Boolean isJoinable) {

        String url = "https://do0nwqqcugpye.cloudfront.net/search_events";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        // Add parameters only if they are not null
        if (eventId != null)
            builder.queryParam("event_id", eventId);
        if (keyword != null) {
            for (String key : keyword.split(",")) {
                builder.queryParam("keyword", key.trim());
            }
        }
        if (ym != null)
            builder.queryParam("ym", ym);
        if (ymd != null)
            builder.queryParam("ymd", ymd);
        if (order != null)
            builder.queryParam("order", order);
        if (start != null)
            builder.queryParam("start", start);
        if (count != null)
            builder.queryParam("count", count);
        if (prefecture != null)
            builder.queryParam("prefecture", prefecture);
        if (isJoinable != null)
            builder.queryParam("is_joinable", isJoinable);
        if (nickname != null && !nickname.isEmpty())
            builder.queryParam("api_nickname", nickname);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("x-api-key", apiKey);
        }

        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                builder.toUriString(),
                org.springframework.http.HttpMethod.GET,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> events = (List<Map<String, Object>>) (body != null ? body.get("events") : List.of());

        List<EventResponse> eventResponses = events.stream().map(event -> {
            return new EventResponse(
                    (String) event.get("event_name"),
                    (String) event.get("event_url"),
                    (String) event.get("started_at"),
                    (String) event.get("owner_display_name"),
                    (String) event.get("participation_type"));
        }).collect(java.util.stream.Collectors.toList());

        return new EventListResponse(eventResponses);
    }

    public record EventResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("event_name") String eventName,
            @com.fasterxml.jackson.annotation.JsonProperty("event_url") String eventUrl,
            @com.fasterxml.jackson.annotation.JsonProperty("started_at") String startedAt,
            @com.fasterxml.jackson.annotation.JsonProperty("owner_display_name") String ownerDisplayName,
            @com.fasterxml.jackson.annotation.JsonProperty("participation_type") String participationType) {
    }

    public record EventListResponse(List<EventResponse> events) {
    }
}
