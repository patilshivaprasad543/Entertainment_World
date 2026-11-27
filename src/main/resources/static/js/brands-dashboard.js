(() => {
    const brandId = Number(localStorage.getItem('ew_brand_id') || 1);

    const PLANS = [
        { code: 'FREE', title: 'Free', price: 0, campaigns: '1 active', seats: 1, search: 'Basic', ai: 'Locked', analytics: 'Basic', team: false, export: false },
        { code: 'PREMIUM_1499', title: 'Premium', price: 1499, campaigns: '5 active', seats: 3, search: 'Advanced', ai: 'Top 5 matches', analytics: 'Detailed', team: false, export: true },
        { code: 'PRO_3999', title: 'Pro', price: 3999, campaigns: '20 active', seats: 10, search: 'Advanced', ai: 'Unlimited', analytics: 'Benchmarks', team: true, export: true }
    ];

    const SAMPLE_TALENT = [
        { id: 1, fullName: 'Aanya Rao', category: 'Influencer', city: 'Bengaluru', baseRate: 68000, averageRating: 4.9, totalReviews: 14, languages: 'English · Hindi · Kannada', verified: true },
        { id: 2, fullName: 'Rohan Mehta', category: 'Actor', city: 'Mumbai', baseRate: 52000, averageRating: 4.8, totalReviews: 9, languages: 'English · Hindi', verified: true },
        { id: 3, fullName: 'Diya Kapoor', category: 'Model', city: 'Delhi NCR', baseRate: 74000, averageRating: 5.0, totalReviews: 18, languages: 'English · Hindi', verified: true },
        { id: 4, fullName: 'Kavya Iyer', category: 'UGC Creator', city: 'Chennai', baseRate: 28000, averageRating: 4.7, totalReviews: 6, languages: 'English · Tamil', verified: false }
    ];

    const state = {
        overview: { activeCampaigns: 0, campaignLimit: 1, pendingRequests: 0, wishlistedTalent: 0, walletBalanceInr: 0, planType: 'FREE' },
        profile: { brandName: 'Atelier North', companyType: 'D2C Brand', industryCategory: 'Fashion & Apparel' },
        talents: [...SAMPLE_TALENT],
        campaigns: [],
        requests: [],
        wishlist: [],
        members: [],
        squads: [],
        invoices: [],
        reviews: [],
        notifications: []
    };

    let toastTimer = null;
    function showToast(msg) {
        const el = document.getElementById('ew-toast');
        if (!el) return;
        el.textContent = msg;
        el.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => { el.hidden = true; }, 2800);
    }

    function esc(text) {
        const div = document.createElement('div');
        div.textContent = text ?? '';
        return div.innerHTML;
    }

    function money(val) {
        return `₹${Number(val || 0).toLocaleString('en-IN')}`;
    }

    async function api(path, options = {}) {
        const res = await fetch(`/api/v1/brand/${brandId}${path}`, {
            headers: { 'Content-Type': 'application/json' },
            ...options
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.message || 'Request failed');
        return body;
    }

    // Navigation & Shell (Matches Professional Dashboard)
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
        const navBtn = e.target.closest('.ew-nav-item[data-view]');
        if (navBtn) {
            switchView(navBtn.dataset.view);
            return;
        }
        const gotoBtn = e.target.closest('[data-goto-view]');
        if (gotoBtn) {
            switchView(gotoBtn.dataset.gotoView);
        }
    });

    function renderTalentCard(t, compact = false) {
        const wished = state.wishlist.some((w) => w.talentId === t.id);
        const initials = (t.fullName || 'CR').split(' ').map((x) => x[0]).join('').slice(0, 2).toUpperCase();
        return `
            <article class="ew-panel" style="display:flex;flex-direction:column;justify-content:space-between;gap:12px;">
                <div>
                    <div style="display:flex;align-items:center;justify-content:space-between;gap:10px;">
                        <div style="display:flex;align-items:center;gap:10px;">
                            <span class="ew-hero-avatar-fallback" style="width:44px;height:44px;font-size:0.95rem;">${esc(initials)}</span>
                            <div>
                                <strong style="display:block;">${esc(t.fullName)}</strong>
                                <span class="ew-app-meta">${esc(t.category || 'Creator')} • ${esc(t.city || 'India')}</span>
                            </div>
                        </div>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-wish-talent="${t.id}" title="Save to Wishlist">
                            ${wished ? '♥' : '♡'}
                        </button>
                    </div>
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px;font-size:0.86rem;">
                        <span>★ ${esc(t.averageRating || 'New')} <span class="ew-app-meta">(${t.totalReviews || 0})</span></span>
                        ${t.verified ? '<span class="ew-badge shortlisted">✓ Verified</span>' : '<span class="ew-badge closed">Standard</span>'}
                    </div>
                    <div style="display:flex;justify-content:space-between;margin-top:10px;padding-top:10px;border-top:1px solid var(--ew-border);font-size:0.88rem;">
                        <span class="ew-app-meta">Starting at</span>
                        <strong>${money(t.baseRate)}</strong>
                    </div>
                </div>
                ${compact ? '' : `
                    <div style="display:flex;gap:8px;">
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" style="flex:1;" data-connect-talent="${t.id}">Connect</button>
                    </div>
                `}
            </article>
        `;
    }

    function renderAll() {
        const planCode = state.overview.planType || 'FREE';
        const planObj = PLANS.find((p) => p.code === planCode) || PLANS[0];

        document.getElementById('brand-plan-pill').textContent = `Plan: ${planObj.title}`;
        document.getElementById('brand-plan-nav-badge').textContent = planObj.title;
        document.getElementById('brand-ai-badge').textContent = planCode === 'PRO_3999' ? 'UNLIMITED' : planCode === 'FREE' ? 'LOCKED' : 'TOP 5';

        const brandName = state.profile.brandName || 'Brand Partner';
        document.getElementById('brand-hero-name').textContent = brandName;
        document.getElementById('topbar-brand-name').textContent = brandName;
        document.getElementById('brand-hero-fallback').textContent = brandName.split(' ').map((x) => x[0]).join('').slice(0, 2).toUpperCase();

        document.getElementById('kpi-brand-campaigns').textContent = `${state.overview.activeCampaigns || 0}/${state.overview.campaignLimit || 1}`;
        document.getElementById('kpi-brand-requests').textContent = String(state.overview.pendingRequests || 0);
        document.getElementById('kpi-brand-wishlist').textContent = String(state.overview.wishlistedTalent || 0);
        document.getElementById('kpi-brand-wallet').textContent = money(state.overview.walletBalanceInr);
        document.getElementById('brand-req-badge').textContent = String(state.overview.pendingRequests || 0);
        document.getElementById('brand-campaign-slots').textContent = `${state.overview.activeCampaigns || state.campaigns.length} / ${state.overview.campaignLimit || 1} slots used`;

        const ovTalent = document.getElementById('brand-overview-talent');
        if (ovTalent) ovTalent.innerHTML = state.talents.slice(0, 3).map((t) => renderTalentCard(t, true)).join('');

        const talentGrid = document.getElementById('brand-talent-grid');
        const talentCount = document.getElementById('brand-talent-count');
        if (talentCount) talentCount.textContent = `${state.talents.length} creators`;
        if (talentGrid) {
            talentGrid.innerHTML = state.talents.length
                ? state.talents.map((t) => renderTalentCard(t, false)).join('')
                : '<div class="ew-panel"><p>No creators match your search.</p></div>';
        }

        const campList = document.getElementById('brand-campaigns-list');
        if (campList) {
            campList.innerHTML = state.campaigns.length
                ? state.campaigns.map((c) => `
                    <article class="ew-ticket-card">
                        <div class="ew-ticket-head">
                            <h3>${esc(c.title)}</h3>
                            <span class="ew-badge shortlisted">${esc(c.status || 'ACTIVE')}</span>
                        </div>
                        <p class="ew-ticket-desc">${esc(c.shootCity || 'Any city')} • ${esc(c.shootType || 'In-Studio')} • Budget: <strong>${money(c.budgetPerTalent)}</strong></p>
                        <div class="ew-app-meta">${esc(c.deliverables || '')}</div>
                    </article>
                `).join('')
                : '<div class="ew-panel"><p>No campaigns posted yet. Create your first campaign brief on the left.</p></div>';
        }

        const reqList = document.getElementById('brand-requests-list');
        if (reqList) {
            reqList.innerHTML = state.requests.length
                ? state.requests.map((r) => `
                    <article class="ew-app-card">
                        <div class="ew-app-info">
                            <h3>Request #${r.id} • Talent #${r.talentId}</h3>
                            <div class="ew-app-meta">Status: ${esc(r.status)}${r.draftUrl ? ` • <a href="${esc(r.draftUrl)}" target="_blank" rel="noopener">Review Draft</a>` : ''}</div>
                        </div>
                        <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
                            <span class="ew-badge ${r.status === 'COMPLETED' ? 'shortlisted' : r.status === 'DECLINED' ? 'closed' : 'review'}">${esc(r.status)}</span>
                            ${r.status === 'ACCEPTED' ? `<button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-req-action="escrow" data-req-id="${r.id}">Lock Escrow</button>` : ''}
                            ${r.status === 'HIRED_IN_ESCROW' ? `<button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-req-action="approve" data-req-id="${r.id}">Approve &amp; Pay</button>` : ''}
                        </div>
                    </article>
                `).join('')
                : '<div class="ew-panel"><p>No connect requests sent yet. Connect with creators from Discover Talent.</p></div>';
        }

        const wishGrid = document.getElementById('brand-wishlist-grid');
        if (wishGrid) {
            const wishedTalents = state.wishlist.map((w) => ({ w, t: state.talents.find((x) => x.id === w.talentId) })).filter((x) => x.t);
            wishGrid.innerHTML = wishedTalents.length
                ? wishedTalents.map(({ w, t }) => `
                    <article class="ew-panel">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                            <strong>${esc(t.fullName)}</strong>
                            <span class="module-tag">${esc(w.folderName || 'Shortlist')}</span>
                        </div>
                        <p class="ew-app-meta" style="margin:0 0 10px;">${esc(t.category)} • ${esc(t.city)} • ${money(t.baseRate)}</p>
                        <div style="display:flex;gap:8px;">
                            <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-connect-talent="${t.id}">Connect</button>
                            <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-remove-wish="${w.id}">Remove</button>
                        </div>
                    </article>
                `).join('')
                : '<div class="ew-panel"><p>Your shortlist is empty. Click the heart icon on any creator card to save them.</p></div>';
        }

        const squadsEl = document.getElementById('brand-squads-container');
        if (squadsEl) {
            if (planCode !== 'PRO_3999') {
                squadsEl.innerHTML = `
                    <article class="ew-panel" style="text-align:center;padding:32px;">
                        <h2 style="margin:0 0 8px;">Influencer Team Builder is a Pro Feature</h2>
                        <p class="ew-app-meta" style="margin:0 0 16px;">Upgrade to Pro (₹3,999/mo) to group creators into campaign squads and calculate combined budgets.</p>
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-goto-view="subscription">Explore Pro Plan</button>
                    </article>
                `;
            } else {
                squadsEl.innerHTML = `
                    <article class="ew-panel" style="margin-bottom:16px;">
                        <form id="brand-squad-form" style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;">
                            <input name="squadName" class="ew-input" type="text" placeholder="Squad name (e.g., Festive Launch Squad)" required style="max-width:260px;">
                            <input name="talentIds" class="ew-input" type="text" placeholder="Talent IDs comma-separated (e.g., 1,2)" required style="max-width:240px;">
                            <button type="submit" class="ew-btn ew-btn-primary ew-btn-sm">Create Squad</button>
                        </form>
                    </article>
                    <div class="ew-card-stack">
                        ${state.squads.length ? state.squads.map((s) => `
                            <article class="ew-panel">
                                <strong>${esc(s.squadName || 'Campaign Squad')}</strong>
                                <div class="ew-app-meta">Talent IDs: ${esc(s.talentIds)} • Combined Budget: <strong>${money(s.combinedBudget)}</strong></div>
                            </article>
                        `).join('') : '<div class="ew-panel"><p>No squads created yet.</p></div>'}
                    </div>
                `;
            }
        }

        const chatSel = document.getElementById('brand-chat-talent');
        if (chatSel && chatSel.options.length === 0) {
            chatSel.innerHTML = state.talents.map((t) => `<option value="${t.id}">${esc(t.fullName)} (${esc(t.category)})</option>`).join('');
        }

        const notifList = document.getElementById('brand-notifications-list');
        if (notifList) {
            notifList.innerHTML = state.notifications.length
                ? state.notifications.map((n) => `
                    <div class="ew-ticket-card">
                        <div class="ew-ticket-head">
                            <strong>${esc(n.title || 'Brand Update')}</strong>
                            <span class="ew-badge ${n.read ? 'closed' : 'audition'}">${n.read ? 'Read' : 'New'}</span>
                        </div>
                        <p class="ew-ticket-desc">${esc(n.message || '')}</p>
                    </div>
                `).join('')
                : '<p class="ew-app-meta">You are all caught up.</p>';
        }

        const invBody = document.getElementById('brand-invoices-tbody');
        if (invBody) {
            invBody.innerHTML = state.invoices.length
                ? state.invoices.map((i) => `
                    <tr>
                        <td><strong>${esc(i.invoiceNumber)}</strong></td>
                        <td>${money(i.subtotal)}</td>
                        <td>${money(i.gstAmount)}</td>
                        <td>${money(i.tdsAmount)}</td>
                        <td><strong>${money(i.totalAmount)}</strong></td>
                        <td><span class="ew-badge ${i.status === 'PAID' ? 'shortlisted' : 'review'}">${esc(i.status || 'PENDING')}</span></td>
                        <td>${i.status !== 'PAID' ? `<button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-pay-invoice="${i.id}">Mark Paid</button>` : '—'}</td>
                    </tr>
                `).join('')
                : '<tr><td colspan="7" style="text-align:center;padding:18px;">No invoices yet. Create one above to calculate GST &amp; TDS.</td></tr>';
        }

        const revList = document.getElementById('brand-reviews-list');
        if (revList) {
            const completed = state.requests.filter((r) => r.status === 'COMPLETED');
            revList.innerHTML = completed.length
                ? completed.map((r) => `
                    <article class="ew-panel">
                        <strong>Completed Hire #${r.id} • Talent #${r.talentId}</strong>
                        <p class="ew-app-meta" style="margin:6px 0 0;">Ready for rating &amp; review.</p>
                    </article>
                `).join('')
                : '<div class="ew-panel"><p>No completed hires to review yet.</p></div>';
        }

        const plansGrid = document.getElementById('brand-plans-grid');
        if (plansGrid) {
            plansGrid.innerHTML = PLANS.map((p) => {
                const isCurrent = planCode === p.code;
                return `
                    <article class="ew-panel" style="display:flex;flex-direction:column;justify-content:space-between;gap:14px;${isCurrent ? 'border-color:var(--ew-crimson);' : ''}">
                        <div>
                            <div class="ew-section-head" style="margin-bottom:8px;">
                                <h3 style="margin:0;">${esc(p.title)}</h3>
                                ${isCurrent ? '<span class="ew-badge shortlisted">Current Plan</span>' : ''}
                            </div>
                            <div style="font-size:1.5rem;font-weight:800;margin-bottom:10px;">${money(p.price)} <span class="ew-app-meta">/ month</span></div>
                            <div class="ew-app-meta">Campaigns: <strong>${esc(p.campaigns)}</strong></div>
                            <div class="ew-app-meta">Talent Search: <strong>${esc(p.search)}</strong></div>
                            <div class="ew-app-meta">AI Matching: <strong>${esc(p.ai)}</strong></div>
                            <div class="ew-app-meta">Team Seats: <strong>${p.seats}</strong> • Squad Builder: <strong>${p.team ? 'Yes' : 'Locked'}</strong></div>
                        </div>
                        <button type="button" class="ew-btn ${isCurrent ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" ${isCurrent ? 'disabled' : ''} data-upgrade-plan="${p.code}">
                            ${isCurrent ? 'Current Plan' : `Switch to ${p.title}`}
                        </button>
                    </article>
                `;
            }).join('');
        }

        const pf = document.getElementById('brand-profile-form');
        if (pf) {
            ['brandName', 'legalEntityName', 'companyType', 'industryCategory', 'establishedYear', 'websiteUrl', 'instagramHandle', 'billingState', 'aboutCompany'].forEach((k) => {
                if (pf.elements[k]) pf.elements[k].value = state.profile[k] ?? '';
            });
        }
        document.getElementById('brand-seats-meta').textContent = `${state.members.length + 1} of ${planObj.seats} seats used`;
        const teamList = document.getElementById('brand-team-list');
        if (teamList) {
            teamList.innerHTML = state.members.length
                ? state.members.map((m) => `
                    <div class="ew-app-card">
                        <div><strong>${esc(m.fullName)}</strong> <span class="ew-app-meta">• ${esc(m.email)}</span></div>
                        <span class="ew-badge audition">${esc(m.role || 'MEMBER')}</span>
                    </div>
                `).join('')
                : '<p class="ew-app-meta">No additional team members invited yet.</p>';
        }
    }

    async function refreshAll() {
        try {
            const [o, p, t, c, r, w, m, inv, rev, notif] = await Promise.all([
                api('/overview'),
                api('/profile'),
                api('/talents'),
                api('/campaigns'),
                api('/requests'),
                api('/wishlist'),
                api('/team-members'),
                api('/invoices'),
                api('/reviews'),
                api('/notifications')
            ]);
            state.overview = o;
            state.profile = p;
            state.talents = t.length ? t : [...SAMPLE_TALENT];
            state.campaigns = c;
            state.requests = r;
            state.wishlist = w;
            state.members = m;
            state.invoices = inv;
            state.reviews = rev;
            state.notifications = notif;
            if ((o.planType || 'FREE') === 'PRO_3999') {
                state.squads = await api('/squads').catch(() => []);
            }
        } catch {
            // Keep sample state when backend data isn't seeded yet
        }
        renderAll();
    }

    document.addEventListener('click', async (e) => {
        const wishBtn = e.target.closest('[data-wish-talent]');
        if (wishBtn) {
            try {
                await api('/wishlist', { method: 'POST', body: JSON.stringify({ talentId: Number(wishBtn.dataset.wishTalent), folderName: 'Summer Lookbook' }) });
                await refreshAll();
                showToast('Saved to wishlist');
            } catch (err) {
                showToast(err.message);
            }
            return;
        }

        const remWish = e.target.closest('[data-remove-wish]');
        if (remWish) {
            try {
                await api(`/wishlist/${remWish.dataset.removeWish}`, { method: 'DELETE' });
                await refreshAll();
                showToast('Removed from wishlist');
            } catch (err) {
                showToast(err.message);
            }
            return;
        }

        const connBtn = e.target.closest('[data-connect-talent]');
        if (connBtn) {
            try {
                await api('/requests', { method: 'POST', body: JSON.stringify({ talentId: Number(connBtn.dataset.connectTalent) }) });
                await refreshAll();
                showToast('Connect request sent');
            } catch (err) {
                showToast(err.message);
            }
            return;
        }

        const reqAction = e.target.closest('[data-req-action]');
        if (reqAction) {
            try {
                await api(`/requests/${reqAction.dataset.reqId}/${reqAction.dataset.reqAction}`, { method: 'PATCH' });
                await refreshAll();
                showToast('Request updated');
            } catch (err) {
                showToast(err.message);
            }
            return;
        }

        const payInv = e.target.closest('[data-pay-invoice]');
        if (payInv) {
            try {
                await api(`/invoices/${payInv.dataset.payInvoice}/paid`, { method: 'PATCH' });
                await refreshAll();
                showToast('Invoice marked as PAID');
            } catch (err) {
                showToast(err.message);
            }
            return;
        }

        const upgBtn = e.target.closest('[data-upgrade-plan]');
        if (upgBtn) {
            try {
                await api('/subscription', { method: 'PUT', body: JSON.stringify({ planType: upgBtn.dataset.upgradePlan }) });
                await refreshAll();
                showToast('Subscription plan updated');
            } catch (err) {
                showToast(err.message);
            }
        }
    });

    document.getElementById('bt-filter-btn')?.addEventListener('click', async () => {
        const params = new URLSearchParams();
        const q = document.getElementById('bt-search')?.value.trim();
        const cat = document.getElementById('bt-category')?.value;
        const gender = document.getElementById('bt-gender')?.value;
        const contract = document.getElementById('bt-contract')?.value;
        const city = document.getElementById('bt-city')?.value;
        const lang = document.getElementById('bt-language')?.value;
        const maxB = document.getElementById('bt-max-budget')?.value;
        const ver = document.getElementById('bt-verified')?.checked;
        if (q) params.set('q', q);
        if (cat) params.set('category', cat);
        if (gender) params.set('gender', gender);
        if (contract) params.set('contractType', contract);
        if (city) params.set('city', city);
        if (lang) params.set('language', lang);
        if (maxB) params.set('maxBudget', maxB);
        if (ver) params.set('verified', 'true');
        try {
            state.talents = await api(`/talents?${params.toString()}`);
            renderAll();
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-ai-match-btn')?.addEventListener('click', async () => {
        try {
            const matches = await api('/ai-matches');
            const box = document.getElementById('brand-ai-results');
            if (box) {
                box.innerHTML = matches.length
                    ? matches.map((m) => `<div class="ew-app-meta">★ <strong>${esc(m.fullName)}</strong> (${esc(m.category)} • ${esc(m.city)})</div>`).join('')
                    : '<p class="ew-app-meta">No AI matches found yet.</p>';
            }
            showToast(`Found ${matches.length} AI matches`);
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-export-csv')?.addEventListener('click', () => {
        if ((state.overview.planType || 'FREE') === 'FREE') {
            showToast('Report export unlocks on Premium and Pro.');
            return;
        }
        const csv = [
            'Metric,Value',
            `Active campaigns,${state.overview.activeCampaigns}`,
            `Pending requests,${state.overview.pendingRequests}`,
            `Wishlisted talent,${state.overview.wishlistedTalent}`
        ].join('\n');
        const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
        const a = document.createElement('a');
        a.href = url;
        a.download = 'brand-analytics.csv';
        a.click();
        URL.revokeObjectURL(url);
    });

    document.getElementById('brand-campaign-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.currentTarget);
        const payload = Object.fromEntries(fd.entries());
        payload.budgetPerTalent = Number(payload.budgetPerTalent || 0);
        payload.productShippingRequired = fd.get('productShippingRequired') === 'on';
        try {
            await api('/campaigns', { method: 'POST', body: JSON.stringify(payload) });
            e.currentTarget.reset();
            await refreshAll();
            showToast('Campaign posted');
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-invoice-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const subtotal = Number(document.getElementById('brand-invoice-amount')?.value || 0);
        try {
            await api('/invoices', { method: 'POST', body: JSON.stringify({ subtotal }) });
            e.currentTarget.reset();
            await refreshAll();
            showToast('GST/TDS invoice created');
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-profile-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = Object.fromEntries(new FormData(e.currentTarget).entries());
        if (payload.establishedYear) payload.establishedYear = Number(payload.establishedYear);
        try {
            await api('/profile', { method: 'PUT', body: JSON.stringify(payload) });
            await refreshAll();
            showToast('Company profile saved');
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-team-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = Object.fromEntries(new FormData(e.currentTarget).entries());
        try {
            await api('/team-members', { method: 'POST', body: JSON.stringify(payload) });
            e.currentTarget.reset();
            await refreshAll();
            showToast('Team member invited');
        } catch (err) {
            showToast(err.message);
        }
    });

    document.getElementById('brand-chat-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const talentId = document.getElementById('brand-chat-talent')?.value;
        const input = document.getElementById('brand-chat-input');
        const message = input?.value.trim();
        if (!talentId || !message) return;
        try {
            await api(`/chat/${talentId}`, { method: 'POST', body: JSON.stringify({ message }) });
            input.value = '';
            const msgs = await api(`/chat/${talentId}`);
            const box = document.getElementById('brand-chat-messages');
            if (box) {
                box.innerHTML = msgs.map((m) => `<div class="ew-ticket-card"><strong>${esc(m.senderType)}</strong>: ${esc(m.message)}</div>`).join('');
            }
        } catch (err) {
            showToast(err.message);
        }
    });

    document.addEventListener('submit', async (e) => {
        if (e.target.id !== 'brand-squad-form') return;
        e.preventDefault();
        const payload = Object.fromEntries(new FormData(e.target).entries());
        try {
            await api('/squads', { method: 'POST', body: JSON.stringify(payload) });
            e.target.reset();
            await refreshAll();
            showToast('Creator squad created');
        } catch (err) {
            showToast(err.message);
        }
    });

    refreshAll();
})();
