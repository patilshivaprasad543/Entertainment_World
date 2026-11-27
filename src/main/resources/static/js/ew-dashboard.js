/**
 * Complete Entertainment World Recruiter Dashboard Controller.
 * Implements all video features: Verified Blue Tick Profile, Post Project + Online Audition Script,
 * Project History + Promote/Social Share, View Applications, Smart Search + Excel Export + Bulk Invite/Tag,
 * My Artists Folders, Recommended Platinum Artists, Get Package (3 Passes), and Transaction History.
 */
(() => {
  let state = {
    overview: null,
    activeSection: 'dashboard',
    selectedArtistIds: new Set(),
    appProjectId: null,
    appTab: 'ALL',
    appRole: 'ALL',
    appQuery: '',
    appSort: 'RELEVANCE',
    myFolderId: 0,
    recTier: 'PLATINUM',
    recProjectId: '',
    txViewType: 'CONTACT_VIEW',
    txTier: 'PAID',
    txSort: 'RECENT',
    showPurchaseHistory: false,
    cachedSearchArtists: [],
    cachedApplications: []
  };

  function closeModal(id) {
    document.getElementById(id).classList.remove('open');
  }
  window.closeModal = closeModal;

  function closeArtistModal() {
    const vid = document.querySelector('#am-body video');
    if (vid) vid.pause();
    closeModal('artist-modal');
  }
  window.closeArtistModal = closeArtistModal;

  async function refreshOverview() {
    try {
      const ov = await EW.api('/api/dashboard/overview');
      state.overview = ov;
      const c = ov.company;
      document.getElementById('hdr-studio-badge').innerHTML = `
        <span>${EW.esc(c.companyName)}</span>
        ${c.verifiedBadge ? '<span class="blue-tick" title="Verified by Entertainment World">✓</span>' : ''}`;
      document.getElementById('notif-badge').textContent = ov.unreadNotifications;
      const bp = document.getElementById('side-badge-projects');
      const ba = document.getElementById('side-badge-apps');
      const bau = document.getElementById('side-badge-auds');
      if (bp) bp.textContent = ov.activeProjectsCount ?? 0;
      if (ba) ba.textContent = ov.totalApplicationsCount ?? 0;
      if (bau) bau.textContent = ov.upcomingAuditionsCount ?? 0;
      renderNotificationsDropdown();
      return ov;
    } catch (err) {
      if (err.status === 401) window.location.href = '/login.html';
      else if (err.status === 403) window.location.href = '/company-status.html';
      else if (err.status === 402) window.location.href = '/passes.html';
      else EW.toast(err.message, 'error');
      throw err;
    }
  }
  window.refreshDashboardOverview = refreshOverview;

  function renderNotificationsDropdown() {
    const notifs = (state.overview && state.overview.notifications) || [];
    const panel = document.getElementById('notif-panel');
    panel.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <strong>Notifications</strong>
        <button type="button" onclick="markAllRead()" class="btn btn-outline-dark btn-sm" style="padding:3px 9px; font-size:0.72rem;">Mark read</button>
      </div>
      ${notifs.length ? notifs.map(n => `
        <div style="padding:9px; border-radius:10px; margin-bottom:6px; background:${n.readFlag ? '#FAF7F2' : '#FDF0F3'}; font-size:0.82rem;">
          <div style="font-weight:700;">${EW.esc(n.title)}</div>
          <div style="color:var(--text-body);">${EW.esc(n.message)}</div>
        </div>`).join('') : '<div style="color:var(--text-muted); font-size:0.84rem;">No notifications yet.</div>'}`;
  }

  window.toggleNotifications = () => {
    document.getElementById('notif-panel').classList.toggle('open');
  };

  window.markAllRead = async () => {
    await EW.api('/api/dashboard/notifications/read-all', { method: 'POST' });
    await refreshOverview();
  };

  function showDashLoading(label = 'Loading section...') {
    document.getElementById('dash-view').innerHTML = `
      <div class="card" style="padding:48px; text-align:center;">
        <div class="ew-spinner" style="margin:0 auto 12px;"></div>
        <div style="font-weight:700; color:var(--text-muted);">${EW.esc(label)}</div>
      </div>`;
  }

  function showDashError(title, err) {
    const msg = (err && err.message) ? err.message : 'Something went wrong while loading this section.';
    EW.toast(msg, 'error');
    document.getElementById('dash-view').innerHTML = `
      <div class="card" style="padding:36px; text-align:center;">
        <h3 style="margin-bottom:6px;">${EW.esc(title)}</h3>
        <p style="margin:0 auto 14px; max-width:460px; color:var(--text-muted); font-size:0.88rem;">${EW.esc(msg)}</p>
        <button type="button" class="btn btn-primary btn-sm" onclick="switchSection('${EW.esc(state.activeSection || 'dashboard')}')">Retry</button>
      </div>`;
  }

  window.switchSection = async (sec) => {
    state.activeSection = sec;
    state.selectedArtistIds.clear();
    document.querySelectorAll('.side-link').forEach(b => b.classList.toggle('active', b.dataset.sec === sec));
    try {
      if (sec === 'dashboard') await renderDashboardHome();
      else if (sec === 'post-project') renderPostProject();
      else if (sec === 'projects') await renderProjects();
      else if (sec === 'applications') await renderApplications();
      else if (sec === 'auditions') await EW_OPS.renderAuditions();
      else if (sec === 'selection') await EW_OPS.renderSelectionPipeline();
      else if (sec === 'my-artists') await renderMyArtists();
      else if (sec === 'search') await renderSearchArtists();
      else if (sec === 'recommended') await renderRecommended();
      else if (sec === 'messages') await EW_OPS.renderMessages();
      else if (sec === 'reports') await EW_OPS.renderReports();
      else if (sec === 'team') await EW_OPS.renderTeam();
      else if (sec === 'contracts') await EW_OPS.renderContractsAndExpenses();
      else if (sec === 'package') await renderPackagesSection();
      else if (sec === 'transactions') await renderTransactions();
    } catch (err) {
      showDashError('Unable to Load Section', err);
    }
  };

  // ================= 1. DASHBOARD OVERVIEW + KPIs + CHARTS =================
  async function renderDashboardHome() {
    const ov = await refreshOverview();
    const c = ov.company;
    let videos = [];
    let photos = [];
    try { videos = JSON.parse(c.showreelVideosJson || '[]'); } catch (_) {}
    try { photos = JSON.parse(c.portfolioPhotosJson || '[]'); } catch (_) {}

    const chartMonths = (ov.chartData && ov.chartData.monthlyActivity) || [];
    const maxVal = Math.max(1, ...chartMonths.map(m => Math.max(m.applications || 0, m.auditions || 0)));
    const statusMap = (ov.chartData && ov.chartData.applicationsByStatus) || {};
    const totalStatusApps = Math.max(1, ov.totalApplicationsCount || 1);
    const activities = ov.recentActivities || [];

    const docLinks = [
      ['GST Certificate', c.gstCertificatePath, c.gstNumber],
      ['Company Registration', c.companyRegistrationCertificatePath, null],
      ['PAN Document', c.panDocumentPath, c.panNumber],
      ['Business Address Proof', c.businessAddressProofPath, null],
      ['Authorization Letter', c.authorizationLetterPath, null]
    ];

    const root = document.getElementById('dash-view');
    root.innerHTML = `
      <div class="card" style="padding:12px 18px; margin-bottom:18px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; background:var(--gold-soft);">
        <div>
          <strong>🔔 Latest Update:</strong>
          ${ov.notifications.length ? EW.esc(ov.notifications[0].title + ' — ' + ov.notifications[0].message) : 'Welcome to your Entertainment World Casting Dashboard!'}
        </div>
        <span class="badge badge-paid">${EW.esc(c.activePassName)} · Expires ${EW.esc(c.subscriptionExpiry)}</span>
      </div>

      <div class="profile-hero">
        <div style="display:flex; gap:20px; align-items:flex-start; flex:1; min-width:280px;">
          <img src="${EW.esc(c.companyLogoPath)}" alt="Company Logo" class="profile-logo">
          <div>
            <div style="display:flex; align-items:center; gap:9px; flex-wrap:wrap;">
              <h1 style="font-size:1.7rem;">${EW.esc(c.companyName)}</h1>
              ${c.verifiedBadge ? '<span class="blue-tick" title="Verified Blue Tick — Authenticated by Entertainment World Super Admin">✓</span><span class="badge badge-approved">Verified Casting Studio</span>' : ''}
            </div>
            <div style="color:var(--text-muted); font-size:0.9rem; margin:4px 0 10px;">
              ${EW.esc(c.organizationType || 'Casting Director')} · 📍 ${EW.esc(c.city)}, ${EW.esc(c.state)}
            </div>
            <div style="display:flex; gap:18px; flex-wrap:wrap; font-size:0.86rem;">
              <div><strong class="mono">${c.followersCount}</strong> Followers</div>
              <div><strong class="mono" style="color:var(--crimson);">${ov.activeProjectsCount} Active projects</strong></div>
              <div><strong class="mono">${ov.profilesViewedCount}</strong> Profiles viewed</div>
              <div>📞 <strong>${EW.esc(c.contactNumber)}</strong></div>
              <div>✉️ <strong>${EW.esc(c.companyEmail)}</strong></div>
            </div>
          </div>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:flex-start;">
          <button type="button" onclick="openEditProfileModal()" class="btn btn-outline-dark btn-sm">✏️ Edit Profile</button>
          <button type="button" onclick="EW_OPS.openScheduleAuditionModal()" class="btn btn-dark btn-sm">+ Schedule Audition</button>
          <button type="button" onclick="switchSection('post-project')" class="btn btn-primary btn-sm">+ Post Casting Call</button>
        </div>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('search')">
          <div class="kpi-label">Registered Actors</div>
          <div class="kpi-val mono">${ov.totalActorsCount ?? 0}</div>
          <div class="kpi-sub">${ov.verifiedActorsCount ?? 0} verified profiles</div>
        </div>
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('projects')">
          <div class="kpi-label">Active Projects</div>
          <div class="kpi-val mono" style="color:var(--crimson);">${ov.activeProjectsCount ?? 0}</div>
          <div class="kpi-sub">of ${ov.totalProjectsCount ?? ov.activeProjectsCount ?? 0} total projects</div>
        </div>
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('applications')">
          <div class="kpi-label">Total Applications</div>
          <div class="kpi-val mono">${ov.totalApplicationsCount ?? 0}</div>
          <div class="kpi-sub">Click to review applicants</div>
        </div>
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('auditions')">
          <div class="kpi-label">Upcoming Auditions</div>
          <div class="kpi-val mono" style="color:#B45309;">${ov.upcomingAuditionsCount ?? 0}</div>
          <div class="kpi-sub">Online &amp; Studio slots</div>
        </div>
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('selection')">
          <div class="kpi-label">Shortlisted Actors</div>
          <div class="kpi-val mono">${ov.shortlistedCount ?? 0}</div>
          <div class="kpi-sub">${ov.favoriteActorsCount ?? 0} saved to folders</div>
        </div>
        <div class="kpi-card" style="cursor:pointer;" onclick="switchSection('selection')">
          <div class="kpi-label">Selected Actors</div>
          <div class="kpi-val mono" style="color:#0D7F44;">${ov.selectedCount ?? 0}</div>
          <div class="kpi-sub">${ov.activeContractsCount ?? 0} contracts · ${ov.teamMembersCount ?? 0} team</div>
        </div>
      </div>

      <div style="display:grid; grid-template-columns:1.1fr 0.9fr 1fr; gap:18px; margin-bottom:20px;">
        <div class="card" style="padding:20px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
            <h3 style="font-size:1.02rem;">6-Month Casting Activity</h3>
            <span style="font-size:0.74rem; color:var(--text-muted); font-weight:700;">Apps vs Auditions</span>
          </div>
          <div style="display:grid; grid-template-columns:repeat(${chartMonths.length || 6}, 1fr); gap:8px; align-items:end; height:150px;">
            ${chartMonths.map(m => {
              const appH = Math.max(8, Math.round(((m.applications || 0) / maxVal) * 105));
              const audH = Math.max(8, Math.round(((m.auditions || 0) / maxVal) * 105));
              return `
                <div style="text-align:center;">
                  <div style="display:flex; gap:3px; justify-content:center; align-items:flex-end; height:112px;">
                    <div title="${m.applications} Applications" style="width:14px; height:${appH}px; background:var(--crimson); border-radius:4px 4px 0 0;"></div>
                    <div title="${m.auditions} Auditions" style="width:14px; height:${audH}px; background:var(--bg-obsidian); border-radius:4px 4px 0 0;"></div>
                  </div>
                  <div style="font-size:0.7rem; font-weight:700; margin-top:4px;">${EW.esc(m.label)}</div>
                </div>`;
            }).join('')}
          </div>
        </div>

        <div class="card" style="padding:20px;">
          <h3 style="font-size:1.02rem; margin-bottom:10px;">Pipeline Funnel</h3>
          <div style="display:flex; flex-direction:column; gap:8px;">
            ${['APPLIED', 'SHORTLISTED', 'INVITED', 'SELECTED', 'REJECTED'].map(k => {
              const cnt = statusMap[k] || 0;
              const pct = Math.round((cnt / totalStatusApps) * 100);
              return `
                <div>
                  <div style="display:flex; justify-content:space-between; font-size:0.78rem; font-weight:700;">
                    <span>${k}</span><span class="mono">${cnt}</span>
                  </div>
                  <div style="height:7px; background:#EAE4D8; border-radius:999px; overflow:hidden; margin-top:2px;">
                    <div style="height:100%; width:${pct}%; background:${k === 'SELECTED' ? '#0D7F44' : k === 'REJECTED' ? '#9B1C1C' : 'var(--crimson)'};"></div>
                  </div>
                </div>`;
            }).join('')}
          </div>
        </div>

        <div class="card" style="padding:20px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
            <h3 style="font-size:1.02rem;">Recent Activities</h3>
            <button type="button" onclick="switchSection('reports')" class="btn btn-outline-dark btn-sm" style="padding:3px 9px; font-size:0.72rem;">Full Report</button>
          </div>
          <div style="display:flex; flex-direction:column; gap:8px; max-height:175px; overflow-y:auto;">
            ${activities.map(act => `
              <div style="padding:8px 10px; border-radius:10px; background:var(--bg-ivory); font-size:0.8rem;">
                <div style="font-weight:700; color:var(--bg-obsidian);">${EW.esc(act.title)}</div>
                <div style="color:var(--text-muted); font-size:0.74rem;">${EW.esc(act.subtitle)}</div>
              </div>`).join('') || '<p style="color:var(--text-muted); font-size:0.84rem;">No recent activity recorded.</p>'}
          </div>
        </div>
      </div>

      <div class="card" style="padding:22px; margin-bottom:20px;">
        <h3 style="margin-bottom:8px;">Company Profile</h3>
        <p style="margin:0; color:var(--text-body);">${EW.esc(c.companyDescription)}</p>
        <div style="margin-top:12px; display:flex; gap:10px; flex-wrap:wrap; font-size:0.84rem; color:var(--text-muted);">
          <span><strong>Representative:</strong> ${EW.esc(c.representativeName)}</span>
          ${c.website ? `<span>· <strong>Website:</strong> <a href="${EW.esc(c.website)}" target="_blank" style="color:var(--crimson);">${EW.esc(c.website)}</a></span>` : ''}
          <span>· <strong>Looking for:</strong> ${EW.esc(c.talentSought || 'Actors, Models, Voice-over Artists')}</span>
        </div>
      </div>

      <div class="card" style="padding:20px; margin-bottom:20px;">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:12px;">
          <div>
            <h3 style="font-size:1.05rem;">Studio KYC &amp; Document Verification</h3>
            <div style="font-size:0.8rem; color:var(--text-muted);">Verified by ${EW.esc(c.verifiedBy || 'Super Admin')} ${c.verificationDate ? 'on ' + EW.esc(c.verificationDate) : ''}</div>
          </div>
          <div style="display:flex; gap:6px; flex-wrap:wrap;">
            <span class="badge ${EW.badgeClass(c.documentVerification)}">Docs: ${EW.esc(c.documentVerification || 'Pending')}</span>
            <span class="badge ${EW.badgeClass(c.emailVerification)}">Email: ${EW.esc(c.emailVerification || 'Not verified')}</span>
            <span class="badge ${EW.badgeClass(c.mobileVerification)}">Mobile: ${EW.esc(c.mobileVerification || 'Not verified')}</span>
          </div>
        </div>
        <div style="display:grid; grid-template-columns:repeat(5, 1fr); gap:10px;">
          ${docLinks.map(([lbl, path, num]) => `
            <div style="padding:10px 12px; border:1px solid var(--border-light); border-radius:12px; background:var(--bg-ivory); font-size:0.8rem;">
              <div style="font-weight:700; margin-bottom:4px;">${lbl}</div>
              ${num ? `<div class="mono" style="font-size:0.74rem; color:var(--text-muted); margin-bottom:4px;">${EW.esc(num)}</div>` : ''}
              ${path ? `<a href="${EW.esc(path)}" target="_blank" style="color:var(--crimson); font-weight:700; font-size:0.78rem;">View Document ↗</a>` : '<span style="color:var(--text-muted); font-size:0.76rem;">On file / Not uploaded</span>'}
            </div>`).join('')}
        </div>
      </div>

      <div style="display:grid; grid-template-columns: 1.5fr 1fr; gap:20px;">
        <div>
          <div class="card" style="padding:22px; margin-bottom:20px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
              <h3>Videos &amp; Showreels</h3>
              <span class="badge badge-neutral">${videos.length} Videos</span>
            </div>
            <div style="display:grid; grid-template-columns:repeat(2, 1fr); gap:12px;">
              ${videos.map(v => `
                <div style="border-radius:14px; overflow:hidden; border:1px solid var(--border-light); cursor:pointer;" onclick="playVideoModal('${EW.esc(v.url)}', '${EW.esc(v.title)}')">
                  <div style="position:relative; height:128px; background:#14131B;">
                    <img src="${EW.esc(v.thumb || '/assets/images/welcome-poster.jpg')}" style="width:100%; height:100%; object-fit:cover; opacity:0.82;">
                    <div style="position:absolute; inset:0; display:grid; place-items:center;"><span style="width:42px; height:42px; border-radius:50%; background:var(--crimson); color:#FFF; display:grid; place-items:center;">▶</span></div>
                  </div>
                  <div style="padding:8px 10px; font-weight:700; font-size:0.84rem;">${EW.esc(v.title)}</div>
                </div>`).join('')}
            </div>
          </div>

          <div class="card" style="padding:22px;">
            <h3 style="margin-bottom:14px;">Studio Photos</h3>
            <div style="display:grid; grid-template-columns:repeat(4, 1fr); gap:10px;">
              ${photos.map(ph => `<img src="${EW.esc(ph)}" style="width:100%; aspect-ratio:1/1; object-fit:cover; border-radius:12px;">`).join('')}
            </div>
          </div>
        </div>

        <div class="card" style="padding:22px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <h3>Posted Projects</h3>
            <button type="button" onclick="switchSection('projects')" class="btn btn-outline-dark btn-sm">View All</button>
          </div>
          ${ov.recentProjects.map(p => `
            <div style="padding:12px; border:1px solid var(--border-light); border-radius:12px; margin-bottom:10px;">
              <div style="display:flex; justify-content:space-between; gap:6px; align-items:center;">
                <div style="font-weight:800; font-size:0.92rem;">${EW.esc(p.projectTitle)}</div>
                <span class="badge ${EW_OPS.opBadge(p.status)}" style="font-size:0.65rem;">${EW.esc(p.status)}</span>
              </div>
              <div style="font-size:0.78rem; color:var(--text-muted); margin:3px 0 8px;">Posted ${EW.esc(p.postedDate)} · ${p.applicationsCount} Applications</div>
              <div style="display:flex; gap:8px;">
                <button type="button" onclick="openProjectApplications(${p.id})" class="btn btn-dark btn-sm" style="padding:5px 10px; font-size:0.74rem;">View Apps</button>
                <span class="badge badge-neutral">${EW.esc(p.targetCategory)}</span>
              </div>
            </div>`).join('') || '<p style="color:var(--text-muted);">No projects posted yet.</p>'}
        </div>
      </div>`;
  }

  window.playVideoModal = (url, title) => {
    document.getElementById('am-title').textContent = title || 'Video Preview';
    document.getElementById('am-body').innerHTML = `<video controls autoplay style="width:100%; border-radius:14px; background:#000;" src="${EW.esc(url)}"></video>`;
    document.getElementById('artist-modal').classList.add('open');
  };

  window.openEditProfileModal = () => {
    const c = state.overview.company;
    document.getElementById('ep-rep').value = c.representativeName || '';
    document.getElementById('ep-phone').value = c.contactNumber || '';
    document.getElementById('ep-city').value = c.city || '';
    document.getElementById('ep-web').value = c.website || '';
    document.getElementById('ep-looking').value = c.talentSought || '';
    document.getElementById('ep-desc').value = c.companyDescription || '';
    document.getElementById('edit-profile-modal').classList.add('open');
  };

  document.getElementById('edit-profile-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      await EW.api('/api/dashboard/profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          representativeName: document.getElementById('ep-rep').value,
          contactNumber: document.getElementById('ep-phone').value,
          city: document.getElementById('ep-city').value,
          website: document.getElementById('ep-web').value,
          talentSought: document.getElementById('ep-looking').value,
          companyDescription: document.getElementById('ep-desc').value
        })
      });
      closeModal('edit-profile-modal');
      EW.toast('Recruiter profile updated!');
      renderDashboardHome();
    } catch (err) { EW.toast(err.message, 'error'); }
  });

  // ================= 2. POST PROJECT (8-SECTION CASTING BRIEF + MEDIA) =================
  function renderPostProject() {
    const c = state.overview.company;
    const secHead = (num, title, subtitle) => `
      <div id="pp-sec-${num}" style="grid-column:span 2; margin-top:14px; padding:10px 14px; background:var(--bg-ivory); border-left:4px solid var(--crimson); border-radius:10px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
        <strong style="font-size:1rem; color:var(--bg-obsidian);">${num}. ${title}</strong>
        <span style="font-size:0.76rem; color:var(--text-muted); font-weight:600;">${subtitle}</span>
      </div>`;

    document.getElementById('dash-view').innerHTML = `
      <div style="display:grid; grid-template-columns:1.65fr 0.75fr; gap:22px; align-items:start;">
        <div class="card" style="padding:28px;">
          <div style="display:flex; gap:10px; flex-wrap:wrap; margin-bottom:18px; padding-bottom:14px; border-bottom:1px solid var(--border-light); font-size:0.78rem; font-weight:700;">
            ${[
              [1, '1. Basic Info'], [2, '2. Talent'], [3, '3. Location & Schedule'], [4, '4. Payment'],
              [5, '5. Audition'], [6, '6. Application'], [7, '7. Contact'], [8, '8. Media']
            ].map(([n, lbl]) => `<a href="#pp-sec-${n}" class="badge badge-neutral" style="text-decoration:none;">${lbl}</a>`).join('')}
          </div>
          <h2 style="margin-bottom:6px;">Add Your Project Details</h2>
          <p style="margin:0 0 18px; color:var(--text-muted); font-size:0.88rem;">Complete all casting post sections below to publish a verified brief with media and script attachments.</p>

          <form id="post-project-form" enctype="multipart/form-data">
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:14px;">

              ${secHead(1, 'Basic Project Information', 'Required')}
              <div style="grid-column:span 2;">
                <label class="form-label">Project title <span class="req">*</span></label>
                <input class="form-control" name="projectTitle" required placeholder="e.g. Royal Horizon Feature Film">
              </div>
              <div>
                <label class="form-label">Project type <span class="req">*</span></label>
                <select class="form-select" name="projectType" required>
                  <option>Movie</option>
                  <option>Short Film</option>
                  <option>Web Series</option>
                  <option>TV Serial</option>
                  <option>Advertisement</option>
                  <option>Music Video</option>
                  <option>Photoshoot</option>
                  <option>Casting Call</option>
                  <option>Audition</option>
                  <option>Event</option>
                  <option>Freelance Project</option>
                </select>
              </div>
              <div>
                <label class="form-label">Project language <span class="req">*</span></label>
                <select class="form-select" name="projectLanguage" required>
                  <option>Kannada</option>
                  <option>Tamil</option>
                  <option>Telugu</option>
                  <option>Hindi</option>
                  <option>English</option>
                  <option>Malayalam</option>
                  <option>Marathi</option>
                  <option>Bengali</option>
                  <option>Punjabi</option>
                  <option>Multi-lingual</option>
                </select>
              </div>
              <div>
                <label class="form-label">Production house / Banner name <span class="req">*</span></label>
                <input class="form-control" name="productionHouseName" required placeholder="e.g. Starlight Pictures Banner">
              </div>
              <div>
                <label class="form-label">Casting company name <span class="req">*</span></label>
                <input class="form-control" name="castingCompanyName" required value="${EW.esc(c.companyName)}">
              </div>
              <div>
                <label class="form-label">Project status <span class="req">*</span></label>
                <select class="form-select" name="projectStage" required>
                  <option>Pre-production</option>
                  <option selected>Casting Open</option>
                  <option>Shooting Started</option>
                </select>
              </div>
              <div>
                <label class="form-label">Tags <span class="opt">(Optional keywords)</span></label>
                <input class="form-control" name="tags" placeholder="e.g. ott, lead, drama, bengaluru">
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Project description <span class="req">*</span></label>
                <textarea class="form-textarea" name="projectDetails" required placeholder="Describe story premise, production scale, shooting schedule, and key highlights..."></textarea>
              </div>

              ${secHead(2, 'Talent Requirements', 'Role & Artist Criteria')}
              <div>
                <label class="form-label">Required talent <span class="req">*</span></label>
                <select class="form-select" name="targetCategory" required>
                  <option>Actor</option>
                  <option>Actress</option>
                  <option>Model</option>
                  <option>Dancer</option>
                  <option>Singer</option>
                  <option>Child Artist</option>
                  <option>Supporting Artist</option>
                  <option>Voice-over Artist</option>
                  <option>Stand-up Comedian</option>
                  <option>Musician</option>
                  <option>Writer</option>
                  <option>Photographer</option>
                  <option>Filmmaker</option>
                </select>
              </div>
              <div>
                <label class="form-label">Gender <span class="req">*</span></label>
                <select class="form-select" name="targetGender" required>
                  <option>Any</option>
                  <option>Male</option>
                  <option>Female</option>
                </select>
              </div>
              <div>
                <label class="form-label">Age range (Min – Max) <span class="req">*</span></label>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                  <input type="number" class="form-control" name="ageMin" min="1" max="90" value="18" required placeholder="Min (e.g. 18)">
                  <input type="number" class="form-control" name="ageMax" min="1" max="95" value="25" required placeholder="Max (e.g. 25)">
                </div>
              </div>
              <div>
                <label class="form-label">Height <span class="opt">(Optional)</span></label>
                <input class="form-control" name="heightRequirement" placeholder="e.g. 5'6&quot; - 6'0&quot;">
              </div>
              <div>
                <label class="form-label">Preferred appearance <span class="req">*</span></label>
                <input class="form-control" name="preferredAppearance" required placeholder="e.g. Urban athletic, expressive screen presence">
              </div>
              <div>
                <label class="form-label">Experience <span class="req">*</span></label>
                <select class="form-select" name="experienceLevel" required>
                  <option>Fresher</option>
                  <option>Experienced</option>
                  <option selected>Both</option>
                </select>
              </div>
              <div>
                <label class="form-label">Number of artists required <span class="req">*</span></label>
                <input type="number" class="form-control" name="artistsRequiredCount" min="1" value="1" required>
              </div>
              <div>
                <label class="form-label">Character name <span class="opt">(Optional)</span></label>
                <input class="form-control" name="characterName" placeholder="e.g. Arjun / Kavya">
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Role description <span class="req">*</span></label>
                <textarea class="form-textarea" name="roleDescription" required placeholder="Describe character personality, dialogue skills, stunts, dance or dialect needs..."></textarea>
              </div>

              ${secHead(3, 'Project Location and Schedule', 'Dates & Availability')}
              <div>
                <label class="form-label">Shooting location <span class="req">*</span></label>
                <input class="form-control" name="location" required value="${EW.esc(c.city)}">
              </div>
              <div>
                <label class="form-label">Audition location <span class="req">*</span></label>
                <input class="form-control" name="auditionLocation" required placeholder="e.g. Online / Studio Hub, Indiranagar">
              </div>
              <div>
                <label class="form-label">Audition date <span class="req">*</span></label>
                <input type="date" class="form-control" name="auditionDate" required>
              </div>
              <div>
                <label class="form-label">Audition type <span class="req">*</span></label>
                <select class="form-select" name="auditionType" required>
                  <option>Online</option>
                  <option>Offline</option>
                </select>
              </div>
              <div>
                <label class="form-label">Shooting start date <span class="req">*</span></label>
                <input type="date" class="form-control" name="shootingStartDate" required>
              </div>
              <div>
                <label class="form-label">Shooting end date <span class="opt">(Optional)</span></label>
                <input type="date" class="form-control" name="shootingEndDate">
              </div>
              <div>
                <label class="form-label">Shooting duration <span class="req">*</span></label>
                <input class="form-control" name="shootingDuration" required placeholder="e.g. 20 Days / 2 Months">
              </div>
              <div>
                <label class="form-label">Schedule type</label>
                <select class="form-select" name="durationType">
                  <option>Full-Time</option>
                  <option>Part-Time</option>
                  <option>Date-specific</option>
                </select>
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Availability requirements <span class="req">*</span></label>
                <input class="form-control" name="availabilityRequirements" required placeholder="e.g. Full-day shoot availability including weekends & outdoor travel">
              </div>

              ${secHead(4, 'Payment Details', 'Compensation & Logistics')}
              <div>
                <label class="form-label">Payment type <span class="req">*</span></label>
                <select class="form-select" name="paymentType" required>
                  <option>Paid</option>
                  <option>Unpaid</option>
                </select>
              </div>
              <div>
                <label class="form-label">Salary / Compensation <span class="req">*</span></label>
                <input class="form-control" name="budget" required placeholder="e.g. Rs. 15,000 / Rs. 1,50,000">
              </div>
              <div>
                <label class="form-label">Payment basis <span class="req">*</span></label>
                <select class="form-select" name="paymentBasis" required>
                  <option>Per day</option>
                  <option>Per project</option>
                  <option>Per episode</option>
                </select>
              </div>
              <div>
                <label class="form-label">Accommodation provided <span class="req">*</span></label>
                <select class="form-select" name="accommodationProvided" required>
                  <option>Yes</option>
                  <option>No</option>
                </select>
              </div>
              <div>
                <label class="form-label">Food provided <span class="req">*</span></label>
                <select class="form-select" name="foodProvided" required>
                  <option>Yes</option>
                  <option>No</option>
                </select>
              </div>
              <div>
                <label class="form-label">Travel expenses covered <span class="req">*</span></label>
                <select class="form-select" name="travelExpensesCovered" required>
                  <option>Yes</option>
                  <option>No</option>
                </select>
              </div>

              ${secHead(5, 'Audition Details', 'Process & Instructions')}
              <div style="grid-column:span 2;">
                <label class="form-label">Audition process <span class="req">*</span></label>
                <input class="form-control" name="auditionProcess" required placeholder="e.g. Round 1: Self-tape screening · Round 2: Director look test">
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Audition instructions <span class="req">*</span></label>
                <textarea class="form-textarea" name="auditionInstructions" required placeholder="Framing guidelines, wardrobe suggestions, lighting & audio clarity instructions..."></textarea>
              </div>
              <div>
                <label class="form-label">Self-introduction video required <span class="req">*</span></label>
                <select class="form-select" name="selfIntroVideoRequired" required>
                  <option>Yes</option>
                  <option>No</option>
                </select>
              </div>
              <div>
                <label class="form-label">Portfolio required <span class="req">*</span></label>
                <select class="form-select" name="portfolioRequired" required>
                  <option>Yes</option>
                  <option>No</option>
                </select>
              </div>
              <div>
                <label class="form-label">Last date to apply <span class="req">*</span></label>
                <input type="date" class="form-control" name="lastDateToApply" required>
              </div>
              <div style="display:flex; align-items:flex-end; padding-bottom:10px;">
                <label style="display:flex; align-items:center; gap:8px; font-weight:700; font-size:0.86rem;">
                  <input type="checkbox" name="onlineAuditionEnabled" value="true" checked>
                  Accept Online Video Auditions
                </label>
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Audition script / Dialogue <span class="opt">(Optional)</span></label>
                <textarea class="form-textarea" name="auditionScript" placeholder="Paste audition script lines or dialogue prompt for self-tape submissions..."></textarea>
              </div>

              ${secHead(6, 'Application Requirements', 'What Artists Must Submit')}
              <div>
                <label class="form-label">Required photographs <span class="req">*</span></label>
                <input class="form-control" name="requiredPhotographs" required placeholder="e.g. 2 Headshots + 1 Full-length unedited photo">
              </div>
              <div>
                <label class="form-label">Introduction video <span class="req">*</span></label>
                <input class="form-control" name="introVideoRequirement" required placeholder="e.g. 30-45 sec video stating name, age, city & height">
              </div>
              <div>
                <label class="form-label">Acting portfolio <span class="req">*</span></label>
                <input class="form-control" name="actingPortfolioRequirement" required placeholder="e.g. PDF portfolio or performance reel">
              </div>
              <div>
                <label class="form-label">Previous work links <span class="req">*</span></label>
                <input class="form-control" name="previousWorkLinksReq" required placeholder="e.g. YouTube / IMDb / Google Drive showreel link">
              </div>
              <div>
                <label class="form-label">Contact information <span class="req">*</span></label>
                <input class="form-control" name="applicantContactReq" required placeholder="e.g. Phone number, WhatsApp & Email">
              </div>
              <div>
                <label class="form-label">Additional documents <span class="opt">(Optional)</span></label>
                <input class="form-control" name="additionalDocumentsReq" placeholder="e.g. Passport / Parent NOC for minors">
              </div>

              ${secHead(7, 'Contact Information', 'Casting Desk Details')}
              <div>
                <label class="form-label">Casting director name <span class="req">*</span></label>
                <input class="form-control" name="castingDirectorName" required value="${EW.esc(c.representativeName)}">
              </div>
              <div>
                <label class="form-label">Official email <span class="req">*</span></label>
                <input type="email" class="form-control" name="contactEmail" required value="${EW.esc(c.companyEmail)}">
              </div>
              <div>
                <label class="form-label">Contact number <span class="req">*</span></label>
                <input type="tel" class="form-control" name="contactNumber" required value="${EW.esc(c.contactNumber)}">
              </div>
              <div>
                <label class="form-label">WhatsApp number <span class="req">*</span></label>
                <input type="tel" class="form-control" name="whatsappNumber" required value="${EW.esc(c.contactNumber)}">
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Website / Social media links <span class="opt">(Optional)</span></label>
                <input class="form-control" name="websiteOrSocialLinks" value="${EW.esc(c.website || '')}" placeholder="https://yourstudio.in · Instagram / LinkedIn">
              </div>

              ${secHead(8, 'Media and Attachments', 'Posters, Looks & Script PDF')}
              <div>
                <label class="form-label">Project poster</label>
                <input type="file" class="form-control" name="projectPoster" accept="image/*">
              </div>
              <div>
                <label class="form-label">Project promotional image</label>
                <input type="file" class="form-control" name="projectPromotionalImage" accept="image/*">
              </div>
              <div>
                <label class="form-label">Reference images <span class="opt">(Multiple allowed)</span></label>
                <input type="file" class="form-control" name="referenceImages" accept="image/*" multiple>
              </div>
              <div>
                <label class="form-label">Character look references <span class="opt">(Multiple allowed)</span></label>
                <input type="file" class="form-control" name="characterLookReferences" accept="image/*" multiple>
              </div>
              <div style="grid-column:span 2;">
                <label class="form-label">Audition script PDF <span class="opt">(PDF or DOC)</span></label>
                <input type="file" class="form-control" name="auditionScriptPdf" accept=".pdf,.doc,.docx,image/*">
              </div>
            </div>

            <div style="display:flex; justify-content:flex-end; margin-top:22px;">
              <button type="submit" id="publish-project-btn" class="btn btn-primary" style="padding:13px 28px;">Publish Casting Call →</button>
            </div>
          </form>
        </div>

        <div class="card" style="padding:22px; position:sticky; top:90px;">
          <h3 style="margin-bottom:12px;">Posted Projects</h3>
          ${(state.overview.recentProjects || []).map(p => `
            <div style="padding:12px; border:1px solid var(--border-light); border-radius:12px; margin-bottom:10px; cursor:pointer;" onclick="openProjectBriefModal(${p.id})">
              <div style="font-weight:700;">${EW.esc(p.projectTitle)}</div>
              <div style="font-size:0.78rem; color:var(--text-muted);">${EW.esc(p.projectType)} · ${EW.esc(p.projectLanguage || '')} · ${p.applicationsCount} apps</div>
            </div>`).join('')}
        </div>
      </div>`;

    document.getElementById('post-project-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      const btn = document.getElementById('publish-project-btn');
      btn.disabled = true;
      btn.textContent = 'Publishing Casting Call...';
      try {
        const fd = new FormData(ev.target);
        if (!fd.has('onlineAuditionEnabled')) {
          fd.set('onlineAuditionEnabled', 'false');
        }
        await EW.api('/api/dashboard/projects', {
          method: 'POST',
          body: fd
        });
        EW.toast('Project published with full casting brief & attachments!');
        await refreshOverview();
        switchSection('projects');
      } catch (err) {
        EW.toast(err.message, 'error');
        btn.disabled = false;
        btn.textContent = 'Publish Casting Call →';
      }
    });
  }

  // ================= 3. PROJECTS / PROJECT HISTORY =================
  let cachedProjects = [];

  async function renderProjects(filter = 'ALL') {
    showDashLoading('Loading casting projects...');
    try {
      const projects = await EW.api(`/api/dashboard/projects?filter=${encodeURIComponent(filter)}`);
      cachedProjects = projects;
    document.getElementById('dash-view').innerHTML = `
      <div class="dash-page-head">
        <div>
          <h2>Casting Project Management</h2>
          <p class="dash-sub">Edit briefs, publish/pause/close casting calls, promote reach, or review incoming applications.</p>
        </div>
        <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
          <label style="font-size:0.84rem; font-weight:700;">Status:</label>
          <select class="form-select" style="width:160px;" onchange="renderProjectsFilter(this.value)">
            <option value="ALL" ${filter === 'ALL' ? 'selected' : ''}>All Projects</option>
            <option value="ACTIVE" ${filter === 'ACTIVE' ? 'selected' : ''}>Active (Published)</option>
            <option value="PAUSED" ${filter === 'PAUSED' ? 'selected' : ''}>Paused</option>
            <option value="CLOSED" ${filter === 'CLOSED' ? 'selected' : ''}>Closed</option>
            <option value="PROMOTED" ${filter === 'PROMOTED' ? 'selected' : ''}>Promoted</option>
          </select>
          <button type="button" onclick="switchSection('post-project')" class="btn btn-primary btn-sm">+ Post Casting Call</button>
        </div>
      </div>
      ${projects.map(p => `
        <div class="card" style="padding:22px; margin-bottom:16px;">
          <div style="display:grid; grid-template-columns:1.7fr 0.85fr; gap:18px;">
            <div>
              <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-bottom:8px;">
                <span class="badge ${EW_OPS.opBadge(p.status)}">${EW.esc(p.status)}</span>
                <span class="badge badge-neutral">${EW.esc(p.projectType)}</span>
                <span class="badge badge-neutral">${EW.esc(p.targetCategory)}</span>
                ${p.projectLanguage ? `<span class="badge badge-neutral">${EW.esc(p.projectLanguage)}</span>` : ''}
                ${p.projectStage ? `<span class="badge badge-pending">${EW.esc(p.projectStage)}</span>` : ''}
                ${p.promoted ? '<span class="badge badge-showcase">★ PROMOTED</span>' : ''}
                ${p.onlineAuditionEnabled ? '<span class="badge badge-approved">Online Auditions</span>' : ''}
              </div>
              <h3 style="font-size:1.3rem; margin-bottom:6px;">${EW.esc(p.projectTitle)}</h3>
              <div style="font-size:0.8rem; color:var(--text-muted); margin-bottom:10px;">
                Posted: ${EW.esc(p.postedDate)} &nbsp;|&nbsp; Apply By: ${EW.esc(p.lastDateToApply || p.expiryDate)} &nbsp;|&nbsp; Shoot: ${EW.esc(p.shootingStartDate || 'TBD')}
              </div>
              <p style="font-size:0.9rem; color:var(--text-body); margin-bottom:10px;">${EW.esc(p.projectDetails)}</p>
              <div style="font-size:0.82rem; color:var(--text-muted);">
                Banner: <strong style="color:var(--text-dark);">${EW.esc(p.productionHouseName || p.companyName)}</strong>
                · Casting: <strong style="color:var(--text-dark);">${EW.esc(p.castingCompanyName || p.companyName)}</strong>
                ${p.companyVerified ? '<span class="blue-tick" style="width:15px; height:15px; font-size:9px; vertical-align:middle;">✓</span>' : ''}
              </div>
            </div>
            <div style="border-left:1px solid var(--border-light); padding-left:18px; font-size:0.86rem;">
              ${p.projectPosterPath ? `<img src="${EW.esc(p.projectPosterPath)}" style="width:100%; height:94px; object-fit:cover; border-radius:10px; margin-bottom:8px;">` : ''}
              <strong>${EW.esc(p.targetCategory)} (${p.artistsRequiredCount || 1} needed)</strong>
              <div style="color:var(--text-muted);">${EW.esc(p.targetGender)}, ${p.ageMin} - ${p.ageMax} Yrs · ${EW.esc(p.experienceLevel || 'Both')}</div>
              <div style="color:var(--text-muted); margin-top:4px;">📍 ${EW.esc(p.location || 'Pan-India')} · ${EW.esc(p.auditionType || 'Online')}</div>
              <div style="color:var(--crimson-dark); font-weight:700; margin-top:4px;">${EW.esc(p.paymentType || 'Paid')}: ${EW.esc(p.budget || 'Negotiable')} (${EW.esc(p.paymentBasis || 'Per project')})</div>
            </div>
          </div>
          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:16px; padding-top:14px; border-top:1px solid var(--border-light);">
            <button type="button" onclick="openProjectApplications(${p.id})" class="btn btn-primary btn-sm">Applications (${p.applicationsCount})</button>
            <button type="button" onclick="openProjectBriefModal(${p.id})" class="btn btn-dark btn-sm">Full Brief &amp; Media</button>
            <button type="button" onclick="openEditProjectModal(${p.id})" class="btn btn-outline-dark btn-sm">✏️ Edit</button>
            ${p.status === 'ACTIVE'
              ? `<button type="button" onclick="setProjectLifecycleStatus(${p.id}, 'PAUSED')" class="btn btn-outline-dark btn-sm">⏸ Pause</button>`
              : `<button type="button" onclick="setProjectLifecycleStatus(${p.id}, 'ACTIVE')" class="btn btn-outline-dark btn-sm">▶ Publish</button>`}
            ${p.status !== 'CLOSED'
              ? `<button type="button" onclick="setProjectLifecycleStatus(${p.id}, 'CLOSED')" class="btn btn-outline-dark btn-sm">Close Call</button>` : ''}
            <button type="button" onclick="promoteProject(${p.id})" class="btn btn-outline-dark btn-sm">${p.promoted ? '✓ Promoted' : '★ Promote'}</button>
            <button type="button" onclick="openShareModal(${p.id}, '${EW.esc(p.projectTitle)}')" class="btn btn-outline-dark btn-sm">Share</button>
            <button type="button" onclick="deleteProjectById(${p.id})" class="btn btn-outline-dark btn-sm" style="color:#9B1C1C; margin-left:auto;">🗑 Delete</button>
          </div>
        </div>`).join('') || '<div class="card" style="padding:36px; text-align:center;">No projects found in this filter. Click + Post Casting Call to launch a new one.</div>'}`;
    } catch (err) {
      showDashError('Unable to Load Casting Projects', err);
    }
  }
  window.renderProjectsFilter = renderProjects;

  window.openEditProjectModal = async (pid) => {
    if (!cachedProjects.length) cachedProjects = await EW.api('/api/dashboard/projects?filter=ALL');
    const p = cachedProjects.find(x => x.id === pid);
    if (!p) return;
    document.getElementById('am-title').textContent = `Edit Project — ${p.projectTitle}`;
    document.getElementById('am-body').innerHTML = `
      <form id="edit-proj-form" style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
        <div style="grid-column:span 2;">
          <label class="form-label">Project Title *</label>
          <input name="projectTitle" class="form-control" required value="${EW.esc(p.projectTitle)}">
        </div>
        <div>
          <label class="form-label">Project Type</label>
          <input name="projectType" class="form-control" value="${EW.esc(p.projectType)}">
        </div>
        <div>
          <label class="form-label">Project Stage</label>
          <select name="projectStage" class="form-select">
            ${['Pre-production', 'Casting Open', 'Shooting Started', 'Closed'].map(s => `<option ${p.projectStage === s ? 'selected' : ''}>${s}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Required Talent / Role</label>
          <input name="targetCategory" class="form-control" value="${EW.esc(p.targetCategory)}">
        </div>
        <div>
          <label class="form-label">Gender</label>
          <select name="targetGender" class="form-select">
            ${['Any', 'Male', 'Female'].map(g => `<option ${p.targetGender === g ? 'selected' : ''}>${g}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Age Min</label>
          <input type="number" name="ageMin" class="form-control" value="${p.ageMin || 18}">
        </div>
        <div>
          <label class="form-label">Age Max</label>
          <input type="number" name="ageMax" class="form-control" value="${p.ageMax || 35}">
        </div>
        <div>
          <label class="form-label">Experience Level</label>
          <select name="experienceLevel" class="form-select">
            ${['Fresher', 'Experienced', 'Both'].map(e => `<option ${p.experienceLevel === e ? 'selected' : ''}>${e}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Audition Mode</label>
          <select name="auditionType" class="form-select">
            ${['Online', 'Offline'].map(m => `<option ${p.auditionType === m ? 'selected' : ''}>${m}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Shooting Location</label>
          <input name="location" class="form-control" value="${EW.esc(p.location || '')}">
        </div>
        <div>
          <label class="form-label">Budget / Compensation</label>
          <input name="budget" class="form-control" value="${EW.esc(p.budget || '')}">
        </div>
        <div>
          <label class="form-label">Shooting Start Date</label>
          <input type="date" name="shootingStartDate" class="form-control" value="${EW.esc(p.shootingStartDate || '')}">
        </div>
        <div>
          <label class="form-label">Shooting End Date</label>
          <input type="date" name="shootingEndDate" class="form-control" value="${EW.esc(p.shootingEndDate || '')}">
        </div>
        <div>
          <label class="form-label">Audition Date</label>
          <input type="date" name="auditionDate" class="form-control" value="${EW.esc(p.auditionDate || '')}">
        </div>
        <div>
          <label class="form-label">Last Date to Apply</label>
          <input type="date" name="lastDateToApply" class="form-control" value="${EW.esc(p.lastDateToApply || '')}">
        </div>
        <div style="grid-column:span 2;">
          <label class="form-label">Role Requirements &amp; Skills</label>
          <textarea name="roleDescription" class="form-textarea" style="min-height:66px;">${EW.esc(p.roleDescription || '')}</textarea>
        </div>
        <div style="grid-column:span 2;">
          <label class="form-label">Project Description *</label>
          <textarea name="projectDetails" class="form-textarea" required>${EW.esc(p.projectDetails)}</textarea>
        </div>
        <div style="grid-column:span 2; display:flex; justify-content:flex-end; gap:10px;">
          <button type="button" onclick="closeArtistModal()" class="btn btn-outline-dark btn-sm">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Project Changes</button>
        </div>
      </form>`;
    document.getElementById('artist-modal').classList.add('open');

    document.getElementById('edit-proj-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        const fd = new FormData(ev.target);
        await EW.api(`/api/dashboard/projects/${pid}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(Object.fromEntries(fd.entries()))
        });
        closeArtistModal();
        EW.toast('Project updated successfully!');
        await refreshOverview();
        renderProjects();
      } catch (err) { EW.toast(err.message, 'error'); }
    });
  };

  window.setProjectLifecycleStatus = async (pid, status) => {
    try {
      const res = await EW.api(`/api/dashboard/projects/${pid}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      EW.toast(res.message);
      await refreshOverview();
      renderProjects();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.deleteProjectById = async (pid) => {
    if (!confirm('Delete this casting project and all linked applications?')) return;
    try {
      await EW.api(`/api/dashboard/projects/${pid}`, { method: 'DELETE' });
      EW.toast('Casting project deleted');
      await refreshOverview();
      renderProjects();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.openProjectBriefModal = async (pid) => {
    if (!cachedProjects.length) {
      cachedProjects = await EW.api('/api/dashboard/projects?filter=ALL');
    }
    const p = cachedProjects.find(x => x.id === pid) || (state.overview.recentProjects || []).find(x => x.id === pid);
    if (!p) return;
    let refImgs = [];
    let lookImgs = [];
    try { refImgs = JSON.parse(p.referenceImagePathsJson || '[]'); } catch (_) {}
    try { lookImgs = JSON.parse(p.characterLookPathsJson || '[]'); } catch (_) {}

    const kv = (k, v) => `<div style="padding:8px 10px; background:var(--bg-ivory); border-radius:8px; font-size:0.84rem;"><div style="font-size:0.72rem; color:var(--text-muted); font-weight:700;">${k}</div><strong>${EW.esc(v || '—')}</strong></div>`;
    document.getElementById('am-title').textContent = p.projectTitle;
    document.getElementById('am-body').innerHTML = `
      <div style="display:flex; flex-direction:column; gap:14px;">
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
          ${kv('Project Type / Language / Status', `${p.projectType} · ${p.projectLanguage || '—'} · ${p.projectStage || '—'}`)}
          ${kv('Production Banner / Casting Company', `${p.productionHouseName || '—'} / ${p.castingCompanyName || p.companyName}`)}
          ${kv('Required Talent & Count', `${p.targetCategory} (${p.artistsRequiredCount || 1}) · ${p.targetGender} · ${p.ageMin}-${p.ageMax} yrs`)}
          ${kv('Height / Appearance / Experience', `${p.heightRequirement || 'Any'} · ${p.preferredAppearance || '—'} · ${p.experienceLevel || 'Both'}`)}
          ${kv('Character & Role', `${p.characterName ? p.characterName + ' — ' : ''}${p.roleDescription || p.projectDetails}`)}
          ${kv('Shoot & Audition Schedule', `Shoot: ${p.location || '—'} (${p.shootingStartDate || 'TBD'} to ${p.shootingEndDate || 'TBD'}, ${p.shootingDuration || '—'}) | Audition: ${p.auditionType || 'Online'} @ ${p.auditionLocation || '—'} on ${p.auditionDate || 'TBD'}`)}
          ${kv('Availability', p.availabilityRequirements)}
          ${kv('Payment & Logistics', `${p.paymentType || 'Paid'} · ${p.budget || '—'} (${p.paymentBasis || '—'}) | Stay: ${p.accommodationProvided || 'No'}, Food: ${p.foodProvided || 'No'}, Travel: ${p.travelExpensesCovered || 'No'}`)}
          ${kv('Audition Process & Deadline', `${p.auditionProcess || '—'} | Self-Intro Video: ${p.selfIntroVideoRequired || 'Yes'}, Portfolio: ${p.portfolioRequired || 'Yes'} | Apply by: ${p.lastDateToApply || p.expiryDate}`)}
          ${kv('Audition Instructions', p.auditionInstructions)}
          ${kv('Application Requirements', `Photos: ${p.requiredPhotographs || '—'} | Intro: ${p.introVideoRequirement || '—'} | Portfolio: ${p.actingPortfolioRequirement || '—'} | Work Links: ${p.previousWorkLinksReq || '—'} | Contact: ${p.applicantContactReq || '—'} | Extra Docs: ${p.additionalDocumentsReq || 'None'}`)}
          ${kv('Casting Contact Info', `${p.castingDirectorName || '—'} | ${p.contactEmail || '—'} | ${p.contactNumber || '—'} | WhatsApp: ${p.whatsappNumber || '—'} | ${p.websiteOrSocialLinks || '—'}`)}
        </div>
        ${p.auditionScript ? `<div style="padding:12px; background:#FAF7F2; border:1px solid var(--border-light); border-radius:10px; font-size:0.86rem;"><strong>Audition Script / Dialogue:</strong><br>${EW.esc(p.auditionScript)}</div>` : ''}
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
          ${p.projectPosterPath ? `<a href="${EW.esc(p.projectPosterPath)}" target="_blank" class="btn btn-outline-dark btn-sm">Project Poster ↗</a>` : ''}
          ${p.projectPromotionalImagePath ? `<a href="${EW.esc(p.projectPromotionalImagePath)}" target="_blank" class="btn btn-outline-dark btn-sm">Promotional Image ↗</a>` : ''}
          ${p.auditionScriptPdfPath ? `<a href="${EW.esc(p.auditionScriptPdfPath)}" target="_blank" class="btn btn-primary btn-sm">Audition Script PDF ↗</a>` : ''}
        </div>
        ${refImgs.length ? `<div><strong style="font-size:0.85rem;">Reference Images:</strong><div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:6px;">${refImgs.map(u => `<a href="${EW.esc(u)}" target="_blank"><img src="${EW.esc(u)}" style="width:76px; height:76px; object-fit:cover; border-radius:8px;"></a>`).join('')}</div></div>` : ''}
        ${lookImgs.length ? `<div><strong style="font-size:0.85rem;">Character Look References:</strong><div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:6px;">${lookImgs.map(u => `<a href="${EW.esc(u)}" target="_blank"><img src="${EW.esc(u)}" style="width:76px; height:76px; object-fit:cover; border-radius:8px;"></a>`).join('')}</div></div>` : ''}
      </div>`;
    document.getElementById('artist-modal').classList.add('open');
  };

  window.openProjectApplications = (pid) => {
    state.appProjectId = pid;
    switchSection('applications');
  };

  window.promoteProject = async (pid) => {
    try {
      const res = await EW.api(`/api/dashboard/projects/${pid}/promote`, { method: 'POST' });
      EW.toast(res.message);
      await renderProjects();
    } catch (err) {
      EW.toast(err.message, 'error');
    }
  };

  window.openShareModal = (pid, title) => {
    const link = `${window.location.origin}/?casting=${pid}`;
    document.getElementById('share-proj-name').textContent = title;
    document.getElementById('share-link-input').value = link;
    document.getElementById('share-x').href = `https://twitter.com/intent/tweet?text=${encodeURIComponent('Audition open on Entertainment World: ' + title + ' ' + link)}`;
    document.getElementById('share-fb').href = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(link)}`;
    document.getElementById('share-wa').href = `https://wa.me/?text=${encodeURIComponent('Casting Call on Entertainment World: ' + title + ' ' + link)}`;
    document.getElementById('share-modal').classList.add('open');
  };

  window.copyShareLink = () => {
    const inp = document.getElementById('share-link-input');
    inp.select();
    navigator.clipboard.writeText(inp.value).catch(() => {});
    EW.toast('Casting link copied to clipboard!');
  };

  // ================= 4. VIEW APPLICATIONS + RATINGS + COMMENTS =================
  async function renderApplications() {
    showDashLoading('Loading applicant pipeline...');
    try {
      const qs = new URLSearchParams({
        tab: state.appTab,
        role: state.appRole,
        q: state.appQuery,
        sort: state.appSort
      });
      if (state.appProjectId) qs.set('projectId', state.appProjectId);
      const data = await EW.api(`/api/dashboard/applications?${qs}`);
    const projects = data.projects || [];
    const active = data.activeProject;
    const apps = data.applications || [];
    const counts = data.statusCounts || {};
    state.cachedApplications = apps;

    const tabList = ['ALL', 'APPLIED', 'SHORTLISTED', 'INVITED', 'SUGGESTED', 'SELECTED', 'REJECTED', 'BACKUPS'];

    document.getElementById('dash-view').innerHTML = `
      <div class="split-2">
        <div class="filter-panel">
          <h3 style="margin-bottom:12px;">Casting Projects</h3>
          ${projects.map(p => `
            <div onclick="selectAppProject(${p.id})" style="padding:12px; border-radius:12px; margin-bottom:8px; cursor:pointer; border:1px solid ${active && active.id === p.id ? 'var(--crimson)' : 'var(--border-light)'}; background:${active && active.id === p.id ? 'var(--crimson-soft)' : '#FFF'};">
              <div style="display:flex; justify-content:space-between; align-items:center; gap:6px;">
                <strong style="font-size:0.86rem;">${EW.esc(p.projectTitle)}</strong>
                <span class="badge badge-rejected">${p.applicationsCount}</span>
              </div>
              <div style="font-size:0.75rem; color:var(--text-muted); margin-top:4px;">${EW.esc(p.status)} · ${EW.esc(p.postedDate)}</div>
            </div>`).join('') || '<p style="color:var(--text-muted); font-size:0.86rem;">No projects created yet.</p>'}
        </div>

        <div>
          <div style="background:linear-gradient(90deg, #16151E, #321C2A); color:#FFFFFF; border-radius:16px; padding:18px 22px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
            <div>
              <div style="font-size:0.72rem; color:var(--gold); font-weight:800;">APPLICATION PIPELINE &amp; PANEL REVIEWS</div>
              <div style="font-size:1.15rem; font-weight:800;">${active ? EW.esc(active.projectTitle) : 'No Active Project'}</div>
            </div>
            <div style="display:flex; gap:8px; flex-wrap:wrap;">
              <button type="button" onclick="switchSection('selection')" class="btn btn-outline-light btn-sm">Compare / Final Cast</button>
              <button type="button" onclick="downloadSelectedExcel()" class="btn btn-gold btn-sm">Download Excel</button>
              <button type="button" onclick="bulkInviteSelected(${active ? active.id : null})" class="btn btn-primary btn-sm">Invite to Audition</button>
            </div>
          </div>

          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; align-items:center;">
            <input class="form-control" style="max-width:220px;" placeholder="Filter applicant name/city..." value="${EW.esc(state.appQuery)}" onchange="setAppQuery(this.value)">
            <select class="form-select" style="width:170px;" onchange="setAppRole(this.value)">
              <option value="ALL">All Roles</option>
              ${(data.roles || []).map(r => `<option value="${EW.esc(r)}" ${state.appRole === r ? 'selected' : ''}>${EW.esc(r)}</option>`).join('')}
            </select>
            <select class="form-select" style="width:160px;" onchange="setAppSort(this.value)">
              <option value="RELEVANCE" ${state.appSort === 'RELEVANCE' ? 'selected' : ''}>Sort: Recent</option>
              <option value="POPULAR" ${state.appSort === 'POPULAR' ? 'selected' : ''}>Sort: Most Viewed</option>
            </select>
          </div>

          <div style="display:flex; gap:6px; flex-wrap:wrap; margin-bottom:14px;">
            ${tabList.map(t => `
              <button class="btn btn-sm ${state.appTab === t ? 'btn-dark' : 'btn-outline-dark'}" onclick="setAppTab('${t}')">
                ${t === 'ALL' ? 'ALL' : t.charAt(0) + t.slice(1).toLowerCase()} (${counts[t] ?? 0})
              </button>`).join('')}
          </div>

          <div class="artists-grid">
            ${apps.map(item => renderArtistCard(item.artist, item)).join('') || '<div class="card" style="grid-column:span 4; padding:28px; text-align:center;">No applications matched in this tab.</div>'}
          </div>
        </div>
      </div>`;
    } catch (err) {
      showDashError('Unable to Load Applications', err);
    }
  }

  window.selectAppProject = (pid) => { state.appProjectId = pid; renderApplications(); };
  window.setAppTab = (t) => { state.appTab = t; renderApplications(); };
  window.setAppRole = (r) => { state.appRole = r; renderApplications(); };
  window.setAppQuery = (q) => { state.appQuery = q; renderApplications(); };
  window.setAppSort = (s) => { state.appSort = s; renderApplications(); };

  window.quickAppStatus = async (appId, status) => {
    try {
      await EW.api(`/api/dashboard/applications/${appId}/review`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      EW.toast(`Application updated to ${status}!`);
      await refreshOverview();
      renderApplications();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.openAppReviewModal = (appId) => {
    const item = (state.cachedApplications || []).find(x => x.applicationId === appId);
    if (!item) return;
    document.getElementById('am-title').textContent = `Review Application — ${item.artist.fullName}`;
    document.getElementById('am-body').innerHTML = `
      <form id="app-review-form" style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
        <div>
          <label class="form-label">Decision Status</label>
          <select name="status" class="form-select">
            ${['APPLIED', 'SHORTLISTED', 'INVITED', 'SUGGESTED', 'SELECTED', 'REJECTED'].map(s =>
              `<option value="${s}" ${item.applicationStatus === s ? 'selected' : ''}>${s}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Internal Rating (1 to 5)</label>
          <select name="rating" class="form-select">
            <option value="">-- Unrated --</option>
            ${[5, 4, 3, 2, 1].map(r => `<option value="${r}" ${item.rating === r ? 'selected' : ''}>${r} Star${r > 1 ? 's' : ''}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Assigned Character / Role</label>
          <input name="assignedRole" class="form-control" value="${EW.esc(item.assignedRole || item.roleName || '')}">
        </div>
        <div style="display:flex; align-items:flex-end; padding-bottom:8px;">
          <label style="display:flex; align-items:center; gap:8px; font-weight:700; font-size:0.86rem;">
            <input type="checkbox" name="backupCandidate" ${item.backupCandidate ? 'checked' : ''}> Mark as Backup Artist
          </label>
        </div>
        <div style="grid-column:span 2;">
          <label class="form-label">Internal Casting Notes / Comment</label>
          <textarea name="internalComment" class="form-textarea" placeholder="Add private casting panel feedback, chemistry notes, or availability concerns...">${EW.esc(item.internalComment || '')}</textarea>
        </div>
        <div style="grid-column:span 2; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
          <div style="display:flex; gap:8px;">
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="closeArtistModal(); EW_OPS.openScheduleAuditionModal(${item.artist.id}, ${item.projectId}, ${item.applicationId}, '${EW.esc(item.roleName)}')">📅 Schedule Audition</button>
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="closeArtistModal(); EW_OPS.openMessageModal(${item.artist.id}, ${item.projectId})">✉ Message Artist</button>
          </div>
          <button type="submit" class="btn btn-primary">Save Review &amp; Rating</button>
        </div>
      </form>`;
    document.getElementById('artist-modal').classList.add('open');

    document.getElementById('app-review-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        const fd = new FormData(ev.target);
        await EW.api(`/api/dashboard/applications/${appId}/review`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            status: fd.get('status'),
            rating: fd.get('rating') || null,
            assignedRole: fd.get('assignedRole'),
            internalComment: fd.get('internalComment'),
            backupCandidate: fd.has('backupCandidate')
          })
        });
        closeArtistModal();
        EW.toast('Application review & rating saved!');
        await refreshOverview();
        renderApplications();
      } catch (err) { EW.toast(err.message, 'error'); }
    });
  };

  // ================= 5. SEARCH ARTISTS + ACTOR MANAGEMENT =================
  async function renderSearchArtists(customFilters = null) {
    const filters = customFilters || {
      sortBy: 'Relevant', category: 'All', gender: 'All', agency: 'All',
      ageMin: 0, ageMax: 75, city: '', expertise: 'All', language: '', skills: '', keyword: '', videoOnly: false, verifiedOnly: false
    };
    const res = await EW.api('/api/dashboard/artists/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(filters)
    });
    state.cachedSearchArtists = res.artists || [];

    document.getElementById('dash-view').innerHTML = `
      <div class="split-2">
        <form id="artist-search-form" class="filter-panel">
          <h3 style="margin-bottom:14px;">ACTOR DIRECTORY &amp; SEARCH</h3>
          <div style="display:flex; flex-direction:column; gap:10px;">
            <div>
              <label class="form-label">Sort By</label>
              <select name="sortBy" class="form-select">
                <option ${filters.sortBy === 'Relevant' ? 'selected' : ''}>Relevant</option>
                <option ${filters.sortBy === 'Most Viewed' ? 'selected' : ''}>Most Viewed</option>
                <option ${filters.sortBy === 'Newest' ? 'selected' : ''}>Newest</option>
              </select>
            </div>
            <div>
              <label class="form-label">Category</label>
              <select name="category" class="form-select">
                ${['All', 'Actor', 'Model', 'Singer', 'Dancer', 'Voice-over Artist', 'Photographer', 'Musician', 'Writer', 'Stand-up Comedian', 'Filmmaker'].map(c =>
                  `<option ${filters.category === c ? 'selected' : ''}>${c}</option>`).join('')}
              </select>
            </div>
            <div>
              <label class="form-label">Gender</label>
              <select name="gender" class="form-select">
                <option ${filters.gender === 'All' ? 'selected' : ''}>All</option>
                <option ${filters.gender === 'Male' ? 'selected' : ''}>Male</option>
                <option ${filters.gender === 'Female' ? 'selected' : ''}>Female</option>
              </select>
            </div>
            <div>
              <label class="form-label">Select an Agency</label>
              <select name="agency" class="form-select">
                <option ${filters.agency === 'All' ? 'selected' : ''}>All</option>
                <option ${filters.agency === 'Entertainment World' ? 'selected' : ''}>Entertainment World</option>
                <option ${filters.agency === 'Partner Studio' ? 'selected' : ''}>Partner Studio</option>
                <option ${filters.agency === 'Independent' ? 'selected' : ''}>Independent</option>
              </select>
            </div>
            <div>
              <label class="form-label">Age Range (${filters.ageMin} - ${filters.ageMax})</label>
              <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                <input type="number" name="ageMin" class="form-control" min="0" max="75" value="${filters.ageMin}">
                <input type="number" name="ageMax" class="form-control" min="0" max="75" value="${filters.ageMax}">
              </div>
            </div>
            <div>
              <label class="form-label">Location</label>
              <input name="city" class="form-control" value="${EW.esc(filters.city)}" placeholder="Any (e.g. Mumbai, Delhi)">
            </div>
            <div>
              <label class="form-label">Experience / Expertise</label>
              <select name="expertise" class="form-select">
                <option ${filters.expertise === 'All' ? 'selected' : ''}>All</option>
                <option ${filters.expertise === 'Professional' ? 'selected' : ''}>Professional</option>
                <option ${filters.expertise === 'Intermediate' ? 'selected' : ''}>Intermediate</option>
              </select>
            </div>
            <div>
              <label class="form-label">Language</label>
              <input name="language" class="form-control" value="${EW.esc(filters.language)}" placeholder="e.g. Hindi, English">
            </div>
            <div>
              <label class="form-label">Skills</label>
              <input name="skills" class="form-control" value="${EW.esc(filters.skills || '')}" placeholder="e.g. Method Acting, Vocal, Stunts">
            </div>
            <div>
              <label class="form-label">Name / Keyword</label>
              <input name="keyword" class="form-control" value="${EW.esc(filters.keyword)}" placeholder="e.g. Aarav, drama, dubbing">
            </div>
            <label style="display:flex; align-items:center; gap:8px; font-size:0.84rem; font-weight:600;">
              <input type="checkbox" name="videoOnly" ${filters.videoOnly ? 'checked' : ''}> Only with showreel video
            </label>
            <label style="display:flex; align-items:center; gap:8px; font-size:0.84rem; font-weight:600;">
              <input type="checkbox" name="verifiedOnly" ${filters.verifiedOnly ? 'checked' : ''}> Only verified actor profiles
            </label>
            <div style="display:flex; gap:8px; margin-top:6px;">
              <button type="submit" class="btn btn-primary btn-sm" style="flex:1;">SEARCH</button>
              <button type="button" onclick="renderSearchReset()" class="btn btn-outline-dark btn-sm">RESET</button>
            </div>
          </div>
        </form>

        <div>
          ${renderBulkActionBar(res.total)}
          <div class="artists-grid">
            ${state.cachedSearchArtists.map(a => renderArtistCard(a)).join('') || '<div class="card" style="grid-column:span 4; padding:28px; text-align:center;">No artists matched your filters.</div>'}
          </div>
        </div>
      </div>`;

    document.getElementById('artist-search-form').addEventListener('submit', (ev) => {
      ev.preventDefault();
      const fd = new FormData(ev.target);
      renderSearchArtists({
        sortBy: fd.get('sortBy'),
        category: fd.get('category'),
        gender: fd.get('gender'),
        agency: fd.get('agency'),
        ageMin: Number(fd.get('ageMin') || 0),
        ageMax: Number(fd.get('ageMax') || 75),
        city: fd.get('city'),
        expertise: fd.get('expertise'),
        language: fd.get('language'),
        skills: fd.get('skills'),
        keyword: fd.get('keyword'),
        videoOnly: fd.has('videoOnly'),
        verifiedOnly: fd.has('verifiedOnly'),
        recordHistory: true
      });
    });
  }
  window.renderSearchReset = () => renderSearchArtists();

  function renderBulkActionBar(count) {
    return `
      <div class="card" style="padding:12px 16px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
        <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
          <button type="button" onclick="toggleSelectAllVisible()" class="btn btn-outline-dark btn-sm">SELECT ALL</button>
          <select id="bulk-action-select" class="form-select" style="width:190px; padding:7px 12px;" onchange="handleBulkSelect(this)">
            <option value="">Bulk Actions...</option>
            <option value="INVITE_AUDITION">Invite to Audition</option>
            <option value="DOWNLOAD_EXCEL">Download Excel</option>
            <option value="TAG_FOLDER">Tag Artists to Folder</option>
            <option value="COMPARE_ACTORS">Compare Selected</option>
          </select>
        </div>
        <div style="font-size:0.86rem; font-weight:700; color:var(--text-muted);">${count} Profiles Found</div>
      </div>`;
  }

  window.toggleSelectAllVisible = () => {
    const checkboxes = document.querySelectorAll('.artist-check');
    const allChecked = checkboxes.length && Array.from(checkboxes).every(cb => cb.checked);
    checkboxes.forEach(cb => {
      cb.checked = !allChecked;
      const id = Number(cb.dataset.id);
      if (cb.checked) state.selectedArtistIds.add(id);
      else state.selectedArtistIds.delete(id);
    });
  };

  window.onArtistCheck = (cb) => {
    const id = Number(cb.dataset.id);
    if (cb.checked) state.selectedArtistIds.add(id);
    else state.selectedArtistIds.delete(id);
  };

  window.handleBulkSelect = async (sel) => {
    const val = sel.value;
    sel.value = '';
    if (!val) return;
    if (val === 'DOWNLOAD_EXCEL') {
      downloadSelectedExcel();
    } else if (val === 'INVITE_AUDITION') {
      await bulkInviteSelected(state.appProjectId);
    } else if (val === 'TAG_FOLDER') {
      await openTagFolderModal();
    } else if (val === 'COMPARE_ACTORS') {
      const ids = Array.from(state.selectedArtistIds).slice(0, 4);
      if (!ids.length) return EW.toast('Select 2 to 4 artists first to compare.', 'error');
      ids.forEach(id => EW_OPS.addCompareId(id));
      switchSection('selection');
    }
  };

  window.downloadSelectedExcel = () => {
    const ids = Array.from(state.selectedArtistIds).join(',');
    window.location.href = `/api/dashboard/artists/export-excel${ids ? '?ids=' + encodeURIComponent(ids) : ''}`;
    EW.toast('Downloading Excel roster...');
  };

  window.bulkInviteSelected = async (projectId = null) => {
    const ids = Array.from(state.selectedArtistIds);
    if (!ids.length) return EW.toast('Select at least one artist checkbox first.', 'error');
    try {
      const res = await EW.api('/api/dashboard/artists/bulk-action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'INVITE_AUDITION', artistIds: ids, projectId })
      });
      EW.toast(res.message);
      await refreshOverview();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  async function openTagFolderModal() {
    const ids = Array.from(state.selectedArtistIds);
    if (!ids.length) return EW.toast('Select at least one artist checkbox first.', 'error');
    try {
      const data = await EW.api('/api/dashboard/folders');
      document.getElementById('tag-folder-select').innerHTML = data.folders.map(f =>
        `<option value="${f.id}">${EW.esc(f.folderName)} (${f.count})</option>`).join('');
      document.getElementById('tag-folder-modal').classList.add('open');
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  window.confirmTagArtists = async () => {
    try {
      const ids = Array.from(state.selectedArtistIds);
      const folderId = Number(document.getElementById('tag-folder-select').value);
      const res = await EW.api('/api/dashboard/artists/bulk-action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'TAG_FOLDER', artistIds: ids, folderId })
      });
      closeModal('tag-folder-modal');
      EW.toast(res.message);
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  // ================= 6. MY ARTISTS (FOLDERS) =================
  async function renderMyArtists(category = 'All', city = '') {
    showDashLoading('Loading saved artist folders...');
    try {
      const qs = new URLSearchParams({ folderId: state.myFolderId || 0, category, city });
      const data = await EW.api(`/api/dashboard/folders?${qs}`);

    document.getElementById('dash-view').innerHTML = `
      <div class="split-2">
        <div class="filter-panel">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <h3>MY ARTISTS</h3>
            <button type="button" onclick="promptNewFolder()" class="btn btn-primary btn-sm">+ Folder</button>
          </div>
          <div class="folder-item ${state.myFolderId === 0 ? 'active' : ''}" onclick="selectMyFolder(0)">
            <span>📁 All Artists</span><span>${data.totalSaved}</span>
          </div>
          ${data.folders.map(f => `
            <div class="folder-item ${state.myFolderId === f.id ? 'active' : ''}" onclick="selectMyFolder(${f.id})">
              <span>📂 ${EW.esc(f.folderName)}</span>
              <div style="display:flex; gap:8px; align-items:center;">
                <span>${f.count}</span>
                <button type="button" onclick="event.stopPropagation(); deleteFolder(${f.id})" style="border:none; background:transparent; cursor:pointer; color:var(--text-muted);">🗑</button>
              </div>
            </div>`).join('')}
        </div>

        <div>
          <div class="card" style="padding:14px 18px; margin-bottom:16px; display:flex; gap:12px; align-items:center; flex-wrap:wrap;">
            <select id="my-cat-filter" class="form-select" style="width:180px;">
              ${['All', 'Actor', 'Model', 'Singer', 'Dancer', 'Voice-over Artist', 'Photographer'].map(c =>
                `<option ${category === c ? 'selected' : ''}>${c}</option>`).join('')}
            </select>
            <input id="my-city-filter" class="form-control" style="width:180px;" value="${EW.esc(city)}" placeholder="Filter by location">
            <button type="button" onclick="applyMyArtistsFilter()" class="btn btn-dark btn-sm">FILTER</button>
            <button type="button" onclick="downloadSelectedExcel()" class="btn btn-outline-dark btn-sm">Download Excel</button>
          </div>
          <div class="artists-grid">
            ${data.artists.map(a => renderArtistCard(a)).join('') || '<div class="card" style="grid-column:span 4; padding:28px; text-align:center;">No shortlisted artists in this folder yet.</div>'}
          </div>
        </div>
      </div>`;
    } catch (err) {
      showDashError('Unable to Load Saved Artists', err);
    }
  }

  window.selectMyFolder = (fid) => { state.myFolderId = fid; renderMyArtists(); };
  window.applyMyArtistsFilter = () => renderMyArtists(document.getElementById('my-cat-filter').value, document.getElementById('my-city-filter').value);
  window.promptNewFolder = async () => {
    const name = prompt('Enter new folder name (e.g. Lead Villains):');
    if (!name || !name.trim()) return;
    try {
      await EW.api('/api/dashboard/folders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ folderName: name.trim() })
      });
      EW.toast('Folder created!');
      renderMyArtists();
    } catch (err) { EW.toast(err.message, 'error'); }
  };
  window.deleteFolder = async (fid) => {
    try {
      await EW.api(`/api/dashboard/folders/${fid}`, { method: 'DELETE' });
      if (state.myFolderId === fid) state.myFolderId = 0;
      EW.toast('Folder removed');
      renderMyArtists();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  // ================= 7. RECOMMENDED & AI MATCHED ARTISTS =================
  async function renderRecommended() {
    showDashLoading('Finding AI & recommended talent matches...');
    try {
      const qs = new URLSearchParams({ tier: state.recTier });
      if (state.recProjectId) qs.set('projectId', state.recProjectId);
      const data = await EW.api(`/api/dashboard/artists/recommended?${qs}`);
      const r = data.risingStar || {};
    document.getElementById('dash-view').innerHTML = `
      <div class="dash-page-head">
        <div>
          <h2>AI &amp; Recommended Talent Matches</h2>
          <p class="dash-sub">Smart casting recommendations ranked by project criteria, skills, verification &amp; search history.</p>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center;">
          <select class="form-select" style="width:200px;" onchange="setRecProject(this.value)">
            <option value="">Match against Latest Project</option>
            ${(data.projects || []).map(p => `<option value="${p.id}" ${String(state.recProjectId || data.activeProjectId || '') === String(p.id) ? 'selected' : ''}>${EW.esc(p.projectTitle)}</option>`).join('')}
          </select>
          <button type="button" onclick="setRecTier('PLATINUM')" class="btn btn-sm ${state.recTier === 'PLATINUM' ? 'btn-dark' : 'btn-outline-dark'}">PLATINUM</button>
          <button type="button" onclick="setRecTier('FEATURED')" class="btn btn-sm ${state.recTier === 'FEATURED' ? 'btn-dark' : 'btn-outline-dark'}">FEATURED</button>
          <button type="button" onclick="setRecTier('AI_MATCH')" class="btn btn-sm ${state.recTier === 'AI_MATCH' ? 'btn-primary' : 'btn-outline-dark'}">✨ AI BEST MATCH</button>
        </div>
      </div>

      <div class="split-2">
        <div class="card" style="overflow:hidden;">
          <div style="background:var(--bg-obsidian); color:var(--gold); padding:10px 14px; font-weight:800; font-size:0.8rem; letter-spacing:0.08em;">★ RISING ARTIST</div>
          <img src="${EW.esc(r.avatarUrl)}" style="width:100%; aspect-ratio:1/1; object-fit:cover;">
          <div style="padding:16px;">
            <h3 style="color:var(--crimson);">${EW.esc(r.fullName)}</h3>
            <div style="font-size:0.84rem; color:var(--text-muted); margin-bottom:10px;">${EW.esc(r.category)} · ${EW.esc(r.city)}</div>
            <p style="font-size:0.85rem; color:var(--text-body);">${EW.esc(r.bio)}</p>
            <button type="button" onclick="viewArtistPortfolio(${r.id})" class="btn btn-primary btn-sm" style="width:100%;">Inspect Portfolio</button>
          </div>
        </div>

        <div class="artists-grid" style="grid-template-columns:repeat(3, 1fr);">
          ${data.artists.map(a => renderArtistCard(a)).join('')}
        </div>
      </div>`;
    } catch (err) {
      showDashError('Unable to Load Recommended Talent', err);
    }
  }
  window.setRecTier = (t) => { state.recTier = t; renderRecommended(); };
  window.setRecProject = (pid) => { state.recProjectId = pid; renderRecommended(); };

  // ================= 8. GET PACKAGE =================
  async function renderPackagesSection() {
    const res = await EW.api('/api/subscription/passes');
    const c = res.company;
    document.getElementById('dash-view').innerHTML = `
      <div class="card" style="padding:26px; margin-bottom:20px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:14px;">
        <div>
          <span class="badge badge-paid">ACTIVE PASS</span>
          <h2 style="margin-top:6px;">${EW.esc(c.activePassName)}</h2>
          <div style="color:var(--text-muted); font-size:0.9rem;">Valid until <strong class="mono">${EW.esc(c.subscriptionExpiry)}</strong> · ${c.contactViewsRemaining} Contact Views Remaining</div>
        </div>
        <a href="/passes.html" class="btn btn-primary">Upgrade or Renew Pass →</a>
      </div>
      <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:18px;">
        ${res.passes.map(p => `
          <div class="card" style="padding:24px; ${p.passCode === c.activePassCode ? 'border:2px solid var(--crimson);' : ''}">
            <h3 style="margin-bottom:6px;">${EW.esc(p.passName)}</h3>
            <div style="font-size:1.7rem; font-weight:900; color:var(--crimson); margin-bottom:10px;">${EW.formatInr(p.basePriceInr)} <span style="font-size:0.82rem; color:var(--text-muted);">/ ${p.defaultMonths} mo</span></div>
            <div style="font-size:0.86rem; color:var(--text-body); display:flex; flex-direction:column; gap:6px; margin-bottom:16px;">
              <div>• Contact Views: <strong>${EW.esc(p.contactViewsLabel)}</strong></div>
              <div>• Portfolio Views: <strong>${EW.esc(p.portfolioViewsLabel)}</strong></div>
              <div>• Audition Invites: <strong>${p.inviteAuditionLimit}</strong></div>
              <div>• Project Promotions: <strong>${EW.esc(p.projectPromotionLabel)}</strong></div>
            </div>
            <a href="/passes.html" class="btn btn-dark btn-sm" style="width:100%;">Select / Upgrade</a>
          </div>`).join('')}
      </div>`;
  }

  // ================= 9. TRANSACTION HISTORY =================
  async function renderTransactions() {
    showDashLoading('Loading transaction & unlock history...');
    try {
      const qs = new URLSearchParams({ viewType: state.txViewType, tier: state.txTier, sort: state.txSort });
      const data = await EW.api(`/api/dashboard/transactions?${qs}`);
    const c = data.company;
    const total = Math.max(1, c.contactViewsTotal);
    const rem = c.contactViewsRemaining;
    const pct = Math.min(100, Math.round((rem / total) * 100));

    document.getElementById('dash-view').innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px; flex-wrap:wrap; gap:12px;">
        <h2>Transaction History</h2>
        <label style="display:flex; align-items:center; gap:8px; font-weight:700; font-size:0.88rem; cursor:pointer; background:#FFF; padding:8px 14px; border-radius:999px; border:1px solid var(--border-light);">
          <input type="checkbox" ${state.showPurchaseHistory ? 'checked' : ''} onchange="togglePurchaseHistory(this.checked)">
          SHOW PURCHASE HISTORY
        </label>
      </div>

      ${state.showPurchaseHistory ? `
        <div class="card" style="padding:20px; margin-bottom:20px; overflow-x:auto;">
          <h3 style="margin-bottom:12px;">Pass Purchase History &amp; Invoices</h3>
          <table style="width:100%; border-collapse:collapse; font-size:0.88rem;">
            <thead>
              <tr style="text-align:left; border-bottom:1px solid var(--border-light);">
                <th style="padding:8px;">Transaction Ref</th><th>Pass</th><th>Duration</th><th>Total Paid</th><th>Method</th><th>Validity</th><th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${data.purchaseHistory.map(p => `
                <tr style="border-bottom:1px solid var(--border-light);">
                  <td style="padding:10px 8px;" class="mono">${EW.esc(p.transactionRef)}</td>
                  <td><strong>${EW.esc(p.passName)}</strong></td>
                  <td>${p.durationMonths} mo</td>
                  <td><strong>${EW.formatInr(p.totalPaidInr)}</strong></td>
                  <td>${EW.esc(p.paymentMethod)}</td>
                  <td class="mono">${EW.esc(p.validFrom)} → ${EW.esc(p.validUntil)}</td>
                  <td><span class="badge badge-paid">${EW.esc(p.paymentStatus)}</span></td>
                </tr>`).join('')}
            </tbody>
          </table>
        </div>` : ''}

      <div class="card" style="padding:24px; margin-bottom:20px;">
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:16px;">
          <button class="btn ${state.txViewType === 'PORTFOLIO_VIEW' ? 'btn-primary' : 'btn-outline-dark'}" onclick="setTxViewType('PORTFOLIO_VIEW')">Portfolio Views</button>
          <button class="btn ${state.txViewType === 'CONTACT_VIEW' ? 'btn-primary' : 'btn-outline-dark'}" onclick="setTxViewType('CONTACT_VIEW')">Contact Views</button>
        </div>

        <div style="display:flex; gap:8px; margin-bottom:16px;">
          <button class="btn btn-sm ${state.txTier === 'FREE' ? 'btn-dark' : 'btn-outline-dark'}" onclick="setTxTier('FREE')">Free</button>
          <button class="btn btn-sm ${state.txTier === 'PAID' ? 'btn-primary' : 'btn-outline-dark'}" onclick="setTxTier('PAID')">Paid</button>
          <button class="btn btn-sm ${state.txTier === 'ALL' ? 'btn-dark' : 'btn-outline-dark'}" onclick="setTxTier('ALL')">All</button>
        </div>

        <div style="text-align:center;">
          <div style="font-weight:800; font-size:1.05rem;">
            ${rem} contact views remaining of ${c.contactViewsTotal}
          </div>
          <div class="quota-track"><div class="quota-fill" style="width:${pct}%;"></div></div>
          <a href="/passes.html" class="btn btn-primary btn-sm">UPGRADE PACKAGE</a>
        </div>
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
        <h3>${state.txViewType === 'CONTACT_VIEW' ? 'Viewed Contact' : 'Viewed Portfolios'}</h3>
        <select class="form-select" style="width:190px;" onchange="setTxSort(this.value)">
          <option value="RECENT" ${state.txSort === 'RECENT' ? 'selected' : ''}>Sort By: Recently viewed</option>
          <option value="OLDEST" ${state.txSort === 'OLDEST' ? 'selected' : ''}>Sort By: Oldest first</option>
        </select>
      </div>
      <div class="artists-grid">
        ${data.viewedItems.map(item => renderArtistCard(item.artist)).join('') || '<div class="card" style="grid-column:span 4; padding:26px; text-align:center;">No view history in this filter yet.</div>'}
      </div>`;
    } catch (err) {
      showDashError('Unable to Load Transaction History', err);
    }
  }

  window.togglePurchaseHistory = (v) => { state.showPurchaseHistory = v; renderTransactions(); };
  window.setTxViewType = (v) => { state.txViewType = v; renderTransactions(); };
  window.setTxTier = (v) => { state.txTier = v; renderTransactions(); };
  window.setTxSort = (v) => { state.txSort = v; renderTransactions(); };

  // ================= SHARED ARTIST CARD & MODALS =================
  function renderArtistCard(a, appMeta = null) {
    const checked = state.selectedArtistIds.has(a.id) ? 'checked' : '';
    return `
      <div class="artist-card">
        <div class="artist-img-wrap">
          <input type="checkbox" class="artist-check" data-id="${a.id}" ${checked} onchange="onArtistCheck(this)">
          <img src="${EW.esc(a.avatarUrl)}" alt="${EW.esc(a.fullName)}">
          ${a.profileVerified ? '<span class="verified-corner">✓ Verified</span>' : ''}
          ${a.matchScore ? `<span class="showcase-ribbon" style="bottom:auto; top:10px; right:${a.profileVerified ? '74px' : '8px'};">${a.matchScore}% MATCH</span>` : (a.showcaseBadge ? '<span class="showcase-ribbon">SHOWCASE</span>' : '')}
          ${a.hasVideo ? `<button type="button" class="video-pill" onclick="playVideoModal('${EW.esc(a.videoUrl)}', '${EW.esc(a.fullName)} — ${EW.esc(a.videoTitle || 'Audition')}')">▶ Video</button>` : ''}
        </div>
        <div class="artist-body">
          <div style="font-weight:800; font-size:0.98rem; color:var(--crimson-dark); cursor:pointer;" onclick="viewArtistPortfolio(${a.id})">${EW.esc(a.fullName)}</div>
          <div style="font-size:0.78rem; color:var(--text-muted);">${EW.esc(a.secondaryRoles || a.category)}</div>
          <div style="font-size:0.78rem; font-weight:600;">${EW.esc(a.city)} · ${a.age} yrs · ${EW.esc(a.expertise)}</div>
          ${a.skills ? `<div style="font-size:0.72rem; color:var(--text-muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${EW.esc(a.skills)}</div>` : ''}
          ${a.matchReasons && a.matchReasons.length ? `<div style="font-size:0.7rem; color:#0D7F44; font-weight:700;">${EW.esc(a.matchReasons.slice(0, 2).join(' · '))}</div>` : ''}
          ${appMeta ? `
            <div style="display:flex; gap:5px; flex-wrap:wrap; align-items:center; margin-top:2px;">
              <span class="badge ${EW_OPS.opBadge(appMeta.applicationStatus)}">${EW.esc(appMeta.applicationStatus)}</span>
              ${appMeta.backupCandidate ? '<span class="badge badge-pending">BACKUP</span>' : ''}
              ${appMeta.rating ? `<span style="color:#D97706; font-size:0.76rem; font-weight:800;">★ ${appMeta.rating}/5</span>` : ''}
            </div>
            ${appMeta.internalComment ? `<div style="font-size:0.72rem; color:var(--text-muted);">"${EW.esc(appMeta.internalComment)}"</div>` : ''}
            <div style="display:flex; gap:5px; flex-wrap:wrap; margin-top:4px;">
              <button type="button" class="icon-btn" onclick="openAppReviewModal(${appMeta.applicationId})">Review/Rate</button>
              <button type="button" class="icon-btn" onclick="quickAppStatus(${appMeta.applicationId}, 'SHORTLISTED')">Shortlist</button>
              <button type="button" class="icon-btn" style="color:#0D7F44;" onclick="quickAppStatus(${appMeta.applicationId}, 'SELECTED')">Select</button>
              <button type="button" class="icon-btn" style="color:#9B1C1C;" onclick="quickAppStatus(${appMeta.applicationId}, 'REJECTED')">Reject</button>
            </div>` : ''}
          <div class="artist-actions">
            <button type="button" class="icon-btn ${a.saved ? 'active' : ''}" onclick="toggleSaveArtist(${a.id}, this)" title="Favorite">♥</button>
            <button type="button" class="icon-btn" onclick="viewArtistPortfolio(${a.id})">Portfolio</button>
            <button type="button" class="icon-btn ${a.contactUnlocked ? 'active' : ''}" onclick="unlockArtistContact(${a.id})">${a.contactUnlocked ? 'Unlocked' : '📞 Contact'}</button>
          </div>
        </div>
      </div>`;
  }

  window.toggleSaveArtist = async (artistId, btn) => {
    try {
      const res = await EW.api(`/api/dashboard/folders/toggle/${artistId}`, { method: 'POST' });
      btn.classList.toggle('active', res.saved);
      EW.toast(res.saved ? `Saved to ${res.folderName}` : `Removed from ${res.folderName}`);
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.viewArtistPortfolio = async (artistId) => {
    try {
      const res = await EW.api(`/api/dashboard/artists/${artistId}/view-portfolio`, { method: 'POST' });
      showArtistDetailModal(res.artist);
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.unlockArtistContact = async (artistId) => {
    try {
      const res = await EW.api(`/api/dashboard/artists/${artistId}/unlock-contact`, { method: 'POST' });
      EW.toast(`Contact unlocked! ${res.company.contactViewsRemaining} views remaining.`);
      showArtistDetailModal(res.artist);
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  window.verifyArtistProfile = async (artistId, status, notes = null) => {
    try {
      const noteInput = document.getElementById('artist-verify-notes');
      const finalNotes = notes !== null ? notes : (noteInput ? noteInput.value : '');
      const res = await EW.api(`/api/dashboard/artists/${artistId}/verify`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, notes: finalNotes })
      });
      EW.toast(res.message);
      await refreshOverview();
      showArtistDetailModal(res.artist);
      if (state.activeSection === 'search') renderSearchArtists();
    } catch (err) { EW.toast(err.message, 'error'); }
  };

  function showArtistDetailModal(a) {
    document.getElementById('am-title').textContent = `${a.fullName} — ${a.category}`;
    document.getElementById('am-body').innerHTML = `
      <div style="display:flex; gap:18px; flex-wrap:wrap; margin-bottom:16px;">
        <img src="${EW.esc(a.avatarUrl)}" style="width:140px; height:140px; border-radius:16px; object-fit:cover;">
        <div style="flex:1; min-width:240px;">
          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:8px;">
            <span class="badge ${EW_OPS.opBadge(a.verificationStatus)}">${a.profileVerified ? '✓ Profile Verified' : 'Verification: ' + EW.esc(a.verificationStatus || 'Pending')}</span>
            ${a.showcaseBadge ? '<span class="badge badge-showcase">SHOWCASE</span>' : ''}
            <span class="badge badge-neutral">${EW.esc(a.expertise)}</span>
            <span class="badge badge-neutral">${EW.esc(a.city)}</span>
          </div>
          <p style="margin:6px 0 8px; font-size:0.9rem;">${EW.esc(a.bio)}</p>
          <div style="font-size:0.84rem; margin-bottom:4px;"><strong>Skills:</strong> ${EW.esc(a.skills || '—')}</div>
          <div style="font-size:0.84rem;"><strong>Languages:</strong> ${EW.esc(a.languages)} · <strong>Agency:</strong> ${EW.esc(a.agencyName)}</div>
          ${a.portfolioUrl ? `<div style="font-size:0.82rem; margin-top:4px;"><a href="${EW.esc(a.portfolioUrl)}" target="_blank" style="color:var(--crimson); font-weight:700;">External Portfolio Reel ↗</a></div>` : ''}
          <div style="margin-top:10px; padding:10px 12px; border-radius:10px; background:var(--bg-ivory); font-size:0.86rem;">
            <div><strong>Email:</strong> <span class="mono">${EW.esc(a.email)}</span></div>
            <div><strong>Phone:</strong> <span class="mono">${EW.esc(a.phone)}</span></div>
            ${a.verificationNotes ? `<div style="font-size:0.78rem; color:var(--text-muted); margin-top:4px;"><strong>Verification Note:</strong> ${EW.esc(a.verificationNotes)}</div>` : ''}
          </div>
          <div style="margin-top:10px;">
            <input id="artist-verify-notes" class="form-control" style="font-size:0.82rem; padding:7px 10px;" value="${EW.esc(a.verificationNotes || 'Identity and portfolio confirmed by Casting Desk')}" placeholder="Verification notes...">
          </div>
          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:10px;">
            ${!a.contactUnlocked ? `<button type="button" onclick="unlockArtistContact(${a.id})" class="btn btn-primary btn-sm">Unlock Contact (1 Credit)</button>` : ''}
            <button type="button" onclick="verifyArtistProfile(${a.id}, 'Verified')" class="btn btn-dark btn-sm">✓ Verify Profile</button>
            <button type="button" onclick="verifyArtistProfile(${a.id}, 'Rejected')" class="btn btn-outline-dark btn-sm">Reject Verification</button>
            <button type="button" onclick="closeArtistModal(); EW_OPS.openScheduleAuditionModal(${a.id})" class="btn btn-outline-dark btn-sm">📅 Schedule Audition</button>
            <button type="button" onclick="closeArtistModal(); EW_OPS.openMessageModal(${a.id})" class="btn btn-outline-dark btn-sm">✉ Message</button>
          </div>
        </div>
      </div>
      ${a.hasVideo && a.videoUrl ? `
        <h4 style="margin-bottom:8px;">Self-Tape Audition: ${EW.esc(a.videoTitle)}</h4>
        <video controls style="width:100%; border-radius:14px; background:#000;" src="${EW.esc(a.videoUrl)}"></video>` : ''}`;
    document.getElementById('artist-modal').classList.add('open');
  }

  // Init
  switchSection('dashboard');
})();
