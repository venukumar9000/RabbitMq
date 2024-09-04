//package com.example.ticketing_queues.service;
//
//import com.example.ticketing_queues.config.RabbitMqProperties;
//import com.example.ticketing_queues.dto.TicketDto;
//import com.example.ticketing_queues.entity.TicketCreation;
//import com.example.ticketing_queues.repository.TicketRepository;
//import org.modelmapper.ModelMapper;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class RabbitMqConsumerService {
//
//    @Autowired
//    private TicketRepository ticketRepository;
//
//    @Autowired
//    private ModelMapper modelMapper;
//
//    @Autowired
//    private RabbitMqProperties rabbitMqProperties;
//
//    @Value("${rabbitmq.queue.crm}")
//    private String crmQueue;
//
//    @Value("${rabbitmq.queue.helpdesk}")
//    private String helpdeskQueue;
//
//    @Value("${rabbitmq.queue.fieldmanager}")
//    private String fieldmanagerQueue;
//
//    @Value("${rabbitmq.queue.fieldrepresentative}")
//    private String fieldrepresentativeQueue;
//
//    @Value("${rabbitmq.queue.sitemanager}")
//    private String sitemanagerQueue;
//
//    @Value("${rabbitmq.queue.default}")
//    private String defaultQueue;
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.crm}")
//    public void consumeFromCRM(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "CRM");
//    }
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.helpdesk}")
//    public void consumeFromHelpdesk(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "HELPDESK");
//    }
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.fieldmanager}")
//    public void consumeFromFieldManager(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "FIELD MANAGER");
//    }
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.fieldrepresentative}")
//    public void consumeFromFieldRepresentative(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "FIELDREPRESENTATIVE");
//    }
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.sitemanager}")
//    public void consumeFromSiteManager(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "SITEMANAGER");
//    }
//
//    @RabbitListener(queues = "#{@rabbitMqProperties.queue.default}")
//    public void consumeFromDefaultQueue(TicketDto ticketDto) {
//        processAndSaveTicket(ticketDto, "DEFAULT");
//    }
//
//    private void processAndSaveTicket(TicketDto ticketDto, String queueName) {
//        try {
//            // Convert DTO to Entity
//            TicketCreation ticket = modelMapper.map(ticketDto, TicketCreation.class);
//
//            // Insert into the database
//            ticketRepository.save(ticket);
//
//            // Implement your business logic here
//            sendNotification(ticketDto, queueName);
//            logProcessingDetails(ticketDto, queueName);
//        } catch (Exception e) {
//            // Handle exception, possibly logging the error
//            e.printStackTrace();
//        }
//    }
//
//    private void sendNotification(TicketDto ticketDto, String queueName) {
//        // Implementation for sending notifications
//        System.out.println("Notification sent for Ticket: " + ticketDto.getTicketTitle() + " from Queue: " + queueName);
//    }
//
//    private void logProcessingDetails(TicketDto ticketDto, String queueName) {
//        // Implementation for logging processing details
//        System.out.println("Processed Ticket: " + ticketDto.getTicketTitle() + " with Status: " + ticketDto.getStatus() + " from Queue: " + queueName);
//    }
//}

package com.example.ticketing_queues.service;

import com.example.ticketing_queues.config.RabbitMqProperties;
import com.example.ticketing_queues.dto.TicketDto;
import com.example.ticketing_queues.entity.TicketCreation;
import com.example.ticketing_queues.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public ObjectMapper objectMapper;


    public List<TicketCreation> consumeMessagesFromQueue(String queueName, int numberOfMessages) {
        List<TicketCreation> tickets = new ArrayList<>();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = rabbitTemplate.receive(queueName);
            if (message != null) {
                TicketCreation ticket = convertMessageToTicketCreation(message);
                tickets.add(ticket);
                saveTicketToDatabase(ticket);
            } else {
                break;  // Stop if no more messages are available in the queue
            }
        }
        return tickets;
    }

    private TicketCreation convertMessageToTicketCreation(Message message) {
        try {
            return objectMapper.readValue(message.getBody(), TicketCreation.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveTicketToDatabase(TicketCreation ticket) {
        if (ticket != null) {
            ticketRepository.save(ticket);
        }
    }


    private void logProcessingDetails(TicketDto ticketDto) {
        // Implementation for logging processing details
        System.out.println("Processed Ticket: " + ticketDto.getTicketTitle() + " with Status: " + ticketDto.getStatus());
    }
}

