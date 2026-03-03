package com.elioneto.pixapi.repository;

import com.elioneto.pixapi.model.PixPayment;
import com.elioneto.pixapi.model.PixStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PixPaymentRepository extends JpaRepository<PixPayment, UUID> {

    List<PixPayment> findByStatusOrderByCreatedAtDesc(PixStatus status);

    List<PixPayment> findAllByOrderByCreatedAtDesc();
}
