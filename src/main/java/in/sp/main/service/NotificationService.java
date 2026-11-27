package in.sp.main.service;

import in.sp.main.entity.CompanyNotification;
import in.sp.main.repository.CompanyNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Persists in-app notifications and optionally sends SMTP mail when configured.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final CompanyNotificationRepository notificationRepo;
    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public NotificationService(CompanyNotificationRepository notificationRepo,
                               @Autowired(required = false) JavaMailSender mailSender) {
        this.notificationRepo = notificationRepo;
        this.mailSender = mailSender;
    }

    public void notifyCompany(Long companyId, String recipientEmail, String title, String message) {
        CompanyNotification n = new CompanyNotification();
        n.setCompanyId(companyId);
        n.setTitle(title);
        n.setMessage(message);
        n.setReadFlag(false);
        notificationRepo.save(n);

        if (mailEnabled && mailSender != null && recipientEmail != null && !recipientEmail.isBlank()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(recipientEmail);
                msg.setSubject("Entertainment World - " + title);
                msg.setText(message);
                mailSender.send(msg);
            } catch (Exception ex) {
                log.warn("Optional email dispatch skipped for {}: {}", recipientEmail, ex.getMessage());
            }
        }
    }
}
