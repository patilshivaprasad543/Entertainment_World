package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Direct message or notice sent by a Casting Company to an artist or candidate.
 */
@Entity
@Table(name = "communication_messages")
public class CommunicationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long artistId;

    private Long projectId;

    @Column(nullable = false, length = 40)
    private String channel = "PORTAL_MESSAGE"; // PORTAL_MESSAGE, AUDITION_INVITE, SELECTION_NOTICE, REJECTION_NOTICE

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(length = 140)
    private String senderName;

    @Column(nullable = false)
    private boolean readByRecipient = false;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public boolean isReadByRecipient() { return readByRecipient; }
    public void setReadByRecipient(boolean readByRecipient) { this.readByRecipient = readByRecipient; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
