package com.example.demo.repository;

import com.example.demo.model.BookingStatus;
import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorBooking;
import com.example.demo.model.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface VendorBookingRepository extends JpaRepository<VendorBooking, Long> {

    List<VendorBooking> findByVendorOrderByIdDesc(UserAccount vendor);

    List<VendorBooking> findByRequesterOrderByIdDesc(UserAccount requester);

    List<VendorBooking> findByServiceAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            VendorService service,
            Collection<BookingStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );

    boolean existsByRequesterAndServiceAndStartDateAndEndDateAndStatus(
            UserAccount requester,
            VendorService service,
            LocalDate startDate,
            LocalDate endDate,
            BookingStatus status
    );

    long countByVendor(UserAccount vendor);

    long countByVendorAndStatus(UserAccount vendor, BookingStatus status);
}
