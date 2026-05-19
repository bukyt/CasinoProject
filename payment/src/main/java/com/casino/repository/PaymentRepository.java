package com.casino.repository;

import com.casino.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPlayerProfileId(Long playerProfileId);

}