package com.example.ticketing_queues.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private String ticketId;
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
