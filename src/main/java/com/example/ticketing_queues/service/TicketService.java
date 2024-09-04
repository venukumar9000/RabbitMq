package com.example.ticketing_queues.service;


import com.example.ticketing_queues.config.RabbitMqProperties;
import com.example.ticketing_queues.dto.TicketDto;
import com.example.ticketing_queues.entity.TicketCreation;
import com.example.ticketing_queues.producer.RabbitMqProducerService;
import com.example.ticketing_queues.repository.TicketRepository;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.Channel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RabbitMqProducerService rabbitMqProducerService;

    @Autowired
    private RabbitMqProperties properties;

    @Autowired
    private ConnectionFactory connection;

    @Transactional
    public TicketDto saveOrUpdateTicket(String id, TicketDto ticketDto) {
        // Check if required fields are null and handle them
        if (ticketDto.getTicketType() == null || ticketDto.getSiteName() == null) {
            throw new RuntimeException("TicketType and SiteName cannot be null.");
        }

        TicketCreation ticket;
        boolean isNew = id == null;

        if (isNew) {
            // Generate a new UUID for the ticket
            id = UUID.randomUUID().toString();
            ticket = new TicketCreation();
            ticket.setTicketId(id);
            ticket.setCreatedTime(LocalDateTime.now());
            if (ticketDto.getStatus() == null || ticketDto.getStatus().isEmpty()) {
                ticket.setStatus("new");
            }
        } else {
            // Find the existing ticket by ID
            ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            // Preserve the original created time
            ticket.setCreatedTime(ticket.getCreatedTime());
        }

        // Update ticket fields
        if (ticketDto.getTicketTitle() != null) {
            ticket.setTicketTitle(ticketDto.getTicketTitle());
        }
        if (ticketDto.getPriority() != null) {
            ticket.setPriority(ticketDto.getPriority());
        }
        if (ticketDto.getStatus() != null) {
            ticket.setStatus(ticketDto.getStatus());
        }
        if (ticketDto.getDescription() != null) {
            ticket.setDescription(ticketDto.getDescription());
        }

        // Set the ticketType and siteName
        ticket.setTicketType(ticketDto.getTicketType());
        ticket.setSiteName(ticketDto.getSiteName());
        ticket.setStatus("new");

        // Determine the queue based on the updated title and status
        String queueName = determineQueue(ticket.getTicketTitle(), ticket.getStatus());
        ticket.setQueue(queueName);

        // Save the entity
        TicketCreation savedTicket = ticketRepository.save(ticket);

        // Convert entity back to DTO
        TicketDto savedTicketDto = modelMapper.map(savedTicket, TicketDto.class);

        // Send the ticket to RabbitMQ
        new Thread(() -> {
            try {
                String routingKey = determineRoutingKey(queueName);
                rabbitMqProducerService.sendMessageToQueue(savedTicket, queueName, routingKey);
            } catch (Exception e) {
                e.printStackTrace(); // Log the error or handle it appropriately
            }
        }).start();

        return savedTicketDto;
    }

    public void updateTicketStatus(String ticketId, String newStatus) throws IOException, TimeoutException {
        Optional<TicketCreation> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isPresent()) {
            TicketCreation ticket = ticketOptional.get();
            try {
                rabbitMqProducerService.purgeQueue(ticket.getQueue());
            } catch (IOException | TimeoutException e) {
                // Handle the exception (e.g., log it or rethrow it)
                throw new RuntimeException("Failed to purge old queue", e);
            }
            ticket.setStatus(newStatus);
            ticket.setUpdatedTime(LocalDateTime.now());

            String queueName = determineQueue(ticket.getTicketTitle(),newStatus);
            String routingKey = determineRoutingKey(queueName);
            ticket.setQueue(queueName);
            ticketRepository.save(ticket);

            // Send the updated message to the new queue
            rabbitMqProducerService.sendMessageToQueue(ticket, queueName, routingKey);
        } else {
            throw new IllegalArgumentException("Ticket with ID " + ticketId + " not found.");
        }
    }

    public List<TicketCreation> getAllTicket(){
        List<TicketCreation> getAllData=ticketRepository.findAll();

        return getAllData;
    }

    public TicketDto getTicketById(String id) {
        // Find the ticket by ID
        TicketCreation ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Convert entity to DTO and return
        return modelMapper.map(ticket, TicketDto.class);
    }

    public void deleteTicket(String id) {
        // Find the ticket by ID
        TicketCreation ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Delete the ticket
        ticketRepository.delete(ticket);
    }

    public void purgeAndSyncMessagesToQueue() {
        try {
            // Purge the queues
            rabbitMqProducerService.purgeQueue("CRM");
            rabbitMqProducerService.purgeQueue("HELPDESK");
            rabbitMqProducerService.purgeQueue("FIELDMANAGER");
            rabbitMqProducerService.purgeQueue("FIELDREPRESENTATIVE");
            rabbitMqProducerService.purgeQueue("DEFAULT");
            rabbitMqProducerService.purgeQueue("TICKETANALYSIS");
            rabbitMqProducerService.purgeQueue("SITEMANAGER");

            // Sync messages from DB to RabbitMQ
            syncMessagesToQueue();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to purge queues", e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void syncMessagesToQueue() {
        List<TicketCreation> tickets = ticketRepository.findAll();

        for (TicketCreation ticket : tickets) {
            String queueName = determineQueue(ticket.getTicketTitle(),ticket.getStatus());
            String routingKey = determineRoutingKey(queueName);
            // Send message to RabbitMQ
            rabbitMqProducerService.sendMessageToQueue(ticket, queueName,routingKey);
        }
    }
    private String determineQueue(String title, String status) {
        switch (title) {
            case "Site Installation":
                return switch (status) {
                    case "new", "closed" -> "SITEMANAGER";
                    case "open", "fixed" -> "CRM";
                    default -> "DEFAULT";
                };
            case "Preventive Maintenance":
            case "Site Down":
            case "Site Relocation":
            case "Site Renovation":
                return switch (status) {
                    case "new", "open", "fixed", "closed" -> "FIELDMANAGER";
                    default -> "DEFAULT";
                };
            case "Two way audio not working/audio issue":
                return switch (status) {
                    case "new", "open", "closed" -> "FIELDMANAGER";
                    case "fixed" -> "FIELDMANAGER";  // If it should be "FIELD MANAGER/HELPDESK", decide which queue is primary
                    default -> "DEFAULT";
                };
            case "Camera Disconnected":
                return switch (status) {
                    case "closed" -> "FIELDREPRESENTATIVE";
                    case "new" -> "HELPDESK";
                    case "open" -> "FIELDMANAGER";
                    default -> "DEFAULT";
                };
            case "Camera Position Changed":
                return switch (status) {
                    case "closed" -> "FIELDREPRESENTATIVE";
                    case "fixed", "new" -> "FIELDMANAGER";
                    case "open" -> "HELPDESK";
                    default -> "DEFAULT";
                };
            case "Client Request":
                return switch (status) {
                    case "closed", "open" -> "HELPDESK";
                    case "new" -> "FIELDMANAGER";
                    default -> "DEFAULT";
                };
            case "Hard Disk Problem":
                return switch (status) {
                    case "Closed" -> "FIELDREPRESENTATIVE";
                    case "new" -> "HELPDESK";
                    default -> "DEFAULT";
                };
            case "HDD Not Recording":
                if (status.equals("closed")) {
                    return "HELPDESK";
                }
                return "DEFAULT";
            default:
                return "DEFAULT";
        }
    }

    private String determineRoutingKey(String queueName) {
        return switch (queueName) {
            case "CRM" -> properties.getRoutingKey().getCrm();
            case "HELPDESK" -> properties.getRoutingKey().getHelpdesk();
            case "FIELDMANAGER" -> properties.getRoutingKey().getFieldmanager();
            case "FIELDREPRESENTATIVE" -> properties.getRoutingKey().getFieldrepresentative();
            case "TICKETANALYSIS" -> properties.getRoutingKey().getTicketanalysis();
            case "SITEMANAGER" -> properties.getRoutingKey().getSitemanager();
            default -> properties.getRoutingKey().getDefaultRoutingKey();
        };

    }
}
