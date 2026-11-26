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
        professionals: [
            { id: 'p-1', title: 'Alex Rivera', meta: 'Actor • Mumbai • Pro Elite (₹399)', status: 'Verified', badge: 'shortlisted' },
            { id: 'p-2', title: 'Diya Sharma', meta: 'Theatre & Screen Actor • Delhi NCR • Pro Elite', status: 'Pending Seal', badge: 'review' },
            { id: 'p-3', title: 'Kabir Malhotra', meta: 'Commercial & Voice Actor • Bengaluru • Premium', status: 'Verified', badge: 'shortlisted' }
        ],
        casting: [
            { id: 'c-1', title: 'Apex Casting Studio', meta: '3 Active Casting Calls • Mumbai', status: 'Approved', badge: 'shortlisted' },
            { id: 'c-2', title: 'RedFrame Originals', meta: 'Lead Actor – Web Series • Under Review', status: 'Active', badge: 'audition' }
        ],
        production: [
            { id: 'pr-1', title: 'SilverScreen Pictures', meta: 'Feature Film Production • Mumbai', status: 'Verified', badge: 'shortlisted' },
            { id: 'pr-2', title: 'NovaCut Studios', meta: 'Post-Production & Editing • Bengaluru', status: 'Active', badge: 'audition' }
        ],
        brands: [
            { id: 'b-1', title: 'Nova Brand House', meta: '2 Sponsored Campaigns • Lifestyle', status: 'Approved', badge: 'shortlisted' },
            { id: 'b-2', title: 'Veloce Audio', meta: 'Tech Launch Influencer Campaign', status: 'Active', badge: 'review' }
        ],
        influencers: [
            { id: 'i-1', title: 'Aarav Mehta', meta: '240K Reach • Lifestyle & Cinema', status: 'Verified', badge: 'shortlisted' },
            { id: 'i-2', title: 'Neha Kulkarni', meta: '115K Reach • Tech & Fashion', status: 'Active', badge: 'audition' }
        ],
        events: [
            { id: 'e-1', title: 'Starlight Events', meta: 'Film Festival & Award Night Permits', status: 'Approved', badge: 'shortlisted' },
            { id: 'e-2', title: 'Prime Stage Vendors', meta: 'Stage, Lighting & Audio Rigging', status: 'Active', badge: 'review' }
        ],
        payments: [
            { id: 'pay-1', title: 'Pro Elite Plan Settlement (#INV-9021)', meta: '216 Subscribers • ₹86,184', status: 'Settled', badge: 'shortlisted' },
            { id: 'pay-2', title: 'Vendor Escrow Payout — Starlight Events', meta: '₹45,000 • Milestone 1', status: 'Pending', badge: 'review' }
        ],
        content: [
            { id: 'cnt-1', title: 'Homepage Banner: Monsoon Casting Drive', meta: 'Featured Spotlight • Actors & Crew', status: 'Published', badge: 'shortlisted' },
            { id: 'cnt-2', title: 'Showreel Moderation Queue (4 reels)', meta: 'Awaiting quality check', status: 'In Review', badge: 'review' }
        ],
        broadcasts: [
            { id: 'bc-1', title: 'New 3-Tier Professional Plans Live', audience: 'Professionals (Actors)', message: 'Basic Free, Premium ₹199, and Pro Elite ₹399 plans are now active.', time: 'Yesterday' }
        ],
        complaints: [
            { id: 'CMP-1009', subject: 'Brand campaign brief missing shoot dates', category: 'Casting / Opportunity Dispute', priority: 'High', status: 'Open', badgeClass: 'audition', createdAt: '1 day ago' }
        ]
    };

    function loadAdminState() {
        try {
            const raw = localStorage.getItem(ADMIN_STORAGE_KEY);
            if (!raw) return structuredClone(DEFAULT_ADMIN_STATE);
            return { ...structuredClone(DEFAULT_ADMIN_STATE), ...JSON.parse(raw) };
        } catch {
            return structuredClone(DEFAULT_ADMIN_STATE);
        }
    }

    let adminState = loadAdminState();
    let cachedVerifications = [];
    let verifFilter = 'ALL';
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
            row.className = 'verification-item';

            const meta = document.createElement('div');
            meta.className = 'verification-meta';
            meta.innerHTML = `
                <h3>${escapeHtml(item.organizationName || item.fullName)} <span class="module-tag">${escapeHtml(item.roleDisplayName)}</span></h3>
                <p>Contact: ${escapeHtml(item.fullName)} (${escapeHtml(item.email)})</p>
                <p>Document: ${escapeHtml(item.documentType || 'N/A')} &bull; No: ${escapeHtml(item.documentNumber || 'N/A')}</p>
            `;

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
        await loadVerifications();
    }

    // Sub-module Renderers
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

    function renderModuleList(containerId, items) {
        const container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = items.map((item) => `
            <article class="ew-app-card">
                <div class="ew-app-info">
                    <h3>${escapeHtml(item.title)}</h3>
                    <div class="ew-app-meta">${escapeHtml(item.meta)}</div>
                </div>
                <span class="ew-badge ${escapeHtml(item.badge)}">${escapeHtml(item.status)}</span>
            </article>
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

    // Event Delegation
    document.addEventListener('click', (e) => {
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
        showToast('Broadcast announcement sent');
    });

    refreshBtn?.addEventListener('click', loadVerifications);

    // Initial Render
    renderUsers();
    renderModuleList('admin-professionals-list', adminState.professionals);
    renderModuleList('admin-casting-list', adminState.casting);
    renderModuleList('admin-production-list', adminState.production);
    renderModuleList('admin-brand-list', adminState.brands);
    renderModuleList('admin-influencer-list', adminState.influencers);
    renderModuleList('admin-event-list', adminState.events);
    renderModuleList('admin-payments-list', adminState.payments);
    renderModuleList('admin-content-list', adminState.content);
    renderBroadcasts();
    renderAdminComplaints();
    loadVerifications();
})();
