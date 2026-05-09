package com.casino.repository;

import com.casino.model.Payment;
import com.casino.model.PaymentStatus;
import com.casino.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPlayerProfileId(Long playerProfileId);

    List<Payment> findByPlayerProfileIdOrderByCreatedDateDesc(Long playerProfileId);

    List<Payment> findByPlayerProfileIdAndType(Long playerProfileId, PaymentType type);

    List<Payment> findByPlayerProfileIdAndStatus(Long playerProfileId, PaymentStatus status);

    List<Payment> findByProvider(String provider);
}