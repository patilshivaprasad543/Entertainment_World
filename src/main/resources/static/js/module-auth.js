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

    const regCategoryEl = document.getElementById('register-category');
    const regStartAgeEl = document.getElementById('register-start-age');
    const regDobEl = document.getElementById('register-dob');
    const regGenderEl = document.getElementById('register-gender');
    const regExperienceEl = document.getElementById('register-experience');

    function getTodayIso() {
        const now = new Date();
        const y = now.getFullYear();
        const m = String(now.getMonth() + 1).padStart(2, '0');
        const d = String(now.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    function calculateAgeFromDob(dobStr) {
        if (!dobStr) return null;
        const dob = new Date(dobStr + 'T00:00:00');
        if (Number.isNaN(dob.getTime())) return null;
        const today = new Date();
        let age = today.getFullYear() - dob.getFullYear();
        const monthDiff = today.getMonth() - dob.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
            age -= 1;
        }
        return age;
    }

    function getExpectedExperienceOption(yearsExp) {
        if (yearsExp <= 1) return 'Fresher (0-1 years)';
        if (yearsExp <= 3) return 'Intermediate (1-3 years)';
        if (yearsExp <= 7) return 'Experienced (3-7 years)';
        return 'Senior / Veteran (7+ years)';
    }

    function applyCategoryGenderRestriction() {
        if (!regCategoryEl || !regGenderEl) return;
        const cat = regCategoryEl.value;
        const lockedGender = cat === 'Actor' ? 'Male' : cat === 'Actress' ? 'Female' : '';

        Array.from(regGenderEl.options).forEach((opt) => {
            if (!opt.value) {
                opt.disabled = Boolean(lockedGender);
                return;
            }
            opt.disabled = lockedGender ? opt.value !== lockedGender : false;
        });

        if (lockedGender) {
            regGenderEl.value = lockedGender;
        }
    }

    function applyAgeAndExperienceRestriction() {
        if (!regDobEl || !regStartAgeEl || !regExperienceEl) return;
        const todayIso = getTodayIso();
        regDobEl.max = todayIso;

        const currentAge = calculateAgeFromDob(regDobEl.value);
        if (currentAge !== null && currentAge >= 1) {
            regStartAgeEl.max = String(currentAge);
        } else {
            regStartAgeEl.max = '100';
        }

        const startAge = Number(regStartAgeEl.value);
        if (currentAge !== null && startAge >= 1 && startAge <= currentAge) {
            const yearsExp = currentAge - startAge;
            const expectedExp = getExpectedExperienceOption(yearsExp);
            Array.from(regExperienceEl.options).forEach((opt) => {
                if (!opt.value) {
                    opt.disabled = true;
                    return;
                }
                opt.disabled = opt.value !== expectedExp;
            });
            regExperienceEl.value = expectedExp;
        } else {
            Array.from(regExperienceEl.options).forEach((opt) => {
                opt.disabled = false;
            });
        }
    }

    if (mode === 'professional') {
        if (regDobEl) regDobEl.max = getTodayIso();
        regCategoryEl?.addEventListener('change', applyCategoryGenderRestriction);
        regDobEl?.addEventListener('change', applyAgeAndExperienceRestriction);
        regStartAgeEl?.addEventListener('input', applyAgeAndExperienceRestriction);
    }

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
            if (professionalCategory === 'Actor' && gender !== 'Male') {
                showStatus('Category "Actor" requires Gender to be Male.', 'error');
                return;
            }
            if (professionalCategory === 'Actress' && gender !== 'Female') {
                showStatus('Category "Actress" requires Gender to be Female.', 'error');
                return;
            }
            const currentAge = calculateAgeFromDob(dateOfBirth);
            if (currentAge === null || currentAge < 1) {
                showStatus('Please enter a valid Date of Birth in the past.', 'error');
                return;
            }
            if (professionalCategory === 'Child Artist' && currentAge >= 18) {
                showStatus('Child Artist category is restricted to age under 18.', 'error');
                return;
            }
            if (actingStartAge > currentAge) {
                showStatus(`Acting started age (${actingStartAge}) cannot be greater than your current age (${currentAge}).`, 'error');
                return;
            }
            const expectedExp = getExpectedExperienceOption(currentAge - actingStartAge);
            if (experienceCategory !== expectedExp) {
                applyAgeAndExperienceRestriction();
                showStatus(`Based on your age (${currentAge}) and starting age (${actingStartAge}), Experience Category must be "${expectedExp}".`, 'error');
                return;
            }
            if (!photoInput.files || photoInput.files.length === 0) {
                showStatus('Please upload a profile photo (mandatory).', 'error');
                return;
            }
            if (!portfolioOrBio) {
                showStatus('Please enter your Portfolio Link / Short Bio.', 'error');
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

    // Professional Dashboard Video Tour Modal (Talentrack-inspired 16:9 HTML5 Video Stream)
    const tourModal = document.getElementById('tour-modal');
    const openTourBtn = document.getElementById('open-video-tour');
    const closeTourBtn = document.getElementById('tour-modal-close');
    const tourCanvas = document.getElementById('tour-canvas');
    const tourVideo = document.getElementById('tour-video');
    const tourChaptersEl = document.getElementById('tour-chapters');
    const tourChapterPill = document.getElementById('tour-chapter-pill');
    const tourTimeLabel = document.getElementById('tour-time-label');
    const tourProgressTrack = document.getElementById('tour-progress-track');
    const tourProgressBar = document.getElementById('tour-progress-bar');
    const tourPlayBtn = document.getElementById('tour-play-btn');
    const tourPrevBtn = document.getElementById('tour-prev-btn');
    const tourNextBtn = document.getElementById('tour-next-btn');
    const tourSoundBtn = document.getElementById('tour-sound-btn');
    const tourFullscreenBtn = document.getElementById('tour-fullscreen-btn');
    const tourDemoBtn = document.getElementById('tour-try-demo');

    const TOUR_CHAPTERS = [
        {
            tag: 'STEP 01 • DIGITAL PORTFOLIO',
            imageUrl: '/images/tour/tour-step1.jpg',
            title: 'Create Your Verified Talent Portfolio',
            subtitle: 'Register as Actor, Model, Singer, Dancer, Anchor, or Influencer. Upload headshots, showreels & audio clips.',
            narration: 'Step one: Create your verified talent portfolio. Register as an actor, model, singer, dancer, anchor, or influencer, and upload your headshots, showreels, and audio clips.',
            cards: [
                { label: 'Verified Profile', desc: 'Shareable digital resume' },
                { label: '5–50 Showreels', desc: 'Video & monologue reels' },
                { label: 'Smart Matching', desc: 'Matched by skills & city' }
            ]
        },
        {
            tag: 'STEP 02 • DISCOVER & INVITATIONS',
            imageUrl: '/images/tour/tour-step2.jpg',
            title: 'Audition, Event & Brand Invitations',
            subtitle: 'Browse open casting calls and receive direct invitations for films, live events & brand campaigns.',
            narration: 'Step two: Discover audition, event, and brand invitations. Browse open casting calls and receive direct invitations for films, live events, and brand campaigns.',
            cards: [
                { label: 'Audition Invites', desc: 'In-person, Self-Tape, Live' },
                { label: 'Event Invites', desc: 'Award nights & premieres' },
                { label: 'Brand Collabs', desc: 'Direct offers & counter-bids' }
            ]
        },
        {
            tag: 'STEP 03 • APPLY & TRACK STATUS',
            imageUrl: '/images/tour/tour-step3.jpg',
            title: 'One-Click Apply & Online Auditions',
            subtitle: 'Submit self-tapes directly and track real-time status: Viewed, Shortlisted, or Audition Scheduled.',
            narration: 'Step three: Apply in one click and track your status. Submit self-tapes directly and follow real-time updates from viewed to shortlisted or audition scheduled.',
            cards: [
                { label: 'One-Click Apply', desc: 'Instant casting submission' },
                { label: 'Live Status', desc: 'Viewed → Shortlisted' },
                { label: 'Slot Confirm', desc: 'Confirm or reschedule fast' }
            ]
        },
        {
            tag: 'STEP 04 • NETWORK & PRO TOOLS',
            imageUrl: '/images/tour/tour-step4.jpg',
            title: 'Mutual Network Chat & Career Tools',
            subtitle: 'Connect with casting directors & producers—chat unlocks after both ends approve. Plus AI Portfolio & Media Kit.',
            narration: 'Step four: Connect with casting directors and producers in your mutual network. Direct chat unlocks after both sides approve, along with AI portfolio and media kit tools.',
            cards: [
                { label: '2-Way Approval', desc: 'Mutual consent before chat' },
                { label: 'Direct Chat', desc: 'Message verified recruiters' },
                { label: 'Pro Toolkit', desc: 'AI Practice & Media Kit' }
            ]
        }
    ];

    const tourLoadedImages = TOUR_CHAPTERS.map((ch) => {
        const img = new Image();
        img.src = ch.imageUrl;
        return img;
    });

    const CHAPTER_DURATION = 7; // seconds per chapter (28s total)
    const TOTAL_DURATION = TOUR_CHAPTERS.length * CHAPTER_DURATION;
    let tourGlobalTime = 0;
    let tourPlaying = false;
    let tourRafId = null;
    let lastFrameTs = 0;
    let streamAttached = false;
    let tourSoundEnabled = true;
    let lastSpokenChapter = -1;

    function stopTourVoiceover() {
        if ('speechSynthesis' in window) {
            window.speechSynthesis.cancel();
        }
        lastSpokenChapter = -1;
    }

    function speakTourChapter(chIdx, force = false) {
        if (!tourSoundEnabled || !tourPlaying) return;
        if (!('speechSynthesis' in window)) return;
        if (!force && chIdx === lastSpokenChapter) return;
        const ch = TOUR_CHAPTERS[chIdx];
        if (!ch || !ch.narration) return;

        lastSpokenChapter = chIdx;
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(ch.narration);
        utterance.lang = 'en-US';
        utterance.rate = 1.02;
        utterance.pitch = 1.0;

        const voices = window.speechSynthesis.getVoices();
        const preferred = voices.find((v) => /en[-_](IN|US|GB)/i.test(v.lang)) || voices.find((v) => /^en/i.test(v.lang));
        if (preferred) utterance.voice = preferred;

        window.speechSynthesis.speak(utterance);
    }

    function updateSoundBtn() {
        if (!tourSoundBtn) return;
        tourSoundBtn.textContent = tourSoundEnabled ? 'Sound: On' : 'Sound: Off';
        tourSoundBtn.setAttribute('aria-pressed', String(tourSoundEnabled));
    }

    function drawRoundRect(ctx, x, y, w, h, r) {
        ctx.beginPath();
        ctx.roundRect(x, y, w, h, r);
    }

    function drawTourVideoFrame(ts) {
        if (!tourCanvas) return;
        const ctx = tourCanvas.getContext('2d');
        if (!ctx) return;

        const W = tourCanvas.width;
        const H = tourCanvas.height;
        const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
        const localT = tourGlobalTime - chIdx * CHAPTER_DURATION;
        const ease = Math.min(1, localT / 0.65);
        const slideY = (1 - ease) * 22;
        const ch = TOUR_CHAPTERS[chIdx];

        // Background: realistic photo with Ken Burns slow zoom + cinematic scrim
        const bgImg = tourLoadedImages[chIdx];
        if (bgImg && bgImg.complete && bgImg.naturalWidth > 0) {
            const zoom = 1 + (localT / CHAPTER_DURATION) * 0.09;
            const panX = (chIdx % 2 === 0 ? -1 : 1) * (localT / CHAPTER_DURATION) * 18;
            const drawW = W * zoom;
            const drawH = H * zoom;
            const drawX = (W - drawW) / 2 + panX;
            const drawY = (H - drawH) / 2;
            ctx.drawImage(bgImg, drawX, drawY, drawW, drawH);

            const scrim = ctx.createLinearGradient(0, 0, W, H);
            scrim.addColorStop(0, 'rgba(9, 11, 18, 0.88)');
            scrim.addColorStop(0.55, 'rgba(15, 12, 24, 0.72)');
            scrim.addColorStop(1, 'rgba(9, 11, 18, 0.86)');
            ctx.fillStyle = scrim;
            ctx.fillRect(0, 0, W, H);
        } else {
            const bg = ctx.createLinearGradient(0, 0, W, H);
            bg.addColorStop(0, '#0b0d17');
            bg.addColorStop(0.55, '#161124');
            bg.addColorStop(1, '#121526');
            ctx.fillStyle = bg;
            ctx.fillRect(0, 0, W, H);
        }

        // Animated glow orbs
        const pulse = Math.sin((ts || 0) * 0.002) * 18;
        const glow1 = ctx.createRadialGradient(220 + pulse, 140, 10, 220 + pulse, 140, 320);
        glow1.addColorStop(0, 'rgba(225, 29, 72, 0.24)');
        glow1.addColorStop(1, 'rgba(225, 29, 72, 0)');
        ctx.fillStyle = glow1;
        ctx.fillRect(0, 0, W, H);

        const glow2 = ctx.createRadialGradient(1040 - pulse, 560, 10, 1040 - pulse, 560, 340);
        glow2.addColorStop(0, 'rgba(212, 168, 83, 0.20)');
        glow2.addColorStop(1, 'rgba(212, 168, 83, 0)');
        ctx.fillStyle = glow2;
        ctx.fillRect(0, 0, W, H);

        // Top brand bar inside video
        ctx.fillStyle = '#e11d48';
        drawRoundRect(ctx, 64, 48, 42, 42, 11);
        ctx.fill();
        ctx.fillStyle = '#fbbf24';
        ctx.font = 'bold 22px system-ui, sans-serif';
        ctx.fillText('★', 76, 77);

        ctx.fillStyle = '#ffffff';
        ctx.font = '700 22px system-ui, sans-serif';
        ctx.fillText('Entertainment World', 120, 68);
        ctx.fillStyle = '#d4a853';
        ctx.font = '700 12px system-ui, sans-serif';
        ctx.fillText('PLATFORM VIDEO TOUR • TALENT & CASTING NETWORK', 120, 88);

        // Step pill
        ctx.fillStyle = 'rgba(225, 29, 72, 0.18)';
        drawRoundRect(ctx, 64, 130 + slideY, 290, 34, 17);
        ctx.fill();
        ctx.strokeStyle = 'rgba(225, 29, 72, 0.55)';
        ctx.lineWidth = 1.2;
        ctx.stroke();
        ctx.fillStyle = '#fda4af';
        ctx.font = '700 14px system-ui, sans-serif';
        ctx.fillText(ch.tag, 82, 152 + slideY);

        // Title & Subtitle
        ctx.globalAlpha = ease;
        ctx.fillStyle = '#ffffff';
        ctx.font = '700 46px system-ui, sans-serif';
        ctx.fillText(ch.title, 64, 224 + slideY);

        ctx.fillStyle = '#cbd5e1';
        ctx.font = '400 22px system-ui, sans-serif';
        const words = ch.subtitle.split(' ');
        let line1 = '';
        let line2 = '';
        words.forEach((w) => {
            if ((line1 + w).length < 66) line1 += w + ' ';
            else line2 += w + ' ';
        });
        ctx.fillText(line1.trim(), 64, 272 + slideY);
        if (line2.trim()) ctx.fillText(line2.trim(), 64, 304 + slideY);

        // 3 Feature Cards
        const cardW = 368;
        const cardH = 190;
        const gap = 24;
        const startX = 64;
        const cardY = 365 + slideY;

        ch.cards.forEach((c, idx) => {
            const cx = startX + idx * (cardW + gap);
            ctx.fillStyle = 'rgba(255, 255, 255, 0.06)';
            drawRoundRect(ctx, cx, cardY, cardW, cardH, 16);
            ctx.fill();
            ctx.strokeStyle = idx === 1 ? 'rgba(212, 168, 83, 0.55)' : 'rgba(255, 255, 255, 0.12)';
            ctx.lineWidth = 1.5;
            ctx.stroke();

            ctx.fillStyle = '#e11d48';
            ctx.font = '700 18px system-ui, sans-serif';
            ctx.fillText(`0${idx + 1}`, cx + 26, cardY + 42);

            ctx.fillStyle = '#fbbf24';
            ctx.font = '700 26px system-ui, sans-serif';
            ctx.fillText(c.label, cx + 26, cardY + 96);

            ctx.fillStyle = '#cbd5e1';
            ctx.font = '400 19px system-ui, sans-serif';
            ctx.fillText(c.desc, cx + 26, cardY + 136);
        });

        // Bottom timeline bar inside video
        ctx.globalAlpha = 1;
        ctx.fillStyle = 'rgba(255,255,255,0.12)';
        drawRoundRect(ctx, 64, 640, W - 128, 6, 3);
        ctx.fill();
        ctx.fillStyle = '#e11d48';
        drawRoundRect(ctx, 64, 640, (W - 128) * Math.min(1, tourGlobalTime / TOTAL_DURATION), 6, 3);
        ctx.fill();

        ctx.fillStyle = '#94a3b8';
        ctx.font = '500 15px system-ui, sans-serif';
        ctx.fillText('actors-login.html • Professional Portal Walkthrough', 64, 678);
        ctx.fillText(`${chIdx + 1} / ${TOUR_CHAPTERS.length}`, W - 104, 678);
    }

    function updateTourChrome() {
        const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
        if (tourChapterPill) tourChapterPill.textContent = `Chapter ${chIdx + 1} of ${TOUR_CHAPTERS.length}`;
        if (tourTimeLabel) {
            const pad = (n) => String(Math.floor(n)).padStart(2, '0');
            tourTimeLabel.textContent = `00:${pad(tourGlobalTime)} / 00:${pad(TOTAL_DURATION)}`;
        }
        if (tourProgressBar) {
            tourProgressBar.style.width = `${Math.min(100, (tourGlobalTime / TOTAL_DURATION) * 100)}%`;
        }
        if (tourPlayBtn) {
            tourPlayBtn.textContent = tourPlaying ? 'Pause' : 'Play';
        }
        if (tourChaptersEl) {
            tourChaptersEl.innerHTML = TOUR_CHAPTERS.map((item, idx) => `
                <button type="button" class="tour-chapter-btn ${idx === chIdx ? 'active' : ''}" data-tour-chapter="${idx}">
                    <strong>${item.title}</strong>
                    <span>${item.cards[0].label} • ${item.cards[1].label}</span>
                </button>
            `).join('');
        }
    }

    function attachVideoStream() {
        if (streamAttached || !tourCanvas || !tourVideo) return;
        if (typeof tourCanvas.captureStream === 'function') {
            const stream = tourCanvas.captureStream(30);
            tourVideo.srcObject = stream;
            tourVideo.play().catch(() => {});
            streamAttached = true;
        } else {
            tourCanvas.hidden = false;
            tourVideo.hidden = true;
        }
    }

    function tickTourVideo(ts) {
        if (!lastFrameTs) lastFrameTs = ts;
        const dt = Math.min(0.1, (ts - lastFrameTs) / 1000);
        lastFrameTs = ts;

        if (tourPlaying) {
            tourGlobalTime += dt;
            if (tourGlobalTime >= TOTAL_DURATION) {
                tourGlobalTime = 0;
            }
        }
        drawTourVideoFrame(ts);
        updateTourChrome();
        if (tourPlaying) {
            const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
            if (chIdx !== lastSpokenChapter) {
                speakTourChapter(chIdx);
            }
        }
        if (tourModal && !tourModal.hidden) {
            tourRafId = requestAnimationFrame(tickTourVideo);
        }
    }

    function stopTourVideo() {
        tourPlaying = false;
        stopTourVoiceover();
        if (tourRafId) {
            cancelAnimationFrame(tourRafId);
            tourRafId = null;
        }
    }

    function closeTourModal() {
        stopTourVideo();
        if (tourVideo) tourVideo.pause();
        if (tourModal) tourModal.hidden = true;
    }

    openTourBtn?.addEventListener('click', () => {
        if (!tourModal) return;
        tourGlobalTime = 0;
        tourPlaying = true;
        lastFrameTs = 0;
        lastSpokenChapter = -1;
        tourModal.hidden = false;
        drawTourVideoFrame(0);
        attachVideoStream();
        if (tourVideo && tourVideo.paused) tourVideo.play().catch(() => {});
        updateSoundBtn();
        updateTourChrome();
        speakTourChapter(0, true);
        if (!tourRafId) tourRafId = requestAnimationFrame(tickTourVideo);
    });

    closeTourBtn?.addEventListener('click', closeTourModal);
    tourModal?.addEventListener('click', (e) => {
        if (e.target === tourModal) closeTourModal();
    });

    tourPlayBtn?.addEventListener('click', () => {
        tourPlaying = !tourPlaying;
        if (tourVideo) {
            if (tourPlaying) tourVideo.play().catch(() => {});
            else tourVideo.pause();
        }
        if (tourPlaying) {
            const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
            speakTourChapter(chIdx, true);
        } else {
            stopTourVoiceover();
        }
        updateTourChrome();
    });

    tourSoundBtn?.addEventListener('click', () => {
        tourSoundEnabled = !tourSoundEnabled;
        updateSoundBtn();
        if (tourSoundEnabled && tourPlaying) {
            const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
            speakTourChapter(chIdx, true);
        } else {
            stopTourVoiceover();
        }
    });

    tourPrevBtn?.addEventListener('click', () => {
        const chIdx = Math.floor(tourGlobalTime / CHAPTER_DURATION);
        const prevIdx = (chIdx - 1 + TOUR_CHAPTERS.length) % TOUR_CHAPTERS.length;
        tourGlobalTime = prevIdx * CHAPTER_DURATION;
        drawTourVideoFrame(performance.now());
        updateTourChrome();
        speakTourChapter(prevIdx, true);
    });

    tourNextBtn?.addEventListener('click', () => {
        const chIdx = Math.floor(tourGlobalTime / CHAPTER_DURATION);
        const nextIdx = (chIdx + 1) % TOUR_CHAPTERS.length;
        tourGlobalTime = nextIdx * CHAPTER_DURATION;
        drawTourVideoFrame(performance.now());
        updateTourChrome();
        speakTourChapter(nextIdx, true);
    });

    tourProgressTrack?.addEventListener('click', (e) => {
        const rect = tourProgressTrack.getBoundingClientRect();
        const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
        tourGlobalTime = ratio * TOTAL_DURATION;
        const chIdx = Math.min(TOUR_CHAPTERS.length - 1, Math.floor(tourGlobalTime / CHAPTER_DURATION));
        drawTourVideoFrame(performance.now());
        updateTourChrome();
        speakTourChapter(chIdx, true);
    });

    tourFullscreenBtn?.addEventListener('click', () => {
        const target = tourVideo?.hidden ? tourCanvas : tourVideo;
        target?.requestFullscreen?.().catch(() => {});
    });

    tourChaptersEl?.addEventListener('click', (e) => {
        const btn = e.target.closest('[data-tour-chapter]');
        if (!btn) return;
        const idx = Number(btn.dataset.tourChapter) || 0;
        tourGlobalTime = idx * CHAPTER_DURATION;
        drawTourVideoFrame(performance.now());
        updateTourChrome();
        speakTourChapter(idx, true);
    });

    tourDemoBtn?.addEventListener('click', () => {
        closeTourModal();
        switchTab('login');
        const emailInput = document.getElementById('login-email');
        const passInput = document.getElementById('login-password');
        if (emailInput) emailInput.value = 'actor@entertainmentworld.com';
        if (passInput) passInput.value = 'actor123';
        loginForm?.requestSubmit();
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeTermsModal();
            closeTourModal();
        }
    });
})();
