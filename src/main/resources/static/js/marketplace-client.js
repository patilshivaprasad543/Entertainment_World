(() => {
    const container = document.getElementById('vendor-marketplace-section');
    if (!container) return;

    const mode = container.dataset.mode || 'booking'; // 'booking' or 'readonly'
    const banner = document.getElementById('mkt-banner');
    const grid = document.getElementById('mkt-services-grid');
    const bookingsTbody = document.getElementById('mkt-bookings-tbody');
    const notifList = document.getElementById('mkt-notifications-list');

    function showBanner(msg, type = 'success') {
        if (!banner) return;
        banner.textContent = msg;
        banner.className = `status-banner ${type}`;
        banner.hidden = false;
    }

    async function api(url, options = {}) {
        const res = await fetch(url, options);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Request failed (${res.status})`);
        }
        const ct = res.headers.get('content-type') || '';
        return ct.includes('application/json') ? res.json() : res;
    }

    async function loadServices() {
        const q = (document.getElementById('mkt-search')?.value || '').trim();
        const cat = document.getElementById('mkt-category')?.value || '';
        const maxPrice = document.getElementById('mkt-max-price')?.value || '';
        const loc = (document.getElementById('mkt-location')?.value || '').trim();

        const params = new URLSearchParams();
        if (q) params.set('query', q);
        if (cat) params.set('category', cat);
        if (maxPrice) params.set('maxPrice', maxPrice);
        if (loc) params.set('location', loc);

        try {
            const services = await api(`/api/vendor-services?${params.toString()}`);
            renderServices(services);
        } catch (err) {
            grid.innerHTML = `<p class="brand-subtitle">${esc(err.message)}</p>`;
        }
    }

    function renderServices(services) {
        if (!services.length) {
            grid.innerHTML = '<p class="brand-subtitle">No approved vendor services match your filters.</p>';
            return;
        }
        grid.innerHTML = '';
        services.forEach((s) => {
            const card = document.createElement('article');
            card.className = 'service-card';
            card.innerHTML = `
                ${s.imageUrl ? `<img src="${s.imageUrl}" alt="${esc(s.serviceName)}">` : ''}
                <span class="module-tag">${esc(s.category)}</span>
                <h4>${esc(s.serviceName)}</h4>
                <p><strong>${esc(s.vendorCompanyName)}</strong> &bull; ${esc(s.location)}</p>
                <p>${esc(s.description || 'Production-grade service')}</p>
                <p><strong>INR ${s.pricePerDay}/day</strong>${s.pricePerEvent ? ` &bull; INR ${s.pricePerEvent}/event` : ''} &bull; Qty: ${s.availableQuantity}</p>
            `;
            if (mode === 'booking') {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'btn-sm btn-primary';
                btn.style.marginTop = 'auto';
                btn.textContent = 'Request Booking';
                btn.addEventListener('click', () => openBookModal(s));
                card.appendChild(btn);
            }
            grid.appendChild(card);
        });
    }

    async function loadMyBookings() {
        if (mode !== 'booking' || !bookingsTbody) return;
        try {
            const list = await api('/api/vendor-bookings/my-requests');
            if (!list.length) {
                bookingsTbody.innerHTML = '<tr><td colspan="6">No booking requests submitted yet.</td></tr>';
                return;
            }
            bookingsTbody.innerHTML = '';
            list.forEach((b) => {
                const tr = document.createElement('tr');
                const invHtml = b.invoice
                    ? `<span class="pill pill-${b.invoice.paymentStatus}">${b.invoice.invoiceNumber} (${b.invoice.paymentStatus})</span>
                       <br><a class="btn-sm btn-secondary" style="margin-top:4px;" href="/api/vendor-bookings/${b.id}/invoice/pdf">Download PDF</a>`
                    : '<small>Pending invoice</small>';

                tr.innerHTML = `
                    <td><strong>${esc(b.bookingCode)}</strong><br><small>${esc(b.vendorCompanyName)}</small></td>
                    <td>${esc(b.serviceName)}<br><small>Qty: ${b.requiredQuantity}</small></td>
                    <td>${b.startDate} to ${b.endDate}<br><small>${esc(b.deliveryLocation)}</small></td>
                    <td>INR ${b.totalEstimatedPrice}</td>
                    <td><span class="pill pill-${b.status}">${b.status}</span><br><small>Delivery: ${esc(b.deliveryStatus)}</small>${b.clarificationNote ? `<br><small>Note: ${esc(b.clarificationNote)}</small>` : ''}</td>
                    <td>${invHtml}</td>
                `;
                bookingsTbody.appendChild(tr);
            });
        } catch (err) {
            bookingsTbody.innerHTML = `<tr><td colspan="6">${esc(err.message)}</td></tr>`;
        }
    }

    async function loadNotifications() {
        if (mode !== 'booking' || !notifList) return;
        try {
            const items = await api('/api/module-notifications');
            if (!items.length) {
                notifList.innerHTML = '<p class="brand-subtitle">No notifications yet.</p>';
                return;
            }
            notifList.innerHTML = '';
            items.slice(0, 10).forEach((n) => {
                const div = document.createElement('div');
                div.className = 'verification-item';
                div.innerHTML = `
                    <div class="verification-meta">
                        <h3>${esc(n.title)} ${n.readFlag ? '' : '<span class="pill pill-PENDING">NEW</span>'}</h3>
                        <p>${esc(n.message)}</p>
                    </div>
                `;
                if (!n.readFlag) {
                    const b = document.createElement('button');
                    b.type = 'button';
                    b.className = 'btn-sm btn-secondary';
                    b.textContent = 'Mark Read';
                    b.addEventListener('click', async () => {
                        await api(`/api/module-notifications/${n.id}/read`, { method: 'POST' });
                        loadNotifications();
                    });
                    div.appendChild(b);
                }
                notifList.appendChild(div);
            });
        } catch (err) {
            // ignore
        }
    }

    function openBookModal(service) {
        const modal = document.getElementById('mkt-book-modal');
        document.getElementById('mb-service-id').value = service.id;
        document.getElementById('mb-title').textContent = `Book: ${service.serviceName}`;
        document.getElementById('mb-subtitle').textContent =
            `${service.vendorCompanyName} • INR ${service.pricePerDay}/day • Available Qty: ${service.availableQuantity}`
            + (service.unavailableDates && service.unavailableDates.length
                ? ` • Blocked dates: ${service.unavailableDates.join(', ')}` : '');
        document.getElementById('mb-qty').max = service.availableQuantity;
        document.getElementById('mb-qty').value = 1;
        modal.hidden = false;
    }

    const closeBtn = document.getElementById('mb-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            document.getElementById('mkt-book-modal').hidden = true;
        });
    }

    const bookForm = document.getElementById('mb-form');
    if (bookForm) {
        bookForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                const created = await api('/api/vendor-bookings', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        serviceId: Number(document.getElementById('mb-service-id').value),
                        requiredQuantity: Number(document.getElementById('mb-qty').value),
                        startDate: document.getElementById('mb-start').value,
                        endDate: document.getElementById('mb-end').value,
                        deliveryLocation: document.getElementById('mb-location').value,
                        specialRequirements: document.getElementById('mb-notes').value
                    })
                });
                document.getElementById('mkt-book-modal').hidden = true;
                bookForm.reset();
                showBanner(`Booking ${created.bookingCode} submitted to ${created.vendorCompanyName}!`);
                loadMyBookings();
                loadNotifications();
            } catch (err) {
                showBanner(err.message, 'error');
            }
        });
    }

    const filterBtn = document.getElementById('mkt-filter-btn');
    if (filterBtn) filterBtn.addEventListener('click', loadServices);

    const refreshBtn = document.getElementById('mkt-refresh-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            loadServices();
            loadMyBookings();
            loadNotifications();
        });
    }

    function esc(text) {
        const d = document.createElement('div');
        d.textContent = text ?? '';
        return d.innerHTML;
    }

    loadServices();
    loadMyBookings();
    loadNotifications();
})();

// ==================== PUBLIC VENUE & STUDIO DISCOVERY INTEGRATION ====================
(() => {
    const anchor = document.getElementById('vendor-marketplace-section');
    if (!anchor) return;

    const wrap = document.createElement('section');
    wrap.className = 'panel-card';
    wrap.style.marginTop = '1.5rem';
    wrap.innerHTML = `
        <div class="panel-header">
            <div>
                <span class="module-tag">Verified Venue &amp; Studio Directory</span>
                <h2 style="margin:0.25rem 0 0;">Search Venues, Studios &amp; Shooting Locations</h2>
            </div>
        </div>
        <div id="pub-venue-banner" class="status-banner" hidden></div>
        <div class="form-grid" style="margin-bottom:1rem;">
            <div class="field-group"><label for="pv-city">City</label><input type="text" id="pv-city" placeholder="Hyderabad / Bengaluru"></div>
            <div class="field-group">
                <label for="pv-type">Venue / Studio Type</label>
                <select id="pv-type">
                    <option value="">All Types</option>
                    <option value="Banquet Hall">Banquet Hall</option>
                    <option value="Marriage Hall">Marriage Hall</option>
                    <option value="Auditorium">Auditorium</option>
                    <option value="Convention Center">Convention Center</option>
                    <option value="Resort">Resort</option>
                    <option value="Outdoor Lawn">Outdoor Lawn</option>
                    <option value="Shooting Location">Shooting Location</option>
                    <option value="Recording Studio">Recording Studio</option>
                    <option value="Photography Studio">Photography Studio</option>
                </select>
            </div>
            <div class="field-group"><label for="pv-guests">Min Guests</label><input type="number" id="pv-guests" placeholder="200"></div>
            <div class="field-group"><label for="pv-budget">Max Budget (₹/day)</label><input type="number" id="pv-budget" placeholder="100000"></div>
            <div style="display:flex;align-items:flex-end;"><button type="button" id="pv-search-btn" class="btn-sm btn-primary">Search Venues</button></div>
        </div>
        <div id="pub-venues-grid" class="dashboard-grid"></div>
    `;
    anchor.insertAdjacentElement('afterend', wrap);

    const pvBanner = wrap.querySelector('#pub-venue-banner');
    const pvGrid = wrap.querySelector('#pub-venues-grid');

    async function searchVenues() {
        const params = new URLSearchParams();
        const c = wrap.querySelector('#pv-city').value.trim();
        const t = wrap.querySelector('#pv-type').value;
        const g = wrap.querySelector('#pv-guests').value;
        const b = wrap.querySelector('#pv-budget').value;
        if (c) params.set('city', c);
        if (t) params.set('venueType', t);
        if (g) params.set('guests', g);
        if (b) params.set('maxBudget', b);

        try {
            const res = await fetch(`/api/public/venues/search?${params.toString()}`);
            const list = await res.json();
            if (!list.length) {
                pvGrid.innerHTML = '<p class="brand-subtitle">No active venues match your search filters yet.</p>';
                return;
            }
            pvGrid.innerHTML = '';
            list.forEach((v) => {
                const card = document.createElement('article');
                card.className = 'studio-work-card';
                card.innerHTML = `
                    <img class="work-card-img" src="${v.coverImageUrl || '/images/vendor-slide-2.jpg'}" alt="${v.venueName}">
                    <span class="module-tag">${v.venueType}</span>
                    <h3>${v.venueName}</h3>
                    <p>${v.city}, ${v.state || ''} • Cap: ${v.minCapacity}-${v.maxCapacity}</p>
                    <p><strong>₹${Number(v.pricePerDay || 0).toLocaleString('en-IN')}/day</strong></p>
                    <button type="button" class="btn-sm btn-primary pv-enq-btn">Send Booking Enquiry</button>
                `;
                card.querySelector('.pv-enq-btn').addEventListener('click', async () => {
                    const eventDate = prompt(`Enter Event Date (YYYY-MM-DD) for ${v.venueName}:`, new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10));
                    if (!eventDate) return;
                    const eventType = prompt('Event / Shoot Type:', 'Event / Production') || 'Event';
                    const guestCount = Number(prompt('Expected Guests / Crew Size:', '100') || '100');
                    const message = prompt('Message / Requirements:', 'Interested in booking this venue.') || '';
                    try {
                        const r = await fetch(`/api/public/venues/${v.id}/enquiries`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ eventDate, eventType, guestCount, budget: v.pricePerDay, message })
                        });
                        const data = await r.json();
                        if (!r.ok) throw new Error(data.message || 'Failed to send enquiry');
                        pvBanner.textContent = `Enquiry ${data.enquiryCode} sent to ${v.venueName}!`;
                        pvBanner.className = 'status-banner success';
                        pvBanner.hidden = false;
                    } catch (err) {
                        pvBanner.textContent = err.message;
                        pvBanner.className = 'status-banner error';
                        pvBanner.hidden = false;
                    }
                });
                pvGrid.appendChild(card);
            });
        } catch (err) {
            pvGrid.innerHTML = `<p class="brand-subtitle">${err.message}</p>`;
        }
    }

    wrap.querySelector('#pv-search-btn').addEventListener('click', searchVenues);
    searchVenues();
})();
