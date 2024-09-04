package com.example.ticketing_queues.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TicketCreation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String ticketId; // UUID is represented as String
    private String ticketType;
    private String siteName;
    private String ticketTitle;
    private String priority;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String queue;
    private String description;
}
