(() => {
    const role = document.body.dataset.role;
    const loginUrl = document.body.dataset.login;
    const sessionKey = `ew_session_${role}`;

    const raw = sessionStorage.getItem(sessionKey);
    if (!raw) {
        window.location.href = loginUrl;
        return;
    }

    try {
        const user = JSON.parse(raw);
        if (user.role !== role) {
            window.location.href = loginUrl;
            return;
        }
        document.getElementById('user-name').textContent = user.fullName;
        document.getElementById('user-email').textContent = user.email;

        const categoryEl = document.getElementById('professional-category');
        if (categoryEl && user.professionalCategory) {
            categoryEl.textContent = user.professionalCategory;
        }

        const startAgeEl = document.getElementById('acting-start-age');
        if (startAgeEl && user.actingStartAge) {
            startAgeEl.textContent = `Started acting at age ${user.actingStartAge}`;
        }

        const photoEl = document.getElementById('profile-photo');
        if (photoEl && user.profilePhotoUrl) {
            photoEl.src = user.profilePhotoUrl;
            photoEl.hidden = false;
        }
    } catch (err) {
        sessionStorage.removeItem(sessionKey);
        window.location.href = loginUrl;
        return;
    }

    document.getElementById('signout-btn').addEventListener('click', () => {
        sessionStorage.removeItem(sessionKey);
        window.location.href = loginUrl;
    });
})();
