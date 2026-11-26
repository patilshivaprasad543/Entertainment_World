(() => {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const loginForm = document.getElementById('panel-login');
    const registerForm = document.getElementById('panel-register');
    const welcomePanel = document.getElementById('welcome-panel');
    const welcomeHeading = document.getElementById('welcome-heading');
    const welcomeEmail = document.getElementById('welcome-email');
    const statusBanner = document.getElementById('status-banner');
    const signOutBtn = document.getElementById('signout-btn');

    function showStatus(message, type) {
        statusBanner.textContent = message;
        statusBanner.className = `status-banner ${type}`;
        statusBanner.hidden = false;
    }

    function clearStatus() {
        statusBanner.hidden = true;
        statusBanner.textContent = '';
    }

    function switchTab(targetTab) {
        clearStatus();
        const isLogin = targetTab === 'login';

        tabButtons.forEach((btn) => {
            const active = btn.dataset.tab === targetTab;
            btn.classList.toggle('active', active);
            btn.setAttribute('aria-selected', String(active));
        });

        loginForm.hidden = !isLogin;
        registerForm.hidden = isLogin;
        welcomePanel.hidden = true;
    }

    tabButtons.forEach((btn) => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    document.querySelectorAll('.toggle-password').forEach((btn) => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.target);
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            btn.textContent = show ? 'Hide' : 'Show';
            btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        });
    });

    async function submitAuth(url, payload, submitBtn, onSuccess) {
        clearStatus();
        submitBtn.disabled = true;

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await response.json();

            if (!response.ok || !data.success) {
                showStatus(data.message || 'Authentication failed.', 'error');
                return;
            }

            onSuccess(data);
        } catch (err) {
            showStatus('Unable to reach the server. Please try again.', 'error');
        } finally {
            submitBtn.disabled = false;
        }
    }

    loginForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            showStatus('Please enter both email and password.', 'error');
            return;
        }

        const submitBtn = document.getElementById('login-submit');
        submitAuth('/api/auth/login', { email, password }, submitBtn, (data) => {
            loginForm.hidden = true;
            registerForm.hidden = true;
            welcomeHeading.textContent = `Welcome, ${data.fullName}!`;
            welcomeEmail.textContent = data.email;
            welcomePanel.hidden = false;
            showStatus(data.message, 'success');
        });
    });

    registerForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const fullName = document.getElementById('register-name').value.trim();
        const email = document.getElementById('register-email').value.trim();
        const password = document.getElementById('register-password').value;
        const confirmPassword = document.getElementById('register-confirm').value;

        if (!fullName || !email || !password || !confirmPassword) {
            showStatus('Please fill in all fields.', 'error');
            return;
        }
        if (password.length < 6) {
            showStatus('Password must be at least 6 characters.', 'error');
            return;
        }
        if (password !== confirmPassword) {
            showStatus('Passwords do not match.', 'error');
            return;
        }

        const submitBtn = document.getElementById('register-submit');
        submitAuth('/api/auth/register', { fullName, email, password }, submitBtn, (data) => {
            registerForm.reset();
            switchTab('login');
            document.getElementById('login-email').value = data.email;
            showStatus(data.message + ' You can now sign in.', 'success');
        });
    });

    signOutBtn.addEventListener('click', () => {
        loginForm.reset();
        switchTab('login');
        showStatus('You have been signed out.', 'success');
    });
})();
