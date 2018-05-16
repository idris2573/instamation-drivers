package com.instamation.drivers.repository;

import com.instamation.drivers.model.Ticket;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TicketRepository extends PagingAndSortingRepository<Ticket, Long> {
    List<Ticket> findAll();
}
