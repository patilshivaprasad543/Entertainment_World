/**
 * Shared client utilities for Entertainment World.
 */
window.EW = (() => {
  function toast(message, type = 'success') {
    let stack = document.getElementById('ew-toast-stack');
    if (!stack) {
      stack = document.createElement('div');
      stack.id = 'ew-toast-stack';
      stack.className = 'ew-toast-stack';
      document.body.appendChild(stack);
    }
    const el = document.createElement('div');
    el.className = `ew-toast ${type}`;
    el.textContent = message;
    stack.appendChild(el);
    setTimeout(() => el.remove(), 4200);
  }

  async function api(url, options = {}) {
    const res = await fetch(url, {
      credentials: 'same-origin',
      ...options
    });
    const contentType = res.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await res.json() : await res.text();
    if (!res.ok) {
      const msg = (data && data.error) || (data && data.message) || `Request failed (${res.status})`;
      const err = new Error(msg);
      err.status = res.status;
      err.data = data;
      throw err;
    }
    return data;
  }

  function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function badgeClass(value) {
    const v = String(value || '').toLowerCase();
    if (['approved', 'verified', 'active', 'paid', 'invited', 'shortlisted'].includes(v)) return 'badge-approved';
    if (['pending', 'pending review', 'applied'].includes(v)) return 'badge-pending';
    if (['rejected', 'suspended', 'not verified'].includes(v)) return 'badge-rejected';
    return 'badge-neutral';
  }

  function formatInr(val) {
    const n = Number(val || 0);
    return '₹' + n.toLocaleString('en-IN');
  }

  async function logout(redirect = '/login.html') {
    try {
      await api('/api/auth/logout', { method: 'POST' });
    } catch (_) {}
    window.location.href = redirect;
  }

  return { toast, api, esc, badgeClass, formatInr, logout };
})();
