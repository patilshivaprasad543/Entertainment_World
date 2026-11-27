(() => {
    const banner = document.getElementById('org-banner');
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    const views = document.querySelectorAll('.view-section');
    const sidebar = document.getElementById('org-sidebar');
    let eventsCache = [];

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
    function openModal(title, html) {
        document.getElementById('org-modal-title').textContent = title;
        document.getElementById('org-modal-body').innerHTML = html;
        document.getElementById('org-modal').hidden = false;
    }
    document.getElementById('org-modal-close')?.addEventListener('click', () => { document.getElementById('org-modal').hidden = true; });
    document.getElementById('org-mobile-menu-btn')?.addEventListener('click', () => sidebar.classList.toggle('open'));

    function switchView(target) {
        banner.hidden = true;
        navItems.forEach((b) => b.classList.toggle('active', b.dataset.view === target));
        views.forEach((v) => { v.hidden = v.id !== `view-${target}`; });
        if (sidebar) sidebar.classList.remove('open');
    }
    function openSection(view) {
        switchView(view);
        if (view === 'overview') loadOverview();
        if (view === 'profile') loadProfile();
        if (view === 'posts') loadPosts();
        if (view === 'manage-events') loadEvents();
        if (view === 'professionals') loadProfessionals();
        if (view === 'prof-bookings') loadProfBookings();
        if (view === 'shortlist') loadShortlist();
        if (view === 'venues') loadVenuesWorkflow();
        if (view === 'wishlist') loadWishlist();
        if (view === 'team-builder') loadTeamBuilder();
        if (view === 'budget') loadBudgetView();
        if (view === 'schedule') loadScheduleView();
        if (view === 'reviews') loadReviews();
        if (view === 'payments') loadPayments();
        if (view === 'reports') loadReports();
        if (view === 'settings') loadSettings();
    }
    navItems.forEach((b) => b.addEventListener('click', () => openSection(b.dataset.view)));
    document.querySelectorAll('[data-jump]').forEach((el) => el.addEventListener('click', (e) => { e.preventDefault(); openSection(el.dataset.jump); }));

    async function refreshEventsCache() {
        eventsCache = await api('/api/events');
        ['tb-event', 'bg-event', 'sc-event'].forEach((id) => {
            const sel = document.getElementById(id);
            if (!sel) return;
            const cur = sel.value;
            sel.innerHTML = eventsCache.map((e) => `<option value="${e.id}">${esc(e.eventName)} (${esc(e.eventDate)})</option>`).join('');
            if (cur) sel.value = cur;
        });
        return eventsCache;
    }

    // 1. OVERVIEW
    async function loadOverview() {
        try {
            const d = await api('/api/organizer/dashboard');
            document.getElementById('org-plan-badge').textContent = d.planLimits?.planName || 'Free Plan';
            document.getElementById('k-total-events').textContent = d.totalEvents;
            document.getElementById('k-upcoming').textContent = d.upcomingEvents;
            document.getElementById('k-completed').textContent = d.completedEvents;
            document.getElementById('k-pend-prof').textContent = d.pendingProfessionalRequests;
            document.getElementById('k-conf-prof').textContent = d.confirmedProfessionalBookings;
            document.getElementById('k-pend-ven').textContent = d.pendingVenueEnquiries;
            document.getElementById('k-conf-ven').textContent = d.confirmedVenueBookings;
            document.getElementById('k-budget').textContent = `₹${Number(d.totalEventBudget || 0).toLocaleString('en-IN')}`;

            const nb = document.getElementById('org-notif-badge');
            if (d.unreadNotifications > 0) { nb.textContent = d.unreadNotifications; nb.hidden = false; } else nb.hidden = true;

            document.getElementById('ov-events').innerHTML = (d.upcomingEventsList || []).map((e) => `<p><strong>${esc(e.eventName)}</strong> (${esc(e.eventDate)}) <span class="pill">${esc(e.status)}</span></p>`).join('') || '<p class="brand-subtitle">No upcoming events.</p>';
            document.getElementById('ov-requests').innerHTML = (d.pendingRequests || []).map((r) => `<p><strong>${esc(r.professionalName)}</strong> for ${esc(r.eventName)} <span class="pill">${esc(r.status)}</span></p>`).join('') || '<p class="brand-subtitle">No pending requests.</p>';
            document.getElementById('ov-schedule').innerHTML = (d.upcomingSchedule || []).map((s) => `<p><strong>${esc(s.scheduleDate)} ${esc(s.startTime)}</strong> — ${esc(s.title)}</p>`).join('') || '<p class="brand-subtitle">No schedule entries.</p>';
            document.getElementById('ov-payments').innerHTML = (d.recentPayments || []).map((p) => `<p><strong>${esc(p.bookingReference)}</strong> — ${esc(p.payeeName)} (₹${p.bookingAmount}) <span class="pill">${esc(p.paymentStatus)}</span></p>`).join('') || '<p class="brand-subtitle">No payment records.</p>';
        } catch (err) { notify(err.message, 'error'); }
    }

    // 2. PROFILE
    async function loadProfile() {
        const p = await api('/api/organizer/profile');
        document.getElementById('op-company').value = p.companyName || '';
        document.getElementById('op-spec').value = p.specialization || '';
        document.getElementById('op-email').value = p.email || '';
        document.getElementById('op-phone').value = p.contactNumber || '';
        document.getElementById('op-country').value = p.country || 'India';
        document.getElementById('op-state').value = p.state || '';
        document.getElementById('op-city').value = p.city || '';
        document.getElementById('op-area').value = p.area || '';
        document.getElementById('op-address').value = p.address || '';
        document.getElementById('op-web').value = p.website || '';
        document.getElementById('op-social').value = p.socialLinks || '';
        document.getElementById('op-bio').value = p.bio || '';
        document.getElementById('op-about').value = p.aboutUs || '';
        document.getElementById('op-ver-badge').textContent = p.verificationStatus || 'APPROVED';
        document.getElementById('op-ver-info').textContent = `${p.organizationName || ''} • ${p.documentType || 'Verified'} (${p.documentNumber || 'N/A'})`;
    }
    document.getElementById('org-profile-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/organizer/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    companyName: document.getElementById('op-company').value,
                    specialization: document.getElementById('op-spec').value,
                    email: document.getElementById('op-email').value,
                    contactNumber: document.getElementById('op-phone').value,
                    country: document.getElementById('op-country').value,
                    state: document.getElementById('op-state').value,
                    city: document.getElementById('op-city').value,
                    area: document.getElementById('op-area').value,
                    address: document.getElementById('op-address').value,
                    website: document.getElementById('op-web').value,
                    socialLinks: document.getElementById('op-social').value,
                    bio: document.getElementById('op-bio').value,
                    aboutUs: document.getElementById('op-about').value
                })
            });
            notify('Organizer profile saved.');
        } catch (err) { notify(err.message, 'error'); }
    });

    // 3. POSTS
    async function loadPosts() {
        const posts = await api('/api/organizer/posts');
        const el = document.getElementById('posts-list');
        el.innerHTML = '';
        if (!posts.length) { el.innerHTML = '<p class="brand-subtitle">No posts yet.</p>'; return; }
        posts.forEach((p) => {
            const c = document.createElement('article');
            c.className = 'studio-work-card';
            c.innerHTML = `
                <span class="module-tag">${esc(p.postType)}</span>
                <h3>${esc(p.title || 'Event Post')}</h3>
                <p>${esc(p.description)}</p>
                <small>By ${esc(p.organizerName)} • Likes: ${p.likesCount}</small>
                <div class="btn-row" style="margin-top:0.5rem;">
                    <button type="button" class="btn-sm btn-secondary p-like">👍 Like</button>
                    <button type="button" class="btn-sm btn-danger p-del">Delete</button>
                </div>
            `;
            c.querySelector('.p-like').addEventListener('click', async () => { await api(`/api/organizer/posts/${p.id}/like`, { method: 'POST' }); loadPosts(); });
            c.querySelector('.p-del').addEventListener('click', async () => {
                try { await api(`/api/organizer/posts/${p.id}`, { method: 'DELETE' }); loadPosts(); } catch (err) { notify(err.message, 'error'); }
            });
            el.appendChild(c);
        });
    }
    document.getElementById('post-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/organizer/posts', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: document.getElementById('ps-title').value, postType: document.getElementById('ps-type').value, description: document.getElementById('ps-desc').value })
            });
            notify('Post published.');
            document.getElementById('post-form').reset();
            loadPosts();
        } catch (err) { notify(err.message, 'error'); }
    });

    // 4 & 5. CREATE & MANAGE EVENTS
    async function saveEventWithStatus(status) {
        const id = document.getElementById('ev-id').value;
        try {
            await api(id ? `/api/events/${id}` : '/api/events', {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    eventName: document.getElementById('ev-name').value,
                    eventType: document.getElementById('ev-type').value,
                    eventDate: document.getElementById('ev-date').value,
                    startTime: document.getElementById('ev-start').value,
                    endTime: document.getElementById('ev-end').value,
                    expectedGuests: Number(document.getElementById('ev-guests').value),
                    budget: Number(document.getElementById('ev-budget').value),
                    country: document.getElementById('ev-country').value,
                    state: document.getElementById('ev-state').value,
                    city: document.getElementById('ev-city').value,
                    area: document.getElementById('ev-area').value,
                    address: document.getElementById('ev-addr').value,
                    mapLink: document.getElementById('ev-map').value,
                    description: document.getElementById('ev-desc').value,
                    requirements: document.getElementById('ev-req').value,
                    status
                })
            });
            notify(`Event saved (${status}).`);
            document.getElementById('event-form').reset();
            document.getElementById('ev-id').value = '';
            openSection('manage-events');
        } catch (err) { notify(err.message, 'error'); }
    }
    document.getElementById('event-form')?.addEventListener('submit', (e) => { e.preventDefault(); saveEventWithStatus('PUBLISHED'); });
    document.getElementById('ev-save-draft')?.addEventListener('click', () => saveEventWithStatus('DRAFT'));
    document.getElementById('ev-cancel-btn')?.addEventListener('click', () => { document.getElementById('event-form').reset(); openSection('manage-events'); });

    async function loadEvents() {
        const list = await refreshEventsCache();
        const grid = document.getElementById('manage-events-grid');
        grid.innerHTML = '';
        if (!list.length) { grid.innerHTML = '<p class="brand-subtitle">No events created yet.</p>'; return; }
        list.forEach((ev) => {
            const c = document.createElement('article');
            c.className = 'studio-work-card';
            c.innerHTML = `
                <span class="module-tag">${esc(ev.eventType)} • ${esc(ev.status)}</span>
                <h3>${esc(ev.eventName)}</h3>
                <p>${esc(ev.city)} • ${esc(ev.eventDate)} • Guests: ${ev.expectedGuests}</p>
                <p><strong>Budget: ₹${Number(ev.budget || 0).toLocaleString('en-IN')}</strong></p>
                <div class="btn-row">
                    <button type="button" class="btn-sm btn-primary ev-det">Details</button>
                    ${ev.status === 'DRAFT' ? '<button type="button" class="btn-sm btn-secondary ev-pub">Publish</button>' : ''}
                    ${ev.status === 'DRAFT' ? '<button type="button" class="btn-sm btn-danger ev-del">Delete</button>' : ''}
                </div>
            `;
            c.querySelector('.ev-det').addEventListener('click', async () => {
                const det = await api(`/api/events/${ev.id}`);
                const vbHtml = (det.venueBookings || []).map((b) => `<li>${esc(b.bookingCode)} — ${esc(b.venueName)} (${esc(b.eventDate)}) <strong>${esc(b.status)}</strong> ₹${b.totalAmount}</li>`).join('') || '<li>No venue bookings yet.</li>';
                const pbHtml = (det.professionalBookings || []).map((b) => `<li>${esc(b.bookingCode)} — ${esc(b.professionalName)} (${esc(b.requiredCategory)}) <strong>${esc(b.status)}</strong></li>`).join('') || '<li>No professional bookings yet.</li>';
                openModal(det.eventName, `
                    <p><strong>Date:</strong> ${esc(det.eventDate)} | <strong>City:</strong> ${esc(det.city)} | <strong>Budget:</strong> ₹${det.budget}</p>
                    <h4>Venue Bookings</h4><ul>${vbHtml}</ul>
                    <h4>Professional Bookings</h4><ul>${pbHtml}</ul>
                `);
            });
            c.querySelector('.ev-pub')?.addEventListener('click', async () => {
                try { await api(`/api/events/${ev.id}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'PUBLISHED' }) }); loadEvents(); } catch (err) { notify(err.message, 'error'); }
            });
            c.querySelector('.ev-del')?.addEventListener('click', async () => {
                if (!confirm('Delete draft event?')) return;
                await api(`/api/events/${ev.id}`, { method: 'DELETE' });
                loadEvents();
            });
            grid.appendChild(c);
        });
    }

    // 6, 7, 8. PROFESSIONALS, HIRE & SHORTLIST
    async function loadProfessionals() {
        await refreshEventsCache();
        const params = new URLSearchParams();
        const q = document.getElementById('pr-q').value.trim();
        const cat = document.getElementById('pr-cat').value;
        const city = document.getElementById('pr-city').value.trim();
        if (q) params.set('query', q); if (cat) params.set('category', cat); if (city) params.set('city', city);
        const list = await api(`/api/organizer/professionals?${params.toString()}`);
        const grid = document.getElementById('professionals-grid');
        grid.innerHTML = '';
        if (!list.length) { grid.innerHTML = '<p class="brand-subtitle">No approved professionals found.</p>'; return; }
        list.forEach((p) => {
            const c = document.createElement('article');
            c.className = 'studio-work-card';
            c.innerHTML = `
                <span class="module-tag">${esc(p.category)}</span>
                <h3>${esc(p.fullName)}</h3>
                <p>${esc(p.city)} • ★ ${p.rating || 'New'} (${p.reviewsCount})</p>
                <p><small>${esc(p.bio)}</small></p>
                <div class="btn-row">
                    <button type="button" class="btn-sm btn-primary p-hire">Send Booking Request</button>
                    <button type="button" class="btn-sm btn-secondary p-short">Shortlist</button>
                    <button type="button" class="btn-sm btn-secondary p-wish">Wishlist</button>
                </div>
            `;
            c.querySelector('.p-hire').addEventListener('click', async () => {
                if (!eventsCache.length) return notify('Create an event first!', 'error');
                const ev = eventsCache[0];
                const fee = Number(prompt(`Offered Fee (₹) for ${p.fullName} (${ev.eventName}):`, '25000') || '0');
                if (!fee) return;
                try {
                    await api('/api/organizer/bookings', {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ eventId: ev.id, professionalId: p.id, requiredCategory: p.category, offeredFee: fee, message: 'Looking forward to working together.' })
                    });
                    notify(`Booking request sent to ${p.fullName}!`);
                } catch (err) { notify(err.message, 'error'); }
            });
            c.querySelector('.p-short').addEventListener('click', async () => {
                try {
                    await api('/api/organizer/shortlist', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ professionalId: p.id, eventId: eventsCache[0]?.id || null, quotedCharge: 25000 }) });
                    notify(`${p.fullName} shortlisted.`);
                } catch (err) { notify(err.message, 'error'); }
            });
            c.querySelector('.p-wish').addEventListener('click', async () => {
                try {
                    await api('/api/organizer/wishlist', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ itemType: 'PROFESSIONAL', targetId: p.id, title: p.fullName, subtitle: p.category }) });
                    notify('Added to wishlist.');
                } catch (err) { notify(err.message, 'error'); }
            });
            grid.appendChild(c);
        });
    }
    document.getElementById('pr-search-btn')?.addEventListener('click', loadProfessionals);
    document.getElementById('pr-clear-btn')?.addEventListener('click', () => { document.getElementById('pr-q').value = ''; document.getElementById('pr-cat').value = ''; document.getElementById('pr-city').value = ''; loadProfessionals(); });

    async function loadProfBookings() {
        const rows = await api('/api/organizer/bookings');
        const tbody = document.getElementById('prof-bookings-tbody');
        tbody.innerHTML = rows.length ? '' : '<tr><td colspan="7">No professional bookings yet.</td></tr>';
        rows.forEach((b) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(b.bookingCode)}</strong></td><td>${esc(b.eventName)}</td><td>${esc(b.professionalName)}</td>
                <td>${esc(b.eventDate)}</td><td>₹${b.offeredFee}${b.counterOfferFee ? ` / Counter ₹${b.counterOfferFee}` : ''}</td>
                <td><span class="pill">${esc(b.status)}</span></td>
                <td class="btn-row"><button type="button" class="btn-sm btn-primary pb-conf">Confirm</button><button type="button" class="btn-sm btn-secondary pb-comp">Complete</button></td>
            `;
            tr.querySelector('.pb-conf').addEventListener('click', async () => { await api(`/api/organizer/bookings/${b.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'CONFIRMED' }) }); loadProfBookings(); });
            tr.querySelector('.pb-comp').addEventListener('click', async () => { await api(`/api/organizer/bookings/${b.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'COMPLETED' }) }); loadProfBookings(); });
            tbody.appendChild(tr);
        });
    }

    async function loadShortlist() {
        const rows = await api('/api/organizer/shortlist');
        const tbody = document.getElementById('shortlist-tbody');
        tbody.innerHTML = rows.length ? '' : '<tr><td colspan="6">No shortlisted professionals.</td></tr>';
        rows.forEach((s) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${esc(s.eventName)}</td><td><strong>${esc(s.professionalName)}</strong></td><td>${esc(s.category)}</td><td>${esc(s.city)}</td><td>₹${s.quotedCharge}</td><td><button type="button" class="btn-sm btn-danger sl-del">Remove</button></td>`;
            tr.querySelector('.sl-del').addEventListener('click', async () => { await api(`/api/organizer/shortlist/${s.id}`, { method: 'DELETE' }); loadShortlist(); });
            tbody.appendChild(tr);
        });
    }

    // 9. VENUE SEARCH & BOOKING WORKFLOW
    async function loadVenuesWorkflow() {
        await refreshEventsCache();
        const params = new URLSearchParams();
        const c = document.getElementById('vs-city').value.trim();
        const t = document.getElementById('vs-type').value;
        const cat = document.getElementById('vs-cat').value;
        const cap = document.getElementById('vs-cap').value;
        const max = document.getElementById('vs-max').value;
        if (c) params.set('city', c); if (t) params.set('venueType', t); if (cat) params.set('venueCategory', cat);
        if (cap) params.set('guests', cap); if (max) params.set('maxBudget', max);

        const [venues, enquiries, quotations, bookings] = await Promise.all([
            api(`/api/organizer/venues/search?${params.toString()}`),
            api('/api/organizer/venues/enquiries'),
            api('/api/organizer/venues/quotations'),
            api('/api/organizer/venues/bookings')
        ]);

        const grid = document.getElementById('org-venues-grid');
        grid.innerHTML = '';
        if (!venues.length) grid.innerHTML = '<p class="brand-subtitle">No approved venues match your filters.</p>';
        venues.forEach((v) => {
            const card = document.createElement('article');
            card.className = 'studio-work-card';
            card.innerHTML = `
                <img class="work-card-img" src="${v.coverImageUrl || '/images/vendor-slide-2.jpg'}" alt="${esc(v.venueName)}">
                <span class="module-tag">${esc(v.venueType)} • ${esc(v.venueCategory || 'Indoor')}</span>
                <h3>${esc(v.venueName)}</h3>
                <p>${esc(v.areaLocality ? v.areaLocality + ', ' : '')}${esc(v.city)} • Cap: ${v.minCapacity}-${v.maxCapacity}</p>
                <p><strong>₹${Number(v.pricePerDay || 0).toLocaleString('en-IN')}/day</strong></p>
                <div class="btn-row">
                    <button type="button" class="btn-sm btn-primary v-enq">Send Enquiry</button>
                    <button type="button" class="btn-sm btn-secondary v-wish">Wishlist</button>
                </div>
            `;
            card.querySelector('.v-enq').addEventListener('click', async () => {
                const ev = eventsCache[0];
                const d = prompt(`Booking Date (YYYY-MM-DD) for ${v.venueName}:`, ev?.eventDate || new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10));
                if (!d) return;
                try {
                    const res = await api(`/api/organizer/venues/${v.id}/enquiries`, {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ eventId: ev?.id || null, eventName: ev?.eventName || 'Event', eventType: ev?.eventType || 'Wedding', eventDate: d, guestCount: ev?.expectedGuests || 300, budget: v.pricePerDay, message: 'Venue enquiry from Event Management Dashboard' })
                    });
                    notify(`Enquiry ${res.enquiryCode} sent to ${v.venueName}!`);
                    loadVenuesWorkflow();
                } catch (err) { notify(err.message, 'error'); }
            });
            card.querySelector('.v-wish').addEventListener('click', async () => {
                try {
                    await api('/api/organizer/wishlist', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ itemType: 'VENUE', targetId: v.id, title: v.venueName, subtitle: `${v.venueType} • ${v.city}` }) });
                    notify('Venue saved to wishlist.');
                } catch (err) { notify(err.message, 'error'); }
            });
            grid.appendChild(card);
        });

        document.getElementById('org-ven-enq-tbody').innerHTML = enquiries.length
            ? enquiries.map((e) => `<tr><td><strong>${esc(e.enquiryCode)}</strong></td><td>${esc(e.eventName || e.eventType)}</td><td>${esc(e.venueName)}</td><td>${esc(e.eventDate)}</td><td><span class="pill">${esc(e.status)}</span></td></tr>`).join('')
            : '<tr><td colspan="5">No venue enquiries sent yet.</td></tr>';

        const qBody = document.getElementById('org-ven-qtn-tbody');
        qBody.innerHTML = quotations.length ? '' : '<tr><td colspan="6">No quotations received yet.</td></tr>';
        quotations.forEach((q) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(q.quotationCode)}</strong></td><td>${esc(q.venueName)}</td><td>${esc(q.eventDate)}</td>
                <td>₹${q.totalAmount} (Adv ₹${q.advanceAmount})</td><td><span class="pill">${esc(q.status)}</span></td>
                <td class="btn-row">
                    ${q.status === 'SENT' ? '<button type="button" class="btn-sm btn-primary q-acc">Accept</button><button type="button" class="btn-sm btn-danger q-rej">Reject</button>' : ''}
                </td>
            `;
            tr.querySelector('.q-acc')?.addEventListener('click', async () => {
                await api(`/api/organizer/venues/quotations/${q.id}/respond`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'ACCEPT' }) });
                notify('Quotation accepted! Confirm booking in My Venue Bookings below.');
                loadVenuesWorkflow();
            });
            tr.querySelector('.q-rej')?.addEventListener('click', async () => {
                await api(`/api/organizer/venues/quotations/${q.id}/respond`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'REJECT' }) });
                loadVenuesWorkflow();
            });
            qBody.appendChild(tr);
        });

        const bBody = document.getElementById('org-ven-bk-tbody');
        bBody.innerHTML = bookings.length ? '' : '<tr><td colspan="7">No venue bookings yet.</td></tr>';
        bookings.forEach((b) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(b.bookingCode)}</strong></td><td>${esc(b.eventName || b.eventType)}</td><td>${esc(b.venueName)}</td>
                <td>${esc(b.eventDate)}</td><td>₹${b.totalAmount}</td><td><span class="pill">${esc(b.status)}</span></td>
                <td>${b.status === 'PENDING' ? '<button type="button" class="btn-sm btn-primary vb-conf">Confirm Booking</button>' : ''}</td>
            `;
            tr.querySelector('.vb-conf')?.addEventListener('click', async () => {
                try {
                    await api(`/api/organizer/venues/bookings/${b.id}/confirm`, { method: 'POST' });
                    notify(`Booking ${b.bookingCode} CONFIRMED and venue date blocked!`);
                    loadVenuesWorkflow();
                } catch (err) { notify(err.message, 'error'); }
            });
            bBody.appendChild(tr);
        });
    }
    document.getElementById('vs-btn')?.addEventListener('click', loadVenuesWorkflow);
    document.getElementById('vs-clear')?.addEventListener('click', () => { ['vs-city', 'vs-type', 'vs-cat', 'vs-cap', 'vs-max'].forEach((id) => { document.getElementById(id).value = ''; }); loadVenuesWorkflow(); });

    // 10. WISHLIST
    async function loadWishlist() {
        const items = await api('/api/platform/wishlist');
        const el = document.getElementById('org-wishlist-list');
        el.innerHTML = '';
        if (!items.length) { el.innerHTML = '<p class="brand-subtitle">Wishlist is empty.</p>'; return; }
        items.forEach((w) => {
            const c = document.createElement('article');
            c.className = 'studio-work-card';
            c.innerHTML = `<span class="module-tag">${esc(w.itemType)}</span><h3>${esc(w.title)}</h3><p>${esc(w.subtitle || '')}</p><button type="button" class="btn-sm btn-danger w-del">Remove</button>`;
            c.querySelector('.w-del').addEventListener('click', async () => { await api(`/api/platform/wishlist/${w.id}`, { method: 'DELETE' }); loadWishlist(); });
            el.appendChild(c);
        });
    }

    // 11. TEAM BUILDER
    async function loadTeamBuilder() {
        await refreshEventsCache();
        const sub = await api('/api/organizer/subscription');
        document.getElementById('tb-gate-msg').textContent = sub.limits?.allowTeamBuilder ? 'Event Team Builder unlocked!' : 'Upgrade to Premium (₹799) or Pro (₹1,999) to use Event Team Builder.';
        const evId = document.getElementById('tb-event').value;
        const tbody = document.getElementById('tb-tbody');
        if (!evId) { tbody.innerHTML = '<tr><td colspan="6">Create an event first.</td></tr>'; return; }
        const rows = await api(`/api/organizer/team/${evId}`);
        tbody.innerHTML = rows.length ? '' : '<tr><td colspan="6">No team members added to this event.</td></tr>';
        rows.forEach((m) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${esc(m.memberType)}</td><td><strong>${esc(m.memberName)}</strong></td><td>${esc(m.assignedRole)}</td><td>₹${m.charges}</td><td>${esc(m.bookingStatus)}</td><td><button type="button" class="btn-sm btn-danger tb-del">Remove</button></td>`;
            tr.querySelector('.tb-del').addEventListener('click', async () => { await api(`/api/organizer/team/member/${m.id}`, { method: 'DELETE' }); loadTeamBuilder(); });
            tbody.appendChild(tr);
        });
    }
    document.getElementById('tb-event')?.addEventListener('change', loadTeamBuilder);
    document.getElementById('tb-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api(`/api/organizer/team/${document.getElementById('tb-event').value}`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ memberType: document.getElementById('tb-type').value, memberName: document.getElementById('tb-name').value, assignedRole: document.getElementById('tb-role').value, charges: Number(document.getElementById('tb-charges').value || 0) })
            });
            notify('Team member added.');
            document.getElementById('tb-form').reset();
            loadTeamBuilder();
        } catch (err) { notify(err.message, 'error'); }
    });

    // 12. BUDGET
    async function loadBudgetView() {
        await refreshEventsCache();
        const evId = document.getElementById('bg-event').value;
        if (!evId) return;
        const d = await api(`/api/organizer/budget/${evId}`);
        document.getElementById('bg-total').textContent = `₹${Number(d.eventBudget || 0).toLocaleString('en-IN')}`;
        document.getElementById('bg-est').textContent = `₹${Number(d.totalEstimated || 0).toLocaleString('en-IN')}`;
        document.getElementById('bg-act').textContent = `₹${Number(d.totalActual || 0).toLocaleString('en-IN')}`;
        document.getElementById('bg-rem').textContent = `₹${Number(d.remainingBudget || 0).toLocaleString('en-IN')}`;
        const tbody = document.getElementById('bg-tbody');
        tbody.innerHTML = d.expenses.length ? '' : '<tr><td colspan="5">No expenses recorded yet.</td></tr>';
        d.expenses.forEach((x) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${esc(x.category)}</td><td>${esc(x.title)}</td><td>₹${x.estimatedAmount}</td><td>₹${x.actualAmount}</td><td><button type="button" class="btn-sm btn-danger bg-del">Delete</button></td>`;
            tr.querySelector('.bg-del').addEventListener('click', async () => { await api(`/api/organizer/budget/expense/${x.id}`, { method: 'DELETE' }); loadBudgetView(); });
            tbody.appendChild(tr);
        });
    }
    document.getElementById('bg-event')?.addEventListener('change', loadBudgetView);
    document.getElementById('bg-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api(`/api/organizer/budget/${document.getElementById('bg-event').value}`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ category: document.getElementById('bg-cat').value, title: document.getElementById('bg-title').value, estimatedAmount: Number(document.getElementById('bg-est-in').value), actualAmount: Number(document.getElementById('bg-act-in').value) })
            });
            document.getElementById('bg-form').reset();
            loadBudgetView();
        } catch (err) { notify(err.message, 'error'); }
    });

    // 13. SCHEDULE
    async function loadScheduleView() {
        await refreshEventsCache();
        const evId = document.getElementById('sc-event').value;
        if (!evId) return;
        const rows = await api(`/api/organizer/schedule/${evId}`);
        const tbody = document.getElementById('sc-tbody');
        tbody.innerHTML = rows.length ? '' : '<tr><td colspan="6">No schedule slots yet.</td></tr>';
        rows.forEach((s) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${esc(s.scheduleDate)}</td><td>${esc(s.startTime)} - ${esc(s.endTime)}</td><td><strong>${esc(s.title)}</strong></td><td>${esc(s.assignedProfessionalName || '-')}</td><td>${esc(s.status)}</td><td><button type="button" class="btn-sm btn-danger sc-del">Delete</button></td>`;
            tr.querySelector('.sc-del').addEventListener('click', async () => { await api(`/api/organizer/schedule/item/${s.id}`, { method: 'DELETE' }); loadScheduleView(); });
            tbody.appendChild(tr);
        });
    }
    document.getElementById('sc-event')?.addEventListener('change', loadScheduleView);
    document.getElementById('sc-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api(`/api/organizer/schedule/${document.getElementById('sc-event').value}`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: document.getElementById('sc-title').value, scheduleDate: document.getElementById('sc-date').value, startTime: document.getElementById('sc-start').value, endTime: document.getElementById('sc-end').value, assignedProfessionalName: document.getElementById('sc-prof').value })
            });
            document.getElementById('sc-form').reset();
            loadScheduleView();
        } catch (err) { notify(err.message, 'error'); }
    });

    // 14, 15, 17, 18. REVIEWS, PAYMENTS, REPORTS, SETTINGS
    async function loadReviews() {
        const rows = await api('/api/organizer/reviews');
        document.getElementById('rv-list').innerHTML = rows.length ? rows.map((r) => `<p><strong>${esc(r.title)}</strong> (${r.rating}/5) — ${esc(r.description)}</p>`).join('') : '<p class="brand-subtitle">No reviews submitted yet.</p>';
    }
    document.getElementById('rv-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/organizer/reviews', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetType: document.getElementById('rv-type').value, targetId: Number(document.getElementById('rv-target').value), bookingId: Number(document.getElementById('rv-booking').value), rating: Number(document.getElementById('rv-rating').value), title: document.getElementById('rv-title').value, description: document.getElementById('rv-desc').value })
            });
            notify('Review submitted.');
            document.getElementById('rv-form').reset();
            loadReviews();
        } catch (err) { notify(err.message, 'error'); }
    });

    async function loadPayments() {
        const rows = await api('/api/organizer/payments');
        const tbody = document.getElementById('pay-tbody');
        tbody.innerHTML = rows.length ? '' : '<tr><td colspan="8">No payment records yet.</td></tr>';
        rows.forEach((p) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td><strong>${esc(p.bookingReference)}</strong></td><td>${esc(p.eventName)}</td><td>${esc(p.payeeName)}</td><td>₹${p.bookingAmount}</td><td>₹${p.paidAmount}</td><td>₹${p.remainingBalance}</td><td><span class="pill">${esc(p.paymentStatus)}</span></td><td><button type="button" class="btn-sm btn-primary pay-rec">Record Payment</button></td>`;
            tr.querySelector('.pay-rec').addEventListener('click', async () => {
                const amt = Number(prompt(`Enter total paid amount (Booking ₹${p.bookingAmount}):`, p.bookingAmount) || '0');
                await api('/api/organizer/payments', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: p.id, paidAmount: amt }) });
                notify('Manual payment record updated.');
                loadPayments();
            });
            tbody.appendChild(tr);
        });
    }

    async function loadReports() {
        const r = await api('/api/organizer/reports');
        document.getElementById('reports-grid').innerHTML = `
            <div class="kpi-card"><span>Total Events</span><strong>${r.totalEvents}</strong></div>
            <div class="kpi-card"><span>Upcoming</span><strong>${r.upcomingEvents}</strong></div>
            <div class="kpi-card"><span>Completed</span><strong>${r.completedEvents}</strong></div>
            <div class="kpi-card"><span>Cancelled</span><strong>${r.cancelledEvents}</strong></div>
            <div class="kpi-card"><span>Total Budget</span><strong>₹${Number(r.totalBudget || 0).toLocaleString('en-IN')}</strong></div>
            <div class="kpi-card"><span>Actual Cost</span><strong>₹${Number(r.totalActualCost || 0).toLocaleString('en-IN')}</strong></div>
            <div class="kpi-card"><span>Venue Bookings</span><strong>${r.venueBookingsCount}</strong></div>
            <div class="kpi-card"><span>Prof Bookings</span><strong>${r.professionalBookingsCount}</strong></div>
        `;
    }

    async function loadSettings() {
        const [team, sub, notifs] = await Promise.all([api('/api/organizer/team-accounts'), api('/api/organizer/subscription'), api('/api/organizer/notifications')]);
        document.getElementById('org-tm-limit').textContent = `Used: ${team.length} / ${sub.limits?.maxTeamAccounts || 1}`;
        document.getElementById('org-tm-list').innerHTML = team.map((t) => `<p><strong>${esc(t.fullName)}</strong> (${esc(t.email)}) — ${esc(t.role)}</p>`).join('') || '<p class="brand-subtitle">No team members added.</p>';
        document.getElementById('org-notif-list').innerHTML = notifs.map((n) => `<p><strong>${esc(n.title)}</strong>: ${esc(n.message)}</p>`).join('') || '<p class="brand-subtitle">No notifications.</p>';
    }
    document.getElementById('org-tm-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/organizer/team-accounts', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ fullName: document.getElementById('otm-name').value, email: document.getElementById('otm-email').value, role: document.getElementById('otm-role').value }) });
            notify('Team account added.');
            document.getElementById('org-tm-form').reset();
            loadSettings();
        } catch (err) { notify(err.message, 'error'); }
    });

    loadOverview();
})();
