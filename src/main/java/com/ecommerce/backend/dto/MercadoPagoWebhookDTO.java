package com.ecommerce.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MercadoPagoWebhookDTO {
    private String action;
    
    @JsonProperty("api_version")
    private String apiVersion;
    
    private WebhookData data;
    
    @JsonProperty("date_created")
    private String dateCreated;
    
    private Long id;
    
    @JsonProperty("live_mode")
    private Boolean liveMode;
    
    private String type;
    
    @JsonProperty("user_id")
    private String userId;

    @Data
    public static class WebhookData {
        private String id;
    }
}
