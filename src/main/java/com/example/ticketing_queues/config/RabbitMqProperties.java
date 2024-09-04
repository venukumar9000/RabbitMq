package com.example.ticketing_queues.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private String exchange;
    private Queues queue;
    private RoutingKeys routingKey;

    @Data
    public static class Queues {
        private String crm;
        private String helpdesk;
        private String fieldmanager;
        private String fieldrepresentative;
        private String defaultQueue;
        private String ticketanalysis;
        private String sitemanager;  // Added new queue
        private String unconsumedmessages;
    }

    @Data
    public static class RoutingKeys {
        private String crm;
        private String helpdesk;
        private String fieldmanager;
        private String fieldrepresentative;
        private String defaultRoutingKey;
        private String ticketanalysis;
        private String sitemanager;  // Added new routing key
        private String unconsumedmessages;
    }
}
