package com.talentplatform.brand.scheduler;

import com.talentplatform.brand.enums.HireRequestStatus;
import com.talentplatform.brand.model.HireRequest;
import com.talentplatform.brand.model.Notification;
import com.talentplatform.brand.repository.HireRequestRepository;
import com.talentplatform.brand.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class RequestExpiryScheduler {
    private final HireRequestRepository requests;
    private final NotificationRepository notifications;
    public RequestExpiryScheduler(HireRequestRepository requests, NotificationRepository notifications) {
        this.requests = requests; this.notifications = notifications;
    }
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void withdrawExpiredRequests() {
        LocalDateTime now = LocalDateTime.now();
        for (HireRequest request : requests.findByStatusAndSentAtBetween(HireRequestStatus.PENDING_RESPONSE,
                now.minusDays(15), now.minusDays(14))) {
            String message = "Hiring request " + request.getId() + " expires within 24 hours.";
            if (!notifications.existsByBrandIdAndTypeAndMessage(request.getBrandId(), "DAY_14_WARNING", message)) {
                Notification warning = new Notification(); warning.setBrandId(request.getBrandId());
                warning.setType("DAY_14_WARNING"); warning.setMessage(message); notifications.save(warning);
            }
        }
        for (HireRequest request : requests.findByStatusAndSentAtBefore(HireRequestStatus.PENDING_RESPONSE, LocalDateTime.now().minusDays(15))) {
            request.setStatus(HireRequestStatus.AUTO_WITHDRAWN);
            requests.save(request);
            Notification notification = new Notification();
            notification.setBrandId(request.getBrandId());
            notification.setType("AUTO_WITHDRAWN");
            notification.setMessage("A hiring request expired after 15 days without a response.");
            notifications.save(notification);
        }
    }
}
