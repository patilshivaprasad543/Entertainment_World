(() => {
    const banner = document.getElementById('vendor-banner');
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    const views = document.querySelectorAll('.view-section');
    const sidebar = document.getElementById('vendor-sidebar');
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');

    let venuesCache = [];
    let currentLimits = null;
    let calYear = new Date().getFullYear();
    let calMonth = new Date().getMonth();
    let calMode = 'month';

    function notify(msg, type = 'success') {
        banner.textContent = msg;
        banner.className = `status-banner ${type}`;
        banner.hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function clearBanner() {
        banner.hidden = true;
    }

    function esc(v) {
        return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    async function api(url, options = {}) {
        const res = await fetch(url, options);
        const ct = res.headers.get('content-type') || '';
        const data = ct.includes('application/json') ? await res.json().catch(() => ({})) : null;
        if (!res.ok) {
            throw new Error(data?.message || `Request failed (${res.status})`);
        }
        return data ?? res;
    }

    function openModal(title, html) {
        document.getElementById('ew-modal-title').textContent = title;
        document.getElementById('ew-modal-body').innerHTML = html;
        document.getElementById('ew-modal').hidden = false;
    }
    document.getElementById('ew-modal-close')?.addEventListener('click', () => {
        document.getElementById('ew-modal').hidden = true;
    });

    function switchView(target) {
        clearBanner();
        navItems.forEach((btn) => btn.classList.toggle('active', btn.dataset.view === target));
        views.forEach((v) => { v.hidden = v.id !== `view-${target}`; });
        if (sidebar) sidebar.classList.remove('open');
    }

    function openSection(view) {
        switchView(view);
        if (view === 'overview') loadOverview();
        if (view === 'profile') loadProfile();
        if (view === 'venues') loadVenues();
        if (view === 'gallery') loadGalleryView();
        if (view === 'pricing') loadPricingView();
        if (view === 'availability') loadAvailabilityView();
        if (view === 'enquiries') loadEnquiries();
        if (view === 'bookings') loadBookings();
        if (view === 'quotations') loadQuotations();
        if (view === 'tour360') load360Tours();
        if (view === 'locations') loadLocations();
        if (view === 'analytics') loadAnalytics('30d');
        if (view === 'team') loadTeam();
        if (view === 'settings') loadNotifications();
    }

    navItems.forEach((btn) => btn.addEventListener('click', () => openSection(btn.dataset.view)));
    document.querySelectorAll('[data-jump]').forEach((el) => {
        el.addEventListener('click', (e) => {
            e.preventDefault();
            openSection(el.dataset.jump);
        });
    });
    mobileMenuBtn?.addEventListener('click', () => sidebar.classList.toggle('open'));

    // Slideshow
    const slides = document.querySelectorAll('.hero-slideshow .slide-item');
    const dots = document.querySelectorAll('#slide-dots .slide-dot');
    let slideIdx = 0;
    function showSlide(i) {
        if (!slides.length) return;
        slideIdx = (i + slides.length) % slides.length;
        slides.forEach((s, idx) => s.classList.toggle('active', idx === slideIdx));
        dots.forEach((d, idx) => d.classList.toggle('active', idx === slideIdx));
    }
    document.getElementById('slide-prev-btn')?.addEventListener('click', () => showSlide(slideIdx - 1));
    document.getElementById('slide-next-btn')?.addEventListener('click', () => showSlide(slideIdx + 1));
    dots.forEach((d) => d.addEventListener('click', () => showSlide(Number(d.dataset.slide))));
    if (slides.length > 1) setInterval(() => showSlide(slideIdx + 1), 5000);

    // Full-Page Side-by-Side Background Slideshow from uploaded Venue Photos
    let bgPhotos = [];
    let bgIdx = 0;
    let bgTimer = null;
    async function initFullPageBackgroundSlideshow() {
        const track = document.getElementById('vendor-bg-track');
        if (!track) return;
        try {
            const venues = await api('/api/vendor/venues');
            const uploaded = [];
            const uploadedVideos = [];
            venues.forEach((v) => {
                (v.media || []).filter((m) => m.mediaType === 'PHOTO').forEach((m) => uploaded.push(m.url));
                (v.media || []).filter((m) => m.mediaType === 'VIDEO').forEach((m) => uploadedVideos.push({ ...m, venueName: v.venueName }));
            });

            // Play uploaded Venue Gallery video(s) inside the larger hero box
            const heroBox = document.getElementById('hero-video-box');
            if (heroBox && uploadedVideos.length) {
                let vIdx = 0;
                const renderHeroVideo = () => {
                    const vid = uploadedVideos[vIdx];
                    heroBox.innerHTML = `
                        <video id="hero-gallery-video" src="${vid.url}" autoplay muted loop playsinline controls style="width:100%;height:100%;object-fit:cover;display:block;background:#000;"></video>
                        <div style="position:absolute;top:14px;left:16px;background:rgba(20,18,25,0.78);color:#fff;padding:0.35rem 0.85rem;border-radius:999px;font-size:0.8rem;font-weight:700;pointer-events:none;">
                            ${esc(vid.venueName)}${vid.title ? ' — ' + esc(vid.title) : ''} (${vIdx + 1}/${uploadedVideos.length})
                        </div>
                        ${uploadedVideos.length > 1 ? '<button type="button" id="hv-prev" class="slide-nav-btn slide-prev" aria-label="Previous video">&lsaquo;</button><button type="button" id="hv-next" class="slide-nav-btn slide-next" aria-label="Next video">&rsaquo;</button>' : ''}
                    `;
                    heroBox.querySelector('#hv-prev')?.addEventListener('click', () => { vIdx = (vIdx - 1 + uploadedVideos.length) % uploadedVideos.length; renderHeroVideo(); });
                    heroBox.querySelector('#hv-next')?.addEventListener('click', () => { vIdx = (vIdx + 1) % uploadedVideos.length; renderHeroVideo(); });
                };
                renderHeroVideo();
            }

            bgPhotos = uploaded.length ? uploaded : ['/images/vendor-slide-1.jpg', '/images/vendor-slide-2.jpg', '/images/vendor-slide-3.jpg'];
            // Clone first photo at end for seamless side-by-side loop without any blank/white gap
            const loopPhotos = bgPhotos.length > 1 ? [...bgPhotos, bgPhotos[0]] : bgPhotos;
            track.innerHTML = loopPhotos.map((url) =>
                `<div style="flex:0 0 100vw;width:100vw;height:100vh;background-image:url('${url}');background-size:cover;background-position:center;background-repeat:no-repeat;"></div>`
            ).join('');
            bgIdx = 0;
            track.style.transition = 'none';
            track.style.transform = 'translateX(0vw)';
            if (bgTimer) clearInterval(bgTimer);
            if (bgPhotos.length > 1) {
                bgTimer = setInterval(() => {
                    bgIdx++;
                    track.style.transition = 'transform 0.85s cubic-bezier(0.22,1,0.36,1)';
                    track.style.transform = `translateX(-${bgIdx * 100}vw)`;
                    if (bgIdx >= bgPhotos.length) {
                        setTimeout(() => {
                            track.style.transition = 'none';
                            bgIdx = 0;
                            track.style.transform = 'translateX(0vw)';
                        }, 880);
                    }
                }, 4000);
            }
        } catch (_) {}
    }

    function renderBarChart(containerId, items, labelKey, valueKey, prefix = '') {
        const el = document.getElementById(containerId);
        if (!el) return;
        if (!items || !items.length) {
            el.innerHTML = '<p class="brand-subtitle">No data available yet.</p>';
            return;
        }
        const max = Math.max(...items.map((x) => Number(x[valueKey] || 0)), 1);
        el.innerHTML = items.map((x) => {
            const val = Number(x[valueKey] || 0);
            const pct = Math.round((val / max) * 100);
            return `<div class="bar-row"><span>${esc(x[labelKey])}</span><div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div><strong>${prefix}${val.toLocaleString('en-IN')}</strong></div>`;
        }).join('');
    }

    // ==================== 1. OVERVIEW ====================
    async function loadOverview() {
        try {
            const d = await api('/api/vendor/dashboard');
            currentLimits = d.planLimits;
            document.getElementById('sidebar-plan-badge').textContent = d.currentPlan || 'Free Plan';
            document.getElementById('kpi-total-venues').textContent = d.totalVenues;
            document.getElementById('kpi-active-venues').textContent = d.activeVenues;
            document.getElementById('kpi-pending-enquiries').textContent = d.pendingEnquiries;
            document.getElementById('kpi-confirmed-bookings').textContent = d.confirmedBookings;
            document.getElementById('kpi-pending-bookings').textContent = d.pendingBookings;
            document.getElementById('kpi-total-revenue').textContent = `₹${Number(d.totalRevenue || 0).toLocaleString('en-IN')}`;
            document.getElementById('kpi-available-venues').textContent = d.availableVenues;
            document.getElementById('kpi-current-plan').textContent = d.planLimits?.tier || 'FREE';

            const enqBadge = document.getElementById('enq-badge');
            if (d.pendingEnquiries > 0) { enqBadge.textContent = d.pendingEnquiries; enqBadge.hidden = false; }
            else { enqBadge.hidden = true; }

            document.getElementById('ov-recent-enquiries').innerHTML = (d.recentEnquiries || []).length
                ? d.recentEnquiries.map((e) => `<p style="margin:0.4rem 0;font-size:0.86rem;"><strong>${esc(e.customerName)}</strong> — ${esc(e.venueName)} (${esc(e.eventDate)}) <span class="pill">${esc(e.status)}</span></p>`).join('')
                : '<p class="brand-subtitle">No enquiries yet.</p>';

            document.getElementById('ov-upcoming-bookings').innerHTML = (d.upcomingBookings || []).length
                ? d.upcomingBookings.map((b) => `<p style="margin:0.4rem 0;font-size:0.86rem;"><strong>${esc(b.bookingCode)}</strong> — ${esc(b.venueName)} (${esc(b.eventDate)}) <span class="pill">${esc(b.status)}</span></p>`).join('')
                : '<p class="brand-subtitle">No upcoming bookings.</p>';

            document.getElementById('ov-recent-activities').innerHTML = (d.recentActivities || []).length
                ? d.recentActivities.map((a) => `<p style="margin:0.4rem 0;font-size:0.84rem;"><strong>${esc(a.title)}</strong>: ${esc(a.message)}</p>`).join('')
                : '<p class="brand-subtitle">No recent activities.</p>';

            const st = d.bookingStatusOverview || {};
            document.getElementById('ov-status-overview').innerHTML = Object.entries(st)
                .map(([k, v]) => `<div class="bar-row"><span>${esc(k)}</span><div class="bar-track"><div class="bar-fill" style="width:${Math.min(100, v * 20)}%"></div></div><strong>${v}</strong></div>`).join('');

            renderBarChart('ov-revenue-chart', d.monthlyRevenueChart, 'month', 'revenue', '₹');
            renderBarChart('ov-performance-chart', d.venuePerformanceChart, 'venueName', 'bookings');
        } catch (err) {
            notify(err.message, 'error');
        }
    }

    // ==================== 2. PROFILE ====================
    async function loadProfile() {
        try {
            const p = await api('/api/vendors/profile');
            document.getElementById('prof-banner-company').textContent = p.companyName || 'Vendor Studio';
            document.getElementById('prof-company').value = p.companyName || '';
            document.getElementById('prof-contact').value = p.contactPerson || '';
            document.getElementById('prof-designation').value = p.designation || '';
            document.getElementById('prof-email').value = p.businessEmail || '';
            document.getElementById('prof-phone').value = p.phone || '';
            document.getElementById('prof-alt-phone').value = p.alternatePhone || '';
            document.getElementById('prof-gst').value = p.gstNumber || '';
            document.getElementById('prof-website').value = p.websiteUrl || '';
            document.getElementById('prof-years').value = p.yearsInBusiness ?? '';
            document.getElementById('prof-venues').value = p.venuesManaged ?? '';
            document.getElementById('prof-country').value = p.country || 'India';
            document.getElementById('prof-state').value = p.state || '';
            document.getElementById('prof-city').value = p.city || '';
            document.getElementById('prof-pincode').value = p.pincode || '';
            document.getElementById('prof-address').value = p.address || '';
            document.getElementById('prof-bio').value = p.bio || '';
            document.getElementById('prof-desc').value = p.description || '';
            document.getElementById('prof-about-us').value = p.aboutUs || '';
            document.getElementById('prof-terms').checked = Boolean(p.acceptedTerms);
            document.getElementById('prof-privacy').checked = Boolean(p.acceptedPrivacyPolicy);
            document.getElementById('prof-ver-status').textContent = p.verificationStatus || 'APPROVED';
            document.getElementById('prof-doc-info').textContent = p.documentType ? `${p.documentType} (${p.documentNumber || ''})` : 'Verified Account';
            const docLink = document.getElementById('prof-doc-link');
            if (p.documentUrl) { docLink.href = p.documentUrl; docLink.hidden = false; } else { docLink.hidden = true; }
            const logoImg = document.getElementById('prof-logo-preview');
            if (p.logoUrl) { logoImg.src = p.logoUrl; logoImg.hidden = false; } else { logoImg.hidden = true; }
            if (p.coverImageUrl) document.getElementById('prof-cover-preview').src = p.coverImageUrl;
            renderProfileVenuePhotos();
        } catch (err) {
            notify(err.message, 'error');
        }
    }

    function buildSlideshowAndFullGalleryHtml(photos, prefixId) {
        if (!photos || !photos.length) return '<p class="brand-subtitle">No venue photos uploaded yet.</p>';
        return `
            <div style="position:relative;border-radius:16px;overflow:hidden;background:#141219;margin-bottom:1.25rem;">
                <img id="${prefixId}-slide-img" src="${photos[0].url}" alt="${esc(photos[0].title || 'Venue Photo')}" style="width:100%;height:460px;object-fit:cover;display:block;">
                <button type="button" id="${prefixId}-prev" class="slide-nav-btn slide-prev" aria-label="Previous photo">&lsaquo;</button>
                <button type="button" id="${prefixId}-next" class="slide-nav-btn slide-next" aria-label="Next photo">&rsaquo;</button>
                <div style="position:absolute;bottom:14px;left:50%;transform:translateX(-50%);background:rgba(20,18,25,0.82);color:#fff;padding:0.35rem 0.9rem;border-radius:999px;font-size:0.82rem;font-weight:700;" id="${prefixId}-counter">1 / ${photos.length} — ${esc(photos[0].title || 'Venue Photo')}</div>
            </div>
            <div style="display:flex;gap:0.5rem;overflow-x:auto;padding-bottom:0.5rem;margin-bottom:1.25rem;">
                ${photos.map((ph, idx) => `<img src="${ph.url}" data-idx="${idx}" class="${prefixId}-thumb" style="width:92px;height:64px;object-fit:cover;border-radius:8px;cursor:pointer;border:2px solid ${idx === 0 ? '#e11d48' : 'transparent'};">`).join('')}
            </div>
            <h3 style="margin:0.5rem 0 0.75rem;">Full-Page Venue Gallery (${photos.length} Photos)</h3>
            <div style="display:flex;flex-direction:column;gap:1rem;">
                ${photos.map((ph) => `
                    <div style="border-radius:14px;overflow:hidden;border:1px solid #e7e2d9;background:#fff;">
                        <img src="${ph.url}" alt="${esc(ph.title || 'Venue Photo')}" style="width:100%;max-height:580px;object-fit:cover;display:block;">
                        <div style="padding:0.6rem 0.9rem;font-size:0.88rem;font-weight:600;">${esc(ph.venueName || '')} ${ph.title ? '— ' + esc(ph.title) : ''}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    function wireSlideshowControls(photos, prefixId) {
        if (!photos || !photos.length) return null;
        let idx = 0;
        const imgEl = document.getElementById(`${prefixId}-slide-img`);
        const cntEl = document.getElementById(`${prefixId}-counter`);
        const thumbs = document.querySelectorAll(`.${prefixId}-thumb`);
        const show = (n) => {
            idx = (n + photos.length) % photos.length;
            if (imgEl) imgEl.src = photos[idx].url;
            if (cntEl) cntEl.textContent = `${idx + 1} / ${photos.length} — ${photos[idx].title || 'Venue Photo'}`;
            thumbs.forEach((t, i) => { t.style.borderColor = i === idx ? '#e11d48' : 'transparent'; });
        };
        document.getElementById(`${prefixId}-prev`)?.addEventListener('click', () => show(idx - 1));
        document.getElementById(`${prefixId}-next`)?.addEventListener('click', () => show(idx + 1));
        thumbs.forEach((t) => t.addEventListener('click', () => show(Number(t.dataset.idx))));
        if (photos.length > 1) {
            return setInterval(() => {
                if (!document.getElementById(`${prefixId}-slide-img`)) return;
                show(idx + 1);
            }, 3500);
        }
        return null;
    }

    async function renderProfileVenuePhotos() {
        const wrap = document.getElementById('prof-venue-photos-wrap');
        if (!wrap) return;
        try {
            const venues = await api('/api/vendor/venues');
            const photos = [];
            venues.forEach((v) => {
                (v.media || []).filter((m) => m.mediaType === 'PHOTO').forEach((m) => {
                    photos.push({ ...m, venueName: v.venueName });
                });
            });
            wrap.innerHTML = `<h3 style="margin:0 0 0.75rem;color:#be123c;border-top:1px solid #e7e2d9;padding-top:1rem;">My Venue Photos Slideshow &amp; Full Gallery</h3>`
                + buildSlideshowAndFullGalleryHtml(photos, 'prof-gal');
            wireSlideshowControls(photos, 'prof-gal');
        } catch (_) {}
    }

    document.getElementById('profile-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/vendors/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    companyName: document.getElementById('prof-company').value,
                    contactPerson: document.getElementById('prof-contact').value,
                    designation: document.getElementById('prof-designation').value,
                    businessEmail: document.getElementById('prof-email').value,
                    phone: document.getElementById('prof-phone').value,
                    alternatePhone: document.getElementById('prof-alt-phone').value,
                    gstNumber: document.getElementById('prof-gst').value,
                    websiteUrl: document.getElementById('prof-website').value,
                    yearsInBusiness: document.getElementById('prof-years').value ? Number(document.getElementById('prof-years').value) : null,
                    venuesManaged: document.getElementById('prof-venues').value ? Number(document.getElementById('prof-venues').value) : null,
                    country: document.getElementById('prof-country').value,
                    state: document.getElementById('prof-state').value,
                    city: document.getElementById('prof-city').value,
                    pincode: document.getElementById('prof-pincode').value,
                    address: document.getElementById('prof-address').value,
                    bio: document.getElementById('prof-bio').value,
                    description: document.getElementById('prof-desc').value,
                    aboutUs: document.getElementById('prof-about-us').value,
                    acceptedTerms: document.getElementById('prof-terms').checked,
                    acceptedPrivacyPolicy: document.getElementById('prof-privacy').checked
                })
            });
            notify('Vendor profile saved successfully.');
            loadProfile();
        } catch (err) { notify(err.message, 'error'); }
    });

    document.getElementById('logo-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const file = document.getElementById('logo-file').files[0];
        if (!file) return notify('Select a logo file first.', 'error');
        const fd = new FormData();
        fd.append('logoFile', file);
        try {
            await api('/api/vendors/profile/logo', { method: 'POST', body: fd });
            notify('Logo uploaded.');
            loadProfile();
        } catch (err) { notify(err.message, 'error'); }
    });

    document.getElementById('cover-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const file = document.getElementById('cover-file').files[0];
        if (!file) return notify('Select a cover image first.', 'error');
        const fd = new FormData();
        fd.append('coverFile', file);
        try {
            await api('/api/vendors/profile/cover', { method: 'POST', body: fd });
            notify('Cover image uploaded.');
            loadProfile();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 3. MY VENUES ====================
    const COUNTRY_STATES = {
        India: ['Andhra Pradesh', 'Telangana', 'Karnataka', 'Tamil Nadu', 'Maharashtra', 'Kerala', 'Delhi', 'Gujarat', 'Rajasthan', 'West Bengal', 'Uttar Pradesh', 'Other'],
        'United States': ['California', 'New York', 'Texas', 'Florida', 'Illinois', 'Washington', 'Other'],
        'United Kingdom': ['England', 'Scotland', 'Wales', 'Northern Ireland'],
        Canada: ['Ontario', 'Quebec', 'British Columbia', 'Alberta', 'Other'],
        Australia: ['New South Wales', 'Victoria', 'Queensland', 'Western Australia', 'Other'],
        UAE: ['Dubai', 'Abu Dhabi', 'Sharjah', 'Ajman', 'Other'],
        Singapore: ['Central Region', 'East Region', 'North Region', 'West Region'],
        Other: ['Other']
    };

    function populateStateDropdown(country, selectedState = '') {
        const stateSel = document.getElementById('vn-state');
        if (!stateSel) return;
        const states = COUNTRY_STATES[country] || ['Other'];
        stateSel.innerHTML = '<option value="">-- Select State --</option>'
            + states.map((s) => `<option value="${esc(s)}">${esc(s)}</option>`).join('');
        if (selectedState) {
            if (!states.includes(selectedState)) {
                stateSel.innerHTML += `<option value="${esc(selectedState)}">${esc(selectedState)}</option>`;
            }
            stateSel.value = selectedState;
        }
    }

    function updateMapPreview() {
        const url = (document.getElementById('vn-map')?.value || '').trim();
        const link = document.getElementById('vn-map-preview');
        if (!link) return;
        if (/^https?:\/\/.+/.test(url)) {
            link.href = url;
            link.hidden = false;
        } else {
            link.hidden = true;
        }
    }

    document.getElementById('vn-country')?.addEventListener('change', (e) => populateStateDropdown(e.target.value));
    document.getElementById('vn-map')?.addEventListener('input', updateMapPreview);
    populateStateDropdown('India');

    async function populateVenueSelects() {
        venuesCache = await api('/api/vendor/venues');
        const ids = ['gal-venue', 'pr-venue', 'cal-venue', 'bk-venue', 'qt-venue', 't360-venue', 'loc-venue'];
        ids.forEach((id) => {
            const sel = document.getElementById(id);
            if (!sel) return;
            const cur = sel.value;
            sel.innerHTML = (id === 'loc-venue' ? '<option value="">-- Optional --</option>' : '')
                + venuesCache.map((v) => `<option value="${v.id}">${esc(v.venueName)} (${esc(v.city)})</option>`).join('');
            if (cur) sel.value = cur;
        });
        return venuesCache;
    }

    async function loadVenues() {
        try {
            const [venues, sub] = await Promise.all([populateVenueSelects(), api('/api/vendor/subscriptions')]);
            currentLimits = sub.limits;
            document.getElementById('venue-limit-label').textContent =
                `Plan: ${currentLimits.planName} (${venues.length} / ${currentLimits.maxVenues} venues used)`;

            const list = document.getElementById('venues-list');
            if (!venues.length) {
                list.innerHTML = '<p class="brand-subtitle">No venues added yet. Use the form above to register your first venue.</p>';
                return;
            }
            list.innerHTML = '';
            venues.forEach((v) => {
                const card = document.createElement('article');
                card.className = 'studio-work-card';
                card.innerHTML = `
                    <img class="work-card-img" src="${v.coverImageUrl || '/images/vendor-slide-2.jpg'}" alt="${esc(v.venueName)}">
                    <span class="module-tag">${esc(v.venueType)} • ${esc(v.venueCategory || 'Indoor')}</span>
                    <h3>${esc(v.venueName)}</h3>
                    <p>${esc(v.areaLocality ? v.areaLocality + ', ' : '')}${esc(v.city)}, ${esc(v.state || '')} (${esc(v.pincode || '')})</p>
                    <p><small>${esc(v.contactCountryCode || '+91')} ${esc(v.contactNumber || '')}</small> • <span class="pill">${v.active ? 'ACTIVE' : 'INACTIVE'}</span></p>
                    <div class="btn-row">
                        <button type="button" class="btn-sm btn-secondary vn-view">View</button>
                        <button type="button" class="btn-sm btn-secondary vn-edit">Edit</button>
                        <button type="button" class="btn-sm btn-primary vn-toggle">${v.active ? 'Deactivate' : 'Activate'}</button>
                        <button type="button" class="btn-sm btn-danger vn-del">Delete</button>
                    </div>
                `;
                card.querySelector('.vn-view').addEventListener('click', () => {
                    const modalCard = document.querySelector('#ew-modal .ew-modal-card');
                    if (modalCard) {
                        modalCard.style.maxWidth = '1120px';
                        modalCard.style.width = '96vw';
                        modalCard.style.maxHeight = '92vh';
                    }
                    const photos = (v.media || []).filter((m) => m.mediaType === 'PHOTO');
                    openModal(v.venueName, `
                        ${buildSlideshowAndFullGalleryHtml(photos, 'vn-modal')}
                        <div style="margin-top:1.25rem;padding-top:1rem;border-top:1px solid #e7e2d9;">
                            <p><strong>Type:</strong> ${esc(v.venueType)} | <strong>Category:</strong> ${esc(v.venueCategory || 'Indoor')}</p>
                            <p><strong>Location:</strong> ${esc(v.areaLocality || '')}, ${esc(v.city)}, ${esc(v.state || '')}, ${esc(v.country || '')} - ${esc(v.pincode || '')}</p>
                            <p><strong>Complete Address:</strong> ${esc(v.address || '')}</p>
                            <p><strong>Contact:</strong> ${esc(v.contactCountryCode || '+91')} ${esc(v.contactNumber || '')}</p>
                            ${v.mapLink ? `<p><a href="${esc(v.mapLink)}" target="_blank" rel="noopener noreferrer" class="btn-sm btn-secondary">Open Google Map</a></p>` : ''}
                            <p><strong>Description:</strong> ${esc(v.description || '')}</p>
                        </div>
                    `);
                    wireSlideshowControls(photos, 'vn-modal');
                });
                card.querySelector('.vn-edit').addEventListener('click', () => fillVenueForm(v));
                card.querySelector('.vn-toggle').addEventListener('click', async () => {
                    await api(`/api/vendor/venues/${v.id}/status`, {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ active: !v.active })
                    });
                    notify(`Venue ${!v.active ? 'activated' : 'deactivated'}.`);
                    loadVenues();
                });
                card.querySelector('.vn-del').addEventListener('click', async () => {
                    if (!confirm(`Delete "${v.venueName}"? This cannot be undone.`)) return;
                    await api(`/api/vendor/venues/${v.id}`, { method: 'DELETE' });
                    notify('Venue deleted.');
                    loadVenues();
                });
                list.appendChild(card);
            });
        } catch (err) { notify(err.message, 'error'); }
    }

    function fillVenueForm(v) {
        document.getElementById('venue-form-card').hidden = false;
        document.getElementById('venue-form-error').hidden = true;
        document.getElementById('venue-form-title').textContent = `Edit Venue: ${v.venueName}`;
        document.getElementById('vn-id').value = v.id;
        document.getElementById('vn-name').value = v.venueName || '';
        document.getElementById('vn-type').value = v.venueType || 'Banquet Hall';
        document.getElementById('vn-desc').value = v.description || '';
        document.getElementById('vn-country').value = v.country || 'India';
        populateStateDropdown(v.country || 'India', v.state || '');
        document.getElementById('vn-city').value = v.city || '';
        document.getElementById('vn-area').value = v.areaLocality || '';
        document.getElementById('vn-address').value = v.address || '';
        document.getElementById('vn-pincode').value = v.pincode || '';
        document.getElementById('vn-map').value = v.mapLink || '';
        updateMapPreview();
        document.getElementById('vn-phone-code').value = v.contactCountryCode || '+91';
        document.getElementById('vn-phone').value = v.contactNumber || '';
        const cat = (v.venueCategory || 'Indoor') === 'Outdoor' ? 'Outdoor' : 'Indoor';
        const radio = document.querySelector(`input[name="vn-category"][value="${cat}"]`);
        if (radio) radio.checked = true;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function resetVenueForm() {
        document.getElementById('venue-form').reset();
        document.getElementById('vn-id').value = '';
        document.getElementById('vn-country').value = 'India';
        populateStateDropdown('India');
        document.getElementById('venue-form-error').hidden = true;
        document.getElementById('venue-form-title').textContent = 'Add New Venue';
        updateMapPreview();
    }

    document.getElementById('venue-reset-btn')?.addEventListener('click', resetVenueForm);
    document.getElementById('venue-cancel-btn')?.addEventListener('click', () => {
        resetVenueForm();
        document.getElementById('venue-form-card').hidden = true;
        document.getElementById('my-venues-list-card')?.scrollIntoView({ behavior: 'smooth' });
    });
    document.getElementById('open-add-venue-btn')?.addEventListener('click', () => {
        resetVenueForm();
        document.getElementById('venue-form-card').hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    function validateVenuePayload(p) {
        if (!p.venueName.trim()) return 'Venue Name is required.';
        if (!p.venueType.trim()) return 'Venue Type is required.';
        if (!p.description.trim()) return 'Venue Description is required.';
        if (!p.country.trim()) return 'Country is required.';
        if (!p.state.trim()) return 'State is required.';
        if (!p.city.trim()) return 'City is required.';
        if (!p.areaLocality.trim()) return 'Area / Locality is required.';
        if (!p.address.trim()) return 'Complete Address is required.';
        if (!p.pincode.trim()) return 'PIN Code is required.';

        const pinPatterns = {
            India: /^[1-9][0-9]{5}$/,
            'United States': /^\d{5}(-\d{4})?$/,
            'United Kingdom': /^[A-Za-z0-9\s-]{3,10}$/,
            Canada: /^[A-Za-z]\d[A-Za-z][ -]?\d[A-Za-z]\d$/,
            Australia: /^\d{4}$/
        };
        const pinRx = pinPatterns[p.country] || /^[A-Za-z0-9\s-]{3,12}$/;
        if (!pinRx.test(p.pincode.trim())) {
            return `Please enter a valid PIN / Postal Code for ${p.country}.`;
        }
        if (!p.mapLink.trim() || !/^https?:\/\/.+/.test(p.mapLink.trim())) {
            return 'Please enter a valid Google Maps Location URL (starting with http:// or https://).';
        }
        const cleanPhone = p.contactNumber.replace(/[\s\-()]/g, '');
        if (!/^[0-9]{7,15}$/.test(cleanPhone)) {
            return 'Please enter a valid Contact Phone Number (7 to 15 digits).';
        }
        if (!p.venueCategory) return 'Please select a Venue Category (Indoor or Outdoor).';
        return null;
    }

    document.getElementById('venue-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errBox = document.getElementById('venue-form-error');
        errBox.hidden = true;
        const id = document.getElementById('vn-id').value;
        const catEl = document.querySelector('input[name="vn-category"]:checked');
        const payload = {
            venueName: document.getElementById('vn-name').value,
            venueType: document.getElementById('vn-type').value,
            description: document.getElementById('vn-desc').value,
            country: document.getElementById('vn-country').value,
            state: document.getElementById('vn-state').value,
            city: document.getElementById('vn-city').value,
            areaLocality: document.getElementById('vn-area').value,
            address: document.getElementById('vn-address').value,
            pincode: document.getElementById('vn-pincode').value,
            mapLink: document.getElementById('vn-map').value,
            contactCountryCode: document.getElementById('vn-phone-code').value,
            contactNumber: document.getElementById('vn-phone').value,
            venueCategory: catEl ? catEl.value : ''
        };

        const validationError = validateVenuePayload(payload);
        if (validationError) {
            errBox.textContent = validationError;
            errBox.hidden = false;
            errBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        try {
            await api(id ? `/api/vendor/venues/${id}` : '/api/vendor/venues', {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            notify(id ? 'Venue updated successfully.' : 'Venue registered successfully.');
            resetVenueForm();
            loadVenues();
        } catch (err) {
            errBox.textContent = err.message;
            errBox.hidden = false;
        }
    });

    // ==================== 4. GALLERY ====================
    async function loadGalleryView() {
        await populateVenueSelects();
        const sub = await api('/api/vendor/subscriptions');
        currentLimits = sub.limits;
        renderVenueMedia();
    }

    async function renderVenueMedia() {
        const venueId = document.getElementById('gal-venue').value;
        const grid = document.getElementById('gallery-media-grid');
        if (!venueId) { grid.innerHTML = '<p class="brand-subtitle">Please create a venue first.</p>'; return; }
        const items = await api(`/api/vendor/venues/${venueId}/media`);
        const photoCount = items.filter((m) => m.mediaType === 'PHOTO').length;
        document.getElementById('gallery-limit-label').textContent =
            `Photos used for this venue: ${photoCount} / ${currentLimits?.maxPhotosPerVenue || 5} (${currentLimits?.planName || 'Free'})`;
        if (!items.length) { grid.innerHTML = '<p class="brand-subtitle">No media uploaded for this venue yet.</p>'; return; }
        grid.innerHTML = '';
        items.forEach((m) => {
            const card = document.createElement('div');
            card.className = 'media-card';
            const mediaHtml = m.mediaType === 'VIDEO'
                ? `<video src="${m.url}" controls></video>`
                : `<img src="${m.url}" alt="${esc(m.title)}">`;
            card.innerHTML = `
                ${mediaHtml}
                <div class="media-card-body">
                    <strong>${esc(m.title || m.originalName)}</strong>
                    <span class="module-tag">${esc(m.mediaType)} ${m.cover ? '• COVER' : ''}</span>
                    <div class="btn-row">
                        ${m.mediaType === 'PHOTO' && !m.cover ? '<button type="button" class="btn-sm btn-secondary m-cov">Set Cover</button>' : ''}
                        <button type="button" class="btn-sm btn-danger m-del">Delete</button>
                    </div>
                </div>
            `;
            card.querySelector('.m-cov')?.addEventListener('click', async () => {
                await api(`/api/vendor/venues/${venueId}/media/${m.id}/cover`, { method: 'PATCH' });
                notify('Cover photo updated.');
                renderVenueMedia();
            });
            card.querySelector('.m-del').addEventListener('click', async () => {
                await api(`/api/vendor/venues/media/${m.id}`, { method: 'DELETE' });
                notify('Media deleted.');
                renderVenueMedia();
            });
            grid.appendChild(card);
        });
    }

    document.getElementById('gal-venue')?.addEventListener('change', renderVenueMedia);
    document.getElementById('gallery-upload-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const venueId = document.getElementById('gal-venue').value;
        const files = document.getElementById('gal-files').files;
        if (!venueId || !files.length) return notify('Select a venue and file(s).', 'error');
        let uploaded = 0;
        try {
            for (const f of files) {
                const fd = new FormData();
                fd.append('mediaType', document.getElementById('gal-type').value);
                fd.append('title', document.getElementById('gal-title').value || f.name);
                fd.append('file', f);
                await api(`/api/vendor/venues/${venueId}/media`, { method: 'POST', body: fd });
                uploaded++;
            }
            notify(`${uploaded} file(s) uploaded successfully.`);
            document.getElementById('gal-files').value = '';
        } catch (err) {
            notify(uploaded > 0 ? `${uploaded} uploaded, stopped: ${err.message}` : err.message, 'error');
        } finally {
            renderVenueMedia();
            initFullPageBackgroundSlideshow();
        }
    });

    // ==================== 5. PRICING ====================
    async function loadPricingView() {
        await populateVenueSelects();
        fillPricingFields();
    }
    function fillPricingFields() {
        const v = venuesCache.find((x) => String(x.id) === document.getElementById('pr-venue').value);
        if (!v) return;
        document.getElementById('pr-day').value = v.pricePerDay ?? 0;
        document.getElementById('pr-hour').value = v.pricePerHour ?? 0;
        document.getElementById('pr-weekday').value = v.weekdayPrice ?? 0;
        document.getElementById('pr-weekend').value = v.weekendPrice ?? 0;
        document.getElementById('pr-seasonal').value = v.seasonalPrice ?? 0;
        document.getElementById('pr-special').value = v.specialEventPrice ?? 0;
        document.getElementById('pr-additional').value = v.additionalCharges ?? 0;
        document.getElementById('pr-decoration').value = v.decorationCharges ?? 0;
        document.getElementById('pr-catering').value = v.cateringCharges ?? 0;
        document.getElementById('pr-advance').value = v.advanceAmount ?? 0;
    }
    document.getElementById('pr-venue')?.addEventListener('change', fillPricingFields);
    document.getElementById('pricing-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('pr-venue').value;
        if (!id) return;
        try {
            await api(`/api/vendor/venues/${id}/pricing`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    pricePerDay: Number(document.getElementById('pr-day').value),
                    pricePerHour: Number(document.getElementById('pr-hour').value || 0),
                    weekdayPrice: Number(document.getElementById('pr-weekday').value || 0),
                    weekendPrice: Number(document.getElementById('pr-weekend').value || 0),
                    seasonalPrice: Number(document.getElementById('pr-seasonal').value || 0),
                    specialEventPrice: Number(document.getElementById('pr-special').value || 0),
                    additionalCharges: Number(document.getElementById('pr-additional').value || 0),
                    decorationCharges: Number(document.getElementById('pr-decoration').value || 0),
                    cateringCharges: Number(document.getElementById('pr-catering').value || 0),
                    advanceAmount: Number(document.getElementById('pr-advance').value || 0)
                })
            });
            notify('Venue pricing updated.');
            populateVenueSelects();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 6. AVAILABILITY CALENDAR ====================
    async function loadAvailabilityView() {
        await populateVenueSelects();
        renderCalendar();
    }

    async function renderCalendar() {
        const venueId = document.getElementById('cal-venue').value;
        const grid = document.getElementById('cal-grid');
        if (!venueId) { grid.innerHTML = '<p class="brand-subtitle">Create a venue first.</p>'; return; }

        const pad = (n) => String(n).padStart(2, '0');
        const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
        const now = new Date();
        const todayStr = fmt(now);

        if (calYear < now.getFullYear() || (calYear === now.getFullYear() && calMonth < now.getMonth())) {
            calYear = now.getFullYear();
            calMonth = now.getMonth();
        }

        const start = new Date(calYear, calMonth, 1);
        const end = new Date(calYear, calMonth + 1, 0);

        document.getElementById('cal-month-label').textContent = start.toLocaleString('en-US', { month: 'long', year: 'numeric' });
        const records = await api(`/api/vendor/venues/${venueId}/availability?start=${fmt(start)}&end=${fmt(end)}`);
        const map = {};
        records.forEach((r) => { map[r.date] = r; });

        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        grid.innerHTML = days.map((d) => `<div class="cal-head">${d}</div>`).join('');

        const isCurrentMonth = (calYear === now.getFullYear() && calMonth === now.getMonth());
        const startDay = (calMode === 'week' && isCurrentMonth) ? now.getDate() : 1;
        const limitDays = calMode === 'week' ? Math.min(startDay + 6, end.getDate()) : end.getDate();

        if (calMode === 'month') {
            for (let i = 0; i < start.getDay(); i++) {
                grid.innerHTML += '<div class="cal-cell empty"></div>';
            }
        }
        for (let day = startDay; day <= limitDays; day++) {
            const dStr = `${calYear}-${pad(calMonth + 1)}-${pad(day)}`;
            const isPast = dStr < todayStr;
            const rec = map[dStr];
            const st = isPast ? 'PAST' : (rec ? rec.status : 'AVAILABLE');
            const cell = document.createElement('div');
            cell.className = `cal-cell status-${isPast ? 'BLOCKED' : st}`;
            if (isPast) {
                cell.style.opacity = '0.45';
                cell.style.cursor = 'not-allowed';
            }
            cell.innerHTML = `<span class="cal-day-num">${day}</span><span class="cal-day-tag">${st}</span>`;
            if (!isPast) {
                cell.addEventListener('click', async () => {
                    if (st === 'BOOKED') {
                        return openModal(`Booked: ${dStr}`, `<p>${esc(rec?.note || 'Confirmed booking on this date.')}</p>`);
                    }
                    const next = st === 'BLOCKED' ? 'AVAILABLE' : 'BLOCKED';
                    try {
                        await api(`/api/vendor/venues/${venueId}/availability`, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ date: dStr, status: next, note: next === 'BLOCKED' ? 'Manually blocked by vendor' : '' })
                        });
                        notify(`Date ${dStr} marked ${next}.`);
                        renderCalendar();
                    } catch (err) { notify(err.message, 'error'); }
                });
            }
            grid.appendChild(cell);
        }
    }

    document.getElementById('cal-venue')?.addEventListener('change', renderCalendar);
    document.getElementById('cal-prev-btn')?.addEventListener('click', () => {
        const now = new Date();
        if (calYear < now.getFullYear() || (calYear === now.getFullYear() && calMonth <= now.getMonth())) return;
        calMonth--;
        if (calMonth < 0) { calMonth = 11; calYear--; }
        renderCalendar();
    });
    document.getElementById('cal-next-btn')?.addEventListener('click', () => { calMonth++; if (calMonth > 11) { calMonth = 0; calYear++; } renderCalendar(); });
    document.getElementById('cal-mode-month')?.addEventListener('click', () => { calMode = 'month'; renderCalendar(); });
    document.getElementById('cal-mode-week')?.addEventListener('click', () => { calMode = 'week'; renderCalendar(); });

    // ==================== 7. ENQUIRIES ====================
    async function loadEnquiries() {
        try {
            const rows = await api('/api/vendor/enquiries');
            const tbody = document.getElementById('enquiries-tbody');
            if (!rows.length) { tbody.innerHTML = '<tr><td colspan="7">No booking enquiries yet.</td></tr>'; return; }
            tbody.innerHTML = '';
            rows.forEach((e) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${esc(e.enquiryCode)}</strong></td>
                    <td>${esc(e.customerName)}<br><small>${esc(e.customerEmail)}</small></td>
                    <td>${esc(e.venueName)}</td>
                    <td>${esc(e.eventDate)}<br><small>${esc(e.eventType)}</small></td>
                    <td>${e.guestCount || '-'} guests<br><small>₹${Number(e.budget || 0).toLocaleString('en-IN')}</small></td>
                    <td><span class="pill">${esc(e.status)}</span></td>
                    <td class="btn-row">
                        <button type="button" class="btn-sm btn-secondary e-rev">Review</button>
                        <button type="button" class="btn-sm btn-primary e-qtn">Send Quotation</button>
                        <button type="button" class="btn-sm btn-danger e-rej">Reject</button>
                    </td>
                `;
                tr.querySelector('.e-rev').addEventListener('click', async () => {
                    await api(`/api/vendor/enquiries/${e.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'UNDER_REVIEW' }) });
                    notify('Enquiry marked UNDER_REVIEW.');
                    loadEnquiries();
                });
                tr.querySelector('.e-rej').addEventListener('click', async () => {
                    await api(`/api/vendor/enquiries/${e.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'REJECTED' }) });
                    notify('Enquiry rejected.');
                    loadEnquiries();
                });
                tr.querySelector('.e-qtn').addEventListener('click', async () => {
                    await openSection('quotations');
                    document.getElementById('qt-enq-id').value = e.id;
                    document.getElementById('qt-venue').value = e.venueId;
                    document.getElementById('qt-name').value = e.customerName;
                    document.getElementById('qt-email').value = e.customerEmail;
                    document.getElementById('qt-mobile').value = e.customerMobile || '';
                    document.getElementById('qt-date').value = e.eventDate;
                    document.getElementById('qt-rental').value = e.budget || 50000;
                    recalcQuotation();
                });
                tbody.appendChild(tr);
            });
        } catch (err) { notify(err.message, 'error'); }
    }

    // ==================== 8. BOOKINGS ====================
    async function loadBookings() {
        await populateVenueSelects();
        const rows = await api('/api/vendor/bookings');
        const tbody = document.getElementById('bookings-tbody');
        if (!rows.length) { tbody.innerHTML = '<tr><td colspan="7">No bookings yet.</td></tr>'; return; }
        tbody.innerHTML = '';
        rows.forEach((b) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(b.bookingCode)}</strong></td>
                <td>${esc(b.customerName)}<br><small>${esc(b.customerEmail)}</small></td>
                <td>${esc(b.venueName)}</td>
                <td>${esc(b.eventDate)}</td>
                <td>₹${Number(b.totalAmount || 0).toLocaleString('en-IN')}<br><small>${esc(b.paymentStatus)}</small></td>
                <td><span class="pill">${esc(b.status)}</span></td>
                <td class="btn-row">
                    <button type="button" class="btn-sm btn-primary b-conf">Confirm</button>
                    <button type="button" class="btn-sm btn-secondary b-comp">Complete</button>
                    <button type="button" class="btn-sm btn-danger b-canc">Cancel</button>
                </td>
            `;
            const upd = async (st) => {
                try {
                    await api(`/api/vendor/bookings/${b.id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: st }) });
                    notify(`Booking ${b.bookingCode} updated to ${st}.`);
                    loadBookings();
                } catch (err) { notify(err.message, 'error'); }
            };
            tr.querySelector('.b-conf').addEventListener('click', () => upd('CONFIRMED'));
            tr.querySelector('.b-comp').addEventListener('click', () => upd('COMPLETED'));
            tr.querySelector('.b-canc').addEventListener('click', () => upd('CANCELLED'));
            tbody.appendChild(tr);
        });
    }

    document.getElementById('booking-create-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/vendor/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    venueId: Number(document.getElementById('bk-venue').value),
                    customerName: document.getElementById('bk-name').value,
                    customerEmail: document.getElementById('bk-email').value,
                    customerMobile: document.getElementById('bk-mobile').value,
                    eventType: document.getElementById('bk-type').value,
                    eventDate: document.getElementById('bk-date').value,
                    guestCount: Number(document.getElementById('bk-guests').value),
                    totalAmount: Number(document.getElementById('bk-total').value),
                    status: 'PENDING'
                })
            });
            notify('Booking created.');
            loadBookings();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 9. QUOTATIONS ====================
    function recalcQuotation() {
        const n = (id) => Number(document.getElementById(id).value || 0);
        const total = Math.max(0, n('qt-rental') + n('qt-add') + n('qt-decor') + n('qt-cater') - n('qt-disc') + n('qt-tax'));
        const rem = Math.max(0, total - n('qt-adv'));
        document.getElementById('qt-summary').textContent = `Total: ₹${total.toLocaleString('en-IN')} | Rem: ₹${rem.toLocaleString('en-IN')}`;
    }
    document.querySelectorAll('.qt-calc').forEach((el) => el.addEventListener('input', recalcQuotation));

    async function saveQuotation(status) {
        const id = document.getElementById('qt-id').value;
        const payload = {
            venueId: Number(document.getElementById('qt-venue').value),
            enquiryId: document.getElementById('qt-enq-id').value ? Number(document.getElementById('qt-enq-id').value) : null,
            customerName: document.getElementById('qt-name').value,
            customerEmail: document.getElementById('qt-email').value,
            customerMobile: document.getElementById('qt-mobile').value,
            eventDate: document.getElementById('qt-date').value,
            validUntil: document.getElementById('qt-valid').value || null,
            venueRentalPrice: Number(document.getElementById('qt-rental').value || 0),
            additionalCharges: Number(document.getElementById('qt-add').value || 0),
            decorationCharges: Number(document.getElementById('qt-decor').value || 0),
            cateringCharges: Number(document.getElementById('qt-cater').value || 0),
            discount: Number(document.getElementById('qt-disc').value || 0),
            taxAmount: Number(document.getElementById('qt-tax').value || 0),
            advanceAmount: Number(document.getElementById('qt-adv').value || 0),
            termsAndConditions: document.getElementById('qt-terms').value,
            status
        };
        try {
            await api(id ? `/api/vendor/quotations/${id}` : '/api/vendor/quotations', {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            notify(`Quotation saved (${status}).`);
            document.getElementById('quotation-form').reset();
            document.getElementById('qt-id').value = '';
            loadQuotations();
        } catch (err) { notify(err.message, 'error'); }
    }

    document.getElementById('quotation-form')?.addEventListener('submit', (e) => { e.preventDefault(); saveQuotation('DRAFT'); });
    document.getElementById('qt-send-btn')?.addEventListener('click', () => saveQuotation('SENT'));

    async function loadQuotations() {
        await populateVenueSelects();
        const rows = await api('/api/vendor/quotations');
        const tbody = document.getElementById('quotations-tbody');
        if (!rows.length) { tbody.innerHTML = '<tr><td colspan="7">No quotations created yet.</td></tr>'; return; }
        tbody.innerHTML = '';
        rows.forEach((q) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(q.quotationCode)}</strong></td>
                <td>${esc(q.customerName)}</td>
                <td>${esc(q.venueName)}</td>
                <td>${esc(q.eventDate)}</td>
                <td>₹${Number(q.totalAmount || 0).toLocaleString('en-IN')}</td>
                <td><span class="pill">${esc(q.status)}</span></td>
                <td class="btn-row">
                    <a href="${q.pdfUrl}" target="_blank" class="btn-sm btn-secondary">PDF</a>
                    <button type="button" class="btn-sm btn-primary q-acc">Customer Accepted</button>
                </td>
            `;
            tr.querySelector('.q-acc').addEventListener('click', async () => {
                await api(`/api/vendor/quotations/${q.id}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: 'ACCEPTED' }) });
                notify('Quotation accepted and pending booking created.');
                loadQuotations();
            });
            tbody.appendChild(tr);
        });
    }

    // ==================== 10. 360 TOUR & 3D VIEW ====================
    let drag360 = false, startX360 = 0, bgPosX = 0;
    const viewport = document.getElementById('tour360-viewport');
    viewport?.addEventListener('mousedown', (e) => { drag360 = true; startX360 = e.clientX; });
    window.addEventListener('mouseup', () => {
        drag360 = false;
        if (viewport) viewport.style.transform = 'perspective(1100px) rotateX(0deg) rotateY(0deg)';
    });
    viewport?.addEventListener('mousemove', (e) => {
        const rect = viewport.getBoundingClientRect();
        const nx = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
        const ny = ((e.clientY - rect.top) / rect.height - 0.5) * 2;
        if (drag360) {
            bgPosX += (e.clientX - startX360) * 1.6;
            startX360 = e.clientX;
            viewport.style.backgroundPosition = `${bgPosX}px center`;
        }
        viewport.style.transform = `perspective(1100px) rotateY(${(nx * 10).toFixed(2)}deg) rotateX(${(-ny * 7).toFixed(2)}deg) scale(1.02)`;
    });
    viewport?.addEventListener('mouseleave', () => {
        viewport.style.transform = 'perspective(1100px) rotateX(0deg) rotateY(0deg) scale(1)';
    });

    function openIn3DViewport(url, label) {
        const wrap = document.getElementById('tour360-viewer-wrap');
        wrap.hidden = false;
        viewport.style.backgroundImage = `url('${url}')`;
        viewport.style.backgroundSize = '135% 115%';
        viewport.style.backgroundPosition = 'center center';
        viewport.style.transition = 'transform 0.15s ease-out';
        bgPosX = 0;
        document.getElementById('tour360-badge').textContent = `${label} — Interactive 3D View (Move mouse or drag to rotate)`;
        wrap.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    async function load360Tours() {
        const venues = await populateVenueSelects();
        const [tours, sub] = await Promise.all([api('/api/vendor/tours-360'), api('/api/vendor/subscriptions')]);
        currentLimits = sub.limits;
        document.getElementById('tour360-gate-msg').textContent = currentLimits.allow360Tour
            ? '360° Venue Tour unlocked on your plan! Click any photo or tour below for 3D View.'
            : 'Upgrade to Premium (₹999) or Pro (₹2,499) to upload dedicated 360° files. You can still preview your uploaded Venue Photos in 3D below!';

        const list = document.getElementById('tour360-list');
        if (!tours.length) {
            list.innerHTML = '<p class="brand-subtitle">No dedicated 360° panoramic files uploaded yet.</p>';
        } else {
            list.innerHTML = '';
            tours.forEach((t) => {
                const card = document.createElement('div');
                card.className = 'media-card';
                card.innerHTML = `
                    <img src="${t.url}" alt="${esc(t.title)}" style="cursor:pointer;">
                    <div class="media-card-body">
                        <strong>${esc(t.title)}</strong>
                        <small>${esc(t.venueName)}</small>
                        <div class="btn-row">
                            <button type="button" class="btn-sm btn-primary t-prev">Launch 360° / 3D</button>
                            <button type="button" class="btn-sm btn-danger t-del">Delete</button>
                        </div>
                    </div>
                `;
                card.querySelector('img').addEventListener('click', () => openIn3DViewport(t.url, t.venueName));
                card.querySelector('.t-prev').addEventListener('click', () => openIn3DViewport(t.url, t.venueName));
                card.querySelector('.t-del').addEventListener('click', async () => {
                    await api(`/api/vendor/venues/media/${t.id}`, { method: 'DELETE' });
                    notify('360° tour deleted.');
                    load360Tours();
                });
                list.appendChild(card);
            });
        }

        // 3D Generated Gallery from uploaded Venue Photos
        const gal3d = document.getElementById('tour360-3d-gallery');
        if (!gal3d) return;
        const photos = [];
        venues.forEach((v) => {
            (v.media || []).filter((m) => m.mediaType === 'PHOTO').forEach((m) => photos.push({ ...m, venueName: v.venueName }));
        });
        if (!photos.length) {
            gal3d.innerHTML = '<p class="brand-subtitle">Upload photos in Venue Gallery to see them in 3D here.</p>';
            return;
        }
        gal3d.innerHTML = '';
        photos.forEach((ph) => {
            const card = document.createElement('div');
            card.className = 'media-card';
            card.style.cssText = 'cursor:pointer;transform-style:preserve-3d;transition:transform 0.16s ease, box-shadow 0.16s ease;box-shadow:0 14px 28px rgba(20,18,25,0.16);';
            card.innerHTML = `
                <div style="position:relative;overflow:hidden;">
                    <img src="${ph.url}" alt="${esc(ph.title)}" style="height:190px;width:100%;object-fit:cover;display:block;">
                    <span style="position:absolute;top:10px;right:10px;background:linear-gradient(135deg,#e11d48,#f59e0b);color:#fff;padding:0.2rem 0.6rem;border-radius:999px;font-size:0.72rem;font-weight:800;">3D VIEW</span>
                </div>
                <div class="media-card-body">
                    <strong>${esc(ph.title || ph.venueName)}</strong>
                    <small>${esc(ph.venueName)} — Click for 3D View</small>
                </div>
            `;
            card.addEventListener('mousemove', (e) => {
                const r = card.getBoundingClientRect();
                const rx = -(((e.clientY - r.top) / r.height) - 0.5) * 18;
                const ry = (((e.clientX - r.left) / r.width) - 0.5) * 18;
                card.style.transform = `perspective(800px) rotateX(${rx.toFixed(1)}deg) rotateY(${ry.toFixed(1)}deg) translateZ(10px)`;
            });
            card.addEventListener('mouseleave', () => {
                card.style.transform = 'perspective(800px) rotateX(0deg) rotateY(0deg) translateZ(0px)';
            });
            card.addEventListener('click', () => openIn3DViewport(ph.url, `${ph.venueName} — ${ph.title || 'Venue Photo'}`));
            gal3d.appendChild(card);
        });
    }

    document.getElementById('tour360-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const venueId = document.getElementById('t360-venue').value;
        const file = document.getElementById('t360-file').files[0];
        if (!venueId || !file) return notify('Select a venue and 360° image.', 'error');
        const fd = new FormData();
        fd.append('mediaType', 'TOUR_360');
        fd.append('title', document.getElementById('t360-title').value || '360° Tour');
        fd.append('file', file);
        try {
            await api(`/api/vendor/venues/${venueId}/media`, { method: 'POST', body: fd });
            notify('360° Tour uploaded.');
            load360Tours();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 11. MULTIPLE LOCATIONS ====================
    async function loadLocations() {
        await populateVenueSelects();
        const [locs, sub] = await Promise.all([api('/api/vendor/locations'), api('/api/vendor/subscriptions')]);
        currentLimits = sub.limits;
        document.getElementById('loc-gate-msg').textContent = currentLimits.allowMultipleLocations
            ? `Locations used: ${locs.length} / ${currentLimits.maxLocations} (${currentLimits.planName})`
            : 'Upgrade to Premium (₹999) or Pro (₹2,499) for Multiple Location Management.';
        const tbody = document.getElementById('locations-tbody');
        if (!locs.length) { tbody.innerHTML = '<tr><td colspan="5">No branch locations added yet.</td></tr>'; return; }
        tbody.innerHTML = '';
        locs.forEach((l) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(l.locationName)}</strong> (${esc(l.branchCode || '')})</td>
                <td>${esc(l.city)}, ${esc(l.state || '')}</td>
                <td>${esc(l.contactPerson || '-')}</td>
                <td>${l.linkedVenueId || '-'}</td>
                <td><button type="button" class="btn-sm btn-danger l-del">Delete</button></td>
            `;
            tr.querySelector('.l-del').addEventListener('click', async () => {
                await api(`/api/vendor/locations/${l.id}`, { method: 'DELETE' });
                notify('Location deleted.');
                loadLocations();
            });
            tbody.appendChild(tr);
        });
    }

    document.getElementById('location-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/vendor/locations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    locationName: document.getElementById('loc-name').value,
                    branchCode: document.getElementById('loc-code').value,
                    city: document.getElementById('loc-city').value,
                    state: document.getElementById('loc-state').value,
                    pincode: document.getElementById('loc-pincode').value,
                    linkedVenueId: document.getElementById('loc-venue').value || null,
                    contactPerson: document.getElementById('loc-person').value,
                    contactPhone: document.getElementById('loc-phone').value,
                    address: document.getElementById('loc-addr').value
                })
            });
            notify('Location saved.');
            document.getElementById('location-form').reset();
            loadLocations();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 12. ANALYTICS ====================
    async function loadAnalytics(range, from = '', to = '') {
        try {
            const qs = from && to ? `from=${from}&to=${to}` : `range=${range}`;
            const a = await api(`/api/vendor/analytics?${qs}`);
            document.getElementById('an-enq').textContent = a.totalEnquiries;
            document.getElementById('an-conf').textContent = a.confirmedBookings;
            document.getElementById('an-rej').textContent = a.rejectedBookings;
            document.getElementById('an-can').textContent = a.cancelledBookings;
            document.getElementById('an-conv').textContent = `${a.conversionRate}%`;
            document.getElementById('an-rev').textContent = `₹${Number(a.totalRevenue || 0).toLocaleString('en-IN')}`;
            document.getElementById('an-pop').textContent = a.mostPopularVenue || 'N/A';
            renderBarChart('an-trends-chart', a.monthlyTrends, 'month', 'revenue', '₹');
            renderBarChart('an-venue-chart', a.venuePerformance, 'venueName', 'bookings');
        } catch (err) { notify(err.message, 'error'); }
    }
    document.querySelectorAll('.an-range').forEach((b) => b.addEventListener('click', () => loadAnalytics(b.dataset.range)));
    document.getElementById('an-custom-btn')?.addEventListener('click', () => {
        loadAnalytics('custom', document.getElementById('an-from').value, document.getElementById('an-to').value);
    });

    // ==================== 14. TEAM MANAGEMENT ====================
    async function loadTeam() {
        const [members, sub] = await Promise.all([api('/api/vendor/team'), api('/api/vendor/subscriptions')]);
        currentLimits = sub.limits;
        document.getElementById('team-limit-label').textContent =
            `Team accounts used: ${members.length} / ${currentLimits.maxTeamAccounts} (${currentLimits.planName})`;
        const tbody = document.getElementById('team-tbody');
        if (!members.length) { tbody.innerHTML = '<tr><td colspan="6">No team members invited yet.</td></tr>'; return; }
        tbody.innerHTML = '';
        members.forEach((m) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${esc(m.fullName)}</strong></td>
                <td>${esc(m.email)}</td>
                <td>${esc(m.role)}</td>
                <td><small>${esc(m.permissions)}</small></td>
                <td><span class="pill">${m.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                <td class="btn-row">
                    <button type="button" class="btn-sm btn-secondary tm-tog">${m.active ? 'Disable' : 'Enable'}</button>
                    <button type="button" class="btn-sm btn-danger tm-del">Remove</button>
                </td>
            `;
            tr.querySelector('.tm-tog').addEventListener('click', async () => {
                await api(`/api/vendor/team/${m.id}/toggle`, { method: 'PATCH' });
                loadTeam();
            });
            tr.querySelector('.tm-del').addEventListener('click', async () => {
                await api(`/api/vendor/team/${m.id}`, { method: 'DELETE' });
                notify('Team member removed.');
                loadTeam();
            });
            tbody.appendChild(tr);
        });
    }

    document.getElementById('team-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/api/vendor/team', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    fullName: document.getElementById('tm-name').value,
                    email: document.getElementById('tm-email').value,
                    phone: document.getElementById('tm-phone').value,
                    role: document.getElementById('tm-role').value,
                    permissions: document.getElementById('tm-perms').value
                })
            });
            notify('Team member added.');
            document.getElementById('team-form').reset();
            loadTeam();
        } catch (err) { notify(err.message, 'error'); }
    });

    // ==================== 15. NOTIFICATIONS ====================
    async function loadNotifications() {
        const items = await api('/api/vendor/notifications');
        const el = document.getElementById('notifications-list');
        if (!items.length) { el.innerHTML = '<p class="brand-subtitle">No notifications yet.</p>'; return; }
        el.innerHTML = items.map((n) => `
            <div class="verification-item">
                <div><strong>${esc(n.title)}</strong><p style="margin:0.25rem 0;">${esc(n.message)}</p></div>
                <span class="pill">${n.read ? 'READ' : 'NEW'}</span>
            </div>
        `).join('');
    }

    // Enforce min=today on booking and quotation date pickers
    (() => {
        const now = new Date();
        const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        ['bk-date', 'qt-date', 'qt-valid'].forEach((id) => {
            const el = document.getElementById(id);
            if (el) el.min = today;
        });
    })();

    // Initial boot
    loadOverview();
    initFullPageBackgroundSlideshow();
})();
