(() => {
    const banner = document.getElementById('cv-banner');
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    const views = document.querySelectorAll('.view-section');
    const sidebar = document.getElementById('cv-sidebar');
    let bookingsCache = [];
    let profileCache = null;

    const CATEGORY_LABELS = {
        'Production & Camera Services': { sub: 'Camera / Equipment Spec', qty: 'Camera Crew Size' },
        'Video Editing & Post-Production': { sub: 'Software / VFX Tools', qty: 'Editors / Team Size' },
        'Audio & Music Production': { sub: 'Studio / Mixing Spec', qty: 'Studio Rooms' },
        'Lighting & Equipment Rental': { sub: 'Equipment Category', qty: 'Available Inventory Qty' },
        'Art, Set, Costume & Makeup': { sub: 'Art / Costume / Makeup Type', qty: 'Artisans / Team Size' },
        'Event & Logistics Services': { sub: 'Logistics / Setup Type', qty: 'Staff / Vehicles Count' },
        'Marketing & Promotion Services': { sub: 'Platforms Supported', qty: 'Campaign Slots' },
        'Other Entertainment Services': { sub: 'Custom Specialty', qty: 'Capacity / Units' }
    };

    function notify(msg, type = 'success') {
        banner.textContent = msg;
        banner.className = `status-banner ${type}`;
        banner.hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
    function esc(v) {
        return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
    async function api(url, options = {}) {
        const res = await fetch(url, options);
        const ct = res.headers.get('content-type') || '';
        const data = ct.includes('application/json') ? await res.json().catch(() => ({})) : null;
        if (!res.ok) throw new Error(data?.message || `Request failed (${res.status})`);
        return data ?? res;
    }

    function switchView(target) {
        banner.hidden = true;
        navItems.forEach((b) => b.classList.toggle('active', b.dataset.view === target));
        views.forEach((v) => { v.hidden = v.id !== `view-${target}`; });
        if (sidebar) sidebar.classList.remove('open');
    }
    function openSection(view) {
        switchView(view);
        if (view === 'overview' || view === 'reports' || view === 'notifications') loadDashboard();
        if (view === 'profile' || view === 'verification' || view === 'marketing') loadProfile();
        if (view === 'services' || view === 'pricing' || view === 'portfolio') loadServices();
        if (view === 'availability') loadAvailability();
        if (view === 'enquiries' || view === 'bookings') loadBookings();
        if (view === 'quotations') loadQuotations();
        if (view === 'projects') loadProjects();
        if (view === 'documents') loadDocuments();
        if (view === 'invoices') loadInvoices();
        if (view === 'clients') loadClients();
        if (view === 'team') loadTeam();
    }
    navItems.forEach((b) => b.addEventListener('click', () => openSection(b.dataset.view)));
    document.querySelectorAll('[data-jump]').forEach((el) => el.addEventListener('click', (e) => { e.preventDefault(); openSection(el.dataset.jump); }));
    document.getElementById('cv-mobile-btn')?.addEventListener('click', () => sidebar.classList.toggle('open'));
    document.getElementById('cv-modal-close')?.addEventListener('click', () => { document.getElementById('cv-modal').hidden = true; });

    function renderBars(elId, items, lKey, vKey, prefix = '') {
        const el = document.getElementById(elId);
        if (!el) return;
        const max = Math.max(...(items || []).map((x) => Number(x[vKey] || 0)), 1);
        el.innerHTML = (items || []).map((x) => {
            const val = Number(x[vKey] || 0);
            return `<div class="bar-row"><span>${esc(x[lKey])}</span><div class="bar-track"><div class="bar-fill" style="width:${Math.round((val / max) * 100)}%"></div></div><strong>${prefix}${val.toLocaleString('en-IN')}</strong></div>`;
        }).join('') || '<p class="brand-subtitle">No data yet.</p>';
    }

    async function loadDashboard() {
        try {
            const d = await api('/api/category-vendors/dashboard');
            const p = d.profile;
            profileCache = p;
            const labels = CATEGORY_LABELS[p.vendorCategory] || { sub: 'Sub-Category', qty: 'Quantity' };
            document.getElementById('cs-sub-lbl').textContent = labels.sub;
            document.getElementById('cs-qty-lbl').textContent = labels.qty;
            document.getElementById('cv-category-pill').textContent = p.vendorCategory;
            document.getElementById('cv-main-heading').textContent = `${p.vendorCategory} Dashboard`;
            document.getElementById('cv-cat-title').textContent = p.businessName || p.vendorCategory;
            document.getElementById('cv-nav-biz').textContent = p.businessName || 'Entertainment World';
            document.getElementById('cv-nav-subtitle').textContent = p.vendorCategory.toUpperCase();
            document.getElementById('cv-plan-badge').textContent = d.currentPlan;
            document.getElementById('cv-ver-cat').textContent = p.vendorCategory;
            document.getElementById('k-tot-svc').textContent = d.totalServices;
            document.getElementById('k-act-svc').textContent = d.activeServices;
            document.getElementById('k-enq').textContent = d.newEnquiries;
            document.getElementById('k-qtn').textContent = d.pendingQuotations;
            document.getElementById('k-conf').textContent = d.confirmedBookings;
            document.getElementById('k-act-prj').textContent = d.activeProjects;
            document.getElementById('k-cmp-prj').textContent = d.completedProjects;
            document.getElementById('k-rev').textContent = `₹${Number(d.monthlyRevenue || 0).toLocaleString('en-IN')}`;
            document.getElementById('k-pend-pay').textContent = `₹${Number(d.pendingPayments || 0).toLocaleString('en-IN')}`;
            document.getElementById('k-conv').textContent = `${d.conversionRate}%`;
            document.getElementById('k-prof-pct').textContent = `${d.profileCompletion}%`;
            document.getElementById('k-plan').textContent = d.currentPlan;
            document.getElementById('cv-top-notif-count').textContent = (d.notifications || []).filter((n) => !n.read).length;

            renderBars('cv-rev-chart', d.monthlyChart, 'month', 'revenue', '₹');
            renderBars('cv-bk-chart', d.monthlyChart, 'month', 'bookings');

            document.getElementById('cv-recent-bookings').innerHTML = (d.recentBookings || []).map((b) =>
                `<p><strong>${esc(b.bookingCode)}</strong> — ${esc(b.customerName)} (${esc(b.eventDate)}) • ₹${b.quotedAmount} <span class="pill">${esc(b.status)}</span></p>`
            ).join('') || '<p class="brand-subtitle">No enquiries or bookings yet.</p>';

            document.getElementById('cv-top-services').innerHTML = (d.topServices || []).map((s) =>
                `<p><strong>${esc(s.serviceName)}</strong> — ₹${s.price} (${esc(s.pricingUnit)})</p>`
            ).join('') || '<p class="brand-subtitle">No services created yet.</p>';

            document.getElementById('cv-reports-box').innerHTML = `
                <p><strong>Category:</strong> ${esc(p.vendorCategory)} | <strong>Services:</strong> ${d.totalServices} | <strong>Confirmed Bookings:</strong> ${d.confirmedBookings} | <strong>Active Projects:</strong> ${d.activeProjects} | <strong>Revenue:</strong> ₹${Number(d.monthlyRevenue || 0).toLocaleString('en-IN')}</p>
            `;
            document.getElementById('cv-notifs').innerHTML = (d.notifications || []).map((n) =>
                `<p><strong>${esc(n.title)}</strong>: ${esc(n.message)}</p>`
            ).join('') || '<p class="brand-subtitle">No notifications yet.</p>';
        } catch (err) { notify(err.message, 'error'); }
    }

    async function loadProfile() {
        const p = await api('/api/category-vendors/profile');
        profileCache = p;
        document.getElementById('cp-completion-lbl').textContent = `Profile Completion: ${p.profileCompletion || 0}%`;
        document.getElementById('cp-biz').value = p.businessName || '';
        document.getElementById('cp-person').value = p.contactPerson || '';
        document.getElementById('cp-phone').value = p.phone || '';
        document.getElementById('cp-alt-phone').value = p.alternatePhone || '';
        document.getElementById('cp-exp').value = p.yearsExperience ?? 0;
        document.getElementById('cp-web').value = p.websiteUrl || '';
        document.getElementById('cp-country').value = p.country || 'India';
        document.getElementById('cp-state').value = p.state || '';
        document.getElementById('cp-city').value = p.city || '';
        document.getElementById('cp-area').value = p.areaLocality || '';
        document.getElementById('cp-pin').value = p.pincode || '';
        document.getElementById('cp-hours').value = p.workingHours || '';
        document.getElementById('cp-areas').value = p.serviceAreas || '';
        document.getElementById('cp-langs').value = p.languagesSupported || '';
        document.getElementById('cp-addr').value = p.address || '';
        document.getElementById('cp-social').value = p.socialLinks || '';
        document.getElementById('cp-about').value = p.aboutBusiness || '';
        document.getElementById('mk-featured').checked = Boolean(p.featuredListing);
        document.getElementById('cv-ver-badge').textContent = p.verificationStatus || 'APPROVED';
    }
    document.getElementById('cp-preview-btn')?.addEventListener('click', () => {
        if (!profileCache) return;
        document.getElementById('cv-modal-title').textContent = profileCache.businessName;
        document.getElementById('cv-modal-body').innerHTML = `
            <p><strong>Category:</strong> ${esc(profileCache.vendorCategory)}</p>
            <p><strong>Location:</strong> ${esc(profileCache.areaLocality)}, ${esc(profileCache.city)}, ${esc(profileCache.state)}</p>
            <p><strong>Contact:</strong> ${esc(profileCache.contactPerson)} (${esc(profileCache.phone)})</p>
            <p>${esc(profileCache.aboutBusiness)}</p>
        `;
        document.getElementById('cv-modal').hidden = false;
    });
    document.getElementById('cv-profile-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/category-vendors/profile', {
                method: 'PUT', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    businessName: document.getElementById('cp-biz').value,
                    contactPerson: document.getElementById('cp-person').value,
                    phone: document.getElementById('cp-phone').value,
                    alternatePhone: document.getElementById('cp-alt-phone').value,
                    yearsExperience: Number(document.getElementById('cp-exp').value || 0),
                    websiteUrl: document.getElementById('cp-web').value,
                    country: document.getElementById('cp-country').value,
                    state: document.getElementById('cp-state').value,
                    city: document.getElementById('cp-city').value,
                    areaLocality: document.getElementById('cp-area').value,
                    pincode: document.getElementById('cp-pin').value,
                    workingHours: document.getElementById('cp-hours').value,
                    serviceAreas: document.getElementById('cp-areas').value,
                    languagesSupported: document.getElementById('cp-langs').value,
                    address: document.getElementById('cp-addr').value,
                    socialLinks: document.getElementById('cp-social').value,
                    aboutBusiness: document.getElementById('cp-about').value
                })
            });
            notify('Business profile updated.');
            loadProfile();
        } catch (err) { notify(err.message, 'error'); }
    });
    document.getElementById('cp-logo-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const f = document.getElementById('cp-logo-file').files[0];
        if (!f) return;
        const fd = new FormData(); fd.append('kind', 'LOGO'); fd.append('file', f);
        await api('/api/category-vendors/profile/media', { method: 'POST', body: fd });
        notify('Logo uploaded.');
    });
    document.getElementById('cp-cover-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const f = document.getElementById('cp-cover-file').files[0];
        if (!f) return;
        const fd = new FormData(); fd.append('kind', 'COVER'); fd.append('file', f);
        await api('/api/category-vendors/profile/media', { method: 'POST', body: fd });
        notify('Cover uploaded.');
    });

    async function loadServices() {
        const rows = await api('/api/category-vendors/services');
        const grid = document.getElementById('cv-services-grid');
        const port = document.getElementById('cv-portfolio-list');
        grid.innerHTML = rows.length ? '' : '<p class="brand-subtitle">No services added yet.</p>';
        port.innerHTML = '';
        document.getElementById('cv-pricing-list').innerHTML = rows.map((s) =>
            `<p><strong>${esc(s.serviceName)}</strong> — ₹${s.price} (${esc(s.pricingUnit)}) • Qty: ${s.quantity}</p>`
        ).join('') || '<p class="brand-subtitle">No pricing packages yet.</p>';

        rows.forEach((s) => {
            const c = document.createElement('article');
            c.className = 'studio-work-card';
            c.innerHTML = `
                <span class="module-tag">${esc(s.subCategory || s.vendorCategory)}</span>
                <h3>${esc(s.serviceName)}</h3>
                <p><strong>₹${Number(s.price).toLocaleString('en-IN')}</strong> (${esc(s.pricingUnit)}) • Qty: ${s.quantity}</p>
                <p><small>${esc(s.description || '')}</small></p>
                <div class="btn-row">
                    <button type="button" class="btn-sm btn-secondary cs-edit">Edit</button>
                    <button type="button" class="btn-sm btn-primary cs-tog">${s.active ? 'Deactivate' : 'Activate'}</button>
                    <button type="button" class="btn-sm btn-danger cs-del">Delete</button>
                </div>
            `;
            c.querySelector('.cs-edit').addEventListener('click', () => {
                document.getElementById('cs-id').value = s.id;
                document.getElementById('cs-name').value = s.serviceName;
                document.getElementById('cs-sub').value = s.subCategory || '';
                document.getElementById('cs-price').value = s.price;
                document.getElementById('cs-unit').value = s.pricingUnit;
                document.getElementById('cs-qty').value = s.quantity;
                document.getElementById('cs-loc').value = s.location || '';
                document.getElementById('cs-port').value = s.portfolioUrl || '';
                document.getElementById('cs-desc').value = s.description || '';
            });
            c.querySelector('.cs-tog').addEventListener('click', async () => { await api(`/api/category-vendors/services/${s.id}/toggle`, { method: 'PATCH' }); loadServices(); });
            c.querySelector('.cs-del').addEventListener('click', async () => { await api(`/api/category-vendors/services/${s.id}`, { method: 'DELETE' }); loadServices(); });
            grid.appendChild(c);

            if (s.portfolioUrl) {
                const pc = document.createElement('article');
                pc.className = 'studio-work-card';
                pc.innerHTML = `<h3>${esc(s.serviceName)}</h3><a href="${esc(s.portfolioUrl)}" target="_blank" class="btn-sm btn-secondary">Open Portfolio Media</a>`;
                port.appendChild(pc);
            }
        });
        if (!port.innerHTML) port.innerHTML = '<p class="brand-subtitle">Add Portfolio URLs in your services to showcase work here.</p>';
    }
    document.getElementById('cs-reset')?.addEventListener('click', () => { document.getElementById('cv-service-form').reset(); document.getElementById('cs-id').value = ''; });
    document.getElementById('cv-service-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('cs-id').value;
        try {
            await api(id ? `/api/category-vendors/services/${id}` : '/api/category-vendors/services', {
                method: id ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    serviceName: document.getElementById('cs-name').value,
                    subCategory: document.getElementById('cs-sub').value,
                    price: Number(document.getElementById('cs-price').value),
                    pricingUnit: document.getElementById('cs-unit').value,
                    quantity: Number(document.getElementById('cs-qty').value || 1),
                    location: document.getElementById('cs-loc').value,
                    portfolioUrl: document.getElementById('cs-port').value,
                    description: document.getElementById('cs-desc').value
                })
            });
            notify('Service saved.');
            document.getElementById('cs-reset').click();
            loadServices();
        } catch (err) { notify(err.message, 'error'); }
    });

    async function loadAvailability() {
        const rows = await api('/api/category-vendors/availability');
        document.getElementById('cv-avail-list').innerHTML = rows.map((r) => `<p><strong>${esc(r.date)}</strong> — <span class="pill">${esc(r.status)}</span> ${esc(r.note)}</p>`).join('') || '<p class="brand-subtitle">No availability overrides set.</p>';
    }
    document.getElementById('cv-avail-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/availability', {
            method: 'PUT', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ date: document.getElementById('ca-date').value, status: document.getElementById('ca-status').value, note: document.getElementById('ca-note').value })
        });
        notify('Availability updated.');
        loadAvailability();
    });

    async function loadBookings() {
        bookingsCache = await api('/api/category-vendors/bookings');
        const enqBody = document.getElementById('cv-enq-tbody');
        const bkBody = document.getElementById('cv-bk-tbody');
        enqBody.innerHTML = bookingsCache.length ? '' : '<tr><td colspan="7">No enquiries yet.</td></tr>';
        bkBody.innerHTML = bookingsCache.length ? '' : '<tr><td colspan="7">No bookings yet.</td></tr>';
        bookingsCache.forEach((b) => {
            const makeRow = () => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${esc(b.bookingCode)}</strong></td><td>${esc(b.customerName)}</td><td>${esc(b.eventName)}</td>
                    <td>${esc(b.eventDate)}</td><td>₹${b.quotedAmount}</td><td><span class="pill">${esc(b.status)}</span></td>
                    <td class="btn-row">
                        <button type="button" class="btn-sm btn-secondary cb-q">Quote</button>
                        <button type="button" class="btn-sm btn-primary cb-c">Confirm</button>
                        <button type="button" class="btn-sm btn-secondary cb-d">Complete</button>
                        <button type="button" class="btn-sm btn-danger cb-x">Cancel</button>
                    </td>
                `;
                const upd = async (st) => {
                    try {
                        await api(`/api/category-vendors/bookings/${b.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: st }) });
                        notify(`Booking ${b.bookingCode} updated to ${st}.`);
                        loadBookings();
                    } catch (err) { notify(err.message, 'error'); }
                };
                tr.querySelector('.cb-q').addEventListener('click', () => upd('QUOTATION_SENT'));
                tr.querySelector('.cb-c').addEventListener('click', () => upd('CONFIRMED'));
                tr.querySelector('.cb-d').addEventListener('click', () => upd('COMPLETED'));
                tr.querySelector('.cb-x').addEventListener('click', () => upd('CANCELLED'));
                return tr;
            };
            enqBody.appendChild(makeRow());
            bkBody.appendChild(makeRow());
        });
    }
    document.getElementById('cv-bk-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/bookings', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ customerName: document.getElementById('cb-cust').value, customerEmail: document.getElementById('cb-email').value, eventName: document.getElementById('cb-ev').value, projectLocation: document.getElementById('cb-loc').value, eventDate: document.getElementById('cb-date').value, quotedAmount: Number(document.getElementById('cb-amt').value) })
        });
        notify('Enquiry recorded.');
        document.getElementById('cv-bk-form').reset();
        loadBookings();
    });

    async function loadQuotations() {
        const rows = await api('/api/category-vendors/quotations');
        document.getElementById('cv-qt-tbody').innerHTML = rows.length
            ? rows.map((q) => `<tr><td><strong>${esc(q.quotationCode)}</strong></td><td>${esc(q.customerName)}</td><td>${esc(q.serviceName)}</td><td>₹${q.grandTotal}</td><td><span class="pill">${esc(q.status)}</span></td><td><a href="${q.pdfUrl}" target="_blank" class="btn-sm btn-secondary">PDF</a></td></tr>`).join('')
            : '<tr><td colspan="6">No quotations created yet.</td></tr>';
    }
    document.getElementById('cv-qt-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/quotations', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ customerName: document.getElementById('cq-cust').value, customerEmail: document.getElementById('cq-email').value, serviceName: document.getElementById('cq-svc').value, quantity: Number(document.getElementById('cq-qty').value), unitPrice: Number(document.getElementById('cq-price').value), taxAmount: Number(document.getElementById('cq-tax').value), discount: Number(document.getElementById('cq-disc').value), validUntil: document.getElementById('cq-valid').value || null, status: 'SENT' })
        });
        notify('Quotation sent.');
        document.getElementById('cv-qt-form').reset();
        loadQuotations();
    });

    async function loadProjects() {
        const rows = await api('/api/category-vendors/projects');
        document.getElementById('cv-prj-tbody').innerHTML = rows.length
            ? rows.map((p) => `<tr><td><strong>${esc(p.projectCode)}</strong></td><td>${esc(p.projectName)}</td><td>${esc(p.clientName || '-')}</td><td>${esc(p.deadline || '-')}</td><td>${p.progressPercent}%</td><td><span class="pill">${esc(p.status)}</span></td></tr>`).join('')
            : '<tr><td colspan="6">No projects created yet.</td></tr>';
    }
    document.getElementById('cv-prj-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/projects', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectName: document.getElementById('cpj-name').value, clientName: document.getElementById('cpj-client').value, startDate: document.getElementById('cpj-start').value || null, deadline: document.getElementById('cpj-dead').value || null, assignedTeam: document.getElementById('cpj-team').value, progressPercent: Number(document.getElementById('cpj-pct').value), status: document.getElementById('cpj-status').value })
        });
        notify('Project saved.');
        document.getElementById('cv-prj-form').reset();
        loadProjects();
    });

    async function loadDocuments() {
        const rows = await api('/api/category-vendors/documents');
        const tb = document.getElementById('cv-doc-tbody');
        tb.innerHTML = rows.length ? '' : '<tr><td colspan="4">No documents uploaded yet.</td></tr>';
        rows.forEach((d) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${esc(d.title)}</td><td>${esc(d.docType)}</td><td>${esc(d.expiryDate || '-')}</td><td class="btn-row"><a href="${d.downloadUrl}" target="_blank" class="btn-sm btn-secondary">Download</a><button type="button" class="btn-sm btn-danger d-del">Delete</button></td>`;
            tr.querySelector('.d-del').addEventListener('click', async () => { await api(`/api/category-vendors/documents/${d.id}`, { method: 'DELETE' }); loadDocuments(); });
            tb.appendChild(tr);
        });
    }
    document.getElementById('cv-doc-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData();
        fd.append('title', document.getElementById('cd-title').value);
        fd.append('docType', document.getElementById('cd-type').value);
        if (document.getElementById('cd-exp').value) fd.append('expiryDate', document.getElementById('cd-exp').value);
        fd.append('file', document.getElementById('cd-file').files[0]);
        await api('/api/category-vendors/documents', { method: 'POST', body: fd });
        notify('Document uploaded.');
        document.getElementById('cv-doc-form').reset();
        loadDocuments();
    });

    async function loadInvoices() {
        const rows = await api('/api/category-vendors/invoices');
        document.getElementById('cv-inv-tbody').innerHTML = rows.length
            ? rows.map((i) => `<tr><td><strong>${esc(i.invoiceNumber)}</strong></td><td>${esc(i.customerName)}</td><td>${esc(i.serviceName)}</td><td>₹${i.totalAmount}</td><td>₹${i.paidAmount}</td><td><span class="pill">${esc(i.paymentStatus)}</span></td><td><a href="${i.pdfUrl}" target="_blank" class="btn-sm btn-secondary">PDF</a></td></tr>`).join('')
            : '<tr><td colspan="7">No invoices created yet.</td></tr>';
    }
    document.getElementById('cv-inv-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/invoices', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ customerName: document.getElementById('ci-cust').value, serviceName: document.getElementById('ci-svc').value, subtotal: Number(document.getElementById('ci-sub').value), taxAmount: Number(document.getElementById('ci-tax').value), paidAmount: Number(document.getElementById('ci-paid').value) })
        });
        notify('Invoice created.');
        document.getElementById('cv-inv-form').reset();
        loadInvoices();
    });

    async function loadClients() {
        const rows = await api('/api/category-vendors/clients');
        document.getElementById('cv-cl-tbody').innerHTML = rows.length
            ? rows.map((c) => `<tr><td><strong>${esc(c.clientName)}</strong></td><td>${esc(c.companyName)}</td><td>${esc(c.email)} ${esc(c.phone)}</td><td>₹${c.totalBusinessValue}</td><td>${esc(c.followUpDate || '-')}</td></tr>`).join('')
            : '<tr><td colspan="5">No clients yet.</td></tr>';
    }
    document.getElementById('cv-cl-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/clients', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ clientName: document.getElementById('ccl-name').value, companyName: document.getElementById('ccl-comp').value, email: document.getElementById('ccl-email').value, phone: document.getElementById('ccl-phone').value, followUpDate: document.getElementById('ccl-fup').value || null }) });
        notify('Client saved.');
        document.getElementById('cv-cl-form').reset();
        loadClients();
    });

    async function loadTeam() {
        const rows = await api('/api/category-vendors/team');
        const tb = document.getElementById('cv-tm-tbody');
        tb.innerHTML = rows.length ? '' : '<tr><td colspan="5">No team members yet.</td></tr>';
        rows.forEach((m) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td><strong>${esc(m.fullName)}</strong></td><td>${esc(m.role)}</td><td>${esc(m.skills)}</td><td>${esc(m.availability)}</td><td><button type="button" class="btn-sm btn-danger tm-del">Remove</button></td>`;
            tr.querySelector('.tm-del').addEventListener('click', async () => { await api(`/api/category-vendors/team/${m.id}`, { method: 'DELETE' }); loadTeam(); });
            tb.appendChild(tr);
        });
    }
    document.getElementById('cv-tm-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await api('/api/category-vendors/team', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ fullName: document.getElementById('ctm-name').value, role: document.getElementById('ctm-role').value, email: document.getElementById('ctm-email').value, phone: document.getElementById('ctm-phone').value, skills: document.getElementById('ctm-skills').value }) });
        notify('Team member added.');
        document.getElementById('cv-tm-form').reset();
        loadTeam();
    });

    document.getElementById('mk-save-btn')?.addEventListener('click', async () => {
        await api('/api/category-vendors/profile', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ featuredListing: document.getElementById('mk-featured').checked }) });
        notify('Promotion settings saved.');
    });

    document.getElementById('cv-export-csv')?.addEventListener('click', () => {
        const csv = ['Code,Customer,Project,Date,Amount,Status', ...bookingsCache.map((b) => `${b.bookingCode},"${b.customerName}","${b.eventName}",${b.eventDate},${b.quotedAmount},${b.status}`)].join('\n');
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
        a.download = 'vendor-bookings-report.csv';
        a.click();
    });

    document.getElementById('cv-pass-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/category-vendors/account/password', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ currentPassword: document.getElementById('pw-cur').value, newPassword: document.getElementById('pw-new').value }) });
            notify('Password updated.');
            document.getElementById('cv-pass-form').reset();
        } catch (err) { notify(err.message, 'error'); }
    });

    (() => {
        const now = new Date();
        const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        ['ca-date', 'cb-date', 'cq-valid', 'cpj-start', 'cpj-dead', 'cd-exp', 'ccl-fup'].forEach((id) => {
            const el = document.getElementById(id);
            if (el) el.min = today;
        });
    })();

    loadDashboard();
})();
