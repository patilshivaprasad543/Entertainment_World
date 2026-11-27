(() => {
    const root = document.getElementById('adm-subscription-root');
    if (!root) return;

    async function api(url, options = {}) {
        const res = await fetch(url, options);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
    }

    function esc(t) {
        const d = document.createElement('div');
        d.textContent = t ?? '';
        return d.innerHTML;
    }

    async function loadAdminSubscriptions() {
        try {
            const [analytics, plans, userSubs, commissions, tickets] = await Promise.all([
                api('/api/admin/subscriptions/analytics'),
                api('/api/admin/subscriptions/plans'),
                api('/api/admin/subscriptions/users'),
                api('/api/admin/subscriptions/commissions'),
                api('/api/admin/support-tickets')
            ]);

            root.innerHTML = `
                <section class="verification-section">
                    <div class="verification-header">
                        <h2>Subscription Management &amp; Revenue Analytics</h2>
                        <button type="button" id="adm-sub-refresh" class="signout-btn">Refresh</button>
                    </div>
                    <div class="kpi-grid">
                        <div class="kpi-card"><span>Total Subscriptions</span><strong>${analytics.totalSubscriptions}</strong></div>
                        <div class="kpi-card"><span>Active Subscriptions</span><strong>${analytics.activeSubscriptions}</strong></div>
                        <div class="kpi-card"><span>Paid Active</span><strong>${analytics.paidActiveSubscriptions}</strong></div>
                        <div class="kpi-card"><span>Subscription Revenue</span><strong>INR ${analytics.totalRevenue}</strong></div>
                    </div>

                    <div class="panel-card">
                        <h3>Create / Update Subscription Plan</h3>
                        <form id="adm-plan-form" class="form-grid" style="margin-top:0.75rem;">
                            <input type="hidden" id="adm-plan-id">
                            <div class="field-group">
                                <label for="adm-plan-role">Dashboard</label>
                                <select id="adm-plan-role">
                                    <option value="ACTOR">Professional</option>
                                    <option value="VENDOR">Vendors</option>
                                    <option value="CASTING">Casting Agency &amp; Production House</option>
                                    <option value="BRAND">Brands &amp; Advertisement Agencies</option>
                                    <option value="EVENT_MANAGEMENT">Event Management</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label for="adm-plan-tier">Tier</label>
                                <select id="adm-plan-tier"><option value="FREE">FREE</option><option value="PREMIUM">PREMIUM</option><option value="PRO">PRO</option></select>
                            </div>
                            <div class="field-group"><label for="adm-plan-name">Plan Name</label><input type="text" id="adm-plan-name" required></div>
                            <div class="field-group"><label for="adm-plan-monthly">Monthly Price (INR)</label><input type="number" id="adm-plan-monthly" min="0" step="0.01" required></div>
                            <div class="field-group"><label for="adm-plan-limit">Usage Limit</label><input type="number" id="adm-plan-limit" min="1" value="5" required></div>
                            <div class="field-group"><label for="adm-plan-team">Team Accounts</label><input type="number" id="adm-plan-team" min="1" value="3" required></div>
                            <div class="field-group full-span"><label for="adm-plan-features">Features (pipe | separated)</label><input type="text" id="adm-plan-features" placeholder="Feature 1|Feature 2|Feature 3" required></div>
                            <div class="full-span"><button type="submit" class="btn-sm btn-primary">Save Plan</button></div>
                        </form>
                    </div>

                    <div class="panel-card">
                        <h3>All Subscription Plans (${plans.length})</h3>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead><tr><th>Dashboard</th><th>Plan</th><th>Monthly</th><th>Annual</th><th>Limits</th><th>Status</th><th>Action</th></tr></thead>
                                <tbody id="adm-plans-tbody"></tbody>
                            </table>
                        </div>
                    </div>

                    <div class="panel-card">
                        <h3>User Subscriptions (${userSubs.length})</h3>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead><tr><th>User</th><th>Role</th><th>Plan</th><th>Billing</th><th>Paid</th><th>Status</th><th>End Date</th></tr></thead>
                                <tbody>
                                    ${userSubs.map((s) => `
                                        <tr>
                                            <td><strong>${esc(s.userName)}</strong><br><small>${esc(s.userEmail)}</small></td>
                                            <td>${esc(s.userRole)}</td>
                                            <td>${esc(s.planName)}</td>
                                            <td>${esc(s.billingCycle)}</td>
                                            <td>INR ${s.totalPaid}</td>
                                            <td><span class="pill pill-${s.status === 'ACTIVE' ? 'APPROVED' : 'CANCELLED'}">${esc(s.status)}</span></td>
                                            <td>${esc(s.endDate)}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="panel-card">
                        <h3>Commission Configuration</h3>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead><tr><th>Module</th><th>Commission %</th><th>Description</th><th>Update</th></tr></thead>
                                <tbody id="adm-comm-tbody"></tbody>
                            </table>
                        </div>
                    </div>

                    <div class="panel-card">
                        <h3>Support &amp; Complaints (${tickets.length})</h3>
                        <div id="adm-tickets-list"></div>
                    </div>
                </section>
            `;

            const pBody = root.querySelector('#adm-plans-tbody');
            plans.forEach((p) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${esc(p.dashboardName)}</td>
                    <td><strong>${esc(p.planName)}</strong> (${esc(p.tier)})</td>
                    <td>INR ${p.monthlyPrice}</td>
                    <td>INR ${p.annualPrice}</td>
                    <td>${p.usageLimit} / ${p.teamAccountsLimit} team</td>
                    <td><span class="pill pill-${p.active ? 'APPROVED' : 'REJECTED'}">${p.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                    <td class="btn-row"></td>
                `;
                const editBtn = document.createElement('button');
                editBtn.type = 'button';
                editBtn.className = 'btn-sm btn-secondary';
                editBtn.textContent = 'Edit';
                editBtn.addEventListener('click', () => {
                    root.querySelector('#adm-plan-id').value = p.id;
                    root.querySelector('#adm-plan-role').value = p.dashboardRole;
                    root.querySelector('#adm-plan-tier').value = p.tier;
                    root.querySelector('#adm-plan-name').value = p.planName;
                    root.querySelector('#adm-plan-monthly').value = p.monthlyPrice;
                    root.querySelector('#adm-plan-limit').value = p.usageLimit;
                    root.querySelector('#adm-plan-team').value = p.teamAccountsLimit;
                    root.querySelector('#adm-plan-features').value = p.featuresSummary || '';
                });
                const togBtn = document.createElement('button');
                togBtn.type = 'button';
                togBtn.className = 'btn-sm btn-primary';
                togBtn.textContent = p.active ? 'Deactivate' : 'Activate';
                togBtn.addEventListener('click', async () => {
                    await api(`/api/admin/subscriptions/plans/${p.id}/toggle`, { method: 'PATCH' });
                    loadAdminSubscriptions();
                });
                tr.lastElementChild.appendChild(editBtn);
                tr.lastElementChild.appendChild(togBtn);
                pBody.appendChild(tr);
            });

            const cBody = root.querySelector('#adm-comm-tbody');
            commissions.forEach((c) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${esc(c.moduleRole)}</strong></td>
                    <td><input type="number" step="0.5" value="${c.commissionPercent}" style="width:90px; padding:0.35rem; border-radius:6px; background:#fdfcfa; color:#141219; border:1px solid #d6d0c4;"></td>
                    <td>${esc(c.description)}</td>
                    <td><button type="button" class="btn-sm btn-primary">Save</button></td>
                `;
                tr.querySelector('button').addEventListener('click', async () => {
                    const val = tr.querySelector('input').value;
                    await api(`/api/admin/subscriptions/commissions/${c.id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ commissionPercent: Number(val) })
                    });
                    loadAdminSubscriptions();
                });
                cBody.appendChild(tr);
            });

            const tEl = root.querySelector('#adm-tickets-list');
            if (!tickets.length) {
                tEl.innerHTML = '<p class="brand-subtitle">No support tickets.</p>';
            } else {
                tickets.forEach((t) => {
                    const div = document.createElement('div');
                    div.className = 'verification-item';
                    div.innerHTML = `
                        <div class="verification-meta">
                            <h3>#${t.id} ${esc(t.subject)} <span class="pill pill-${t.status === 'RESOLVED' ? 'APPROVED' : 'PENDING'}">${esc(t.status)}</span></h3>
                            <p>${esc(t.userName)} (${esc(t.userRole)}) — ${esc(t.description)}</p>
                        </div>
                    `;
                    if (t.status !== 'RESOLVED') {
                        const rBtn = document.createElement('button');
                        rBtn.type = 'button';
                        rBtn.className = 'btn-sm btn-success';
                        rBtn.textContent = 'Resolve';
                        rBtn.addEventListener('click', async () => {
                            const response = prompt('Resolution message:', 'Resolved by Super Admin') || 'Resolved';
                            await api(`/api/admin/support-tickets/${t.id}/resolve`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ response })
                            });
                            loadAdminSubscriptions();
                        });
                        div.appendChild(rBtn);
                    }
                    tEl.appendChild(div);
                });
            }

            root.querySelector('#adm-plan-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                const id = root.querySelector('#adm-plan-id').value;
                const payload = {
                    dashboardRole: root.querySelector('#adm-plan-role').value,
                    tier: root.querySelector('#adm-plan-tier').value,
                    planName: root.querySelector('#adm-plan-name').value,
                    monthlyPrice: Number(root.querySelector('#adm-plan-monthly').value),
                    usageLimit: Number(root.querySelector('#adm-plan-limit').value),
                    teamAccountsLimit: Number(root.querySelector('#adm-plan-team').value),
                    featuresSummary: root.querySelector('#adm-plan-features').value,
                    active: true,
                    visible: true
                };
                await api(id ? `/api/admin/subscriptions/plans/${id}` : '/api/admin/subscriptions/plans', {
                    method: id ? 'PUT' : 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                loadAdminSubscriptions();
            });

            root.querySelector('#adm-sub-refresh').addEventListener('click', loadAdminSubscriptions);
        } catch (err) {
            // ignore
        }
    }

    loadAdminSubscriptions();
})();
