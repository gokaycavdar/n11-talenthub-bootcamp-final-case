package com.gokaycavdar.paymentservice.repository;

import com.gokaycavdar.paymentservice.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    Optional<PaymentAttempt> findByConversationId(String conversationId);
}
