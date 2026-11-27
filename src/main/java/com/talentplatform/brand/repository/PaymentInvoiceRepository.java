package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.PaymentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, Long> { List<PaymentInvoice> findByBrandIdOrderByCreatedAtDesc(Long brandId); }
