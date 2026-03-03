package com.elioneto.pixapi.repository;

import com.elioneto.pixapi.model.PixPaymentStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PixPaymentStatusLogRepository extends JpaRepository<PixPaymentStatusLog, UUID> {

    List<PixPaymentStatusLog> findByPaymentIdOrderByChangedAtAsc(UUID paymentId);
}
