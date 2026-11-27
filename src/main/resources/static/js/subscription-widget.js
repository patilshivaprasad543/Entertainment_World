(() => {
    const subRoot = document.getElementById('ew-subscription-root');
    const socialRoot = document.getElementById('ew-social-root');
    if (!subRoot && !socialRoot) return;

    let billingCycle = 'MONTHLY';
    let activeChatUserId = null;

    async function api(url, options = {}) {
        const res = await fetch(url, options);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Request failed (${res.status})`);
        }
        return res.json();
    }

    function esc(t) {
        const d = document.createElement('div');
        d.textContent = t ?? '';
        return d.innerHTML;
    }

    // ==================== MY SUBSCRIPTION ====================
    async function loadSubscriptions() {
        if (!subRoot) return;
        try {
            const data = await api('/api/subscriptions/my');
            const active = data.activeSubscription;
            const plans = data.availablePlans || [];
            const history = data.history || [];

            subRoot.innerHTML = `
                <section class="panel-card" style="margin-top:1.5rem;">
                    <div class="panel-header">
                        <div>
                            <span class="module-tag">${esc(data.roleDisplayName)}</span>
                            <h2>My Subscription</h2>
                        </div>
                        <div class="btn-row">
                            <button type="button" id="ew-sub-m" class="btn-sm ${billingCycle === 'MONTHLY' ? 'btn-primary' : 'btn-secondary'}">Monthly</button>
                            <button type="button" id="ew-sub-a" class="btn-sm ${billingCycle === 'ANNUAL' ? 'btn-primary' : 'btn-secondary'}">Yearly (20% off)</button>
                        </div>
                    </div>
                    <div id="ew-sub-banner" class="status-banner" hidden></div>
                    ${active ? `
                        <div class="verification-item" style="margin-bottom:1rem;">
                            <div class="verification-meta">
                                <h3>Current Plan: ${esc(active.planName)} <span class="pill pill-APPROVED">${esc(active.tier)}</span></h3>
                                <p>Status: <strong>${esc(active.status)}</strong> &bull; Cycle: ${esc(active.billingCycle)} &bull; Valid until: ${esc(active.endDate)}</p>
                                <p>Limits: ${active.usageLimit} active items &bull; ${active.teamAccountsLimit} team accounts &bull; Invoice: ${esc(active.invoiceNumber)}</p>
                            </div>
                            ${active.tier !== 'FREE' ? '<button type="button" id="ew-sub-cancel" class="btn-sm btn-danger">Cancel Plan</button>' : ''}
                        </div>
                    ` : ''}
                    <div class="dashboard-grid" id="ew-sub-plans-grid"></div>
                    <div style="margin-top:1.25rem;">
                        <h3 style="margin:0 0 0.65rem;">Subscription Billing History</h3>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead><tr><th>Invoice</th><th>Plan</th><th>Cycle</th><th>Total (incl. GST)</th><th>Status</th><th>Valid Until</th></tr></thead>
                                <tbody>
                                    ${history.length ? history.map((h) => `
                                        <tr>
                                            <td><strong>${esc(h.invoiceNumber)}</strong></td>
                                            <td>${esc(h.planName)} (${esc(h.tier)})</td>
                                            <td>${esc(h.billingCycle)}</td>
                                            <td>INR ${h.totalPaid}</td>
                                            <td><span class="pill pill-${h.status === 'ACTIVE' ? 'APPROVED' : 'CANCELLED'}">${esc(h.status)}</span></td>
                                            <td>${esc(h.endDate)}</td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="6">No subscription history yet.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>
            `;

            const grid = subRoot.querySelector('#ew-sub-plans-grid');
            plans.forEach((p) => {
                const isCurrent = active && active.planId === p.id && active.status === 'ACTIVE';
                const price = billingCycle === 'ANNUAL' ? p.annualPrice : p.monthlyPrice;
                const card = document.createElement('article');
                card.className = 'studio-work-card';
                if (isCurrent) card.style.borderColor = '#22c55e';
                card.innerHTML = `
                    <span class="module-tag">${esc(p.tier)}</span>
                    <h3>${esc(p.planName)}</h3>
                    <p style="font-size:1.35rem; font-weight:800; color:#141219;">INR ${price} <small style="font-size:0.8rem; color:#5c5966;">/${billingCycle === 'ANNUAL' ? 'yr' : 'mo'}</small></p>
                    <p><small>Limit: ${p.usageLimit} | Team: ${p.teamAccountsLimit}</small></p>
                    <ul style="margin:0.4rem 0; padding-left:1rem; font-size:0.82rem; color:#4b4754;">
                        ${(p.features || []).map((f) => `<li>${esc(f)}</li>`).join('')}
                    </ul>
                `;
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = `btn-sm ${isCurrent ? 'btn-secondary' : 'btn-primary'}`;
                btn.disabled = isCurrent;
                btn.textContent = isCurrent ? 'Current Active Plan' : `Activate ${p.tier}`;
                btn.addEventListener('click', async () => {
                    const banner = subRoot.querySelector('#ew-sub-banner');
                    try {
                        const res = await api('/api/subscriptions/subscribe', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ planId: p.id, billingCycle })
                        });
                        await loadSubscriptions();
                        const b2 = subRoot.querySelector('#ew-sub-banner');
                        if (b2) {
                            b2.textContent = `Activated ${res.planName} (${res.invoiceNumber})!`;
                            b2.className = 'status-banner success';
                            b2.hidden = false;
                        }
                    } catch (err) {
                        if (banner) {
                            banner.textContent = err.message;
                            banner.className = 'status-banner error';
                            banner.hidden = false;
                        }
                    }
                });
                card.appendChild(btn);
                grid.appendChild(card);
            });

            subRoot.querySelector('#ew-sub-m')?.addEventListener('click', () => { billingCycle = 'MONTHLY'; loadSubscriptions(); });
            subRoot.querySelector('#ew-sub-a')?.addEventListener('click', () => { billingCycle = 'ANNUAL'; loadSubscriptions(); });
            subRoot.querySelector('#ew-sub-cancel')?.addEventListener('click', async () => {
                await api('/api/subscriptions/cancel', { method: 'POST' });
                loadSubscriptions();
            });
        } catch (err) {
            subRoot.innerHTML = `<section class="panel-card"><p class="brand-subtitle">${esc(err.message)}</p></section>`;
        }
    }

    // ==================== SOCIAL, CHAT, WISHLIST, SUPPORT ====================
    async function loadSocial() {
        if (!socialRoot) return;
        try {
            const [directory, wishlist, tickets] = await Promise.all([
                api('/api/platform/directory'),
                api('/api/platform/wishlist'),
                api('/api/platform/support')
            ]);

            socialRoot.innerHTML = `
                <section class="panel-card" style="margin-top:1.5rem;">
                    <div class="panel-header"><h2>Network, Chat, Wishlist &amp; Support</h2></div>
                    <div class="dashboard-grid">
                        <div class="studio-work-card">
                            <h3>Followers &amp; Directory</h3>
                            <div id="ew-dir-list" style="display:flex; flex-direction:column; gap:0.55rem; max-height:260px; overflow-y:auto;"></div>
                        </div>
                        <div class="studio-work-card">
                            <h3 id="ew-chat-heading">Shared Chat</h3>
                            <div id="ew-chat-box" style="min-height:140px; max-height:200px; overflow-y:auto; background:#faf8f5; border:1px solid #e7e2d9; border-radius:10px; padding:0.65rem; font-size:0.84rem; color:#141219;">
                                <p class="brand-subtitle">Select a user from Directory to start chatting.</p>
                            </div>
                            <form id="ew-chat-form" class="btn-row" style="margin-top:0.5rem;">
                                <input type="text" id="ew-chat-input" placeholder="Type message..." style="flex:1; padding:0.45rem 0.65rem; border-radius:8px; border:1px solid #d6d0c4; background:#fdfcfa; color:#141219;">
                                <button type="submit" class="btn-sm btn-primary">Send</button>
                            </form>
                        </div>
                        <div class="studio-work-card">
                            <h3>Saved Wishlist (${wishlist.length})</h3>
                            <div id="ew-wish-list" style="display:flex; flex-direction:column; gap:0.45rem; max-height:240px; overflow-y:auto;"></div>
                        </div>
                    </div>

                    <div style="margin-top:1.25rem; border-top:1px solid #e7e2d9; padding-top:1rem;">
                        <h3 style="margin:0 0 0.65rem;">Help &amp; Support Tickets</h3>
                        <form id="ew-ticket-form" class="form-grid" style="margin-bottom:0.85rem;">
                            <div class="field-group">
                                <label for="ew-tkt-sub">Subject</label>
                                <input type="text" id="ew-tkt-sub" placeholder="Issue or question" required>
                            </div>
                            <div class="field-group">
                                <label for="ew-tkt-desc">Description</label>
                                <input type="text" id="ew-tkt-desc" placeholder="Describe your issue" required>
                            </div>
                            <div style="display:flex; align-items:flex-end;">
                                <button type="submit" class="btn-sm btn-primary">Submit Ticket</button>
                            </div>
                        </form>
                        <div id="ew-ticket-list" style="font-size:0.84rem; color:#4b4754;">
                            ${tickets.length ? tickets.map((t) => `<p><strong>#${t.id} ${esc(t.subject)}</strong> <span class="pill pill-${t.status === 'RESOLVED' ? 'APPROVED' : 'PENDING'}">${esc(t.status)}</span> — ${esc(t.description)} ${t.adminResponse ? `<em>(Admin: ${esc(t.adminResponse)})</em>` : ''}</p>`).join('') : '<p class="brand-subtitle">No support tickets submitted.</p>'}
                        </div>
                    </div>
                </section>
            `;

            const dirEl = socialRoot.querySelector('#ew-dir-list');
            directory.forEach((u) => {
                const row = document.createElement('div');
                row.style.cssText = 'display:flex; justify-content:space-between; align-items:center; gap:0.5rem; padding:0.45rem; border-radius:8px; background:rgba(255,255,255,0.04);';
                row.innerHTML = `
                    <div style="font-size:0.82rem;">
                        <strong>${esc(u.organizationName || u.fullName)}</strong><br>
                        <small style="color:#9aa3bd;">${esc(u.roleDisplayName)} &bull; ${u.followersCount} followers</small>
                    </div>
                    <div class="btn-row"></div>
                `;
                const btns = row.querySelector('.btn-row');
                const fBtn = document.createElement('button');
                fBtn.type = 'button';
                fBtn.className = `btn-sm ${u.following ? 'btn-secondary' : 'btn-primary'}`;
                fBtn.textContent = u.following ? 'Unfollow' : 'Follow';
                fBtn.addEventListener('click', async () => {
                    await api(`/api/platform/follow/${u.id}`, { method: u.following ? 'DELETE' : 'POST' });
                    loadSocial();
                });
                const cBtn = document.createElement('button');
                cBtn.type = 'button';
                cBtn.className = 'btn-sm btn-secondary';
                cBtn.textContent = 'Chat';
                cBtn.addEventListener('click', () => openChat(u.id, u.organizationName || u.fullName));
                btns.appendChild(fBtn);
                btns.appendChild(cBtn);
                dirEl.appendChild(row);
            });

            const wishEl = socialRoot.querySelector('#ew-wish-list');
            if (!wishlist.length) {
                wishEl.innerHTML = '<p class="brand-subtitle">No saved items yet.</p>';
            } else {
                wishlist.forEach((w) => {
                    const row = document.createElement('div');
                    row.style.cssText = 'display:flex; justify-content:space-between; align-items:center; gap:0.5rem; padding:0.45rem; border-radius:8px; background:rgba(255,255,255,0.04); font-size:0.82rem;';
                    row.innerHTML = `<span><strong>${esc(w.title)}</strong> <small>(${esc(w.itemType)})</small></span>`;
                    const rm = document.createElement('button');
                    rm.type = 'button';
                    rm.className = 'btn-sm btn-danger';
                    rm.textContent = 'Remove';
                    rm.addEventListener('click', async () => {
                        await api(`/api/platform/wishlist/${w.id}`, { method: 'DELETE' });
                        loadSocial();
                    });
                    row.appendChild(rm);
                    wishEl.appendChild(row);
                });
            }

            socialRoot.querySelector('#ew-chat-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                if (!activeChatUserId) return;
                const input = socialRoot.querySelector('#ew-chat-input');
                const text = input.value.trim();
                if (!text) return;
                await api('/api/platform/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ recipientId: activeChatUserId, messageText: text })
                });
                input.value = '';
                openChat(activeChatUserId, socialRoot.querySelector('#ew-chat-heading').dataset.name || 'Chat');
            });

            socialRoot.querySelector('#ew-ticket-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await api('/api/platform/support', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        subject: socialRoot.querySelector('#ew-tkt-sub').value,
                        description: socialRoot.querySelector('#ew-tkt-desc').value
                    })
                });
                loadSocial();
            });
        } catch (err) {
            // ignore if unauthenticated
        }
    }

    async function openChat(userId, name) {
        activeChatUserId = userId;
        const heading = socialRoot.querySelector('#ew-chat-heading');
        if (heading) {
            heading.textContent = `Chat with ${name}`;
            heading.dataset.name = name;
        }
        const box = socialRoot.querySelector('#ew-chat-box');
        const msgs = await api(`/api/platform/chat/${userId}`);
        box.innerHTML = msgs.length
            ? msgs.map((m) => `<p style="margin:0.25rem 0;"><strong>${esc(m.senderName)}:</strong> ${esc(m.messageText)}</p>`).join('')
            : '<p class="brand-subtitle">No messages yet. Say hello!</p>';
        box.scrollTop = box.scrollHeight;
    }

    loadSubscriptions();
    loadSocial();
})();
