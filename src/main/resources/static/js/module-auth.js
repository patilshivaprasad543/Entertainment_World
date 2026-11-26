(() => {
    const role = document.body.dataset.role;
    const apiBase = document.body.dataset.endpoint;
    const mode = document.body.dataset.mode || 'standard';

    const tabButtons = document.querySelectorAll('.tab-btn');
    const loginForm = document.getElementById('panel-login');
    const registerForm = document.getElementById('panel-register');
    const statusBanner = document.getElementById('status-banner');
    const photoInput = document.getElementById('register-photo');
    const photoPreview = document.getElementById('photo-preview');

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
        if (!registerForm) return;
        clearStatus();
        const isLogin = targetTab === 'login';
        tabButtons.forEach((btn) => {
            const active = btn.dataset.tab === targetTab;
            btn.classList.toggle('active', active);
            btn.setAttribute('aria-selected', String(active));
        });
        loginForm.hidden = !isLogin;
        registerForm.hidden = isLogin;
    }

    tabButtons.forEach((btn) => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    if (photoInput && photoPreview) {
        photoInput.addEventListener('change', () => {
            const file = photoInput.files && photoInput.files[0];
            if (!file) {
                photoPreview.hidden = true;
                photoPreview.src = '';
                return;
            }
            photoPreview.src = URL.createObjectURL(file);
            photoPreview.hidden = false;
        });
    }

    document.querySelectorAll('.toggle-password').forEach((btn) => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.target);
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            btn.textContent = show ? 'Hide' : 'Show';
            btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        });
    });

    async function sendRequest(url, options, submitBtn, onSuccess) {
        clearStatus();
        submitBtn.disabled = true;

        try {
            const response = await fetch(url, options);
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
        sendRequest(
            `${apiBase}/login`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            },
            submitBtn,
            (data) => {
                const photoStorageKey = `ew_profile_photo_${role}_${data.email}`;
                const savedPhoto = data.profilePhotoUrl || localStorage.getItem(photoStorageKey) || '';

                sessionStorage.setItem(`ew_session_${role}`, JSON.stringify({
                    fullName: data.fullName,
                    email: data.email,
                    role: data.role,
                    professionalCategory: data.professionalCategory,
                    actingStartAge: data.actingStartAge,
                    dateOfBirth: data.dateOfBirth,
                    experienceCategory: data.experienceCategory,
                    gender: data.gender,
                    profilePhotoUrl: savedPhoto
                }));
                showStatus(data.message, 'success');
                window.location.href = data.redirectUrl;
            }
        );
    });

    if (!registerForm) return;

    registerForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const fullName = document.getElementById('register-name').value.trim();
        const email = document.getElementById('register-email').value.trim();
        const password = document.getElementById('register-password').value;
        const confirmPassword = document.getElementById('register-confirm').value;

        if (!fullName || !email || !password || !confirmPassword) {
            showStatus('Please fill in all required fields.', 'error');
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

        const registerTerms = document.getElementById('register-terms');
        if (registerTerms && !registerTerms.checked) {
            showStatus('Please accept the Terms & Conditions to continue.', 'error');
            return;
        }

        const submitBtn = document.getElementById('register-submit');

        if (mode === 'professional') {
            const professionalCategory = document.getElementById('register-category').value;
            const actingStartAge = Number(document.getElementById('register-start-age').value);
            const dateOfBirth = document.getElementById('register-dob')?.value || '';
            const experienceCategory = document.getElementById('register-experience')?.value || '';
            const gender = document.getElementById('register-gender')?.value || '';
            const phone = document.getElementById('register-phone').value.trim();
            const city = document.getElementById('register-city').value.trim();
            const portfolioOrBio = document.getElementById('register-bio').value.trim();

            if (!professionalCategory || !phone || !city) {
                showStatus('Please select your category and enter phone & city.', 'error');
                return;
            }
            if (!dateOfBirth || !experienceCategory || !gender) {
                showStatus('Please provide your date of birth, experience category, and gender.', 'error');
                return;
            }
            if (!actingStartAge || actingStartAge < 1 || actingStartAge > 100) {
                showStatus('Please enter a valid acting started age (1-100).', 'error');
                return;
            }
            if (!photoInput.files || photoInput.files.length === 0) {
                showStatus('Please upload a profile photo.', 'error');
                return;
            }

            const formData = new FormData();
            formData.append('fullName', fullName);
            formData.append('email', email);
            formData.append('password', password);
            formData.append('professionalCategory', professionalCategory);
            formData.append('actingStartAge', String(actingStartAge));
            formData.append('dateOfBirth', dateOfBirth);
            formData.append('experienceCategory', experienceCategory);
            formData.append('gender', gender);
            formData.append('phone', phone);
            formData.append('city', city);
            formData.append('portfolioOrBio', portfolioOrBio);
            formData.append('photoFile', photoInput.files[0]);

            sendRequest(
                `${apiBase}/register`,
                {
                    method: 'POST',
                    body: formData
                },
                submitBtn,
                (data) => {
                    registerForm.reset();
                    if (photoPreview) {
                        photoPreview.hidden = true;
                        photoPreview.src = '';
                    }
                    switchTab('login');
                    document.getElementById('login-email').value = data.email;
                    showStatus(`${data.message} You can now sign in.`, 'success');
                }
            );
            return;
        }

        if (mode === 'document-verification') {
            const organizationName = document.getElementById('register-org').value.trim();
            const documentType = document.getElementById('register-doc-type').value;
            const documentNumber = document.getElementById('register-doc-number').value.trim();
            const fileInput = document.getElementById('register-doc-file');

            if (!organizationName || !documentType || !documentNumber) {
                showStatus('Please enter organization name, document type, and document number.', 'error');
                return;
            }
            if (!fileInput.files || fileInput.files.length === 0) {
                showStatus('Please upload a verification document file.', 'error');
                return;
            }

            const formData = new FormData();
            formData.append('fullName', fullName);
            formData.append('email', email);
            formData.append('password', password);
            formData.append('organizationName', organizationName);
            formData.append('documentType', documentType);
            formData.append('documentNumber', documentNumber);
            formData.append('documentFile', fileInput.files[0]);

            sendRequest(
                `${apiBase}/register`,
                {
                    method: 'POST',
                    body: formData
                },
                submitBtn,
                (data) => {
                    registerForm.reset();
                    switchTab('login');
                    document.getElementById('login-email').value = data.email;
                    showStatus(data.message, 'success');
                }
            );
        }
    });

    // Terms & Conditions Modal
    const termsModal = document.getElementById('terms-modal');
    const termsCloseBtn = document.getElementById('terms-modal-close');
    const termsAcceptBtn = document.getElementById('terms-modal-accept');

    function closeTermsModal() {
        if (termsModal) termsModal.hidden = true;
    }

    document.querySelectorAll('[data-open-terms]').forEach((btn) => {
        btn.addEventListener('click', () => {
            if (termsModal) termsModal.hidden = false;
        });
    });

    termsCloseBtn?.addEventListener('click', closeTermsModal);
    termsAcceptBtn?.addEventListener('click', () => {
        const regTerms = document.getElementById('register-terms');
        if (regTerms) regTerms.checked = true;
        closeTermsModal();
    });
    termsModal?.addEventListener('click', (e) => {
        if (e.target === termsModal) closeTermsModal();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeTermsModal();
    });
})();
