package com.elioneto.pixapi.repository;

import com.elioneto.pixapi.model.PixPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PixPaymentRepository extends JpaRepository<PixPayment, UUID> {
}
