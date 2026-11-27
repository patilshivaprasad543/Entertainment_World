package in.sp.main.entity;

import jakarta.persistence.*;

/**
 * Represents the 3 Subscription Passes available to approved Casting Companies.
 */
@Entity
@Table(name = "subscription_passes")
public class SubscriptionPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String passCode;

    @Column(nullable = false, length = 120)
    private String passName;

    @Column(length = 220)
    private String subtitle;

    @Column(length = 120)
    private String badgeText;

    @Column(nullable = false)
    private int basePriceInr;

    @Column(nullable = false)
    private int defaultMonths;

    @Column(nullable = false)
    private int contactViewsLimit;

    @Column(nullable = false, length = 120)
    private String contactViewsLabel;

    @Column(nullable = false)
    private int portfolioViewsLimit; // -1 = Unlimited

    @Column(nullable = false, length = 120)
    private String portfolioViewsLabel;

    @Column(nullable = false)
    private int inviteAuditionLimit;

    @Column(nullable = false)
    private boolean zeroCommission = true;

    @Column(nullable = false)
    private boolean organiseFolders = true;

    @Column(nullable = false)
    private boolean socialShare = true;

    @Column(nullable = false)
    private int projectPromotionQuota;

    @Column(nullable = false, length = 80)
    private String projectPromotionLabel;

    @Column(nullable = false, length = 80)
    private String callbackSupport;

    @Column(nullable = false)
    private int displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }

    public String getPassName() { return passName; }
    public void setPassName(String passName) { this.passName = passName; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public int getBasePriceInr() { return basePriceInr; }
    public void setBasePriceInr(int basePriceInr) { this.basePriceInr = basePriceInr; }

    public int getDefaultMonths() { return defaultMonths; }
    public void setDefaultMonths(int defaultMonths) { this.defaultMonths = defaultMonths; }

    public int getContactViewsLimit() { return contactViewsLimit; }
    public void setContactViewsLimit(int contactViewsLimit) { this.contactViewsLimit = contactViewsLimit; }

    public String getContactViewsLabel() { return contactViewsLabel; }
    public void setContactViewsLabel(String contactViewsLabel) { this.contactViewsLabel = contactViewsLabel; }

    public int getPortfolioViewsLimit() { return portfolioViewsLimit; }
    public void setPortfolioViewsLimit(int portfolioViewsLimit) { this.portfolioViewsLimit = portfolioViewsLimit; }

    public String getPortfolioViewsLabel() { return portfolioViewsLabel; }
    public void setPortfolioViewsLabel(String portfolioViewsLabel) { this.portfolioViewsLabel = portfolioViewsLabel; }

    public int getInviteAuditionLimit() { return inviteAuditionLimit; }
    public void setInviteAuditionLimit(int inviteAuditionLimit) { this.inviteAuditionLimit = inviteAuditionLimit; }

    public boolean isZeroCommission() { return zeroCommission; }
    public void setZeroCommission(boolean zeroCommission) { this.zeroCommission = zeroCommission; }

    public boolean isOrganiseFolders() { return organiseFolders; }
    public void setOrganiseFolders(boolean organiseFolders) { this.organiseFolders = organiseFolders; }

    public boolean isSocialShare() { return socialShare; }
    public void setSocialShare(boolean socialShare) { this.socialShare = socialShare; }

    public int getProjectPromotionQuota() { return projectPromotionQuota; }
    public void setProjectPromotionQuota(int projectPromotionQuota) { this.projectPromotionQuota = projectPromotionQuota; }

    public String getProjectPromotionLabel() { return projectPromotionLabel; }
    public void setProjectPromotionLabel(String projectPromotionLabel) { this.projectPromotionLabel = projectPromotionLabel; }

    public String getCallbackSupport() { return callbackSupport; }
    public void setCallbackSupport(String callbackSupport) { this.callbackSupport = callbackSupport; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
