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
    myFolderId: 0,
    recTier: 'PLATINUM',
    txViewType: 'CONTACT_VIEW',
    txTier: 'PAID',
    txSort: 'RECENT',
    showPurchaseHistory: false,
    cachedSearchArtists: []
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

  window.switchSection = async (sec) => {
    state.activeSection = sec;
    state.selectedArtistIds.clear();
    document.querySelectorAll('.side-link').forEach(b => b.classList.toggle('active', b.dataset.sec === sec));
    if (sec === 'dashboard') await renderDashboardHome();
    else if (sec === 'post-project') renderPostProject();
    else if (sec === 'projects') await renderProjects();
    else if (sec === 'applications') await renderApplications();
    else if (sec === 'my-artists') await renderMyArtists();
    else if (sec === 'search') await renderSearchArtists();
    else if (sec === 'recommended') await renderRecommended();
    else if (sec === 'package') await renderPackagesSection();
    else if (sec === 'transactions') await renderTransactions();
  };

  // ================= 1. DASHBOARD HOME (VERIFIED PROFILE + SHOWREELS) =================
  async function renderDashboardHome() {
    const ov = await refreshOverview();
    const c = ov.company;
    let videos = [];
    let photos = [];
    try { videos = JSON.parse(c.showreelVideosJson || '[]'); } catch (_) {}
    try { photos = JSON.parse(c.portfolioPhotosJson || '[]'); } catch (_) {}

    const root = document.getElementById('dash-view');
    root.innerHTML = `
      <div class="card" style="padding:14px 18px; margin-bottom:18px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; background:var(--gold-soft);">
        <div>
          <strong>🔔 Latest Update:</strong>
          ${ov.notifications.length ? EW.esc(ov.notifications[0].title + ' — ' + ov.notifications[0].message) : 'Welcome to your Entertainment World Recruiter Dashboard!'}
        </div>
        <span class="badge badge-paid">${EW.esc(c.activePassName)} · Expires ${EW.esc(c.subscriptionExpiry)}</span>
      </div>

      <div class="profile-hero">
        <div style="display:flex; gap:20px; align-items:flex-start; flex:1; min-width:280px;">
          <img src="${EW.esc(c.companyLogoPath)}" alt="Company Logo" class="profile-logo">
          <div>
            <div style="display:flex; align-items:center; gap:9px; flex-wrap:wrap;">
              <h1 style="font-size:1.75rem;">${EW.esc(c.companyName)}</h1>
              ${c.verifiedBadge ? '<span class="blue-tick" title="Verified Blue Tick — Authenticated by Entertainment World Super Admin">✓</span><span class="badge badge-approved">Verified Casting Studio</span>' : ''}
            </div>
            <div style="color:var(--text-muted); font-size:0.9rem; margin:4px 0 12px;">
              ${EW.esc(c.organizationType || 'Casting Director')} · 📍 ${EW.esc(c.city)}, ${EW.esc(c.state)}
            </div>
            <div style="display:flex; gap:20px; flex-wrap:wrap; font-size:0.88rem;">
              <div><strong class="mono">${c.followersCount}</strong> Followers</div>
              <div><strong class="mono" style="color:var(--crimson);">${ov.activeProjectsCount} Active projects</strong></div>
              <div><strong class="mono">${ov.profilesViewedCount}</strong> Profiles viewed</div>
              <div>📞 <strong>${EW.esc(c.contactNumber)}</strong></div>
              <div>✉️ <strong>${EW.esc(c.companyEmail)}</strong></div>
            </div>
          </div>
        </div>
        <div style="display:flex; flex-direction:column; gap:8px; align-items:flex-end;">
          <button type="button" onclick="openEditProfileModal()" class="btn btn-outline-dark btn-sm">✏️ EDIT PROFILE</button>
          <button type="button" onclick="switchSection('post-project')" class="btn btn-primary btn-sm">+ Post A Project</button>
        </div>
      </div>

      <div class="card" style="padding:24px; margin-bottom:20px;">
        <h3 style="margin-bottom:8px;">Company Profile</h3>
        <p style="margin:0; color:var(--text-body);">${EW.esc(c.companyDescription)}</p>
        <div style="margin-top:12px; display:flex; gap:10px; flex-wrap:wrap; font-size:0.84rem; color:var(--text-muted);">
          <span><strong>Representative:</strong> ${EW.esc(c.representativeName)}</span>
          ${c.website ? `<span>· <strong>Website:</strong> <a href="${EW.esc(c.website)}" target="_blank" style="color:var(--crimson);">${EW.esc(c.website)}</a></span>` : ''}
          <span>· <strong>Looking for:</strong> ${EW.esc(c.talentSought || 'Actors, Models, Voice-over Artists')}</span>
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
              <div style="font-weight:800; font-size:0.92rem;">${EW.esc(p.projectTitle)}</div>
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
    const projects = await EW.api(`/api/dashboard/projects?filter=${encodeURIComponent(filter)}`);
    cachedProjects = projects;
    document.getElementById('dash-view').innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px; flex-wrap:wrap; gap:12px;">
        <h2>PROJECT HISTORY</h2>
        <div style="display:flex; gap:10px; align-items:center;">
          <label style="font-size:0.86rem; font-weight:700;">Filter by:</label>
          <select class="form-select" style="width:160px;" onchange="renderProjectsFilter(this.value)">
            <option value="ALL" ${filter === 'ALL' ? 'selected' : ''}>All</option>
            <option value="ACTIVE" ${filter === 'ACTIVE' ? 'selected' : ''}>Active</option>
            <option value="PROMOTED" ${filter === 'PROMOTED' ? 'selected' : ''}>Promoted</option>
          </select>
          <button type="button" onclick="switchSection('post-project')" class="btn btn-primary btn-sm">+ Post Project</button>
        </div>
      </div>
      ${projects.map(p => `
        <div class="card" style="padding:24px; margin-bottom:16px;">
          <div style="display:grid; grid-template-columns:1.7fr 0.85fr; gap:18px;">
            <div>
              <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-bottom:8px;">
                <span class="badge badge-neutral">${EW.esc(p.projectType)}</span>
                <span class="badge badge-neutral">${EW.esc(p.targetCategory)}</span>
                ${p.projectLanguage ? `<span class="badge badge-neutral">${EW.esc(p.projectLanguage)}</span>` : ''}
                ${p.projectStage ? `<span class="badge badge-pending">${EW.esc(p.projectStage)}</span>` : ''}
                ${p.promoted ? '<span class="badge badge-showcase">★ PROMOTED</span>' : ''}
                ${p.onlineAuditionEnabled ? '<span class="badge badge-approved">Online Auditions Active</span>' : ''}
              </div>
              <h3 style="font-size:1.35rem; margin-bottom:6px;">${EW.esc(p.projectTitle)}</h3>
              <div style="font-size:0.8rem; color:var(--text-muted); margin-bottom:10px;">
                Posted on: ${EW.esc(p.postedDate)} &nbsp;|&nbsp; Last Date: ${EW.esc(p.lastDateToApply || p.expiryDate)} &nbsp;|&nbsp; Last viewed on: ${EW.esc(p.lastViewedDate)}
              </div>
              <p style="font-size:0.9rem; color:var(--text-body); margin-bottom:10px;">${EW.esc(p.projectDetails)}</p>
              <div style="font-size:0.82rem; color:var(--text-muted);">
                Banner: <strong style="color:var(--text-dark);">${EW.esc(p.productionHouseName || p.companyName)}</strong>
                · Casting: <strong style="color:var(--text-dark);">${EW.esc(p.castingCompanyName || p.companyName)}</strong>
                ${p.companyVerified ? '<span class="blue-tick" style="width:15px; height:15px; font-size:9px; vertical-align:middle;">✓</span>' : ''}
              </div>
            </div>
            <div style="border-left:1px solid var(--border-light); padding-left:18px; font-size:0.86rem;">
              ${p.projectPosterPath ? `<img src="${EW.esc(p.projectPosterPath)}" style="width:100%; height:96px; object-fit:cover; border-radius:10px; margin-bottom:8px;">` : ''}
              <strong>${EW.esc(p.targetCategory)} (${p.artistsRequiredCount || 1} needed)</strong>
              <div style="color:var(--text-muted);">${EW.esc(p.targetGender)}, ${p.ageMin} - ${p.ageMax} Yrs · ${EW.esc(p.experienceLevel || 'Both')}</div>
              <div style="color:var(--text-muted); margin-top:4px;">📍 ${EW.esc(p.location || 'Pan-India')} · ${EW.esc(p.auditionType || 'Online')}</div>
              <div style="color:var(--crimson-dark); font-weight:700; margin-top:4px;">${EW.esc(p.paymentType || 'Paid')}: ${EW.esc(p.budget || 'Negotiable')} (${EW.esc(p.paymentBasis || 'Per project')})</div>
            </div>
          </div>
          <div style="display:grid; grid-template-columns:repeat(4, 1fr); gap:10px; margin-top:16px; padding-top:14px; border-top:1px solid var(--border-light);">
            <button type="button" onclick="openProjectApplications(${p.id})" class="btn btn-outline-dark btn-sm">VIEW ${p.applicationsCount} APPS</button>
            <button type="button" onclick="openProjectBriefModal(${p.id})" class="btn btn-dark btn-sm">FULL BRIEF &amp; MEDIA</button>
            <button type="button" onclick="promoteProject(${p.id})" class="btn btn-outline-dark btn-sm">${p.promoted ? '✓ PROMOTED' : 'PROMOTE PROJECT'}</button>
            <button type="button" onclick="openShareModal(${p.id}, '${EW.esc(p.projectTitle)}')" class="btn btn-outline-dark btn-sm">SHARE JOB</button>
          </div>
        </div>`).join('')}`;
  }
  window.renderProjectsFilter = renderProjects;

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

  // ================= 4. VIEW APPLICATIONS =================
  async function renderApplications() {
    const qs = new URLSearchParams({
      tab: state.appTab,
      role: state.appRole
    });
    if (state.appProjectId) qs.set('projectId', state.appProjectId);
    const data = await EW.api(`/api/dashboard/applications?${qs}`);
    const projects = data.projects || [];
    const active = data.activeProject;
    const apps = data.applications || [];

    document.getElementById('dash-view').innerHTML = `
      <div class="split-2">
        <div class="filter-panel">
          <h3 style="margin-bottom:12px;">Projects</h3>
          ${projects.map(p => `
            <div onclick="selectAppProject(${p.id})" style="padding:12px; border-radius:12px; margin-bottom:8px; cursor:pointer; border:1px solid ${active && active.id === p.id ? 'var(--crimson)' : 'var(--border-light)'}; background:${active && active.id === p.id ? 'var(--crimson-soft)' : '#FFF'};">
              <div style="display:flex; justify-content:space-between; align-items:center; gap:6px;">
                <strong style="font-size:0.86rem;">${EW.esc(p.projectTitle)}</strong>
                <span class="badge badge-rejected">${p.applicationsCount}</span>
              </div>
              <div style="font-size:0.75rem; color:var(--text-muted); margin-top:4px;">Posted on ${EW.esc(p.postedDate)}</div>
            </div>`).join('')}
        </div>

        <div>
          <div style="background:linear-gradient(90deg, #16151E, #321C2A); color:#FFFFFF; border-radius:16px; padding:18px 22px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
            <div>
              <div style="font-size:0.72rem; color:var(--gold); font-weight:800;">ENTERTAINMENT WORLD TALENT STREAM</div>
              <div style="font-size:1.15rem; font-weight:800;">${active ? EW.esc(active.projectTitle) : 'No Active Project'}</div>
            </div>
            <div style="display:flex; gap:8px;">
              <button type="button" onclick="downloadSelectedExcel()" class="btn btn-gold btn-sm">Download Excel</button>
              <button type="button" onclick="bulkInviteSelected(${active ? active.id : null})" class="btn btn-primary btn-sm">Invite to Audition</button>
            </div>
          </div>

          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:14px;">
            ${['ALL', 'SHORTLISTED', 'INVITED', 'SUGGESTED'].map(t => `
              <button class="btn btn-sm ${state.appTab === t ? 'btn-dark' : 'btn-outline-dark'}" onclick="setAppTab('${t}')">
                ${t === 'ALL' ? 'ALL' : t === 'INVITED' ? 'Invited Artists' : t.charAt(0) + t.slice(1).toLowerCase()}
              </button>`).join('')}
          </div>

          <div class="artists-grid">
            ${apps.map(item => renderArtistCard(item.artist, item)).join('') || '<div class="card" style="grid-column:span 4; padding:28px; text-align:center;">No applications in this tab yet.</div>'}
          </div>
        </div>
      </div>`;
  }

  window.selectAppProject = (pid) => { state.appProjectId = pid; renderApplications(); };
  window.setAppTab = (t) => { state.appTab = t; renderApplications(); };

  // ================= 5. SEARCH ARTISTS =================
  async function renderSearchArtists(customFilters = null) {
    const filters = customFilters || {
      sortBy: 'Relevant', category: 'All', gender: 'All', agency: 'All',
      ageMin: 0, ageMax: 75, city: '', expertise: 'All', language: '', keyword: '', videoOnly: false
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
          <h3 style="margin-bottom:14px;">SEARCH ARTISTS</h3>
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
              <label class="form-label">Expertise</label>
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
              <label class="form-label">Keyword</label>
              <input name="keyword" class="form-control" value="${EW.esc(filters.keyword)}" placeholder="e.g. drama, thriller">
            </div>
            <label style="display:flex; align-items:center; gap:8px; font-size:0.84rem; font-weight:600;">
              <input type="checkbox" name="videoOnly" ${filters.videoOnly ? 'checked' : ''}> Show only profiles with video
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
        keyword: fd.get('keyword'),
        videoOnly: fd.has('videoOnly'),
        recordHistory: true
      });
    });
  }
  window.renderSearchReset = () => renderSearchArtists();

  function renderBulkActionBar(count) {
    return `
      <div class="card" style="padding:12px 16px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
        <div style="display:flex; align-items:center; gap:12px;">
          <button type="button" onclick="toggleSelectAllVisible()" class="btn btn-outline-dark btn-sm">SELECT ALL</button>
          <select id="bulk-action-select" class="form-select" style="width:190px; padding:7px 12px;" onchange="handleBulkSelect(this)">
            <option value="">Bulk Actions...</option>
            <option value="INVITE_AUDITION">Invite to Audition</option>
            <option value="DOWNLOAD_EXCEL">Download Excel</option>
            <option value="TAG_FOLDER">Tag Artists to Folder</option>
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
    const data = await EW.api('/api/dashboard/folders');
    document.getElementById('tag-folder-select').innerHTML = data.folders.map(f =>
      `<option value="${f.id}">${EW.esc(f.folderName)} (${f.count})</option>`).join('');
    document.getElementById('tag-folder-modal').classList.add('open');
  }

  window.confirmTagArtists = async () => {
    const ids = Array.from(state.selectedArtistIds);
    const folderId = Number(document.getElementById('tag-folder-select').value);
    const res = await EW.api('/api/dashboard/artists/bulk-action', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'TAG_FOLDER', artistIds: ids, folderId })
    });
    closeModal('tag-folder-modal');
    EW.toast(res.message);
  };

  // ================= 6. MY ARTISTS (FOLDERS) =================
  async function renderMyArtists(category = 'All', city = '') {
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
    await EW.api(`/api/dashboard/folders/${fid}`, { method: 'DELETE' });
    if (state.myFolderId === fid) state.myFolderId = 0;
    EW.toast('Folder removed');
    renderMyArtists();
  };

  // ================= 7. RECOMMENDED ARTISTS =================
  async function renderRecommended() {
    const data = await EW.api(`/api/dashboard/artists/recommended?tier=${state.recTier}`);
    const r = data.risingStar;
    document.getElementById('dash-view').innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px; flex-wrap:wrap; gap:12px;">
        <div>
          <h2>Platinum &amp; Recommended Artists</h2>
          <p style="margin:4px 0 0; color:var(--text-muted); font-size:0.88rem;">Powered by your Search History &amp; active casting requirements</p>
        </div>
        <div style="display:flex; gap:8px;">
          <button type="button" onclick="setRecTier('PLATINUM')" class="btn btn-sm ${state.recTier === 'PLATINUM' ? 'btn-dark' : 'btn-outline-dark'}">PLATINUM</button>
          <button type="button" onclick="setRecTier('FEATURED')" class="btn btn-sm ${state.recTier === 'FEATURED' ? 'btn-dark' : 'btn-outline-dark'}">FEATURED</button>
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
  }
  window.setRecTier = (t) => { state.recTier = t; renderRecommended(); };

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
          ${a.showcaseBadge ? '<span class="showcase-ribbon">SHOWCASE</span>' : ''}
          ${a.hasVideo ? `<button type="button" class="video-pill" onclick="playVideoModal('${EW.esc(a.videoUrl)}', '${EW.esc(a.fullName)} — ${EW.esc(a.videoTitle || 'Audition')}')">▶ Video</button>` : ''}
        </div>
        <div class="artist-body">
          <div style="font-weight:800; font-size:0.98rem; color:var(--crimson-dark); cursor:pointer;" onclick="viewArtistPortfolio(${a.id})">${EW.esc(a.fullName)}</div>
          <div style="font-size:0.78rem; color:var(--text-muted);">${EW.esc(a.secondaryRoles || a.category)}</div>
          <div style="font-size:0.78rem; font-weight:600;">${EW.esc(a.city)} · ${a.age} yrs</div>
          ${appMeta ? `<span class="badge ${EW.badgeClass(appMeta.applicationStatus)}" style="align-self:flex-start;">${EW.esc(appMeta.applicationStatus)}</span>` : ''}
          <div class="artist-actions">
            <button type="button" class="icon-btn ${a.saved ? 'active' : ''}" onclick="toggleSaveArtist(${a.id}, this)" title="Shortlist">♥</button>
            <button type="button" class="icon-btn" onclick="viewArtistPortfolio(${a.id})">Portfolio</button>
            <button type="button" class="icon-btn ${a.contactUnlocked ? 'active' : ''}" onclick="unlockArtistContact(${a.id})">${a.contactUnlocked ? 'Unlocked' : '📞 Contact'}</button>
          </div>
        </div>
      </div>`;
  }

  window.toggleSaveArtist = async (artistId, btn) => {
    const res = await EW.api(`/api/dashboard/folders/toggle/${artistId}`, { method: 'POST' });
    btn.classList.toggle('active', res.saved);
    EW.toast(res.saved ? `Saved to ${res.folderName}` : `Removed from ${res.folderName}`);
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

  function showArtistDetailModal(a) {
    document.getElementById('am-title').textContent = `${a.fullName} — ${a.category}`;
    document.getElementById('am-body').innerHTML = `
      <div style="display:flex; gap:18px; flex-wrap:wrap; margin-bottom:16px;">
        <img src="${EW.esc(a.avatarUrl)}" style="width:140px; height:140px; border-radius:16px; object-fit:cover;">
        <div style="flex:1; min-width:220px;">
          <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:8px;">
            ${a.showcaseBadge ? '<span class="badge badge-showcase">SHOWCASE</span>' : ''}
            <span class="badge badge-neutral">${EW.esc(a.expertise)}</span>
            <span class="badge badge-neutral">${EW.esc(a.city)}</span>
          </div>
          <p style="margin:6px 0 10px; font-size:0.9rem;">${EW.esc(a.bio)}</p>
          <div style="font-size:0.86rem;"><strong>Languages:</strong> ${EW.esc(a.languages)} · <strong>Agency:</strong> ${EW.esc(a.agencyName)}</div>
          <div style="margin-top:10px; padding:10px 12px; border-radius:10px; background:var(--bg-ivory); font-size:0.88rem;">
            <div><strong>Email:</strong> <span class="mono">${EW.esc(a.email)}</span></div>
            <div><strong>Phone:</strong> <span class="mono">${EW.esc(a.phone)}</span></div>
          </div>
          ${!a.contactUnlocked ? `<button type="button" onclick="unlockArtistContact(${a.id})" class="btn btn-primary btn-sm" style="margin-top:10px;">Unlock Full Contact (1 Credit)</button>` : ''}
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
