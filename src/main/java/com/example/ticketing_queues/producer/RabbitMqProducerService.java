package com.example.ticketing_queues.producer;

import com.example.ticketing_queues.config.RabbitMqProperties;
import com.example.ticketing_queues.entity.TicketCreation;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class RabbitMqProducerService {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RabbitMqProperties properties;

    @Autowired
    private ConnectionFactory connectionFactory;

    public void sendMessageToQueue(TicketCreation ticketDto, String queueName, String routingKey) {
        try {
            // Convert TicketDto to a message object if necessary
            // Since we're using Jackson2JsonMessageConverter, we can send the TicketDto directly
            Object message = ticketDto;

            // Send the message to the specified exchange with the given routing key
            amqpTemplate.convertAndSend(properties.getExchange(), routingKey, message);

            System.out.println("Message sent to queue: " + queueName + " with routing key: " + routingKey);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            // Handle the error appropriately (e.g., retry logic, alerting, etc.)
        }
    }

    // Method to purge all messages from a queue
    public void purgeQueue(String queueName) throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        try {
            channel.queuePurge(queueName); // Purge all messages from the queue
        } finally {
            channel.close();
            connection.close();
        }
    }
}
