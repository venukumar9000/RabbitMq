package com.example.ticketing_queues.controller;

import com.example.ticketing_queues.dto.TicketDto;
import com.example.ticketing_queues.entity.TicketCreation;
import com.example.ticketing_queues.service.RabbitMqConsumerService;
import com.example.ticketing_queues.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private RabbitMqConsumerService rabbitMqConsumerService;

    @PostMapping("/create")
    public ResponseEntity<TicketDto> createTicket(@RequestBody TicketDto ticketDto) {
        TicketDto createdTicket = ticketService.saveOrUpdateTicket(null,ticketDto);
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @GetMapping("/allTickets")
    public ResponseEntity<?> getAllCreate(){
        List<TicketCreation> getAllData=ticketService.getAllTicket();

        return new ResponseEntity<>(getAllData,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicket(@PathVariable String id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }


    @GetMapping("/hi")
    public String hello(){
        return "Hello world";
    }


    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable String id, @RequestBody TicketDto ticketDto) {
        TicketDto updatedTicket = ticketService.saveOrUpdateTicket(id, ticketDto);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }

    @PutMapping("/updateStatus/{ticketId}")
    private ResponseEntity<String> updateTicketStatus(@PathVariable String ticketId, @RequestParam String status) throws IOException, TimeoutException {
        ticketService.updateTicketStatus(ticketId, status);
        return new ResponseEntity<>("Ticket status updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        ticketService.deleteTicket(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/syncMessages")
    public ResponseEntity<String> syncMessages() {
        try {
            ticketService.purgeAndSyncMessagesToQueue();
            return new ResponseEntity<>("Messages purged and synced successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to purge and sync messages", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{queueName}/consume")
    public ResponseEntity<List<TicketCreation>> consumeMessages(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "1") int count) {
        List<TicketCreation> tickets = rabbitMqConsumerService.consumeMessagesFromQueue(queueName, count);
        return ResponseEntity.ok(tickets);
    }
}
