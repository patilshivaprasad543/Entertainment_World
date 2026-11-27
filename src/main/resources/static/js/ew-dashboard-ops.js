/**
 * Casting Company Operational Modules for Entertainment World Dashboard:
 * - Audition Scheduling, Video Reviews, Ratings & Feedback
 * - Actor Side-by-Side Compare, Shortlist, Selection & Backup Pipeline
 * - Communication Center & Notifications History
 * - Casting Analytics Reports + Excel & PDF Downloads
 * - Team Management & Role Permissions
 * - Artist Contracts & Production Expense Tracking
 */
window.EW_OPS = (() => {
  function opBadge(val) {
    const v = String(val || '').toLowerCase();
    if (['selected', 'completed', 'signed', 'reimbursed', 'verified'].includes(v)) return 'badge-approved';
    if (['scheduled', 'rescheduled', 'sent', 'draft', 'in_progress', 'paused'].includes(v)) return 'badge-pending';
    if (['cancelled', 'terminated', 'closed'].includes(v)) return 'badge-rejected';
    return EW.badgeClass(val);
  }

  function stars(rating) {
    if (!rating) return '<span style="color:var(--text-muted); font-size:0.78rem;">Not rated</span>';
    const r = Number(rating);
    return `<span style="color:#D97706; font-weight:800;">${'★'.repeat(r)}${'☆'.repeat(5 - r)}</span> <span class="mono" style="font-size:0.78rem;">(${r}/5)</span>`;
  }

  function showLoading(label = 'Loading module data...') {
    document.getElementById('dash-view').innerHTML = `
      <div class="card" style="padding:48px; text-align:center;">
        <div class="ew-spinner" style="margin:0 auto 12px;"></div>
        <div style="font-weight:700; color:var(--text-muted);">${EW.esc(label)}</div>
      </div>`;
  }

  function emptyState(title, subtitle, actionHtml = '') {
    return `
      <div class="card" style="padding:36px; text-align:center;">
        <div style="font-size:1.1rem; font-weight:800; color:var(--bg-obsidian); margin-bottom:6px;">${EW.esc(title)}</div>
        <p style="margin:0 auto 14px; max-width:460px; color:var(--text-muted); font-size:0.88rem;">${EW.esc(subtitle)}</p>
        ${actionHtml}
      </div>`;
  }

  let audState = { projectId: '', status: 'ALL', mode: 'ALL', data: null };
  let selState = { projectId: '', compareIds: [] };
  let msgState = { channel: 'ALL', artistId: '' };
  let teamState = { members: [], projects: [] };
  let finState = { subTab: 'CONTRACTS' };

  // ==================== 5. AUDITION MANAGEMENT ====================
  async function renderAuditions() {
    showLoading('Loading audition schedule...');
    try {
      const qs = new URLSearchParams();
      if (audState.projectId) qs.set('projectId', audState.projectId);
      qs.set('status', audState.status);
      qs.set('mode', audState.mode);

      const data = await EW.api(`/api/dashboard/ops/auditions?${qs}`);
      audState.data = data;
      const stats = data.stats || {};
      const list = data.auditions || [];

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Audition Management</h2>
            <p class="dash-sub">Schedule online or studio auditions, review self-tapes, record panel ratings and reschedule slots.</p>
          </div>
          <button type="button" class="btn btn-primary btn-sm" onclick="EW_OPS.openScheduleAuditionModal()">+ Schedule Audition</button>
        </div>

        <div class="kpi-grid" style="grid-template-columns:repeat(5, 1fr); margin-bottom:18px;">
          <div class="kpi-card"><div class="kpi-label">Total Auditions</div><div class="kpi-val mono">${stats.total || 0}</div></div>
          <div class="kpi-card"><div class="kpi-label">Scheduled / Rescheduled</div><div class="kpi-val mono" style="color:var(--crimson);">${stats.scheduled || 0}</div></div>
          <div class="kpi-card"><div class="kpi-label">Completed</div><div class="kpi-val mono" style="color:#0D7F44;">${stats.completed || 0}</div></div>
          <div class="kpi-card"><div class="kpi-label">Online / Offline</div><div class="kpi-val mono">${stats.online || 0} / ${stats.offline || 0}</div></div>
          <div class="kpi-card"><div class="kpi-label">Cancelled</div><div class="kpi-val mono" style="color:#9B1C1C;">${stats.cancelled || 0}</div></div>
        </div>

        <div class="card" style="padding:14px 18px; margin-bottom:18px; display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
          <select class="form-select" style="width:220px;" onchange="EW_OPS.filterAuditions('projectId', this.value)">
            <option value="">All Casting Projects</option>
            ${(data.projects || []).map(p => `<option value="${p.id}" ${String(audState.projectId) === String(p.id) ? 'selected' : ''}>${EW.esc(p.projectTitle)}</option>`).join('')}
          </select>
          <select class="form-select" style="width:160px;" onchange="EW_OPS.filterAuditions('status', this.value)">
            ${['ALL', 'SCHEDULED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED'].map(s => `<option value="${s}" ${audState.status === s ? 'selected' : ''}>Status: ${s}</option>`).join('')}
          </select>
          <select class="form-select" style="width:150px;" onchange="EW_OPS.filterAuditions('mode', this.value)">
            ${['ALL', 'Online', 'Offline'].map(m => `<option value="${m}" ${audState.mode === m ? 'selected' : ''}>Mode: ${m}</option>`).join('')}
          </select>
        </div>

        ${list.length ? `
          <div style="display:grid; gap:14px;">
            ${list.map(a => `
              <div class="card" style="padding:18px 20px; display:grid; grid-template-columns:1.6fr 1fr auto; gap:16px; align-items:center;">
                <div style="display:flex; gap:14px; align-items:center;">
                  <img src="${EW.esc(a.artistAvatar)}" style="width:62px; height:62px; border-radius:14px; object-fit:cover;">
                  <div>
                    <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-bottom:4px;">
                      <strong style="font-size:1rem;">${EW.esc(a.artistName)}</strong>
                      <span class="badge badge-neutral">${EW.esc(a.artistCategory)}</span>
                      <span class="badge ${opBadge(a.status)}">${EW.esc(a.status)}</span>
                      <span class="badge badge-neutral">${EW.esc(a.auditionMode)}</span>
                    </div>
                    <div style="font-size:0.84rem; color:var(--text-muted);">
                      Project: <strong style="color:var(--text-dark);">${EW.esc(a.projectTitle)}</strong> · Role: <strong>${EW.esc(a.roleName || 'Candidate')}</strong>
                    </div>
                    <div style="font-size:0.82rem; color:var(--text-body); margin-top:4px;">
                      📅 <strong>${EW.esc(a.auditionDate)}</strong> at <strong>${EW.esc(a.auditionTime)}</strong> · 📍 ${EW.esc(a.locationOrLink || '—')}
                    </div>
                    ${a.feedback ? `<div style="font-size:0.8rem; color:var(--text-muted); margin-top:4px;">Panel note: "${EW.esc(a.feedback)}"</div>` : ''}
                  </div>
                </div>
                <div>
                  <div style="margin-bottom:6px;">${stars(a.rating)}</div>
                  <div style="font-size:0.78rem; color:var(--text-muted);">${a.invitationSent ? '✓ Invitation sent' : 'Pending invite'}</div>
                </div>
                <div style="display:flex; gap:8px; flex-wrap:wrap; justify-content:flex-end;">
                  ${a.auditionVideoUrl ? `<button type="button" class="btn btn-outline-dark btn-sm" onclick="playVideoModal('${EW.esc(a.auditionVideoUrl)}', '${EW.esc(a.artistName)} — Audition Reel')">▶ Video</button>` : ''}
                  <button type="button" class="btn btn-dark btn-sm" onclick="EW_OPS.openReviewAuditionModal(${a.id})">Review / Reschedule</button>
                  ${a.status !== 'CANCELLED' ? `<button type="button" class="btn btn-outline-dark btn-sm" onclick="EW_OPS.quickCancelAudition(${a.id})">Cancel</button>` : ''}
                  <button type="button" class="btn btn-outline-dark btn-sm" style="color:#9B1C1C;" onclick="EW_OPS.deleteAudition(${a.id})">🗑</button>
                </div>
              </div>`).join('')}
          </div>` : emptyState('No Auditions Found', 'Schedule an online video call or in-person audition to manage slots and record feedback.', '<button type="button" class="btn btn-primary btn-sm" onclick="EW_OPS.openScheduleAuditionModal()">+ Schedule First Audition</button>')}`;
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Auditions', err.message);
    }
  }

  function filterAuditions(key, val) {
    audState[key] = val;
    renderAuditions();
  }

  async function openScheduleAuditionModal(prefillArtistId = '', prefillProjectId = '', prefillAppId = '', prefillRole = '') {
    try {
      const data = audState.data || await EW.api('/api/dashboard/ops/auditions');
      audState.data = data;
      const projects = data.projects || [];
      const artists = data.artists || [];
      if (!projects.length) {
        EW.toast('Create a casting project first before scheduling an audition.', 'error');
        return;
      }
      const defaultDate = new Date(Date.now() + 2 * 86400000).toISOString().slice(0, 10);

      document.getElementById('am-title').textContent = 'Schedule Online / Offline Audition';
      document.getElementById('am-body').innerHTML = `
        <form id="ops-audition-form" style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
          <input type="hidden" name="applicationId" value="${EW.esc(prefillAppId)}">
          <div>
            <label class="form-label">Casting Project *</label>
            <select name="projectId" class="form-select" required>
              ${projects.map(p => `<option value="${p.id}" ${String(prefillProjectId) === String(p.id) ? 'selected' : ''}>${EW.esc(p.projectTitle)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">Artist / Actor *</label>
            <select name="artistId" class="form-select" required>
              ${artists.map(a => `<option value="${a.id}" ${String(prefillArtistId) === String(a.id) ? 'selected' : ''}>${EW.esc(a.fullName)} (${EW.esc(a.category)})</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">Role / Character</label>
            <input name="roleName" class="form-control" value="${EW.esc(prefillRole)}" placeholder="e.g. Lead Actor">
          </div>
          <div>
            <label class="form-label">Audition Mode *</label>
            <select name="auditionMode" class="form-select" required>
              <option value="Online">Online (Video Call / Self-Tape)</option>
              <option value="Offline">Offline (Studio In-Person)</option>
            </select>
          </div>
          <div>
            <label class="form-label">Audition Date *</label>
            <input type="date" name="auditionDate" class="form-control" value="${defaultDate}" required>
          </div>
          <div>
            <label class="form-label">Time Slot *</label>
            <input name="auditionTime" class="form-control" value="11:30 AM" required placeholder="e.g. 03:00 PM">
          </div>
          <div style="grid-column:span 2;">
            <label class="form-label">Studio Address or Video Meeting Link</label>
            <input name="locationOrLink" class="form-control" placeholder="Leave blank to auto-generate Entertainment World meeting room or use studio venue">
          </div>
          <div style="grid-column:span 2;">
            <label class="form-label">Audition Invitation Message</label>
            <textarea name="invitationMessage" class="form-textarea" placeholder="Please prepare Scene 2 monologue and arrive 15 mins early..."></textarea>
          </div>
          <div style="grid-column:span 2; display:flex; justify-content:flex-end; gap:10px; margin-top:6px;">
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="closeArtistModal()">Cancel</button>
            <button type="submit" class="btn btn-primary">Schedule &amp; Send Invitation</button>
          </div>
        </form>`;
      document.getElementById('artist-modal').classList.add('open');

      document.getElementById('ops-audition-form').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const fd = new FormData(ev.target);
        try {
          await EW.api('/api/dashboard/ops/auditions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(Object.fromEntries(fd.entries()))
          });
          closeArtistModal();
          EW.toast('Audition scheduled & invitation sent to artist!');
          if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
          if (document.querySelector('.side-link[data-sec="auditions"]').classList.contains('active')) {
            renderAuditions();
          } else {
            window.switchSection('auditions');
          }
        } catch (err) { EW.toast(err.message, 'error'); }
      });
    } catch (err) {
      EW.toast(err.message, 'error');
    }
  }

  function openReviewAuditionModal(id) {
    const a = (audState.data?.auditions || []).find(x => x.id === id);
    if (!a) return;
    document.getElementById('am-title').textContent = `Review / Reschedule — ${a.artistName}`;
    document.getElementById('am-body').innerHTML = `
      <form id="ops-aud-edit-form" style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
        <div>
          <label class="form-label">Status</label>
          <select name="status" class="form-select">
            ${['SCHEDULED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED'].map(s => `<option ${a.status === s ? 'selected' : ''}>${s}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Audition Mode</label>
          <select name="auditionMode" class="form-select">
            ${['Online', 'Offline'].map(m => `<option ${a.auditionMode === m ? 'selected' : ''}>${m}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Date</label>
          <input type="date" name="auditionDate" class="form-control" value="${EW.esc(a.auditionDate)}">
        </div>
        <div>
          <label class="form-label">Time</label>
          <input name="auditionTime" class="form-control" value="${EW.esc(a.auditionTime)}">
        </div>
        <div style="grid-column:span 2;">
          <label class="form-label">Venue / Meeting Link</label>
          <input name="locationOrLink" class="form-control" value="${EW.esc(a.locationOrLink || '')}">
        </div>
        <div>
          <label class="form-label">Panel Rating (1 to 5)</label>
          <select name="rating" class="form-select">
            <option value="">-- Unrated --</option>
            ${[5, 4, 3, 2, 1].map(r => `<option value="${r}" ${a.rating === r ? 'selected' : ''}>${r} Star${r > 1 ? 's' : ''}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="form-label">Audition Video URL</label>
          <input name="auditionVideoUrl" class="form-control" value="${EW.esc(a.auditionVideoUrl || '')}">
        </div>
        <div style="grid-column:span 2;">
          <label class="form-label">Casting Director Feedback</label>
          <textarea name="feedback" class="form-textarea">${EW.esc(a.feedback || '')}</textarea>
        </div>
        <div style="grid-column:span 2; display:flex; justify-content:flex-end; gap:10px;">
          <button type="submit" class="btn btn-primary">Save Audition Update</button>
        </div>
      </form>`;
    document.getElementById('artist-modal').classList.add('open');

    document.getElementById('ops-aud-edit-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        const fd = new FormData(ev.target);
        await EW.api(`/api/dashboard/ops/auditions/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(Object.fromEntries(fd.entries()))
        });
        closeArtistModal();
        EW.toast('Audition details & feedback saved!');
        if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
        renderAuditions();
      } catch (err) { EW.toast(err.message, 'error'); }
    });
  }

  async function quickCancelAudition(id) {
    try {
      await EW.api(`/api/dashboard/ops/auditions/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'CANCELLED' })
      });
      EW.toast('Audition marked Cancelled');
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderAuditions();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  async function deleteAudition(id) {
    if (!confirm('Remove this audition schedule permanently?')) return;
    try {
      await EW.api(`/api/dashboard/ops/auditions/${id}`, { method: 'DELETE' });
      EW.toast('Audition deleted');
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderAuditions();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  // ==================== 6. SHORTLISTING, COMPARE & SELECTION ====================
  async function renderSelectionPipeline() {
    showLoading('Loading shortlist & final cast selection...');
    try {
      const qs = new URLSearchParams();
      if (selState.projectId) qs.set('projectId', selState.projectId);
      const compareQs = selState.compareIds.length ? `?artistIds=${selState.compareIds.join(',')}` : '';

      const [summary, compareData] = await Promise.all([
        EW.api(`/api/dashboard/ops/selection/summary?${qs}`),
        EW.api(`/api/dashboard/ops/compare${compareQs}`)
      ]);

      const compared = compareData.compared || [];
      if (!selState.compareIds.length && compared.length) {
        selState.compareIds = compared.map(c => c.id);
      }

      const renderCandidateRow = (it, actionMode) => `
        <div class="card" style="padding:14px 16px; margin-bottom:10px; display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap;">
          <div style="display:flex; gap:12px; align-items:center;">
            <img src="${EW.esc(it.artistAvatar)}" style="width:50px; height:50px; border-radius:12px; object-fit:cover;">
            <div>
              <div style="display:flex; gap:6px; align-items:center; flex-wrap:wrap;">
                <strong style="cursor:pointer; color:var(--crimson-dark);" onclick="viewArtistPortfolio(${it.artistId})">${EW.esc(it.artistName)}</strong>
                ${it.profileVerified ? '<span class="badge badge-approved" style="font-size:0.65rem;">✓ Verified</span>' : ''}
                ${it.backupCandidate ? '<span class="badge badge-pending" style="font-size:0.65rem;">BACKUP</span>' : ''}
              </div>
              <div style="font-size:0.8rem; color:var(--text-muted);">${EW.esc(it.projectTitle)} · Role: <strong>${EW.esc(it.assignedRole)}</strong></div>
              <div style="font-size:0.78rem; margin-top:2px;">${stars(it.rating)} ${it.internalComment ? `· "${EW.esc(it.internalComment)}"` : ''}</div>
            </div>
          </div>
          <div style="display:flex; gap:6px; flex-wrap:wrap;">
            ${it.artistVideoUrl ? `<button type="button" class="btn btn-outline-dark btn-sm" onclick="playVideoModal('${EW.esc(it.artistVideoUrl)}', '${EW.esc(it.artistName)} Showreel')">▶ Reel</button>` : ''}
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="EW_OPS.addCompareId(${it.artistId})">+ Compare</button>
            ${actionMode !== 'selected' ? `<button type="button" class="btn btn-primary btn-sm" onclick="EW_OPS.updatePipelineStatus(${it.applicationId}, 'SELECTED', false)">Select Cast</button>` : ''}
            ${!it.backupCandidate ? `<button type="button" class="btn btn-dark btn-sm" onclick="EW_OPS.updatePipelineStatus(${it.applicationId}, 'SHORTLISTED', true)">Set Backup</button>` : ''}
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="EW_OPS.openMessageModal(${it.artistId}, ${it.projectId}, 'SELECTION_NOTICE')">✉ Notify</button>
          </div>
        </div>`;

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Actor Shortlisting, Compare &amp; Selection</h2>
            <p class="dash-sub">Compare artist profiles side by side, lock final selected roles, and maintain backup talent rosters.</p>
          </div>
          <select class="form-select" style="width:240px;" onchange="EW_OPS.setSelectionProject(this.value)">
            <option value="">All Casting Projects</option>
            ${(summary.projects || []).map(p => `<option value="${p.id}" ${String(selState.projectId) === String(p.id) ? 'selected' : ''}>${EW.esc(p.projectTitle)}</option>`).join('')}
          </select>
        </div>

        <div class="kpi-grid" style="grid-template-columns:repeat(4, 1fr); margin-bottom:20px;">
          <div class="kpi-card"><div class="kpi-label">Shortlisted Pool</div><div class="kpi-val mono">${(summary.shortlisted || []).length}</div></div>
          <div class="kpi-card"><div class="kpi-label">Final Selected Cast</div><div class="kpi-val mono" style="color:#0D7F44;">${(summary.selected || []).length}</div></div>
          <div class="kpi-card"><div class="kpi-label">Backup Artists</div><div class="kpi-val mono" style="color:#B45309;">${(summary.backups || []).length}</div></div>
          <div class="kpi-card"><div class="kpi-label">Audition Invited</div><div class="kpi-val mono">${(summary.invited || []).length}</div></div>
        </div>

        <div class="card" style="padding:20px; margin-bottom:22px;">
          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:14px;">
            <h3>Side-by-Side Actor Profile Comparison (Up to 4)</h3>
            <div style="display:flex; gap:8px; align-items:center;">
              <select id="compare-add-select" class="form-select" style="width:220px;">
                <option value="">+ Add artist to compare...</option>
                ${(compareData.availableArtists || []).map(a => `<option value="${a.id}">${EW.esc(a.fullName)} (${EW.esc(a.category)})</option>`).join('')}
              </select>
              <button type="button" class="btn btn-dark btn-sm" onclick="EW_OPS.addCompareFromDropdown()">Compare</button>
            </div>
          </div>
          ${compared.length ? `
            <div style="display:grid; grid-template-columns:repeat(${Math.min(4, compared.length)}, 1fr); gap:14px;">
              ${compared.map(c => `
                <div style="border:1px solid var(--border-light); border-radius:14px; padding:14px; background:var(--bg-ivory); display:flex; flex-direction:column; gap:6px;">
                  <div style="display:flex; justify-content:space-between; align-items:start;">
                    <img src="${EW.esc(c.avatarUrl)}" style="width:68px; height:68px; border-radius:12px; object-fit:cover;">
                    <button type="button" class="btn btn-outline-dark btn-sm" style="padding:2px 7px;" onclick="EW_OPS.removeCompareId(${c.id})">✕</button>
                  </div>
                  <strong style="font-size:0.98rem; color:var(--crimson-dark); cursor:pointer;" onclick="viewArtistPortfolio(${c.id})">${EW.esc(c.fullName)}</strong>
                  <div style="font-size:0.78rem; color:var(--text-muted);">${EW.esc(c.category)} · ${c.age} yrs · ${EW.esc(c.city)}</div>
                  <div style="font-size:0.78rem;"><strong>Verification:</strong> <span class="badge ${opBadge(c.verificationStatus)}">${EW.esc(c.verificationStatus)}</span></div>
                  <div style="font-size:0.78rem;"><strong>Panel Avg:</strong> ${c.avgRating ? `<strong class="mono">${c.avgRating}/5 ★</strong>` : '—'}</div>
                  <div style="font-size:0.78rem;"><strong>Experience:</strong> ${EW.esc(c.expertise)}</div>
                  <div style="font-size:0.78rem;"><strong>Languages:</strong> ${EW.esc(c.languages)}</div>
                  <div style="font-size:0.76rem; color:var(--text-muted);"><strong>Skills:</strong> ${EW.esc(c.skills || '—')}</div>
                  <div style="display:flex; gap:6px; margin-top:auto; padding-top:8px;">
                    ${c.hasVideo && c.videoUrl ? `<button type="button" class="btn btn-dark btn-sm" style="flex:1;" onclick="playVideoModal('${EW.esc(c.videoUrl)}', '${EW.esc(c.fullName)} Showreel')">▶ Reel</button>` : ''}
                    <button type="button" class="btn btn-outline-dark btn-sm" onclick="viewArtistPortfolio(${c.id})">Profile</button>
                  </div>
                </div>`).join('')}
            </div>` : '<p style="color:var(--text-muted);">Select up to 4 candidates from any list below to compare side by side.</p>'}
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:18px;">
          <div class="card" style="padding:20px;">
            <h3 style="margin-bottom:12px;">✓ Final Selected Actors</h3>
            ${(summary.selected || []).map(it => renderCandidateRow(it, 'selected')).join('') || '<p style="color:var(--text-muted);">No final cast selected yet. Mark candidates as Selected from Applications or Shortlist.</p>'}
            <h3 style="margin:18px 0 12px;">🛟 Backup Actor List</h3>
            ${(summary.backups || []).map(it => renderCandidateRow(it, 'backup')).join('') || '<p style="color:var(--text-muted);">No backup artists designated yet.</p>'}
          </div>
          <div class="card" style="padding:20px;">
            <h3 style="margin-bottom:12px;">★ Shortlisted &amp; Invited Candidates</h3>
            ${[...(summary.shortlisted || []), ...(summary.invited || [])].map(it => renderCandidateRow(it, 'shortlist')).join('') || '<p style="color:var(--text-muted);">No shortlisted or invited candidates in this filter.</p>'}
          </div>
        </div>`;
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Selection Pipeline', err.message);
    }
  }

  function setSelectionProject(val) { selState.projectId = val; renderSelectionPipeline(); }
  function addCompareId(id) {
    if (!selState.compareIds.includes(id)) {
      if (selState.compareIds.length >= 4) selState.compareIds.shift();
      selState.compareIds.push(id);
    }
    EW.toast('Added to side-by-side comparison');
    renderSelectionPipeline();
  }
  function removeCompareId(id) {
    selState.compareIds = selState.compareIds.filter(x => x !== id);
    renderSelectionPipeline();
  }
  function addCompareFromDropdown() {
    const val = Number(document.getElementById('compare-add-select').value);
    if (val) addCompareId(val);
  }
  async function updatePipelineStatus(appId, status, backupCandidate) {
    try {
      await EW.api(`/api/dashboard/applications/${appId}/review`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, backupCandidate })
      });
      EW.toast(backupCandidate ? 'Added to backup actor roster' : `Candidate marked ${status}!`);
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderSelectionPipeline();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  // ==================== 7. COMMUNICATION & NOTIFICATIONS ====================
  async function renderMessages() {
    showLoading('Loading messages & notifications...');
    try {
      const qs = new URLSearchParams({ channel: msgState.channel });
      if (msgState.artistId) qs.set('artistId', msgState.artistId);
      const data = await EW.api(`/api/dashboard/ops/messages?${qs}`);

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Communication &amp; Notifications</h2>
            <p class="dash-sub">Send direct messages, audition invitations, and selection or rejection notices with full audit history.</p>
          </div>
          <button type="button" class="btn btn-outline-dark btn-sm" onclick="markAllRead()">Mark ${data.unreadNotifications || 0} Notifications Read</button>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1.4fr; gap:20px; align-items:start;">
          <div class="card" style="padding:22px;">
            <h3 style="margin-bottom:14px;">Compose Artist Message / Notice</h3>
            <form id="ops-msg-form" style="display:flex; flex-direction:column; gap:10px;">
              <div>
                <label class="form-label">Recipient Artist *</label>
                <select name="artistId" class="form-select" required>
                  ${(data.artists || []).map(a => `<option value="${a.id}">${EW.esc(a.fullName)} (${EW.esc(a.category)})</option>`).join('')}
                </select>
              </div>
              <div>
                <label class="form-label">Related Casting Project</label>
                <select name="projectId" class="form-select">
                  <option value="">General Studio Communication</option>
                  ${(data.projects || []).map(p => `<option value="${p.id}">${EW.esc(p.projectTitle)}</option>`).join('')}
                </select>
              </div>
              <div>
                <label class="form-label">Notice / Message Type *</label>
                <select name="channel" class="form-select" required>
                  <option value="PORTAL_MESSAGE">Direct Portal Message</option>
                  <option value="AUDITION_INVITE">Audition Invitation</option>
                  <option value="SELECTION_NOTICE">Selection Confirmation</option>
                  <option value="REJECTION_NOTICE">Polite Rejection Notice</option>
                </select>
              </div>
              <div>
                <label class="form-label">Subject *</label>
                <input name="subject" class="form-control" required placeholder="e.g. Callback Invitation for Screen Test">
              </div>
              <div>
                <label class="form-label">Message Body *</label>
                <textarea name="messageBody" class="form-textarea" required placeholder="Write clear instructions, dates, or decision feedback..."></textarea>
              </div>
              <button type="submit" class="btn btn-primary">Send Communication →</button>
            </form>
          </div>

          <div>
            <div class="card" style="padding:18px; margin-bottom:16px;">
              <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:12px;">
                <h3>Communication History (${(data.messages || []).length})</h3>
                <select class="form-select" style="width:190px;" onchange="EW_OPS.filterMessages(this.value)">
                  ${['ALL', 'PORTAL_MESSAGE', 'AUDITION_INVITE', 'SELECTION_NOTICE', 'REJECTION_NOTICE'].map(ch =>
                    `<option value="${ch}" ${msgState.channel === ch ? 'selected' : ''}>${ch}</option>`).join('')}
                </select>
              </div>
              ${(data.messages || []).map(m => `
                <div style="padding:12px; border:1px solid var(--border-light); border-radius:12px; margin-bottom:10px;">
                  <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; margin-bottom:4px;">
                    <strong>${EW.esc(m.artistName)} <span style="font-weight:400; color:var(--text-muted);">(${EW.esc(m.projectTitle)})</span></strong>
                    <span class="badge badge-neutral">${EW.esc(m.channel)}</span>
                  </div>
                  <div style="font-weight:700; font-size:0.88rem; color:var(--crimson-dark);">${EW.esc(m.subject)}</div>
                  <p style="margin:4px 0; font-size:0.84rem; color:var(--text-body);">${EW.esc(m.messageBody)}</p>
                  <div style="font-size:0.74rem; color:var(--text-muted);">Sent by ${EW.esc(m.senderName)} · ${EW.esc(String(m.sentAt || '').replace('T', ' ').slice(0, 16))}</div>
                </div>`).join('') || '<p style="color:var(--text-muted);">No communication history in this filter yet.</p>'}
            </div>
          </div>
        </div>`;

      document.getElementById('ops-msg-form').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        try {
          const fd = new FormData(ev.target);
          await EW.api('/api/dashboard/ops/messages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(Object.fromEntries(fd.entries()))
          });
          EW.toast('Message & notification sent!');
          await window.refreshDashboardOverview();
          renderMessages();
        } catch (err) { EW.toast(err.message, 'error'); }
      });
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Messages', err.message);
    }
  }

  function filterMessages(ch) { msgState.channel = ch; renderMessages(); }

  async function openMessageModal(artistId, projectId = '', defaultChannel = 'PORTAL_MESSAGE') {
    try {
      const ov = await EW.api('/api/dashboard/ops/messages');
      const artist = (ov.artists || []).find(a => a.id === artistId);
      document.getElementById('am-title').textContent = `Message ${artist ? artist.fullName : 'Artist'}`;
      document.getElementById('am-body').innerHTML = `
        <form id="quick-msg-form" style="display:flex; flex-direction:column; gap:10px;">
          <input type="hidden" name="artistId" value="${artistId}">
          <input type="hidden" name="projectId" value="${EW.esc(projectId)}">
          <div>
            <label class="form-label">Channel</label>
            <select name="channel" class="form-select">
              ${['PORTAL_MESSAGE', 'AUDITION_INVITE', 'SELECTION_NOTICE', 'REJECTION_NOTICE'].map(ch =>
                `<option value="${ch}" ${defaultChannel === ch ? 'selected' : ''}>${ch}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">Subject *</label>
            <input name="subject" class="form-control" required value="Casting Update from Studio">
          </div>
          <div>
            <label class="form-label">Message *</label>
            <textarea name="messageBody" class="form-textarea" required placeholder="Enter details for the artist..."></textarea>
          </div>
          <div style="display:flex; justify-content:flex-end; gap:8px;">
            <button type="button" class="btn btn-outline-dark btn-sm" onclick="closeArtistModal()">Cancel</button>
            <button type="submit" class="btn btn-primary btn-sm">Send Now</button>
          </div>
        </form>`;
      document.getElementById('artist-modal').classList.add('open');
      document.getElementById('quick-msg-form').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        try {
          const fd = new FormData(ev.target);
          await EW.api('/api/dashboard/ops/messages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(Object.fromEntries(fd.entries()))
          });
          closeArtistModal();
          EW.toast('Message sent to artist!');
          if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
        } catch (err) { EW.toast(err.message, 'error'); }
      });
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  // ==================== 8. REPORTS & ANALYTICS ====================
  async function renderReports() {
    showLoading('Compiling MySQL casting analytics & reports...');
    try {
      const r = await EW.api('/api/dashboard/ops/reports');
      const act = r.actorReport || {};
      const aud = r.auditionReport || {};
      const fin = r.financialSummary || {};
      const months = r.monthlyActivity || [];
      const maxM = Math.max(1, ...months.map(m => Math.max(m.applications || 0, m.auditions || 0)));

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Casting Reports &amp; Analytics</h2>
            <p class="dash-sub">Live database insights across registered talent, projects, auditions, selections, and expenses.</p>
          </div>
          <div style="display:flex; gap:10px; flex-wrap:wrap;">
            <a href="/api/dashboard/ops/reports/export-excel" class="btn btn-outline-dark btn-sm">⬇ Export Excel (.xls)</a>
            <a href="/api/dashboard/ops/reports/export-pdf" class="btn btn-primary btn-sm">⬇ Export PDF Report</a>
          </div>
        </div>

        <div class="kpi-grid" style="grid-template-columns:repeat(4, 1fr); margin-bottom:20px;">
          <div class="kpi-card">
            <div class="kpi-label">Registered / Verified Actors</div>
            <div class="kpi-val mono">${act.totalActors || 0} <span style="font-size:0.9rem; color:#0D7F44;">(${act.verifiedActors || 0} ✓)</span></div>
            <div class="kpi-sub">${act.withVideo || 0} with video showreels</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Audition Completion</div>
            <div class="kpi-val mono">${aud.completed || 0} / ${aud.total || 0}</div>
            <div class="kpi-sub">Avg Rating: ${aud.averageRating || 0} ★ · ${aud.online || 0} Online</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Selected / Shortlisted Placements</div>
            <div class="kpi-val mono">${(r.selectionReport || []).length}</div>
            <div class="kpi-sub">${fin.activeContractsCount || 0} active artist contracts</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Committed Casting Spend</div>
            <div class="kpi-val mono" style="color:var(--crimson);">${EW.formatInr(fin.committedCompensationInr || 0)}</div>
            <div class="kpi-sub">Paid Expenses: ${EW.formatInr(fin.expensesPaidInr || 0)}</div>
          </div>
        </div>

        <div style="display:grid; grid-template-columns:1.25fr 1fr; gap:18px; margin-bottom:20px;">
          <div class="card" style="padding:20px;">
            <h3 style="margin-bottom:14px;">Monthly Activity Chart (Applications vs Auditions)</h3>
            <div style="display:grid; grid-template-columns:repeat(${months.length || 6}, 1fr); gap:10px; align-items:end; height:170px; padding-top:16px;">
              ${months.map(m => {
                const appH = Math.max(8, Math.round(((m.applications || 0) / maxM) * 115));
                const audH = Math.max(8, Math.round(((m.auditions || 0) / maxM) * 115));
                return `
                  <div style="text-align:center;">
                    <div style="display:flex; gap:4px; justify-content:center; align-items:flex-end; height:125px;">
                      <div title="${m.applications} Applications" style="width:16px; height:${appH}px; background:var(--crimson); border-radius:6px 6px 0 0;"></div>
                      <div title="${m.auditions} Auditions" style="width:16px; height:${audH}px; background:var(--bg-obsidian); border-radius:6px 6px 0 0;"></div>
                    </div>
                    <div style="font-size:0.72rem; font-weight:700; margin-top:6px;">${EW.esc(m.month)}</div>
                    <div style="font-size:0.68rem; color:var(--text-muted);">${m.applications}A / ${m.auditions}Au</div>
                  </div>`;
              }).join('')}
            </div>
          </div>

          <div class="card" style="padding:20px;">
            <h3 style="margin-bottom:12px;">Actors by Category</h3>
            <div style="display:flex; flex-direction:column; gap:8px;">
              ${Object.entries(act.byCategory || {}).map(([cat, cnt]) => {
                const pct = Math.min(100, Math.round((cnt / Math.max(1, act.totalActors)) * 100));
                return `
                  <div>
                    <div style="display:flex; justify-content:space-between; font-size:0.82rem; font-weight:700;">
                      <span>${EW.esc(cat)}</span><span class="mono">${cnt}</span>
                    </div>
                    <div style="height:8px; background:#EAE4D8; border-radius:999px; overflow:hidden; margin-top:3px;">
                      <div style="height:100%; width:${pct}%; background:var(--crimson);"></div>
                    </div>
                  </div>`;
              }).join('')}
            </div>
          </div>
        </div>

        <div class="card" style="padding:20px; margin-bottom:20px; overflow-x:auto;">
          <h3 style="margin-bottom:12px;">Project-Wise Application Report</h3>
          <table class="ew-table">
            <thead><tr><th>Project Title</th><th>Status</th><th>Category</th><th>Total</th><th>Applied</th><th>Shortlisted</th><th>Invited</th><th>Selected</th><th>Backups</th><th>Rejected</th></tr></thead>
            <tbody>
              ${(r.projectReport || []).map(p => `
                <tr>
                  <td><strong>${EW.esc(p.projectTitle)}</strong></td>
                  <td><span class="badge ${opBadge(p.status)}">${EW.esc(p.status)}</span></td>
                  <td>${EW.esc(p.targetCategory)}</td>
                  <td class="mono"><strong>${p.totalApplications}</strong></td>
                  <td class="mono">${p.applied}</td>
                  <td class="mono">${p.shortlisted}</td>
                  <td class="mono">${p.invited}</td>
                  <td class="mono" style="color:#0D7F44; font-weight:800;">${p.selected}</td>
                  <td class="mono">${p.backups}</td>
                  <td class="mono">${p.rejected}</td>
                </tr>`).join('') || '<tr><td colspan="10" style="text-align:center; color:var(--text-muted);">No casting projects recorded.</td></tr>'}
            </tbody>
          </table>
        </div>

        <div class="card" style="padding:20px; overflow-x:auto;">
          <h3 style="margin-bottom:12px;">Actor Selection &amp; Shortlist Report</h3>
          <table class="ew-table">
            <thead><tr><th>Artist</th><th>Project</th><th>Assigned Role</th><th>Status</th><th>Backup</th><th>Rating</th></tr></thead>
            <tbody>
              ${(r.selectionReport || []).map(s => `
                <tr>
                  <td><strong>${EW.esc(s.artistName)}</strong></td>
                  <td>${EW.esc(s.projectTitle)}</td>
                  <td>${EW.esc(s.assignedRole)}</td>
                  <td><span class="badge ${opBadge(s.status)}">${EW.esc(s.status)}</span></td>
                  <td>${s.backupCandidate ? 'Yes' : 'No'}</td>
                  <td>${stars(s.rating)}</td>
                </tr>`).join('') || '<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No shortlisted or selected actors yet.</td></tr>'}
            </tbody>
          </table>
        </div>`;
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Reports', err.message);
    }
  }

  // ==================== 9. TEAM MANAGEMENT ====================
  async function renderTeam() {
    showLoading('Loading casting team members...');
    try {
      const data = await EW.api('/api/dashboard/ops/team');
      const members = data.members || [];
      const projects = data.projects || [];
      teamState = { members, projects };

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Casting Team Management</h2>
            <p class="dash-sub">Add casting assistants and coordinators, assign them to specific projects, and configure permissions.</p>
          </div>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1.45fr; gap:20px; align-items:start;">
          <div class="card" style="padding:22px;">
            <h3 style="margin-bottom:14px;">+ Add Team Member</h3>
            <form id="ops-team-form" style="display:flex; flex-direction:column; gap:10px;">
              <div>
                <label class="form-label">Full Name *</label>
                <input name="fullName" class="form-control" required placeholder="e.g. Rhea Deshmukh">
              </div>
              <div>
                <label class="form-label">Official Email *</label>
                <input type="email" name="email" class="form-control" required placeholder="rhea@studio.in">
              </div>
              <div>
                <label class="form-label">Mobile Number</label>
                <input name="phone" class="form-control" placeholder="+91 98200 11223">
              </div>
              <div>
                <label class="form-label">Role *</label>
                <select name="roleTitle" class="form-select">
                  <option>Casting Coordinator</option>
                  <option>Casting Assistant</option>
                  <option>Casting Director</option>
                  <option>Talent Scout</option>
                </select>
              </div>
              <div>
                <label class="form-label">Assign Casting Projects</label>
                <div style="max-height:110px; overflow-y:auto; border:1px solid var(--border-light); border-radius:10px; padding:8px;">
                  ${projects.map(p => `
                    <label style="display:flex; align-items:center; gap:8px; font-size:0.82rem; padding:3px 0;">
                      <input type="checkbox" class="tm-proj-cb" value="${p.id}"> ${EW.esc(p.projectTitle)}
                    </label>`).join('') || '<span style="font-size:0.8rem; color:var(--text-muted);">No projects available</span>'}
                </div>
              </div>
              <div>
                <label class="form-label">Permissions</label>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:4px; font-size:0.78rem;">
                  ${['VIEW_APPLICATIONS', 'SHORTLIST_ARTISTS', 'SCHEDULE_AUDITIONS', 'MANAGE_PROJECTS', 'VERIFY_ACTORS', 'MANAGE_CONTRACTS'].map(perm => `
                    <label style="display:flex; align-items:center; gap:6px;">
                      <input type="checkbox" class="tm-perm-cb" value="${perm}" ${perm.startsWith('VIEW') || perm.startsWith('SHORTLIST') ? 'checked' : ''}> ${perm.replace('_', ' ')}
                    </label>`).join('')}
                </div>
              </div>
              <button type="submit" class="btn btn-primary" style="margin-top:6px;">Add Team Member</button>
            </form>
          </div>

          <div class="card" style="padding:20px;">
            <h3 style="margin-bottom:14px;">Active Casting Team (${members.length})</h3>
            ${members.map(m => `
              <div style="padding:14px; border:1px solid var(--border-light); border-radius:14px; margin-bottom:12px;">
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                  <div>
                    <strong style="font-size:0.98rem;">${EW.esc(m.fullName)}</strong>
                    <span class="badge badge-neutral" style="margin-left:6px;">${EW.esc(m.roleTitle)}</span>
                    <span class="badge ${m.active ? 'badge-approved' : 'badge-rejected'}" style="margin-left:4px;">${m.active ? 'Active' : 'Disabled'}</span>
                  </div>
                  <div style="display:flex; gap:8px;">
                    <button type="button" class="btn btn-dark btn-sm" onclick="EW_OPS.openEditTeamModal(${m.id})">Edit / Assign</button>
                    <button type="button" class="btn btn-outline-dark btn-sm" onclick="EW_OPS.toggleTeamActive(${m.id}, ${!m.active})">${m.active ? 'Disable' : 'Activate'}</button>
                    <button type="button" class="btn btn-outline-dark btn-sm" style="color:#9B1C1C;" onclick="EW_OPS.deleteTeamMember(${m.id})">Remove</button>
                  </div>
                </div>
                <div style="font-size:0.82rem; color:var(--text-muted); margin-top:4px;">✉️ ${EW.esc(m.email)} ${m.phone ? '· 📞 ' + EW.esc(m.phone) : ''}</div>
                <div style="font-size:0.8rem; margin-top:6px;"><strong>Projects:</strong> ${EW.esc(m.assignedProjectNames || 'All Projects / Unassigned')}</div>
                <div style="display:flex; gap:6px; flex-wrap:wrap; margin-top:6px;">
                  ${String(m.permissionsJson || '').split(',').filter(Boolean).map(p => `<span class="badge badge-neutral" style="font-size:0.68rem;">${EW.esc(p)}</span>`).join('')}
                </div>
              </div>`).join('') || '<p style="color:var(--text-muted);">No team members added yet.</p>'}
          </div>
        </div>`;

      document.getElementById('ops-team-form').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        try {
          const fd = new FormData(ev.target);
          const projectIds = Array.from(document.querySelectorAll('.tm-proj-cb:checked')).map(cb => cb.value).join(',');
          const perms = Array.from(document.querySelectorAll('.tm-perm-cb:checked')).map(cb => cb.value).join(',');
          await EW.api('/api/dashboard/ops/team', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              fullName: fd.get('fullName'),
              email: fd.get('email'),
              phone: fd.get('phone'),
              roleTitle: fd.get('roleTitle'),
              assignedProjectIds: projectIds,
              permissionsJson: perms
            })
          });
          EW.toast('Team member added & assigned!');
          if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
          renderTeam();
        } catch (err) { EW.toast(err.message, 'error'); }
      });
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Team', err.message);
    }
  }

  function openEditTeamModal(id) {
    const m = (teamState.members || []).find(x => x.id === id);
    if (!m) return;
    const assignedSet = new Set(String(m.assignedProjectIds || '').split(',').map(s => s.trim()).filter(Boolean));
    const permSet = new Set(String(m.permissionsJson || '').split(',').map(s => s.trim()).filter(Boolean));
    document.getElementById('am-title').textContent = `Edit Team Member — ${m.fullName}`;
    document.getElementById('am-body').innerHTML = `
      <form id="ops-team-edit-form" style="display:flex; flex-direction:column; gap:12px;">
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
          <div>
            <label class="form-label">Full Name *</label>
            <input name="fullName" class="form-control" required value="${EW.esc(m.fullName)}">
          </div>
          <div>
            <label class="form-label">Role *</label>
            <select name="roleTitle" class="form-select">
              ${['Casting Coordinator', 'Casting Assistant', 'Casting Director', 'Talent Scout'].map(r => `<option ${m.roleTitle === r ? 'selected' : ''}>${r}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" required value="${EW.esc(m.email)}">
          </div>
          <div>
            <label class="form-label">Phone</label>
            <input name="phone" class="form-control" value="${EW.esc(m.phone || '')}">
          </div>
        </div>
        <div>
          <label class="form-label">Assign Projects</label>
          <div style="max-height:110px; overflow-y:auto; border:1px solid var(--border-light); border-radius:10px; padding:8px;">
            ${(teamState.projects || []).map(p => `
              <label style="display:flex; align-items:center; gap:8px; font-size:0.82rem; padding:3px 0;">
                <input type="checkbox" class="tme-proj-cb" value="${p.id}" ${assignedSet.has(String(p.id)) ? 'checked' : ''}> ${EW.esc(p.projectTitle)}
              </label>`).join('') || '<span style="font-size:0.8rem; color:var(--text-muted);">No projects available</span>'}
          </div>
        </div>
        <div>
          <label class="form-label">Permissions</label>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px; font-size:0.8rem;">
            ${['VIEW_APPLICATIONS', 'SHORTLIST_ARTISTS', 'SCHEDULE_AUDITIONS', 'MANAGE_PROJECTS', 'VERIFY_ACTORS', 'MANAGE_CONTRACTS'].map(perm => `
              <label style="display:flex; align-items:center; gap:6px;">
                <input type="checkbox" class="tme-perm-cb" value="${perm}" ${permSet.has(perm) ? 'checked' : ''}> ${perm.replace('_', ' ')}
              </label>`).join('')}
          </div>
        </div>
        <div style="display:flex; justify-content:flex-end; gap:10px;">
          <button type="button" class="btn btn-outline-dark btn-sm" onclick="closeArtistModal()">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Team Updates</button>
        </div>
      </form>`;
    document.getElementById('artist-modal').classList.add('open');

    document.getElementById('ops-team-edit-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        const fd = new FormData(ev.target);
        const projectIds = Array.from(document.querySelectorAll('.tme-proj-cb:checked')).map(cb => cb.value).join(',');
        const perms = Array.from(document.querySelectorAll('.tme-perm-cb:checked')).map(cb => cb.value).join(',');
        await EW.api(`/api/dashboard/ops/team/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            fullName: fd.get('fullName'),
            email: fd.get('email'),
            phone: fd.get('phone'),
            roleTitle: fd.get('roleTitle'),
            assignedProjectIds: projectIds,
            permissionsJson: perms
          })
        });
        closeArtistModal();
        EW.toast('Team member updated!');
        renderTeam();
      } catch (err) { EW.toast(err.message, 'error'); }
    });
  }

  async function toggleTeamActive(id, nextState) {
    try {
      await EW.api(`/api/dashboard/ops/team/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ active: nextState })
      });
      EW.toast(nextState ? 'Team member activated' : 'Team member disabled');
      renderTeam();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  async function deleteTeamMember(id) {
    if (!confirm('Remove this team member from your casting workspace?')) return;
    try {
      await EW.api(`/api/dashboard/ops/team/${id}`, { method: 'DELETE' });
      EW.toast('Team member removed');
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderTeam();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  // ==================== 10. CONTRACTS & EXPENSE TRACKING ====================
  async function renderContractsAndExpenses() {
    showLoading('Loading contracts and casting financials...');
    try {
      const [conData, expData] = await Promise.all([
        EW.api('/api/dashboard/ops/contracts'),
        EW.api('/api/dashboard/ops/expenses')
      ]);
      const contracts = conData.contracts || [];
      const expenses = expData.expenses || [];
      const sum = expData.summary || {};

      document.getElementById('dash-view').innerHTML = `
        <div class="dash-page-head">
          <div>
            <h2>Contracts, Payments &amp; Expense Tracking</h2>
            <p class="dash-sub">Manage signed talent agreements and track production fees, audition venue costs, and payouts.</p>
          </div>
          <div style="display:flex; gap:8px;">
            <button type="button" class="btn btn-sm ${finState.subTab === 'CONTRACTS' ? 'btn-dark' : 'btn-outline-dark'}" onclick="EW_OPS.setFinTab('CONTRACTS')">Artist Contracts (${contracts.length})</button>
            <button type="button" class="btn btn-sm ${finState.subTab === 'EXPENSES' ? 'btn-dark' : 'btn-outline-dark'}" onclick="EW_OPS.setFinTab('EXPENSES')">Payments &amp; Expenses (${expenses.length})</button>
          </div>
        </div>

        <div class="kpi-grid" style="grid-template-columns:repeat(4, 1fr); margin-bottom:20px;">
          <div class="kpi-card"><div class="kpi-label">Active / Total Contracts</div><div class="kpi-val mono">${contracts.length}</div></div>
          <div class="kpi-card"><div class="kpi-label">Expenses Paid</div><div class="kpi-val mono" style="color:#0D7F44;">${EW.formatInr(sum.paidInr || 0)}</div></div>
          <div class="kpi-card"><div class="kpi-label">Pending Artist Payouts</div><div class="kpi-val mono" style="color:var(--crimson);">${EW.formatInr(sum.pendingInr || 0)}</div></div>
          <div class="kpi-card"><div class="kpi-label">Platform Pass Spend</div><div class="kpi-val mono">${EW.formatInr(sum.subscriptionPaidInr || 0)}</div></div>
        </div>

        ${finState.subTab === 'CONTRACTS' ? `
          <div style="display:grid; grid-template-columns:0.95fr 1.5fr; gap:20px; align-items:start;">
            <div class="card" style="padding:20px;">
              <h3 style="margin-bottom:12px;">+ Issue Artist Contract</h3>
              <form id="ops-contract-form" enctype="multipart/form-data" style="display:flex; flex-direction:column; gap:10px;">
                <div>
                  <label class="form-label">Contract Title *</label>
                  <input name="contractTitle" class="form-control" required placeholder="e.g. Lead Actor Agreement">
                </div>
                <div>
                  <label class="form-label">Project *</label>
                  <select name="projectId" class="form-select" required>
                    ${(conData.projects || []).map(p => `<option value="${p.id}">${EW.esc(p.projectTitle)}</option>`).join('')}
                  </select>
                </div>
                <div>
                  <label class="form-label">Artist *</label>
                  <select name="artistId" class="form-select" required>
                    ${(conData.artists || []).map(a => `<option value="${a.id}">${EW.esc(a.fullName)} (${EW.esc(a.category)})</option>`).join('')}
                  </select>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                  <div>
                    <label class="form-label">Compensation (₹) *</label>
                    <input type="number" name="compensationAmount" class="form-control" value="75000" required>
                  </div>
                  <div>
                    <label class="form-label">Initial Status</label>
                    <select name="contractStatus" class="form-select">
                      <option>DRAFT</option><option>SENT</option><option>SIGNED</option><option>ACTIVE</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label class="form-label">Payment Terms</label>
                  <input name="paymentTerms" class="form-control" value="50% advance, 50% on wrap">
                </div>
                <div>
                  <label class="form-label">Attach Agreement PDF (Optional)</label>
                  <input type="file" name="document" class="form-control" accept=".pdf,.doc,.docx,image/*">
                </div>
                <button type="submit" class="btn btn-primary">Create Contract</button>
              </form>
            </div>
            <div class="card" style="padding:20px;">
              <h3 style="margin-bottom:12px;">Managed Contracts</h3>
              ${contracts.map(c => `
                <div style="padding:14px; border:1px solid var(--border-light); border-radius:14px; margin-bottom:10px;">
                  <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; flex-wrap:wrap;">
                    <strong>${EW.esc(c.contractTitle)}</strong>
                    <span class="badge ${opBadge(c.contractStatus)}">${EW.esc(c.contractStatus)}</span>
                  </div>
                  <div style="font-size:0.82rem; color:var(--text-muted); margin:4px 0;">
                    ${EW.esc(c.artistName)} · ${EW.esc(c.projectTitle)} · Fee: <strong style="color:var(--crimson-dark);">${EW.formatInr(c.compensationAmount)}</strong>
                  </div>
                  <div style="font-size:0.78rem; color:var(--text-body); margin-bottom:8px;">Terms: ${EW.esc(c.paymentTerms)}</div>
                  <div style="display:flex; gap:8px; flex-wrap:wrap;">
                    ${c.documentPath ? `<a href="${EW.esc(c.documentPath)}" target="_blank" class="btn btn-outline-dark btn-sm">Document ↗</a>` : ''}
                    <select class="form-select" style="width:140px; padding:4px 8px; font-size:0.78rem;" onchange="EW_OPS.updateContractStatus(${c.id}, this.value)">
                      ${['DRAFT', 'SENT', 'SIGNED', 'ACTIVE', 'COMPLETED', 'TERMINATED'].map(st => `<option ${c.contractStatus === st ? 'selected' : ''}>${st}</option>`).join('')}
                    </select>
                    <button type="button" class="btn btn-outline-dark btn-sm" style="color:#9B1C1C;" onclick="EW_OPS.deleteContract(${c.id})">Delete</button>
                  </div>
                </div>`).join('') || '<p style="color:var(--text-muted);">No contracts issued yet.</p>'}
            </div>
          </div>
        ` : `
          <div style="display:grid; grid-template-columns:0.95fr 1.5fr; gap:20px; align-items:start;">
            <div class="card" style="padding:20px;">
              <h3 style="margin-bottom:12px;">+ Log Payment / Expense</h3>
              <form id="ops-expense-form" style="display:flex; flex-direction:column; gap:10px;">
                <div>
                  <label class="form-label">Expense Title *</label>
                  <input name="title" class="form-control" required placeholder="e.g. Callback Audition Hall Booking">
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                  <div>
                    <label class="form-label">Category</label>
                    <select name="category" class="form-select">
                      <option value="ARTIST_FEE">Artist Fee</option>
                      <option value="AUDITION_VENUE">Audition Venue</option>
                      <option value="TRAVEL_FOOD">Travel &amp; Food</option>
                      <option value="PROMOTION">Promotion</option>
                      <option value="CREW_MISC">Crew / Misc</option>
                    </select>
                  </div>
                  <div>
                    <label class="form-label">Amount (₹) *</label>
                    <input type="number" name="amountInr" class="form-control" required value="15000">
                  </div>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                  <div>
                    <label class="form-label">Project</label>
                    <select name="projectId" class="form-select">
                      <option value="">General Studio</option>
                      ${(expData.projects || []).map(p => `<option value="${p.id}">${EW.esc(p.projectTitle)}</option>`).join('')}
                    </select>
                  </div>
                  <div>
                    <label class="form-label">Status</label>
                    <select name="paymentStatus" class="form-select">
                      <option value="PENDING">PENDING</option>
                      <option value="PAID">PAID</option>
                      <option value="REIMBURSED">REIMBURSED</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label class="form-label">Payment Method</label>
                  <select name="paymentMethod" class="form-select">
                    <option>UPI</option><option>NEFT</option><option>Bank Transfer</option><option>Card</option>
                  </select>
                </div>
                <button type="submit" class="btn btn-primary">Record Expense</button>
              </form>
            </div>
            <div class="card" style="padding:20px; overflow-x:auto;">
              <h3 style="margin-bottom:12px;">Expense Ledger</h3>
              <table class="ew-table">
                <thead><tr><th>Date</th><th>Title &amp; Category</th><th>Project</th><th>Amount</th><th>Status</th><th>Action</th></tr></thead>
                <tbody>
                  ${expenses.map(e => `
                    <tr>
                      <td class="mono">${EW.esc(e.expenseDate)}</td>
                      <td><strong>${EW.esc(e.title)}</strong><div style="font-size:0.74rem; color:var(--text-muted);">${EW.esc(e.category)} · ${EW.esc(e.paymentMethod)}</div></td>
                      <td>${EW.esc(e.projectTitle)}</td>
                      <td class="mono"><strong>${EW.formatInr(e.amountInr)}</strong></td>
                      <td><span class="badge ${opBadge(e.paymentStatus)}">${EW.esc(e.paymentStatus)}</span></td>
                      <td>
                        <div style="display:flex; gap:6px;">
                          ${e.paymentStatus === 'PENDING' ? `<button type="button" class="btn btn-dark btn-sm" onclick="EW_OPS.markExpensePaid(${e.id})">Mark Paid</button>` : ''}
                          <button type="button" class="btn btn-outline-dark btn-sm" style="color:#9B1C1C;" onclick="EW_OPS.deleteExpense(${e.id})">🗑</button>
                        </div>
                      </td>
                    </tr>`).join('') || '<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No expenses recorded yet.</td></tr>'}
                </tbody>
              </table>
            </div>
          </div>
        `}`;

      const conForm = document.getElementById('ops-contract-form');
      if (conForm) {
        conForm.addEventListener('submit', async (ev) => {
          ev.preventDefault();
          try {
            const fd = new FormData(ev.target);
            await EW.api('/api/dashboard/ops/contracts', { method: 'POST', body: fd });
            EW.toast('Contract saved!');
            if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
            renderContractsAndExpenses();
          } catch (err) { EW.toast(err.message, 'error'); }
        });
      }
      const expForm = document.getElementById('ops-expense-form');
      if (expForm) {
        expForm.addEventListener('submit', async (ev) => {
          ev.preventDefault();
          try {
            const fd = new FormData(ev.target);
            await EW.api('/api/dashboard/ops/expenses', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(Object.fromEntries(fd.entries()))
            });
            EW.toast('Expense recorded!');
            renderContractsAndExpenses();
          } catch (err) { EW.toast(err.message, 'error'); }
        });
      }
    } catch (err) {
      EW.toast(err.message, 'error');
      document.getElementById('dash-view').innerHTML = emptyState('Unable to Load Contracts & Expenses', err.message);
    }
  }

  function setFinTab(tab) { finState.subTab = tab; renderContractsAndExpenses(); }
  async function updateContractStatus(id, contractStatus) {
    try {
      await EW.api(`/api/dashboard/ops/contracts/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contractStatus })
      });
      EW.toast(`Contract marked ${contractStatus}`);
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderContractsAndExpenses();
    } catch (err) { EW.toast(err.message, 'error'); }
  }
  async function deleteContract(id) {
    if (!confirm('Delete this contract?')) return;
    try {
      await EW.api(`/api/dashboard/ops/contracts/${id}`, { method: 'DELETE' });
      EW.toast('Contract deleted');
      if (window.refreshDashboardOverview) await window.refreshDashboardOverview();
      renderContractsAndExpenses();
    } catch (err) { EW.toast(err.message, 'error'); }
  }
  async function markExpensePaid(id) {
    try {
      await EW.api(`/api/dashboard/ops/expenses/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paymentStatus: 'PAID' })
      });
      EW.toast('Expense marked Paid!');
      renderContractsAndExpenses();
    } catch (err) { EW.toast(err.message, 'error'); }
  }
  async function deleteExpense(id) {
    if (!confirm('Delete this expense entry?')) return;
    try {
      await EW.api(`/api/dashboard/ops/expenses/${id}`, { method: 'DELETE' });
      EW.toast('Expense removed');
      renderContractsAndExpenses();
    } catch (err) { EW.toast(err.message, 'error'); }
  }

  return {
    opBadge,
    stars,
    renderAuditions,
    filterAuditions,
    openScheduleAuditionModal,
    openReviewAuditionModal,
    quickCancelAudition,
    deleteAudition,
    renderSelectionPipeline,
    setSelectionProject,
    addCompareId,
    removeCompareId,
    addCompareFromDropdown,
    updatePipelineStatus,
    renderMessages,
    filterMessages,
    openMessageModal,
    renderReports,
    renderTeam,
    openEditTeamModal,
    toggleTeamActive,
    deleteTeamMember,
    renderContractsAndExpenses,
    setFinTab,
    updateContractStatus,
    deleteContract,
    markExpensePaid,
    deleteExpense
  };
})();
