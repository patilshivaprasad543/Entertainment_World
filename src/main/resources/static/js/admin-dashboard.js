(() => {
    const ADMIN_STORAGE_KEY = 'ew_admin_dashboard_state_v1';
    const PRO_STORAGE_KEY = 'ew_professional_dashboard_state_v1';

    const listEl = document.getElementById('verification-list');
    const refreshBtn = document.getElementById('refresh-verifications');

    const DEFAULT_ADMIN_STATE = {
        users: [
            { id: 'u-1', name: 'Alex Rivera', email: 'actor@entertainmentworld.com', role: 'ACTOR', active: true },
            { id: 'u-2', name: 'Apex Casting Studio', email: 'casting@entertainmentworld.com', role: 'CASTING', active: true },
            { id: 'u-3', name: 'Prime Stage Vendors', email: 'vendor@entertainmentworld.com', role: 'VENDOR', active: true },
            { id: 'u-4', name: 'Nova Brand House', email: 'brand@entertainmentworld.com', role: 'BRAND', active: true },
            { id: 'u-5', name: 'Starlight Events', email: 'events@entertainmentworld.com', role: 'EVENT_MANAGEMENT', active: true }
        ],
        portalPlans: [
            {
                id: 'pp-actors',
                portal: 'Professionals (Actors & Talent)',
                tiers: 'Basic Free • Premium ₹199 • Pro ₹399',
                subscribers: 912,
                paidSubscribers: 500,
                monthlyRevenue: 142700,
                badge: 'Top Volume',
                badgeClass: 'shortlisted'
            },
            {
                id: 'pp-casting',
                portal: 'Casting Productions',
                tiers: 'Starter Free • Studio Pro ₹1,499 • Enterprise ₹3,999',
                subscribers: 48,
                paidSubscribers: 42,
                monthlyRevenue: 98400,
                badge: 'High ARPU',
                badgeClass: 'review'
            },
            {
                id: 'pp-brands',
                portal: 'Brand Promotions',
                tiers: 'Brand Growth ₹2,499 • Enterprise ₹5,999',
                subscribers: 26,
                paidSubscribers: 26,
                monthlyRevenue: 89200,
                badge: '100% Paid',
                badgeClass: 'shortlisted'
            },
            {
                id: 'pp-events',
                portal: 'Event Management',
                tiers: 'Standard ₹999 • Premier ₹1,999 • Festival Pro ₹4,999',
                subscribers: 34,
                paidSubscribers: 29,
                monthlyRevenue: 76500,
                badge: 'Active',
                badgeClass: 'audition'
            },
            {
                id: 'pp-vendors',
                portal: 'Vendors Management',
                tiers: 'Supplier Basic • Verified ₹999 • Fleet Pro ₹2,499',
                subscribers: 62,
                paidSubscribers: 44,
                monthlyRevenue: 68400,
                badge: 'Growing',
                badgeClass: 'review'
            }
        ],
        payments: [
            { id: 'pay-1', subscriberName: 'Aarav Mehta', email: 'aarav.mehta@talent.in', portal: 'Professionals', plan: 'Pro Elite (₹399)', planTier: 'Pro', amount: 399, date: '27 Nov 2026', method: 'UPI', invoice: '#INV-9028', status: 'Paid', badge: 'shortlisted' },
            { id: 'pay-2', subscriberName: 'Apex Casting Studio', email: 'casting@entertainmentworld.com', portal: 'Casting', plan: 'Enterprise (₹3,999)', planTier: 'Pro', amount: 3999, date: '26 Nov 2026', method: 'Corporate Card', invoice: '#INV-9027', status: 'Settled', badge: 'shortlisted' },
            { id: 'pay-3', subscriberName: 'Nova Brand House', email: 'brand@entertainmentworld.com', portal: 'Brands', plan: 'Enterprise (₹5,999)', planTier: 'Pro', amount: 5999, date: '25 Nov 2026', method: 'NetBanking', invoice: '#INV-9026', status: 'Settled', badge: 'shortlisted' },
            { id: 'pay-4', subscriberName: 'Starlight Events', email: 'events@entertainmentworld.com', portal: 'Events', plan: 'Festival Pro (₹4,999)', planTier: 'Pro', amount: 4999, date: '24 Nov 2026', method: 'NEFT', invoice: '#INV-9025', status: 'Pending', badge: 'review' },
            { id: 'pay-5', subscriberName: 'Prime Stage Vendors', email: 'vendor@entertainmentworld.com', portal: 'Vendors', plan: 'Fleet Pro (₹2,499)', planTier: 'Pro', amount: 2499, date: '23 Nov 2026', method: 'UPI', invoice: '#INV-9024', status: 'Settled', badge: 'shortlisted' },
            { id: 'pay-6', subscriberName: 'Diya Sharma', email: 'diya.sharma@talent.in', portal: 'Professionals', plan: 'Premium (₹199)', planTier: 'Premium', amount: 199, date: '22 Nov 2026', method: 'Card', invoice: '#INV-9023', status: 'Paid', badge: 'shortlisted' },
            { id: 'pay-7', subscriberName: 'RedFrame Originals', email: 'studio@redframe.in', portal: 'Casting', plan: 'Studio Pro (₹1,499)', planTier: 'Premium', amount: 1499, date: '21 Nov 2026', method: 'UPI', invoice: '#INV-9022', status: 'Settled', badge: 'shortlisted' },
            { id: 'pay-8', subscriberName: 'Veloce Audio Campaigns', email: 'ads@veloceaudio.in', portal: 'Brands', plan: 'Brand Growth (₹2,499)', planTier: 'Premium', amount: 2499, date: '20 Nov 2026', method: 'Card', invoice: '#INV-9021', status: 'Pending', badge: 'review' },
            { id: 'pay-9', subscriberName: 'CineLight Rentals', email: 'ops@cinelight.in', portal: 'Vendors', plan: 'Verified Vendor (₹999)', planTier: 'Standard', amount: 999, date: '19 Nov 2026', method: 'UPI', invoice: '#INV-9020', status: 'Failed', badge: 'closed' },
            { id: 'pay-10', subscriberName: 'Encore Live Shows', email: 'billing@encorelive.in', portal: 'Events', plan: 'Event Standard (₹999)', planTier: 'Standard', amount: 999, date: '18 Nov 2026', method: 'Card', invoice: '#INV-9019', status: 'Paid', badge: 'shortlisted' }
        ],
        broadcasts: [
            { id: 'bc-1', title: 'New 3-Tier Professional Plans Live', audience: 'Professionals (Actors)', message: 'Basic Free, Premium ₹199, and Pro ₹399 plans are now active.', time: 'Yesterday' }
        ],
        complaints: [
            { id: 'CMP-1009', subject: 'Brand campaign brief missing shoot dates', category: 'Casting / Opportunity Dispute', priority: 'High', status: 'Open', badgeClass: 'audition', createdAt: '1 day ago' }
        ],
        subscriptionPlans: [
            { id: 'sp-pro-1', portal: 'Professionals', name: 'Basic Free', price: 0, cycle: 'Monthly', status: 'Active', badge: 'Starter', quota: '5 applications / mo • Standard profile', features: ['Public talent profile', 'Browse open casting calls', 'Up to 5 applications per month'] },
            { id: 'sp-pro-2', portal: 'Professionals', name: 'Premium', price: 199, cycle: 'Monthly', status: 'Active', badge: 'Popular', quota: '25 applications / mo • Video reel upload', features: ['25 monthly applications', '3 portfolio video uploads', 'Shortlist & audition alerts'] },
            { id: 'sp-pro-3', portal: 'Professionals', name: 'Pro', price: 399, cycle: 'Monthly', status: 'Active', badge: 'Best Value', quota: 'Unlimited applications • Priority boost', features: ['Unlimited casting applications', 'Priority search placement', 'Verified talent badge & analytics'] },
            { id: 'sp-cast-1', portal: 'Casting', name: 'Starter Free', price: 0, cycle: 'Monthly', status: 'Active', badge: 'Free', quota: '1 active project • 10 contact views', features: ['1 active casting project', 'Up to 10 artist contact views', 'Basic applicant filtering'] },
            { id: 'sp-cast-2', portal: 'Casting', name: 'Studio Pro', price: 1499, cycle: 'Monthly', status: 'Active', badge: 'Popular', quota: '10 projects • 250 contact views • 50 invites', features: ['Up to 10 active projects', '250 artist contact views', '50 direct audition invites'] },
            { id: 'sp-cast-3', portal: 'Casting', name: 'Enterprise', price: 3999, cycle: 'Monthly', status: 'Active', badge: 'Enterprise', quota: 'Unlimited projects • Unlimited portfolio views', features: ['Unlimited casting projects', 'Unlimited portfolio & contact views', 'Dedicated studio verification badge'] },
            { id: 'sp-brand-1', portal: 'Brands', name: 'Brand Growth', price: 2499, cycle: 'Monthly', status: 'Active', badge: 'Growth', quota: '5 campaigns • 100 creator invites', features: ['5 active brand campaigns', '100 creator collaboration invites', 'Campaign performance summary'] },
            { id: 'sp-brand-2', portal: 'Brands', name: 'Enterprise', price: 5999, cycle: 'Monthly', status: 'Active', badge: 'Enterprise', quota: 'Unlimited campaigns • Priority talent match', features: ['Unlimited brand campaigns', 'Priority verified talent matching', 'Dedicated account manager'] },
            { id: 'sp-event-1', portal: 'Events', name: 'Standard', price: 999, cycle: 'Monthly', status: 'Active', badge: 'Standard', quota: '3 events • 50 crew bookings', features: ['Up to 3 active events', '50 crew & talent bookings', 'Standard vendor directory access'] },
            { id: 'sp-event-2', portal: 'Events', name: 'Premier', price: 1999, cycle: 'Monthly', status: 'Active', badge: 'Popular', quota: '10 events • 200 crew bookings', features: ['Up to 10 active events', '200 crew & talent bookings', 'Priority event promotion'] },
            { id: 'sp-event-3', portal: 'Events', name: 'Festival Pro', price: 4999, cycle: 'Monthly', status: 'Active', badge: 'Pro', quota: 'Unlimited events • Full production suite', features: ['Unlimited live events', 'Unlimited crew & vendor bookings', 'Multi-city stage scheduling'] },
            { id: 'sp-vend-1', portal: 'Vendors', name: 'Supplier Basic', price: 0, cycle: 'Monthly', status: 'Active', badge: 'Free', quota: '5 equipment listings • Standard profile', features: ['Up to 5 equipment listings', 'Basic quote requests', 'Standard directory listing'] },
            { id: 'sp-vend-2', portal: 'Vendors', name: 'Verified', price: 999, cycle: 'Monthly', status: 'Active', badge: 'Verified', quota: '25 listings • Verified vendor badge', features: ['25 equipment & service listings', 'Verified vendor trust badge', 'Direct production inquiries'] },
            { id: 'sp-vend-3', portal: 'Vendors', name: 'Fleet Pro', price: 2499, cycle: 'Monthly', status: 'Active', badge: 'Pro', quota: 'Unlimited listings • Priority dispatch leads', features: ['Unlimited catalog listings', 'Priority event & shoot RFQs', 'Invoice & settlement tracking'] }
        ]
    };

    function loadAdminState() {
        try {
            const raw = localStorage.getItem(ADMIN_STORAGE_KEY);
            if (!raw) return structuredClone(DEFAULT_ADMIN_STATE);
            const parsed = JSON.parse(raw);
            const hasRichPayments = Array.isArray(parsed.payments) && parsed.payments.length && parsed.payments[0].subscriberName;
            return {
                ...structuredClone(DEFAULT_ADMIN_STATE),
                ...parsed,
                portalPlans: Array.isArray(parsed.portalPlans) && parsed.portalPlans.length
                    ? parsed.portalPlans
                    : structuredClone(DEFAULT_ADMIN_STATE.portalPlans),
                payments: hasRichPayments ? parsed.payments : structuredClone(DEFAULT_ADMIN_STATE.payments),
                subscriptionPlans: Array.isArray(parsed.subscriptionPlans) && parsed.subscriptionPlans.length
                    ? parsed.subscriptionPlans
                    : structuredClone(DEFAULT_ADMIN_STATE.subscriptionPlans)
            };
        } catch {
            return structuredClone(DEFAULT_ADMIN_STATE);
        }
    }

    let adminState = loadAdminState();
    let cachedVerifications = [];
    let verifFilter = 'ALL';
    let subPortalFilter = 'ALL';
    let toastTimer = null;

    function saveAdminState() {
        try {
            localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(adminState));
        } catch {
            // Ignore quota errors
        }
    }

    function showToast(message) {
        const toast = document.getElementById('ew-toast');
        if (!toast) return;
        toast.textContent = message;
        toast.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.hidden = true;
        }, 2600);
    }

    // Navigation & Shell
    const sidebar = document.getElementById('ew-sidebar');
    const backdrop = document.getElementById('ew-sidebar-backdrop');
    const menuToggle = document.getElementById('ew-menu-toggle');
    const userMenuBtn = document.getElementById('user-menu-btn');
    const userMenuPopover = document.getElementById('user-menu-popover');

    function closeMobileSidebar() {
        sidebar?.classList.remove('open');
        backdrop?.classList.remove('open');
    }

    function switchView(viewName) {
        document.querySelectorAll('.ew-nav-item[data-view]').forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.view === viewName);
        });
        document.querySelectorAll('.ew-view[data-view-panel]').forEach((panel) => {
            panel.classList.toggle('active', panel.dataset.viewPanel === viewName);
        });
        closeMobileSidebar();
        if (userMenuPopover) userMenuPopover.hidden = true;
    }

    menuToggle?.addEventListener('click', () => {
        sidebar?.classList.toggle('open');
        backdrop?.classList.toggle('open');
    });
    backdrop?.addEventListener('click', closeMobileSidebar);

    userMenuBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        if (userMenuPopover) userMenuPopover.hidden = !userMenuPopover.hidden;
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.ew-topbar-right') && userMenuPopover) {
            userMenuPopover.hidden = true;
        }
    });

    // Live Document Verification Queue
    async function loadVerifications() {
        if (!listEl) return;
        listEl.innerHTML = '<p class="ew-app-meta">Loading verification requests...</p>';
        try {
            const response = await fetch('/api/admin/verifications');
            cachedVerifications = await response.json();
            renderVerifications();
        } catch {
            listEl.innerHTML = '<p class="ew-app-meta">Failed to load verification requests.</p>';
        }
    }

    function renderVerifications() {
        if (!listEl) return;
        const pendingCount = cachedVerifications.filter((i) => i.verificationStatus === 'PENDING').length;
        const approvedCount = cachedVerifications.filter((i) => i.verificationStatus === 'APPROVED').length;

        const pendingKpi = document.getElementById('admin-kpi-pending');
        const approvedKpi = document.getElementById('admin-kpi-approved');
        const pendingBadge = document.getElementById('pending-verif-badge');
        if (pendingKpi) pendingKpi.textContent = String(pendingCount);
        if (approvedKpi) approvedKpi.textContent = String(approvedCount);
        if (pendingBadge) pendingBadge.textContent = String(pendingCount);

        const filtered = verifFilter === 'ALL'
            ? cachedVerifications
            : cachedVerifications.filter((i) => i.verificationStatus === verifFilter);

        if (!filtered.length) {
            listEl.innerHTML = '<div class="ew-panel"><p>No verification requests match this filter.</p></div>';
            return;
        }

        listEl.innerHTML = '';
        filtered.forEach((item) => {
            const row = document.createElement('div');
            row.className = 'verification-item ew-card-clickable';
            row.title = 'Click to view all registered & uploaded details';

            const meta = document.createElement('div');
            meta.className = 'verification-meta';
            meta.innerHTML = `
                <h3>${escapeHtml(item.organizationName || item.fullName)} <span class="module-tag">${escapeHtml(item.roleDisplayName)}</span></h3>
                <p>Contact: ${escapeHtml(item.fullName)} (${escapeHtml(item.email)})</p>
                <p>Document: ${escapeHtml(item.documentType || 'N/A')} &bull; No: ${escapeHtml(item.documentNumber || 'N/A')}</p>
            `;

            row.addEventListener('click', (e) => {
                if (e.target.closest('.verification-actions')) return;
                openVerificationModal(item);
            });

            const actions = document.createElement('div');
            actions.className = 'verification-actions';

            const badge = document.createElement('span');
            badge.className = `status-pill ${item.verificationStatus}`;
            badge.textContent = item.verificationStatus;
            actions.appendChild(badge);

            if (item.documentFileName) {
                const docLink = document.createElement('a');
                docLink.className = 'action-btn btn-doc';
                docLink.href = `/api/admin/verifications/${item.id}/document`;
                docLink.target = '_blank';
                docLink.rel = 'noopener';
                docLink.textContent = 'View Document';
                actions.appendChild(docLink);
            }

            if (item.verificationStatus !== 'APPROVED') {
                const approveBtn = document.createElement('button');
                approveBtn.type = 'button';
                approveBtn.className = 'action-btn btn-approve';
                approveBtn.textContent = 'Approve';
                approveBtn.addEventListener('click', () => updateDecision(item.id, 'approve'));
                actions.appendChild(approveBtn);
            }

            if (item.verificationStatus !== 'REJECTED') {
                const rejectBtn = document.createElement('button');
                rejectBtn.type = 'button';
                rejectBtn.className = 'action-btn btn-reject';
                rejectBtn.textContent = 'Reject';
                rejectBtn.addEventListener('click', () => updateDecision(item.id, 'reject'));
                actions.appendChild(rejectBtn);
            }

            row.appendChild(meta);
            row.appendChild(actions);
            listEl.appendChild(row);
        });
    }

    async function updateDecision(id, action) {
        await fetch(`/api/admin/verifications/${id}/${action}`, { method: 'POST' });
        showToast(`Verification ${action === 'approve' ? 'approved' : 'rejected'}`);
        closeVerificationModal();
        await loadVerifications();
    }

    const verifModal = document.getElementById('admin-verif-modal');
    const verifModalContent = document.getElementById('admin-verif-modal-content');
    const verifModalCloseBtn = document.getElementById('admin-verif-modal-close');

    function closeVerificationModal() {
        if (verifModal) verifModal.hidden = true;
        if (verifModalContent) verifModalContent.innerHTML = '';
    }

    function openVerificationModal(item) {
        if (!verifModal || !verifModalContent) return;

        const docUrl = item.documentFileName ? `/api/admin/verifications/${encodeURIComponent(item.id)}/document` : '';
        const fileNameLower = (item.documentFileName || '').toLowerCase();
        const isImage = /\.(png|jpe?g|webp|gif)$/i.test(fileNameLower);
        const isPdf = /\.pdf$/i.test(fileNameLower);

        const statusBadgeMap = {
            APPROVED: 'shortlisted',
            PENDING: 'review',
            REJECTED: 'closed'
        };
        const statusBadge = statusBadgeMap[item.verificationStatus] || 'review';

        const detailRows = [
            { label: 'Organization / Studio', value: item.organizationName || '—' },
            { label: 'Contact Full Name', value: item.fullName || '—' },
            { label: 'Registered Email', value: item.email || '—' },
            { label: 'Portal / Role', value: item.roleDisplayName || item.role || '—' },
            { label: 'Document Type', value: item.documentType || '—' },
            { label: 'Document Number', value: item.documentNumber || '—' },
            { label: 'Uploaded File Name', value: item.documentFileName || 'No file uploaded' }
        ];

        if (item.phone) detailRows.push({ label: 'Phone', value: item.phone });
        if (item.city) detailRows.push({ label: 'City', value: item.city });
        if (item.portfolioOrBio) detailRows.push({ label: 'Bio / Notes', value: item.portfolioOrBio });

        let docPreviewHtml = '<p class="ew-app-meta">No verification document was uploaded.</p>';
        if (docUrl) {
            if (isImage) {
                docPreviewHtml = `
                    <div class="ew-verif-preview-box">
                        <img src="${escapeHtml(docUrl)}" alt="Uploaded verification document" class="ew-verif-preview-img">
                    </div>
                `;
            } else if (isPdf) {
                docPreviewHtml = `
                    <div class="ew-verif-preview-box">
                        <iframe src="${escapeHtml(docUrl)}" title="Uploaded verification PDF" class="ew-verif-preview-pdf"></iframe>
                    </div>
                `;
            } else {
                docPreviewHtml = `<p class="ew-app-meta">Preview not available for this file type. Use the button below to open or download it.</p>`;
            }
        }

        verifModalContent.innerHTML = `
            <div class="ew-person-modal-head">
                <div>
                    <h3 id="admin-verif-modal-title">${escapeHtml(item.organizationName || item.fullName)}</h3>
                    <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 4px;">
                        <span class="module-tag">${escapeHtml(item.roleDisplayName || item.role)}</span>
                        <span class="ew-badge ${escapeHtml(statusBadge)}">${escapeHtml(item.verificationStatus)}</span>
                    </div>
                </div>
            </div>
            <div class="ew-person-modal-body">
                <div class="ew-verif-detail-grid">
                    ${detailRows.map((row) => `
                        <div class="ew-verif-detail-item">
                            <span class="ew-verif-detail-label">${escapeHtml(row.label)}</span>
                            <strong class="ew-verif-detail-value">${escapeHtml(row.value)}</strong>
                        </div>
                    `).join('')}
                </div>
                <div class="ew-verif-doc-section">
                    <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 8px; flex-wrap: wrap;">
                        <strong>Uploaded Verification Document</strong>
                        ${docUrl ? `<a class="ew-btn ew-btn-outline ew-btn-sm" href="${escapeHtml(docUrl)}" target="_blank" rel="noopener">Open in New Tab</a>` : ''}
                    </div>
                    ${docPreviewHtml}
                </div>
            </div>
            <div class="ew-inv-modal-actions">
                ${item.verificationStatus !== 'APPROVED'
                    ? `<button type="button" class="action-btn btn-approve" data-modal-verif-action="approve" data-modal-verif-id="${escapeHtml(item.id)}">Approve</button>`
                    : ''}
                ${item.verificationStatus !== 'REJECTED'
                    ? `<button type="button" class="action-btn btn-reject" data-modal-verif-action="reject" data-modal-verif-id="${escapeHtml(item.id)}">Reject</button>`
                    : ''}
                <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" id="admin-verif-modal-done">Close</button>
            </div>
        `;

        verifModal.hidden = false;
    }

    verifModalCloseBtn?.addEventListener('click', closeVerificationModal);
    verifModal?.addEventListener('click', (e) => {
        if (e.target === verifModal || e.target.id === 'admin-verif-modal-done') {
            closeVerificationModal();
            return;
        }
        const actionBtn = e.target.closest('[data-modal-verif-action]');
        if (actionBtn) {
            updateDecision(actionBtn.dataset.modalVerifId, actionBtn.dataset.modalVerifAction);
        }
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && verifModal && !verifModal.hidden) {
            closeVerificationModal();
        }
    });

    function renderUsers() {
        const container = document.getElementById('admin-users-list');
        if (!container) return;
        const query = (document.getElementById('admin-user-search')?.value || '').trim().toLowerCase();
        const roleFilter = document.getElementById('admin-user-role-filter')?.value || 'ALL';

        const filtered = adminState.users.filter((u) => {
            const matchesRole = roleFilter === 'ALL' || u.role === roleFilter;
            const matchesQuery = !query || u.name.toLowerCase().includes(query) || u.email.toLowerCase().includes(query);
            return matchesRole && matchesQuery;
        });

        container.innerHTML = filtered.map((u) => `
            <article class="ew-app-card">
                <div class="ew-app-info">
                    <h3>${escapeHtml(u.name)} <span class="module-tag">${escapeHtml(u.role)}</span></h3>
                    <div class="ew-app-meta">${escapeHtml(u.email)}</div>
                </div>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <span class="ew-badge ${u.active ? 'shortlisted' : 'closed'}">${u.active ? 'Active' : 'Suspended'}</span>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-user="${escapeHtml(u.id)}">
                        ${u.active ? 'Suspend' : 'Activate'}
                    </button>
                </div>
            </article>
        `).join('');
    }

    const PAYMENT_STATUS_OPTIONS = [
        { value: 'In Review', label: 'In Review', badge: 'review' },
        { value: 'Settled', label: 'Settled', badge: 'shortlisted' },
        { value: 'Paid', label: 'Paid', badge: 'shortlisted' },
        { value: 'Pending', label: 'Pending', badge: 'audition' },
        { value: 'Failed', label: 'Failed', badge: 'closed' }
    ];

    function closePaymentActionMenus() {
        document.querySelectorAll('.ew-payment-action-menu').forEach((menu) => {
            menu.hidden = true;
        });
    }

    function renderPayments() {
        const portalPlans = Array.isArray(adminState.portalPlans) ? adminState.portalPlans : DEFAULT_ADMIN_STATE.portalPlans;
        const totalRevenue = portalPlans.reduce((sum, p) => sum + (p.monthlyRevenue || 0), 0);
        const totalPaidSubs = portalPlans.reduce((sum, p) => sum + (p.paidSubscribers || 0), 0);

        const totalPill = document.getElementById('admin-total-revenue-pill');
        if (totalPill) {
            totalPill.textContent = `₹${totalRevenue.toLocaleString('en-IN')} / month`;
        }

        const payments = Array.isArray(adminState.payments) ? adminState.payments : [];
        const settledTotal = payments
            .filter((p) => p.status === 'Settled' || p.status === 'Paid')
            .reduce((sum, p) => sum + (p.amount || 0), 0);
        const pendingTotal = payments
            .filter((p) => p.status === 'Pending')
            .reduce((sum, p) => sum + (p.amount || 0), 0);

        const summaryStrip = document.getElementById('admin-revenue-summary');
        if (summaryStrip) {
            summaryStrip.innerHTML = `
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">₹${totalRevenue.toLocaleString('en-IN')}</div>
                    <div class="ew-inv-stat-label">Monthly Portal Revenue</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">${totalPaidSubs.toLocaleString('en-IN')}</div>
                    <div class="ew-inv-stat-label">Paid Subscribers</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">₹${settledTotal.toLocaleString('en-IN')}</div>
                    <div class="ew-inv-stat-label">Settled &amp; Paid</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">₹${pendingTotal.toLocaleString('en-IN')}</div>
                    <div class="ew-inv-stat-label">Pending Clearance</div>
                </div>
            `;
        }

        const portalGrid = document.getElementById('admin-portal-revenue-grid');
        if (portalGrid) {
            portalGrid.innerHTML = portalPlans.map((p) => `
                <article class="ew-panel">
                    <div class="ew-section-head" style="margin-bottom: 6px;">
                        <h3 style="margin: 0;">${escapeHtml(p.portal)}</h3>
                        <span class="ew-badge ${escapeHtml(p.badgeClass)}">${escapeHtml(p.badge)}</span>
                    </div>
                    <p style="margin: 0 0 8px; font-size: 0.84rem; color: var(--ew-text-secondary);">${escapeHtml(p.tiers)}</p>
                    <p style="margin: 0;"><strong>${p.subscribers.toLocaleString('en-IN')}</strong> total (${p.paidSubscribers.toLocaleString('en-IN')} paid) • <strong>₹${p.monthlyRevenue.toLocaleString('en-IN')}</strong> / mo</p>
                </article>
            `).join('');
        }

        const tbody = document.getElementById('admin-payments-tbody');
        const countEl = document.getElementById('admin-payments-count');
        if (!tbody) return;

        const query = (document.getElementById('admin-payment-search')?.value || '').trim().toLowerCase();
        const portalFilter = document.getElementById('admin-payment-portal-filter')?.value || 'ALL';
        const planFilter = document.getElementById('admin-payment-plan-filter')?.value || 'ALL';
        const statusFilter = document.getElementById('admin-payment-status-filter')?.value || 'ALL';

        const filtered = payments.filter((item) => {
            const matchesPortal = portalFilter === 'ALL' || item.portal === portalFilter;
            const matchesPlan = planFilter === 'ALL' || item.planTier === planFilter;
            const matchesStatus = statusFilter === 'ALL' || item.status === statusFilter;
            const matchesQuery = !query ||
                (item.subscriberName || '').toLowerCase().includes(query) ||
                (item.email || '').toLowerCase().includes(query) ||
                (item.invoice || '').toLowerCase().includes(query) ||
                (item.plan || '').toLowerCase().includes(query);
            return matchesPortal && matchesPlan && matchesStatus && matchesQuery;
        });

        if (countEl) {
            countEl.textContent = `${filtered.length} of ${payments.length} transactions`;
        }

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 18px;">No payment records match your search or filters.</td></tr>`;
            return;
        }

        tbody.innerHTML = filtered.map((item) => `
            <tr>
                <td>
                    <strong>${escapeHtml(item.subscriberName)}</strong>
                    <div class="ew-app-meta">${escapeHtml(item.email)}</div>
                </td>
                <td><span class="module-tag">${escapeHtml(item.portal)}</span></td>
                <td>${escapeHtml(item.plan)}</td>
                <td><strong>₹${Number(item.amount || 0).toLocaleString('en-IN')}</strong></td>
                <td>${escapeHtml(item.date)} • ${escapeHtml(item.method)} (${escapeHtml(item.invoice)})</td>
                <td><span class="ew-badge ${escapeHtml(item.badge || 'review')}">${escapeHtml(item.status)}</span></td>
                <td>
                    <div class="ew-payment-action-wrap">
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-payment-menu-toggle="${escapeHtml(item.id)}">
                            Manage ▾
                        </button>
                        <div class="ew-payment-action-menu" data-payment-menu="${escapeHtml(item.id)}" hidden>
                            ${PAYMENT_STATUS_OPTIONS.map((opt) => `
                                <button type="button"
                                    class="ew-payment-action-item${item.status === opt.value ? ' active' : ''}"
                                    data-payment-id="${escapeHtml(item.id)}"
                                    data-payment-status="${escapeHtml(opt.value)}">
                                    <span class="ew-badge ${escapeHtml(opt.badge)}">${escapeHtml(opt.label)}</span>
                                </button>
                            `).join('')}
                        </div>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    function renderBroadcasts() {
        const container = document.getElementById('admin-broadcast-history');
        if (!container) return;
        container.innerHTML = adminState.broadcasts.map((b) => `
            <article class="ew-ticket-card">
                <div class="ew-ticket-head">
                    <h3>${escapeHtml(b.title)}</h3>
                    <span class="ew-badge audition">${escapeHtml(b.audience)}</span>
                </div>
                <p class="ew-ticket-desc">${escapeHtml(b.message)}</p>
                <div class="ew-app-meta">Sent ${escapeHtml(b.time)}</div>
            </article>
        `).join('');
    }

    function getMergedComplaints() {
        let proComplaints = [];
        try {
            const rawPro = localStorage.getItem(PRO_STORAGE_KEY);
            if (rawPro) {
                const parsed = JSON.parse(rawPro);
                if (Array.isArray(parsed.complaints)) {
                    proComplaints = parsed.complaints;
                }
            }
        } catch {
            // Ignore malformed storage
        }
        const byId = new Map();
        [...proComplaints, ...adminState.complaints].forEach((c) => {
            if (!byId.has(c.id)) byId.set(c.id, c);
        });
        return Array.from(byId.values());
    }

    function renderAdminComplaints() {
        const complaints = getMergedComplaints();
        const openCount = complaints.filter((c) => c.status !== 'Resolved').length;

        const kpiEl = document.getElementById('admin-kpi-complaints');
        const badgeEl = document.getElementById('admin-complaints-badge');
        if (kpiEl) kpiEl.textContent = String(openCount);
        if (badgeEl) badgeEl.textContent = String(openCount);

        const container = document.getElementById('admin-complaints-list');
        if (!container) return;

        container.innerHTML = complaints.map((c) => `
            <article class="ew-ticket-card">
                <div class="ew-ticket-head">
                    <h3>${escapeHtml(c.subject)}</h3>
                    <span class="ew-badge ${escapeHtml(c.badgeClass || 'review')}">${escapeHtml(c.status)}</span>
                </div>
                <p class="ew-ticket-desc">${escapeHtml(c.description || '')}</p>
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px; flex-wrap: wrap;">
                    <span class="ew-app-meta">${escapeHtml(c.id)} • ${escapeHtml(c.category)} • Priority: ${escapeHtml(c.priority)} • ${escapeHtml(c.createdAt)}</span>
                    ${c.status !== 'Resolved'
                        ? `<button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-admin-resolve="${escapeHtml(c.id)}">Resolve Ticket</button>`
                        : ''}
                </div>
            </article>
        `).join('');
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text ?? '';
        return div.innerHTML;
    }

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.ew-payment-action-wrap')) {
            closePaymentActionMenus();
        }

        const paymentMenuBtn = e.target.closest('[data-payment-menu-toggle]');
        if (paymentMenuBtn) {
            const paymentId = paymentMenuBtn.dataset.paymentMenuToggle;
            const menu = document.querySelector(`[data-payment-menu="${CSS.escape(paymentId)}"]`);
            const wasHidden = menu ? menu.hidden : true;
            closePaymentActionMenus();
            if (menu && wasHidden) {
                menu.hidden = false;
                const rect = paymentMenuBtn.getBoundingClientRect();
                const menuRect = menu.getBoundingClientRect();
                const spaceBelow = window.innerHeight - rect.bottom;
                const top = spaceBelow >= menuRect.height + 8
                    ? rect.bottom + 6
                    : Math.max(8, rect.top - menuRect.height - 6);
                const left = Math.max(8, Math.min(rect.right - menuRect.width, window.innerWidth - menuRect.width - 8));
                menu.style.top = `${top}px`;
                menu.style.left = `${left}px`;
            }
            return;
        }

        const paymentStatusBtn = e.target.closest('[data-payment-status]');
        if (paymentStatusBtn) {
            const paymentId = paymentStatusBtn.dataset.paymentId;
            const newStatus = paymentStatusBtn.dataset.paymentStatus;
            const option = PAYMENT_STATUS_OPTIONS.find((opt) => opt.value === newStatus);
            const payment = (adminState.payments || []).find((p) => p.id === paymentId);
            if (payment && option) {
                payment.status = option.value;
                payment.badge = option.badge;
                saveAdminState();
                renderPayments();
                showToast(`${payment.invoice || payment.subscriberName} marked as ${option.value}`);
            } else {
                closePaymentActionMenus();
            }
            return;
        }

        const navBtn = e.target.closest('.ew-nav-item[data-view]');
        if (navBtn) {
            switchView(navBtn.dataset.view);
            return;
        }

        const gotoBtn = e.target.closest('[data-goto-view]');
        if (gotoBtn) {
            switchView(gotoBtn.dataset.gotoView);
            return;
        }

        const verifChip = e.target.closest('.ew-chip[data-verif-filter]');
        if (verifChip) {
            verifFilter = verifChip.dataset.verifFilter;
            document.querySelectorAll('.ew-chip[data-verif-filter]').forEach((c) => {
                c.classList.toggle('active', c === verifChip);
            });
            renderVerifications();
            return;
        }

        const toggleUserBtn = e.target.closest('[data-toggle-user]');
        if (toggleUserBtn) {
            const user = adminState.users.find((u) => u.id === toggleUserBtn.dataset.toggleUser);
            if (user) {
                user.active = !user.active;
                saveAdminState();
                renderUsers();
                showToast(`${user.name} ${user.active ? 'activated' : 'suspended'}`);
            }
            return;
        }

        const resolveBtn = e.target.closest('[data-admin-resolve]');
        if (resolveBtn) {
            const ticketId = resolveBtn.dataset.adminResolve;
            const localTicket = adminState.complaints.find((c) => c.id === ticketId);
            if (localTicket) {
                localTicket.status = 'Resolved';
                localTicket.badgeClass = 'shortlisted';
                saveAdminState();
            }
            try {
                const rawPro = localStorage.getItem(PRO_STORAGE_KEY);
                if (rawPro) {
                    const parsed = JSON.parse(rawPro);
                    const proTicket = (parsed.complaints || []).find((c) => c.id === ticketId);
                    if (proTicket) {
                        proTicket.status = 'Resolved';
                        proTicket.badgeClass = 'shortlisted';
                        localStorage.setItem(PRO_STORAGE_KEY, JSON.stringify(parsed));
                    }
                }
            } catch {
                // Ignore storage errors
            }
            renderAdminComplaints();
            showToast(`Resolved complaint ${ticketId}`);
        }
    });

    document.getElementById('admin-user-search')?.addEventListener('input', renderUsers);
    document.getElementById('admin-user-role-filter')?.addEventListener('change', renderUsers);
    document.getElementById('admin-payment-search')?.addEventListener('input', renderPayments);
    document.getElementById('admin-payment-portal-filter')?.addEventListener('change', renderPayments);
    document.getElementById('admin-payment-plan-filter')?.addEventListener('change', renderPayments);
    document.getElementById('admin-payment-status-filter')?.addEventListener('change', renderPayments);

    document.getElementById('admin-broadcast-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const audience = document.getElementById('broadcast-audience').value;
        const title = document.getElementById('broadcast-title').value.trim();
        const message = document.getElementById('broadcast-message').value.trim();
        if (!title || !message) return;

        adminState.broadcasts.unshift({
            id: `bc-${Date.now()}`,
            title,
            audience,
            message,
            time: 'Just now'
        });
        saveAdminState();
        renderBroadcasts();
        e.target.reset();
        showToast('Announcement sent');
    });

    const SUB_PORTAL_META = {
        Professionals: { planId: 'pp-actors', label: 'Professionals (Actors & Talent)' },
        Casting: { planId: 'pp-casting', label: 'Casting Productions' },
        Brands: { planId: 'pp-brands', label: 'Brand Promotions' },
        Events: { planId: 'pp-events', label: 'Event Management' },
        Vendors: { planId: 'pp-vendors', label: 'Vendors Management' }
    };

    const planModal = document.getElementById('admin-plan-modal');
    const planForm = document.getElementById('admin-plan-form');

    function syncPortalPlansFromCatalog() {
        const catalog = Array.isArray(adminState.subscriptionPlans) ? adminState.subscriptionPlans : [];
        const portalPlans = Array.isArray(adminState.portalPlans) ? adminState.portalPlans : [];
        Object.entries(SUB_PORTAL_META).forEach(([portalKey, meta]) => {
            const activeForPortal = catalog.filter((p) => p.portal === portalKey && p.status === 'Active');
            const summary = activeForPortal.length
                ? activeForPortal.map((p) => `${p.name} ${p.price === 0 ? 'Free' : `₹${Number(p.price).toLocaleString('en-IN')}`}`).join(' • ')
                : 'No active plans';
            const portalRow = portalPlans.find((pp) => pp.id === meta.planId);
            if (portalRow) {
                portalRow.tiers = summary;
            }
        });
    }

    function renderSubscriptionManager() {
        const grid = document.getElementById('admin-subscriptions-grid');
        if (!grid) return;

        const plans = Array.isArray(adminState.subscriptionPlans) ? adminState.subscriptionPlans : [];
        const filtered = subPortalFilter === 'ALL'
            ? plans
            : plans.filter((p) => p.portal === subPortalFilter);

        if (!filtered.length) {
            grid.innerHTML = '<div class="ew-panel"><p>No subscription plans configured for this portal yet.</p></div>';
            return;
        }

        grid.innerHTML = filtered.map((plan) => {
            const isActive = plan.status === 'Active';
            const priceLabel = Number(plan.price) === 0
                ? 'Free'
                : `₹${Number(plan.price).toLocaleString('en-IN')}`;
            const cycleSuffix = Number(plan.price) === 0 ? '' : `<span>/ ${escapeHtml(plan.cycle || 'Monthly')}</span>`;
            const features = Array.isArray(plan.features) ? plan.features : [];
            const portalLabel = SUB_PORTAL_META[plan.portal]?.label || plan.portal;

            return `
                <article class="ew-panel ew-sub-plan-card">
                    <div>
                        <div class="ew-section-head" style="margin-bottom: 8px; gap: 8px;">
                            <span class="module-tag">${escapeHtml(portalLabel)}</span>
                            <span class="ew-badge ${isActive ? 'shortlisted' : 'closed'}">${escapeHtml(plan.status)}</span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 6px;">
                            <h3 style="margin: 0;">${escapeHtml(plan.name)}</h3>
                            ${plan.badge ? `<span class="ew-badge review">${escapeHtml(plan.badge)}</span>` : ''}
                        </div>
                        <div class="ew-sub-plan-price">${priceLabel} ${cycleSuffix}</div>
                        ${plan.quota ? `<p class="ew-app-meta" style="margin: 6px 0 10px;">${escapeHtml(plan.quota)}</p>` : ''}
                        ${features.length ? `
                            <ul class="ew-sub-plan-features">
                                ${features.map((f) => `<li>${escapeHtml(f)}</li>`).join('')}
                            </ul>
                        ` : ''}
                    </div>
                    <div class="ew-sub-plan-actions">
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-sub-plan="${escapeHtml(plan.id)}">
                            ${isActive ? 'Pause' : 'Activate'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-edit-sub-plan="${escapeHtml(plan.id)}">
                            Edit Plan
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function openPlanModal(planId) {
        if (!planModal || !planForm) return;
        const plan = planId ? (adminState.subscriptionPlans || []).find((p) => p.id === planId) : null;
        document.getElementById('admin-plan-modal-title').textContent = plan ? `Edit Plan — ${plan.name}` : 'Add New Subscription Plan';
        document.getElementById('sub-plan-id').value = plan ? plan.id : '';
        document.getElementById('sub-plan-portal').value = plan ? plan.portal : (subPortalFilter !== 'ALL' ? subPortalFilter : 'Professionals');
        document.getElementById('sub-plan-name').value = plan ? plan.name : '';
        document.getElementById('sub-plan-price').value = plan ? String(plan.price) : '';
        document.getElementById('sub-plan-cycle').value = plan ? (plan.cycle || 'Monthly') : 'Monthly';
        document.getElementById('sub-plan-status').value = plan ? (plan.status || 'Active') : 'Active';
        document.getElementById('sub-plan-badge').value = plan ? (plan.badge || '') : '';
        document.getElementById('sub-plan-quota').value = plan ? (plan.quota || '') : '';
        document.getElementById('sub-plan-features').value = plan && Array.isArray(plan.features) ? plan.features.join('\n') : '';
        planModal.hidden = false;
    }

    function closePlanModal() {
        if (planModal) planModal.hidden = true;
    }

    document.addEventListener('click', (e) => {
        const subChip = e.target.closest('.ew-chip[data-sub-portal-filter]');
        if (subChip) {
            subPortalFilter = subChip.dataset.subPortalFilter;
            document.querySelectorAll('.ew-chip[data-sub-portal-filter]').forEach((c) => {
                c.classList.toggle('active', c === subChip);
            });
            renderSubscriptionManager();
            return;
        }

        const editPlanBtn = e.target.closest('[data-edit-sub-plan]');
        if (editPlanBtn) {
            openPlanModal(editPlanBtn.dataset.editSubPlan);
            return;
        }

        const togglePlanBtn = e.target.closest('[data-toggle-sub-plan]');
        if (togglePlanBtn) {
            const plan = (adminState.subscriptionPlans || []).find((p) => p.id === togglePlanBtn.dataset.toggleSubPlan);
            if (plan) {
                plan.status = plan.status === 'Active' ? 'Paused' : 'Active';
                syncPortalPlansFromCatalog();
                saveAdminState();
                renderSubscriptionManager();
                renderPayments();
                showToast(`${plan.name} (${plan.portal}) marked ${plan.status}`);
            }
        }
    });

    document.getElementById('admin-add-plan-btn')?.addEventListener('click', () => openPlanModal(null));
    document.getElementById('admin-reset-plans-btn')?.addEventListener('click', () => {
        adminState.subscriptionPlans = structuredClone(DEFAULT_ADMIN_STATE.subscriptionPlans);
        syncPortalPlansFromCatalog();
        saveAdminState();
        renderSubscriptionManager();
        renderPayments();
        showToast('Subscription plans reset to defaults');
    });

    document.getElementById('admin-plan-modal-close')?.addEventListener('click', closePlanModal);
    document.getElementById('admin-plan-cancel')?.addEventListener('click', closePlanModal);
    planModal?.addEventListener('click', (e) => {
        if (e.target === planModal) closePlanModal();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && planModal && !planModal.hidden) {
            closePlanModal();
        }
    });

    planForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('sub-plan-id').value.trim();
        const portal = document.getElementById('sub-plan-portal').value;
        const name = document.getElementById('sub-plan-name').value.trim();
        const price = Math.max(0, Number(document.getElementById('sub-plan-price').value || 0));
        const cycle = document.getElementById('sub-plan-cycle').value;
        const status = document.getElementById('sub-plan-status').value;
        const badge = document.getElementById('sub-plan-badge').value.trim();
        const quota = document.getElementById('sub-plan-quota').value.trim();
        const features = document.getElementById('sub-plan-features').value
            .split(/\r?\n/)
            .map((line) => line.trim())
            .filter(Boolean);

        if (!name) return;

        if (!Array.isArray(adminState.subscriptionPlans)) {
            adminState.subscriptionPlans = [];
        }

        if (id) {
            const existing = adminState.subscriptionPlans.find((p) => p.id === id);
            if (existing) {
                existing.portal = portal;
                existing.name = name;
                existing.price = price;
                existing.cycle = cycle;
                existing.status = status;
                existing.badge = badge;
                existing.quota = quota;
                existing.features = features;
            }
            showToast(`Updated ${name} plan for ${portal}`);
        } else {
            adminState.subscriptionPlans.push({
                id: `sp-${Date.now()}`,
                portal,
                name,
                price,
                cycle,
                status,
                badge,
                quota,
                features
            });
            showToast(`Added ${name} plan for ${portal}`);
        }

        syncPortalPlansFromCatalog();
        saveAdminState();
        renderSubscriptionManager();
        renderPayments();
        closePlanModal();
    });

    refreshBtn?.addEventListener('click', loadVerifications);

    renderUsers();
    syncPortalPlansFromCatalog();
    renderPayments();
    renderSubscriptionManager();
    renderBroadcasts();
    renderAdminComplaints();
    loadVerifications();
})();
