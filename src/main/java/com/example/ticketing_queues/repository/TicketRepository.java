package com.example.ticketing_queues.repository;

import com.example.ticketing_queues.entity.TicketCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<TicketCreation, String> {
}
