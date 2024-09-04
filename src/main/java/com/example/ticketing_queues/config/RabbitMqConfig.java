package com.example.ticketing_queues.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
public class RabbitMqConfig {

    @Autowired
    private RabbitMqProperties properties;

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dead_letter_exchange");
    }

    @Bean
    public Queue crmQueue() {
        return QueueBuilder.durable(properties.getQueue().getCrm())
                .ttl(120000)
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }

    @Bean
    public Binding crmBinding() {
        return BindingBuilder
                .bind(crmQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getCrm());
    }

    @Bean
    public Queue helpdeskQueue() {
        return QueueBuilder.durable(properties.getQueue().getHelpdesk())
                .ttl(120000) // 2 minutes TTL
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }

    @Bean
    public Binding helpdeskBinding() {
        return BindingBuilder
                .bind(helpdeskQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getHelpdesk());
    }

    @Bean
    public Queue fieldmanagerQueue() {
        return QueueBuilder.durable(properties.getQueue().getFieldmanager())
                .ttl(120000) // 2 minutes TTL
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }

    @Bean
    public Binding fieldmanagerBinding() {
        return BindingBuilder
                .bind(fieldmanagerQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getFieldmanager());
    }

    @Bean
    public Queue fieldrepresentativeQueue() {
        return QueueBuilder.durable(properties.getQueue().getFieldrepresentative())
                .ttl(120000) // 2 minutes TTL
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }
    @Bean
    public Binding fieldrepresentativeBinding() {
        return BindingBuilder
                .bind(fieldrepresentativeQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getFieldrepresentative());
    }

    @Bean
    public Queue defaultQueue() {
        return QueueBuilder.durable(properties.getQueue().getDefaultQueue()).build();
    }
    @Bean
    public Binding defaultBinding() {
        return BindingBuilder
                .bind(defaultQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getDefaultRoutingKey());
    }

    @Bean
    public Queue ticketanalysisQueue() {
        return QueueBuilder.durable(properties.getQueue().getTicketanalysis())
                .ttl(120000) // 2 minutes TTL
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }
    @Bean
    public Binding ticketanalysisBinding() {
        return BindingBuilder
                .bind(ticketanalysisQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getTicketanalysis());
    }

    @Bean
    public Queue sitemanagerQueue() {
        return QueueBuilder.durable(properties.getQueue().getSitemanager())
                .ttl(120000) // 2 minutes TTL
                .deadLetterExchange("dead_letter_exchange")
                .deadLetterRoutingKey("unconsumedmessagesRoutingKey")
                .build();
    }

    @Bean
    public Binding sitemanagerBinding() {
        return BindingBuilder
                .bind(sitemanagerQueue())
                .to(topicExchange())
                .with(properties.getRoutingKey().getSitemanager());
    }

    @Bean
    public Queue unconsumedmessagesQueue() {
        return QueueBuilder.durable(properties.getQueue().getUnconsumedmessages()).build();
    }

    @Bean
    public Binding unconsumedmessagesBinding() {
        return BindingBuilder
                .bind(unconsumedmessagesQueue())
                .to(deadLetterExchange())
                .with("unconsumedmessagesRoutingKey");
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }


    private static final String RABBITMQ_SBIN_PATH = "C:\\Program Files\\RabbitMQ Server\\rabbitmq_server-3.13.7\\sbin\\rabbitmqctl.bat";

//    @PostConstruct
    public static void setupRabbitMQUser() {
        try {
            // Adding a new user
            executeCommand(new String[]{RABBITMQ_SBIN_PATH, "add_user", "user", "user"});

            // Setting user permissions
            executeCommand(new String[]{RABBITMQ_SBIN_PATH, "set_permissions", "-p", "/", "user", "", "", ".*"});

            // Tagging the user with 'monitoring' tag
            executeCommand(new String[]{RABBITMQ_SBIN_PATH, "set_user_tags", "user", "monitoring"});

            System.out.println("User setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeCommand(String[] command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }
}
