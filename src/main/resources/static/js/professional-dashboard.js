(() => {
    const STORAGE_KEY = 'ew_professional_dashboard_state_v1';
    const SESSION_KEY = 'ew_session_ACTOR';

    const PLAN_LIMITS = {
        free: {
            maxVideos: 5,
            maxApps: 1,
            maxWishlist: 5,
            advancedFilters: false,
            aiPortfolio: false,
            analytics: false,
            aiPractice: 'Locked',
            prioritySupport: false,
            mediaKit: false,
            priceEstimator: false,
            teamCampaigns: false,
            perksLabel: '5 Portfolio Files • 1 Audition/Event Application'
        },
        premium: {
            maxVideos: 20,
            maxApps: Infinity,
            maxWishlist: Infinity,
            advancedFilters: true,
            aiPortfolio: true,
            analytics: true,
            aiPractice: 'Limited',
            prioritySupport: false,
            mediaKit: true,
            priceEstimator: true,
            teamCampaigns: false,
            perksLabel: '20 Portfolio Files • AI Builder • Analytics • Media Kit'
        },
        pro: {
            maxVideos: 50,
            maxApps: Infinity,
            maxWishlist: Infinity,
            advancedFilters: true,
            aiPortfolio: true,
            analytics: true,
            aiPractice: 'Expanded',
            prioritySupport: true,
            mediaKit: true,
            priceEstimator: true,
            teamCampaigns: true,
            perksLabel: '50 Portfolio Files • Expanded AI • Team Campaigns • Priority Support'
        }
    };

    const PLANS = [
        {
            id: 'free',
            name: 'Basic Plan',
            shortLabel: 'Free',
            price: 'Free',
            period: '₹0 / month',
            badge: '',
            cardClass: '',
            buttonClass: 'ew-btn-outline',
            description: 'Create your profile, manage availability, and start applying.',
            features: [
                'Upload up to 5 portfolio videos/files',
                '1 Audition / Event application',
                'Availability Calendar included',
                'Influencer Profile & Brand Requests'
            ]
        },
        {
            id: 'premium',
            name: 'Premium Plan',
            shortLabel: '₹199',
            price: '₹199',
            period: '/ month',
            badge: 'Most Popular',
            cardClass: 'popular',
            buttonClass: 'ew-btn-primary',
            description: 'AI Portfolio Builder, Analytics, Digital Media Kit, and Price Estimator.',
            features: [
                'Upload up to 20 portfolio videos/files',
                'Advanced Job & Event Filters',
                'AI Portfolio Builder & Analytics',
                'Digital Media Kit & Price Estimator',
                'AI Audition Practice (Limited)'
            ]
        },
        {
            id: 'pro',
            name: 'Pro Plan',
            shortLabel: '₹399',
            price: '₹399',
            period: '/ month',
            badge: 'Best Value',
            cardClass: 'pro-tier',
            buttonClass: 'ew-btn-gold',
            description: '50 portfolio files, Expanded AI Audition Practice, Team Campaigns & Priority Support.',
            features: [
                'Upload up to 50 portfolio videos/files',
                'AI Audition Practice (Expanded)',
                'Influencer Team Campaigns unlocked',
                'Priority Support & Detailed Reports',
                'Everything in Premium (₹199)'
            ]
        }
    ];

    function makeActorAvatar(initials, bg1, bg2) {
        const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160">
            <defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="${bg1}"/><stop offset="100%" stop-color="${bg2}"/></linearGradient></defs>
            <rect width="160" height="160" fill="url(#g)"/>
            <circle cx="80" cy="58" r="28" fill="rgba(255,255,255,0.22)"/>
            <path d="M32 146c6-28 28-42 48-42s42 14 48 42" fill="rgba(255,255,255,0.18)"/>
            <text x="80" y="92" text-anchor="middle" font-family="Inter,Segoe UI,sans-serif" font-size="38" font-weight="700" fill="#ffffff">${initials}</text>
        </svg>`;
        return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
    }

    const DEFAULT_STATE = {
        currentPlan: 'free',
        auditionsCount: 3,
        categories: [
            { id: 'cat-actors', name: 'Actors', description: 'Lead, supporting, theatre, and web-series performers.', followers: 18420 },
            { id: 'cat-directors', name: 'Directors', description: 'Film, commercial, and episodic directors.', followers: 6340 },
            { id: 'cat-editors', name: 'Video Editors', description: 'Post-production, trailer, and colorist specialists.', followers: 9120 },
            { id: 'cat-dop', name: 'Cinematographers', description: 'Directors of photography and camera operators.', followers: 5480 },
            { id: 'cat-influencers', name: 'Brand Influencers', description: 'Digital creators, lifestyle, and tech reviewers.', followers: 14900 },
            { id: 'cat-voice', name: 'Voiceover Artists', description: 'Dubbing, animation, audiobook, and ad voice talent.', followers: 4720 },
            { id: 'cat-hosts', name: 'Event Hosts & Emcees', description: 'Live stage hosts, festival anchors, and moderators.', followers: 3890 },
            { id: 'cat-casting', name: 'Casting Agencies', description: 'Verified casting studios and talent scouts.', followers: 7650 }
        ],
        followedCategoryIds: ['cat-actors', 'cat-directors', 'cat-casting'],
        otherActors: [
            { id: 'act-1', name: 'Aarav Mehta', category: 'Lead Actor • Web Series', city: 'Mumbai', planId: 'pro', credits: '14 Credits • Verified Showreel', following: true, photoUrl: makeActorAvatar('AM', '#1f2937', '#b45309') },
            { id: 'act-2', name: 'Diya Sharma', category: 'Screen & Theatre Actor', city: 'Delhi NCR', planId: 'pro', credits: '11 Credits • Award Nominee', following: false, photoUrl: makeActorAvatar('DS', '#312e81', '#db2777') },
            { id: 'act-3', name: 'Kabir Malhotra', category: 'Commercial & Voice Actor', city: 'Bengaluru', planId: 'premium', credits: '9 Credits • 4 Brand Shoots', following: false, photoUrl: makeActorAvatar('KM', '#0f766e', '#1d4ed8') },
            { id: 'act-4', name: 'Neha Kulkarni', category: 'Character Actor & Host', city: 'Pune', planId: 'premium', credits: '7 Credits • Bilingual', following: true, photoUrl: makeActorAvatar('NK', '#7c2d12', '#d97706') },
            { id: 'act-5', name: 'Rishi Verma', category: 'Indie Film Actor', city: 'Hyderabad', planId: 'free', credits: '3 Short Films', following: false, photoUrl: makeActorAvatar('RV', '#374151', '#059669') },
            { id: 'act-6', name: 'Tara Singhania', category: 'Model & Screen Performer', city: 'Mumbai', planId: 'free', credits: '2 Music Videos', following: false, photoUrl: makeActorAvatar('TS', '#4c1d95', '#e11d48') }
        ],
        earningsText: '₹25K',
        applications: [
            {
                id: 'app-1',
                title: 'Lead Actor – Web Series',
                company: 'RedFrame Originals',
                location: 'Mumbai',
                appliedDate: '2 days ago',
                status: 'Under Review',
                badgeClass: 'review',
                compensation: '₹1,20,000 / episode'
            },
            {
                id: 'app-2',
                title: 'Video Editor – Production',
                company: 'NovaCut Studios',
                location: 'Bengaluru / Remote',
                appliedDate: '5 days ago',
                status: 'Shortlisted',
                badgeClass: 'shortlisted',
                compensation: '₹45,000 / project'
            },
            {
                id: 'app-3',
                title: 'Commercial Brand Shoot',
                company: 'Aura Lifestyle',
                location: 'Delhi NCR',
                appliedDate: '1 week ago',
                status: 'Audition Scheduled',
                badgeClass: 'audition',
                compensation: '₹60,000'
            },
            {
                id: 'app-4',
                title: 'Voiceover Artist – Documentary',
                company: 'Horizon Media',
                location: 'Remote',
                appliedDate: '2 weeks ago',
                status: 'Shortlisted',
                badgeClass: 'shortlisted',
                compensation: '₹25,000'
            }
        ],
        opportunities: [
            {
                id: 'opp-1',
                title: 'Supporting Role – Feature Film',
                projectName: 'Monsoon Echoes (Bilingual Drama)',
                category: 'Acting',
                roleCategory: 'Supporting Actor',
                ageRange: '24–32 yrs',
                gender: 'Any',
                duration: '18 Shoot Days (Jan 2027)',
                company: 'SilverScreen Pictures',
                location: 'Mumbai',
                budget: '₹1,80,000',
                deadline: 'Closes in 4 days',
                description: 'Looking for a versatile screen actor for a bilingual drama shooting across Mumbai.'
            },
            {
                id: 'opp-2',
                title: 'Cinematographer – Music Video',
                projectName: 'Tides of Goa (Single Track)',
                category: 'Production',
                roleCategory: 'Director of Photography',
                ageRange: '22–40 yrs',
                gender: 'Any',
                duration: '3 Days',
                company: 'Pulse Records',
                location: 'Goa',
                budget: '₹75,000',
                deadline: 'Closes in 6 days',
                description: 'Coastal music video shoot requiring anamorphic lens experience and dynamic lighting.'
            },
            {
                id: 'opp-3',
                title: 'Influencer Campaign – Tech Launch',
                projectName: 'Veloce Pro ANC Launch',
                category: 'Brand Collab',
                roleCategory: 'Lifestyle & Tech Creator',
                ageRange: '18–35 yrs',
                gender: 'Female / Non-Binary',
                duration: '2 Weeks (2 Reels + 3 Stories)',
                company: 'Veloce Audio',
                location: 'Remote / Pan-India',
                budget: '₹50,000',
                deadline: 'Closes in 9 days',
                description: 'Create 2 high-energy reels and 3 story frames showcasing flagship wireless headphones.'
            },
            {
                id: 'opp-4',
                title: 'Stage Host – Film Festival',
                projectName: 'IndiCine Annual Awards Night',
                category: 'Hosting',
                roleCategory: 'Anchor / Emcee',
                ageRange: '21–38 yrs',
                gender: 'Any',
                duration: '2 Days (Evening Live)',
                company: 'IndiCine Collective',
                location: 'Hyderabad',
                budget: '₹40,000',
                deadline: 'Closes in 12 days',
                description: 'Bilingual emcee needed for opening night red carpet and panel moderation.'
            }
        ],
        wishlistIds: ['opp-1', 'opp-3'],
        network: [
            { id: 'net-1', name: 'Meera Nair', role: 'Casting Director • Mumbai', connected: true },
            { id: 'net-2', name: 'Rohan Kapoor', role: 'Executive Producer • RedFrame', connected: true },
            { id: 'net-3', name: 'Ananya Verma', role: 'Talent Manager • Starlight', connected: false },
            { id: 'net-4', name: 'Vikram Sethi', role: 'Director of Photography', connected: false }
        ],
        teammateSuggestions: [
            {
                id: 'tm-1',
                name: 'Siddharth Rao',
                profession: 'Director',
                city: 'Mumbai',
                reasonType: 'profession',
                reasonLabel: 'Profession Match',
                detail: 'Looking for Lead & Supporting Actors for upcoming OTT thriller.',
                interests: 'Web Series, Indie Cinema',
                added: false
            },
            {
                id: 'tm-2',
                name: 'Priya Menon',
                profession: 'Actor',
                city: 'Mumbai',
                reasonType: 'friends',
                reasonLabel: 'Previous Friend',
                detail: 'Worked together on Stage Play • 3 mutual friends (Meera Nair)',
                interests: 'Theatre, Screen Acting',
                added: false
            },
            {
                id: 'tm-3',
                name: 'Arjun Deshmukh',
                profession: 'Cinematographer',
                city: 'Pune',
                reasonType: 'interests',
                reasonLabel: 'Shared Interest',
                detail: 'Shares your interest in Commercial Shoots & Short Films.',
                interests: 'Commercial Shoots, Music Videos',
                added: false
            },
            {
                id: 'tm-4',
                name: 'Kavya Iyer',
                profession: 'Video Editor',
                city: 'Bengaluru',
                reasonType: 'profession',
                reasonLabel: 'Profession Match',
                detail: 'Showreel & trailer editor collaborating with actors.',
                interests: 'Showreels, Web Series',
                added: false
            },
            {
                id: 'tm-5',
                name: 'Devansh Khanna',
                profession: 'Producer',
                city: 'Delhi NCR',
                reasonType: 'friends',
                reasonLabel: 'Previous Friend',
                detail: 'Co-crew on Brand Campaign • 2 mutual friends (Rohan Kapoor)',
                interests: 'Brand Collabs, Web Series',
                added: false
            },
            {
                id: 'tm-6',
                name: 'Zoya Shaikh',
                profession: 'Voiceover Artist',
                city: 'Mumbai',
                reasonType: 'interests',
                reasonLabel: 'Shared Interest',
                detail: 'Shares your interest in Dubbing, Theatre & Storytelling.',
                interests: 'Theatre, Voiceover, Indie Cinema',
                added: false
            }
        ],
        notifications: [
            { id: 'n-1', text: 'Your application for "Video Editor – Production" was shortlisted.', time: '1 hour ago', unread: true },
            { id: 'n-2', text: 'Audition slot confirmed for "Commercial Brand Shoot" on Friday 11:00 AM.', time: 'Yesterday', unread: true },
            { id: 'n-3', text: 'Meera Nair viewed your showreel.', time: '3 days ago', unread: false }
        ],
        complaints: [
            {
                id: 'CMP-1024',
                category: 'Website Bug / Issue',
                priority: 'Medium',
                subject: 'Showreel link preview slow on mobile',
                description: 'When opening portfolio links on mobile data, the preview takes a few seconds to load.',
                status: 'In Review',
                badgeClass: 'review',
                createdAt: '2 days ago'
            }
        ],
        profileExtras: {
            phone: '+91 98765 43210',
            city: 'Mumbai, India',
            bio: 'Passionate screen actor and creative storyteller with experience in web series, commercial shoots, and theatre.',
            showreelUrl: 'https://vimeo.com/showcase/showreel',
            availability: 'Available immediately'
        },
        videos: [
            {
                id: 'vid-1',
                title: 'Acting Showreel 2026',
                category: 'Showreel',
                fileName: 'acting-showreel-2026.mp4',
                sizeLabel: '14.2 MB',
                uploadedAt: '3 days ago'
            }
        ],
        settings: {
            emailAlerts: true,
            auditionReminders: true,
            publicProfile: true
        }
    };

    function loadState() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return structuredClone(DEFAULT_STATE);
            return { ...structuredClone(DEFAULT_STATE), ...JSON.parse(raw) };
        } catch {
            return structuredClone(DEFAULT_STATE);
        }
    }

    let state = loadState();
    let appStatusFilter = 'All';
    let categoryFilterMode = 'all';
    let teammateFilterMode = 'all';
    let compareTrack = 'professional';
    let toastTimer = null;
    const videoObjectUrls = new Map();

    const COMPARE_MATRICES = {
        professional: {
            note: 'Entertainment Professional (e.g., Rahul — Singer for movies, weddings, college & corporate events): Professionals get better tools to showcase their skills and manage opportunities. Paying does not guarantee selection.',
            rows: [
                ['Create Profile', 'Yes', 'Yes', 'Yes'],
                ['Upload Portfolio', '5 files', '20 files', '50 files'],
                ['Apply for Auditions / Events', '1', 'Unlimited', 'Unlimited'],
                ['Advanced Job & Event Filters', 'No', 'Yes', 'Yes'],
                ['AI Portfolio Builder', 'No', 'Yes', 'Yes'],
                ['Portfolio Analytics', 'No', 'Yes', 'Yes'],
                ['AI Audition Practice', 'No', 'Limited', 'Expanded'],
                ['Availability Calendar', 'Yes', 'Yes', 'Yes'],
                ['Priority Support', 'No', 'No', 'Yes']
            ]
        },
        influencer: {
            note: 'Influencer (e.g., Priya — Fashion influencer for clothing brand collaborations & product-launch events): Influencers get advanced business tools. The platform can separately earn its disclosed 20–25% commission on completed brand collaborations.',
            rows: [
                ['Influencer Profile', 'Yes', 'Yes', 'Yes'],
                ['Receive Brand Requests', 'Yes', 'Yes', 'Yes'],
                ['Apply for Campaigns', 'Yes', 'Yes', 'Yes'],
                ['Advanced Analytics', 'No', 'Yes', 'Yes'],
                ['Digital Media Kit', 'No', 'Yes', 'Yes'],
                ['Campaign Price Estimator', 'No', 'Yes', 'Yes'],
                ['Event Promotion Tools', 'Basic', 'Advanced', 'Advanced'],
                ['Influencer Team Campaigns', 'No', 'No', 'Yes'],
                ['Detailed Reports', 'No', 'Yes', 'Yes']
            ]
        }
    };

    function getActiveLimits() {
        return PLAN_LIMITS[state.currentPlan] || PLAN_LIMITS.free;
    }

    function saveState() {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
        } catch {
            // Ignore storage quota errors
        }
    }

    function showToast(message) {
        const toast = document.getElementById('ew-toast');
        if (!toast) return;
        toast.textContent = message;
        toast.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.hidden = true;
        }, 2800);
    }

    function getSessionUser() {
        try {
            const raw = sessionStorage.getItem(SESSION_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    }

    // View Navigation
    function switchView(viewName) {
        document.querySelectorAll('.ew-nav-item[data-view]').forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.view === viewName);
        });
        document.querySelectorAll('.ew-view[data-view-panel]').forEach((panel) => {
            panel.classList.toggle('active', panel.dataset.viewPanel === viewName);
        });
        closeMobileSidebar();
        closePopovers();
    }

    // Mobile Sidebar
    const sidebar = document.getElementById('ew-sidebar');
    const backdrop = document.getElementById('ew-sidebar-backdrop');
    const menuToggle = document.getElementById('ew-menu-toggle');

    function closeMobileSidebar() {
        sidebar?.classList.remove('open');
        backdrop?.classList.remove('open');
    }

    menuToggle?.addEventListener('click', () => {
        sidebar?.classList.toggle('open');
        backdrop?.classList.toggle('open');
    });

    backdrop?.addEventListener('click', closeMobileSidebar);

    // Popovers
    const notifBtn = document.getElementById('notif-btn');
    const notifPopover = document.getElementById('notif-popover');
    const userMenuBtn = document.getElementById('user-menu-btn');
    const userMenuPopover = document.getElementById('user-menu-popover');

    function closePopovers() {
        if (notifPopover) notifPopover.hidden = true;
        if (userMenuPopover) userMenuPopover.hidden = true;
    }

    notifBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        const willOpen = notifPopover.hidden;
        closePopovers();
        notifPopover.hidden = !willOpen;
    });

    userMenuBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        const willOpen = userMenuPopover.hidden;
        closePopovers();
        userMenuPopover.hidden = !willOpen;
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.ew-topbar-right')) {
            closePopovers();
        }
    });

    // Render KPIs (keeps screenshot numbers 12, 4, 3, ₹25K as baseline + dynamic delta)
    function renderKpis() {
        const extraApps = Math.max(0, state.applications.length - DEFAULT_STATE.applications.length);
        const totalApps = 12 + extraApps;
        const shortlisted = 4 + state.applications.filter(
            (a, idx) => idx >= DEFAULT_STATE.applications.length && a.status === 'Shortlisted'
        ).length;

        document.getElementById('kpi-applications').textContent = String(totalApps);
        document.getElementById('kpi-shortlisted').textContent = String(shortlisted);
        document.getElementById('kpi-auditions').textContent = String(state.auditionsCount);
        document.getElementById('kpi-earnings').textContent = state.earningsText;

        const wishlistBadge = document.getElementById('wishlist-nav-badge');
        if (wishlistBadge) {
            wishlistBadge.textContent = String(state.wishlistIds.length);
        }
    }

    function renderRecentApplications() {
        const container = document.getElementById('recent-applications-list');
        if (!container) return;
        const recent = state.applications.slice(0, 3);
        container.innerHTML = recent.map((app) => `
            <article class="ew-app-card">
                <div class="ew-app-info">
                    <h3>${escapeHtml(app.title)}</h3>
                    <span class="ew-badge ${escapeHtml(app.badgeClass)}">${escapeHtml(app.status)}</span>
                    <div class="ew-app-meta">${escapeHtml(app.company)} • ${escapeHtml(app.location)} • Applied ${escapeHtml(app.appliedDate)}</div>
                </div>
                <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-goto-view="applications">View Details</button>
            </article>
        `).join('');
    }

    function renderAllApplications() {
        const container = document.getElementById('all-applications-list');
        if (!container) return;
        const filtered = appStatusFilter === 'All'
            ? state.applications
            : state.applications.filter((a) => a.status === appStatusFilter);

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No applications match the "${escapeHtml(appStatusFilter)}" filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((app) => `
            <article class="ew-app-card">
                <div class="ew-app-info">
                    <h3>${escapeHtml(app.title)}</h3>
                    <span class="ew-badge ${escapeHtml(app.badgeClass)}">${escapeHtml(app.status)}</span>
                    <div class="ew-app-meta">${escapeHtml(app.company)} • ${escapeHtml(app.location)} • ${escapeHtml(app.compensation)}</div>
                </div>
                <div class="ew-app-meta">Applied ${escapeHtml(app.appliedDate)}</div>
            </article>
        `).join('');
    }

    function renderOpportunities() {
        const container = document.getElementById('opportunities-list');
        if (!container) return;
        const query = (document.getElementById('opp-search')?.value || '').trim().toLowerCase();
        const category = document.getElementById('opp-category')?.value || 'All';
        const advFilter = document.getElementById('opp-advanced-filter')?.value || 'All';
        const limits = getActiveLimits();

        const filtered = state.opportunities.filter((opp) => {
            const matchesCategory = category === 'All' || opp.category === category;
            const matchesQuery = !query ||
                opp.title.toLowerCase().includes(query) ||
                opp.company.toLowerCase().includes(query) ||
                opp.location.toLowerCase().includes(query);
            let matchesAdv = true;
            if (limits.advancedFilters && advFilter === 'high') {
                const numericBudget = Number(String(opp.budget).replace(/[^0-9]/g, ''));
                matchesAdv = numericBudget >= 50000;
            } else if (limits.advancedFilters && advFilter === 'remote') {
                matchesAdv = opp.location.toLowerCase().includes('remote');
            }
            return matchesCategory && matchesQuery && matchesAdv;
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No opportunities found for your current search.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((opp) => {
            const defaults = DEFAULT_STATE.opportunities.find((d) => d.id === opp.id) || {};
            const projectName = opp.projectName || defaults.projectName || opp.company;
            const roleCategory = opp.roleCategory || defaults.roleCategory || opp.category;
            const ageRange = opp.ageRange || defaults.ageRange || 'Open';
            const gender = opp.gender || defaults.gender || 'Any';
            const duration = opp.duration || defaults.duration || 'Flexible';
            const saved = state.wishlistIds.includes(opp.id);
            const applied = state.applications.some((a) => a.title === opp.title);
            return `
                <article class="ew-panel">
                    <div class="ew-section-head">
                        <span class="ew-badge audition">${escapeHtml(roleCategory)}</span>
                        <span class="ew-app-meta">${escapeHtml(opp.deadline)}</span>
                    </div>
                    <h3 style="margin-top: 10px; margin-bottom: 2px;">${escapeHtml(opp.title)}</h3>
                    <div class="ew-app-meta" style="margin-bottom: 10px;">Project: <strong>${escapeHtml(projectName)}</strong> • ${escapeHtml(opp.company)}</div>
                    <p>${escapeHtml(opp.description)}</p>
                    <div class="ew-opp-specs">
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Role Category</span>
                            <span class="ew-opp-spec-val">${escapeHtml(roleCategory)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Age Range</span>
                            <span class="ew-opp-spec-val">${escapeHtml(ageRange)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Gender</span>
                            <span class="ew-opp-spec-val">${escapeHtml(gender)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Duration</span>
                            <span class="ew-opp-spec-val">${escapeHtml(duration)}</span>
                        </div>
                    </div>
                    <div class="ew-app-meta" style="margin-bottom: 14px;">
                        ${escapeHtml(opp.location)} • <strong>${escapeHtml(opp.budget)}</strong>
                    </div>
                    <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-apply-opp="${escapeHtml(opp.id)}" ${applied ? 'disabled' : ''}>
                            ${applied ? 'Applied' : 'Apply Now'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-wishlist="${escapeHtml(opp.id)}">
                            ${saved ? 'Saved in Wishlist' : 'Save to Wishlist'}
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderWishlist() {
        const container = document.getElementById('wishlist-list');
        if (!container) return;
        const saved = state.opportunities.filter((o) => state.wishlistIds.includes(o.id));
        if (saved.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>Your wishlist is empty. Explore Opportunities to save roles you like.</p></div>`;
            return;
        }
        container.innerHTML = saved.map((opp) => `
            <article class="ew-app-card">
                <div class="ew-app-info">
                    <h3>${escapeHtml(opp.title)}</h3>
                    <div class="ew-app-meta">${escapeHtml(opp.company)} • ${escapeHtml(opp.location)} • ${escapeHtml(opp.budget)}</div>
                </div>
                <div style="display: flex; gap: 8px;">
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-apply-opp="${escapeHtml(opp.id)}">Apply</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-wishlist="${escapeHtml(opp.id)}">Remove</button>
                </div>
            </article>
        `).join('');
    }

    function renderNetwork() {
        const container = document.getElementById('network-list');
        if (!container) return;
        container.innerHTML = state.network.map((person) => `
            <article class="ew-panel">
                <h3>${escapeHtml(person.name)}</h3>
                <p>${escapeHtml(person.role)}</p>
                <button type="button" class="ew-btn ${person.connected ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-toggle-connect="${escapeHtml(person.id)}">
                    ${person.connected ? 'Connected' : 'Connect'}
                </button>
            </article>
        `).join('');
    }

    function renderTeammateSuggestions() {
        const container = document.getElementById('teammate-suggestions-list');
        const countEl = document.getElementById('teammate-suggestions-count');
        if (!container) return;

        const professionFilter = document.getElementById('teammate-profession-filter')?.value || 'All';

        const filtered = state.teammateSuggestions.filter((tm) => {
            const matchesReason = teammateFilterMode === 'all' || tm.reasonType === teammateFilterMode;
            const matchesProfession = professionFilter === 'All' || tm.profession === professionFilter;
            return matchesReason && matchesProfession;
        });

        if (countEl) {
            countEl.textContent = `${filtered.length} ${filtered.length === 1 ? 'suggestion' : 'suggestions'}`;
        }

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No teammate suggestions match this filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((tm) => `
            <article class="ew-panel">
                <div class="ew-suggestion-meta">
                    <span class="ew-reason-tag ${escapeHtml(tm.reasonType)}">${escapeHtml(tm.reasonLabel)}</span>
                    <span class="ew-badge closed">${escapeHtml(tm.profession)}</span>
                </div>
                <h3>${escapeHtml(tm.name)}</h3>
                <p>${escapeHtml(tm.detail)}</p>
                <div class="ew-app-meta" style="margin-bottom: 14px;">
                    ${escapeHtml(tm.city)} • Interests: ${escapeHtml(tm.interests)}
                </div>
                <button type="button" class="ew-btn ${tm.added ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-add-teammate="${escapeHtml(tm.id)}" ${tm.added ? 'disabled' : ''}>
                    ${tm.added ? 'Added to Network' : 'Add Teammate'}
                </button>
            </article>
        `).join('');
    }

    function renderNotifications() {
        const list = document.getElementById('notif-list');
        const dot = document.getElementById('notif-dot');
        if (!list) return;
        const hasUnread = state.notifications.some((n) => n.unread);
        if (dot) dot.hidden = !hasUnread;

        list.innerHTML = state.notifications.map((n) => `
            <div class="ew-notif-item ${n.unread ? 'unread' : ''}">
                <p>${escapeHtml(n.text)}</p>
                <span>${escapeHtml(n.time)}</span>
            </div>
        `).join('');
    }

    function syncProfilePhoto(photoUrl) {
        const topbarAvatar = document.getElementById('topbar-avatar-img');
        const topbarFallback = document.getElementById('topbar-avatar-fallback');
        const menuAvatar = document.getElementById('profile-photo');
        const profileViewPhoto = document.getElementById('profile-view-photo');
        const profileViewFallback = document.getElementById('profile-view-photo-fallback');

        if (photoUrl) {
            if (topbarAvatar && topbarFallback) {
                topbarAvatar.src = photoUrl;
                topbarAvatar.hidden = false;
                topbarFallback.hidden = true;
            }
            if (menuAvatar) {
                menuAvatar.src = photoUrl;
                menuAvatar.hidden = false;
            }
            if (profileViewPhoto && profileViewFallback) {
                profileViewPhoto.src = photoUrl;
                profileViewPhoto.hidden = false;
                profileViewFallback.hidden = true;
            }
        } else {
            if (topbarAvatar && topbarFallback) {
                topbarAvatar.hidden = true;
                topbarFallback.hidden = false;
            }
            if (menuAvatar) menuAvatar.hidden = true;
            if (profileViewPhoto && profileViewFallback) {
                profileViewPhoto.hidden = true;
                profileViewFallback.hidden = false;
            }
        }
    }

    function populateProfileForm() {
        const user = getSessionUser();
        if (user) {
            const nameInput = document.getElementById('profile-full-name');
            const emailInput = document.getElementById('profile-email');
            const catInput = document.getElementById('profile-category-input');
            const topbarName = document.getElementById('topbar-user-name');
            const viewName = document.getElementById('profile-view-name');
            const viewCat = document.getElementById('profile-view-category');

            if (nameInput) nameInput.value = user.fullName || '';
            if (emailInput) emailInput.value = user.email || '';
            if (catInput) catInput.value = user.professionalCategory || 'Actor / Performer';
            if (topbarName) topbarName.textContent = user.fullName || 'Professional';
            if (viewName) viewName.textContent = user.fullName || 'Professional';
            if (viewCat) viewCat.textContent = user.professionalCategory || 'Actor / Performer';

            const storedPhoto = user.profilePhotoUrl || (user.email ? localStorage.getItem(`ew_profile_photo_ACTOR_${user.email}`) : '');
            syncProfilePhoto(storedPhoto);
        }
        document.getElementById('profile-phone').value = state.profileExtras.phone;
        document.getElementById('profile-city').value = state.profileExtras.city;
        document.getElementById('profile-dob').value = user?.dateOfBirth || state.profileExtras.dateOfBirth || '';
        document.getElementById('profile-gender').value = user?.gender || state.profileExtras.gender || '';
        document.getElementById('profile-experience').value = user?.experienceCategory || state.profileExtras.experienceCategory || '';
        document.getElementById('profile-bio').value = state.profileExtras.bio;
        document.getElementById('profile-showreel').value = state.profileExtras.showreelUrl;
        document.getElementById('profile-availability').value = state.profileExtras.availability;
    }

    function renderComplaints() {
        const openCount = state.complaints.filter((c) => c.status !== 'Resolved').length;
        const badge = document.getElementById('support-nav-badge');
        if (badge) badge.textContent = String(openCount);

        const countLabel = document.getElementById('complaints-count-label');
        if (countLabel) {
            countLabel.textContent = `${state.complaints.length} ${state.complaints.length === 1 ? 'ticket' : 'tickets'}`;
        }

        const list = document.getElementById('complaints-list');
        if (!list) return;

        if (state.complaints.length === 0) {
            list.innerHTML = `<div class="ew-panel"><p>No complaints submitted yet.</p></div>`;
            return;
        }

        list.innerHTML = state.complaints.map((ticket) => `
            <article class="ew-ticket-card">
                <div class="ew-ticket-head">
                    <h3>${escapeHtml(ticket.subject)}</h3>
                    <span class="ew-badge ${escapeHtml(ticket.badgeClass)}">${escapeHtml(ticket.status)}</span>
                </div>
                <p class="ew-ticket-desc">${escapeHtml(ticket.description)}</p>
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px; flex-wrap: wrap;">
                    <span class="ew-app-meta">${escapeHtml(ticket.id)} • ${escapeHtml(ticket.category)} • Priority: ${escapeHtml(ticket.priority)} • ${escapeHtml(ticket.createdAt)}</span>
                    ${ticket.status !== 'Resolved'
                        ? `<button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-resolve-complaint="${escapeHtml(ticket.id)}">Mark Resolved</button>`
                        : ''}
                </div>
            </article>
        `).join('');
    }

    function populateSettings() {
        document.getElementById('setting-email-alerts').checked = state.settings.emailAlerts;
        document.getElementById('setting-audition-reminders').checked = state.settings.auditionReminders;
        document.getElementById('setting-public-profile').checked = state.settings.publicProfile;
    }

    function renderPlans() {
        const activePlan = PLANS.find((p) => p.id === state.currentPlan) || PLANS[0];

        const navBadge = document.getElementById('plan-nav-badge');
        if (navBadge) navBadge.textContent = activePlan.shortLabel;

        const headerLabel = document.getElementById('header-plan-label');
        if (headerLabel) headerLabel.textContent = `Plan: ${activePlan.name} (${activePlan.shortLabel})`;

        const activeStatus = document.getElementById('plans-active-status');
        if (activeStatus) activeStatus.textContent = `Active: ${activePlan.name} (${activePlan.shortLabel})`;

        const bannerTitle = document.getElementById('plans-banner-title');
        const bannerSub = document.getElementById('plans-banner-subtitle');
        if (bannerTitle && bannerSub) {
            bannerTitle.textContent = `You are on the ${activePlan.name} (${activePlan.shortLabel})`;
            bannerSub.textContent = activePlan.id === 'free'
                ? 'Upgrade to Premium (₹199/mo) or Pro Elite (₹399/mo) for unlimited applications and priority casting visibility.'
                : activePlan.id === 'premium'
                    ? 'Enjoy unlimited applications and priority alerts. Upgrade to Pro Elite (₹399/mo) for 0% commission and top search placement.'
                    : 'You have full Pro Elite access with 0% booking commission, verified seal, and early casting invites.';
        }

        const checkSvg = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`;

        const buildCardsHtml = (compact) => PLANS.map((plan) => {
            const isCurrent = plan.id === activePlan.id;
            const features = compact ? plan.features.slice(0, 3) : plan.features;
            const tagHtml = plan.badge
                ? `<span class="ew-plan-tag ${plan.id === 'pro' ? 'elite' : ''}">${escapeHtml(plan.badge)}</span>`
                : '';
            const btnText = isCurrent
                ? 'Current Plan'
                : (plan.id === 'free' ? 'Switch to Free' : `Select ${plan.shortLabel}/mo`);

            return `
                <article class="ew-plan-card ${plan.cardClass} ${isCurrent ? 'current' : ''}">
                    ${tagHtml}
                    <h3 class="ew-plan-name">${escapeHtml(plan.name)}</h3>
                    <p class="ew-plan-desc">${escapeHtml(plan.description)}</p>
                    <div class="ew-plan-price-row">
                        <span class="ew-plan-price">${escapeHtml(plan.price)}</span>
                        <span class="ew-plan-period">${escapeHtml(plan.period)}</span>
                    </div>
                    <ul class="ew-plan-features">
                        ${features.map((feat) => `<li>${checkSvg}<span>${escapeHtml(feat)}</span></li>`).join('')}
                    </ul>
                    <button type="button" class="ew-btn ${plan.buttonClass}" data-select-plan="${escapeHtml(plan.id)}" ${isCurrent ? 'disabled' : ''}>
                        ${escapeHtml(btnText)}
                    </button>
                </article>
            `;
        }).join('');

        const previewContainer = document.getElementById('dashboard-plans-preview');
        if (previewContainer) previewContainer.innerHTML = buildCardsHtml(true);

        const fullContainer = document.getElementById('plans-full-grid');
        if (fullContainer) fullContainer.innerHTML = buildCardsHtml(false);

        renderPlanUsage();
        renderVideos();
        renderCareerTools();
        renderPlanComparison();
    }

    function renderPlanComparison() {
        const tbody = document.getElementById('plan-compare-tbody');
        const noteEl = document.getElementById('plan-compare-note');
        const matrix = COMPARE_MATRICES[compareTrack] || COMPARE_MATRICES.professional;
        if (tbody) {
            tbody.innerHTML = matrix.rows.map((row) => `
                <tr>
                    <td><strong>${escapeHtml(row[0])}</strong></td>
                    <td>${escapeHtml(row[1])}</td>
                    <td>${escapeHtml(row[2])}</td>
                    <td>${escapeHtml(row[3])}</td>
                </tr>
            `).join('');
        }
        if (noteEl) {
            noteEl.textContent = matrix.note;
        }
    }

    function renderCareerTools() {
        const container = document.getElementById('career-plan-tools');
        if (!container) return;
        const limits = getActiveLimits();

        const tools = [
            {
                title: 'AI Portfolio Builder',
                desc: 'Auto-structure your bio, credits, and showreel highlights for casting directors.',
                unlocked: limits.aiPortfolio,
                badge: limits.aiPortfolio ? 'Unlocked' : 'Premium ₹199+',
                badgeClass: limits.aiPortfolio ? 'shortlisted' : 'closed'
            },
            {
                title: 'Portfolio & Advanced Analytics',
                desc: 'Track profile views, showreel watch time, and campaign engagement metrics.',
                unlocked: limits.analytics,
                badge: limits.analytics ? 'Unlocked' : 'Premium ₹199+',
                badgeClass: limits.analytics ? 'shortlisted' : 'closed'
            },
            {
                title: 'AI Audition Practice',
                desc: 'Rehearse monologues, vocal warmups, and scene cues with AI feedback.',
                unlocked: limits.aiPractice !== 'Locked',
                badge: limits.aiPractice === 'Locked' ? 'Premium ₹199+' : limits.aiPractice,
                badgeClass: limits.aiPractice === 'Expanded' ? 'shortlisted' : (limits.aiPractice === 'Limited' ? 'audition' : 'closed')
            },
            {
                title: 'Digital Media Kit & Price Estimator',
                desc: 'Generate shareable brand media kits and estimate fair campaign rates.',
                unlocked: limits.mediaKit,
                badge: limits.mediaKit ? 'Unlocked' : 'Premium ₹199+',
                badgeClass: limits.mediaKit ? 'shortlisted' : 'closed'
            },
            {
                title: 'Influencer Team Campaigns',
                desc: 'Collaborate on multi-creator brand campaigns and split deliverables.',
                unlocked: limits.teamCampaigns,
                badge: limits.teamCampaigns ? 'Unlocked' : 'Pro ₹399 Only',
                badgeClass: limits.teamCampaigns ? 'shortlisted' : 'closed'
            },
            {
                title: 'Availability Calendar',
                desc: 'Manage open dates for movies, weddings, college fests, and brand shoots.',
                unlocked: true,
                badge: 'Included in All Plans',
                badgeClass: 'shortlisted'
            }
        ];

        container.innerHTML = tools.map((t) => `
            <article class="ew-panel">
                <h3>${escapeHtml(t.title)}</h3>
                <p>${escapeHtml(t.desc)}</p>
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
                    <span class="ew-badge ${escapeHtml(t.badgeClass)}">${escapeHtml(t.badge)}</span>
                    ${!t.unlocked
                        ? `<button type="button" class="ew-link-btn" data-goto-view="plans">Upgrade</button>`
                        : `<button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-open-tool="${escapeHtml(t.title)}">Open</button>`}
                </div>
            </article>
        `).join('');
    }

    function renderPlanUsage() {
        const limits = getActiveLimits();
        const activePlan = PLANS.find((p) => p.id === state.currentPlan) || PLANS[0];
        const videoLimitLabel = limits.maxVideos === Infinity ? 'Unlimited' : String(limits.maxVideos);
        const newAppsUsed = Math.max(0, state.applications.length - DEFAULT_STATE.applications.length);

        const usageVideos = document.getElementById('usage-videos');
        const usageApps = document.getElementById('usage-apps');
        const usagePerks = document.getElementById('usage-perks');
        const profilePlanBadge = document.getElementById('profile-plan-badge');
        const supportTierBadge = document.getElementById('support-tier-badge');
        const advFilterEl = document.getElementById('opp-advanced-filter');

        if (usageVideos) usageVideos.textContent = `${state.videos.length} / ${videoLimitLabel} Used`;
        if (usageApps) {
            usageApps.textContent = limits.maxApps === Infinity
                ? `${state.applications.length} (Unlimited)`
                : `${newAppsUsed} / ${limits.maxApps} Free Used`;
        }
        if (usagePerks) usagePerks.textContent = limits.perksLabel;
        if (profilePlanBadge) {
            profilePlanBadge.textContent = `${activePlan.name} (${activePlan.shortLabel})`;
            profilePlanBadge.className = `ew-plan-badge-inline ${activePlan.id}`;
        }
        if (supportTierBadge) {
            supportTierBadge.textContent = limits.prioritySupport
                ? 'Priority Support (Pro ₹399 — 2h SLA)'
                : 'Standard Support (24h SLA)';
        }
        if (advFilterEl) {
            advFilterEl.disabled = !limits.advancedFilters;
            if (!limits.advancedFilters) advFilterEl.value = 'All';
        }
    }

    function renderVideos() {
        const limits = getActiveLimits();
        const activePlan = PLANS.find((p) => p.id === state.currentPlan) || PLANS[0];
        const maxVideos = limits.maxVideos;
        const limitReached = state.videos.length >= maxVideos;

        const quotaPill = document.getElementById('video-quota-pill');
        if (quotaPill) {
            quotaPill.textContent = maxVideos === Infinity
                ? `${state.videos.length} Videos (Unlimited)`
                : `${state.videos.length} / ${maxVideos} Videos`;
        }

        const alertBox = document.getElementById('profile-video-limit-alert');
        const alertText = document.getElementById('profile-video-limit-text');
        const submitBtn = document.getElementById('profile-video-submit');

        if (alertBox && alertText) {
            alertBox.hidden = !limitReached;
            if (limitReached) {
                alertText.textContent = `Your ${activePlan.name} (${activePlan.shortLabel}) allows up to ${maxVideos} ${maxVideos === 1 ? 'video' : 'videos'}. Upgrade to ${state.currentPlan === 'free' ? 'Premium (₹199/mo) for 5 videos or Pro Elite (₹399/mo) for unlimited' : 'Pro Elite (₹399/mo) for unlimited'} uploads.`;
            }
        }
        if (submitBtn) {
            submitBtn.disabled = limitReached;
        }

        const grid = document.getElementById('profile-videos-grid');
        if (!grid) return;

        if (state.videos.length === 0) {
            grid.innerHTML = `<div class="ew-panel" style="grid-column: 1 / -1;"><p>No videos uploaded yet. Upload your first showreel or audition clip above.</p></div>`;
            return;
        }

        grid.innerHTML = state.videos.map((vid) => {
            const objUrl = videoObjectUrls.get(vid.id);
            const mediaHtml = objUrl
                ? `<video class="ew-video-player" src="${escapeHtml(objUrl)}" controls preload="metadata"></video>`
                : `<div class="ew-video-placeholder">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
                        <strong>${escapeHtml(vid.fileName)}</strong>
                        <span>${escapeHtml(vid.sizeLabel)}</span>
                   </div>`;

            return `
                <article class="ew-video-card">
                    ${mediaHtml}
                    <div class="ew-video-body">
                        <div>
                            <h4>${escapeHtml(vid.title)}</h4>
                            <div class="ew-app-meta">${escapeHtml(vid.category)} • ${escapeHtml(vid.uploadedAt)}</div>
                        </div>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-delete-video="${escapeHtml(vid.id)}">Delete</button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderCategories() {
        const navBadge = document.getElementById('categories-nav-badge');
        if (navBadge) navBadge.textContent = String(state.followedCategoryIds.length);

        const summary = document.getElementById('categories-following-summary');
        if (summary) {
            const count = state.followedCategoryIds.length;
            summary.textContent = `Following ${count} ${count === 1 ? 'category' : 'categories'}`;
        }

        const container = document.getElementById('categories-grid');
        if (!container) return;

        const query = (document.getElementById('category-search')?.value || '').trim().toLowerCase();
        const filtered = state.categories.filter((cat) => {
            const isFollowing = state.followedCategoryIds.includes(cat.id);
            const matchesMode = categoryFilterMode === 'all' || isFollowing;
            const matchesQuery = !query ||
                cat.name.toLowerCase().includes(query) ||
                cat.description.toLowerCase().includes(query);
            return matchesMode && matchesQuery;
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No categories match your current filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((cat) => {
            const isFollowing = state.followedCategoryIds.includes(cat.id);
            const displayCount = cat.followers + (isFollowing ? 1 : 0);
            return `
                <article class="ew-category-card ${isFollowing ? 'following' : ''}">
                    <div>
                        <h3>${escapeHtml(cat.name)}</h3>
                        <p>${escapeHtml(cat.description)}</p>
                    </div>
                    <div class="ew-category-foot">
                        <span class="ew-app-meta">${displayCount.toLocaleString('en-IN')} followers</span>
                        <button type="button" class="ew-btn ${isFollowing ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-toggle-follow-category="${escapeHtml(cat.id)}">
                            ${isFollowing ? 'Following (Unfollow)' : 'Follow'}
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderActorSearch() {
        const container = document.getElementById('actor-search-results');
        const countEl = document.getElementById('actor-search-count');
        if (!container) return;

        const query = (document.getElementById('actor-search-input')?.value || '').trim().toLowerCase();
        const planFilter = document.getElementById('actor-plan-filter')?.value || 'all';

        const filtered = state.otherActors.filter((actor) => {
            const matchesPlan = planFilter === 'all' || actor.planId === planFilter;
            const matchesQuery = !query ||
                actor.name.toLowerCase().includes(query) ||
                actor.category.toLowerCase().includes(query) ||
                actor.city.toLowerCase().includes(query);
            return matchesPlan && matchesQuery;
        });

        if (countEl) {
            countEl.textContent = `${filtered.length} ${filtered.length === 1 ? 'actor' : 'actors'} found`;
        }

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No actors found matching your search or plan filter.</p></div>`;
            return;
        }

        const planLabels = {
            free: 'Basic (Free)',
            premium: 'Premium (₹199)',
            pro: 'Pro Elite (₹399)'
        };

        container.innerHTML = filtered.map((actor) => {
            const fallbackActor = DEFAULT_STATE.otherActors.find((a) => a.id === actor.id);
            const photoSrc = actor.photoUrl || (fallbackActor ? fallbackActor.photoUrl : '');
            return `
            <article class="ew-actor-card">
                <div class="ew-actor-left">
                    <img class="ew-actor-avatar ew-photo-clickable" src="${escapeHtml(photoSrc)}" alt="${escapeHtml(actor.name)}" title="Click to enlarge">
                    <div>
                        <h3>
                            ${escapeHtml(actor.name)}
                            <span class="ew-plan-badge-inline ${escapeHtml(actor.planId)}">${escapeHtml(planLabels[actor.planId] || actor.planId)}</span>
                        </h3>
                        <div class="ew-app-meta">${escapeHtml(actor.category)} • ${escapeHtml(actor.city)} • ${escapeHtml(actor.credits)}</div>
                    </div>
                </div>
                <button type="button" class="ew-btn ${actor.following ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-toggle-follow-actor="${escapeHtml(actor.id)}">
                    ${actor.following ? 'Following' : 'Follow'}
                </button>
            </article>
        `;
        }).join('');
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // Event Delegation
    document.addEventListener('click', (e) => {
        const navBtn = e.target.closest('.ew-nav-item[data-view]');
        if (navBtn) {
            switchView(navBtn.dataset.view);
            return;
        }

        const gotoBtn = e.target.closest('[data-goto-view]');
        if (gotoBtn) {
            switchView(gotoBtn.dataset.gotoView);
            return;
        }

        const applyBtn = e.target.closest('[data-apply-opp]');
        if (applyBtn) {
            const limits = getActiveLimits();
            const newAppsUsed = Math.max(0, state.applications.length - DEFAULT_STATE.applications.length);
            if (newAppsUsed >= limits.maxApps) {
                showToast(`Free plan limit (${limits.maxApps} audition/event application) reached. Upgrade to ₹199 or ₹399 for unlimited.`);
                switchView('plans');
                return;
            }
            const oppId = applyBtn.dataset.applyOpp;
            const opp = state.opportunities.find((o) => o.id === oppId);
            if (opp && !state.applications.some((a) => a.title === opp.title)) {
                state.applications.unshift({
                    id: `app-${Date.now()}`,
                    title: opp.title,
                    company: opp.company,
                    location: opp.location,
                    appliedDate: 'Just now',
                    status: 'Under Review',
                    badgeClass: 'review',
                    compensation: opp.budget
                });
                saveState();
                renderKpis();
                renderRecentApplications();
                renderAllApplications();
                renderOpportunities();
                renderPlanUsage();
                showToast(`Applied to "${opp.title}"`);
            }
            return;
        }

        const wishBtn = e.target.closest('[data-toggle-wishlist]');
        if (wishBtn) {
            const oppId = wishBtn.dataset.toggleWishlist;
            const idx = state.wishlistIds.indexOf(oppId);
            if (idx >= 0) {
                state.wishlistIds.splice(idx, 1);
                showToast('Removed from Wishlist');
            } else {
                const limits = getActiveLimits();
                if (state.wishlistIds.length >= limits.maxWishlist) {
                    showToast(`Free plan Wishlist limit (${limits.maxWishlist}) reached. Upgrade to ₹199/mo for unlimited.`);
                    switchView('plans');
                    return;
                }
                state.wishlistIds.push(oppId);
                showToast('Saved to Wishlist');
            }
            saveState();
            renderKpis();
            renderOpportunities();
            renderWishlist();
            return;
        }

        const connectBtn = e.target.closest('[data-toggle-connect]');
        if (connectBtn) {
            const person = state.network.find((n) => n.id === connectBtn.dataset.toggleConnect);
            if (person) {
                person.connected = !person.connected;
                saveState();
                renderNetwork();
                showToast(person.connected ? `Connected with ${person.name}` : `Disconnected from ${person.name}`);
            }
            return;
        }

        const filterChip = e.target.closest('.ew-chip[data-app-filter]');
        if (filterChip) {
            appStatusFilter = filterChip.dataset.appFilter;
            document.querySelectorAll('.ew-chip[data-app-filter]').forEach((c) => {
                c.classList.toggle('active', c === filterChip);
            });
            renderAllApplications();
            return;
        }

        const planBtn = e.target.closest('[data-select-plan]');
        if (planBtn) {
            const planId = planBtn.dataset.selectPlan;
            const chosen = PLANS.find((p) => p.id === planId);
            if (chosen && state.currentPlan !== chosen.id) {
                state.currentPlan = chosen.id;
                saveState();
                renderPlans();
                showToast(`Switched to ${chosen.name} (${chosen.shortLabel})`);
            }
            return;
        }

        const catFollowBtn = e.target.closest('[data-toggle-follow-category]');
        if (catFollowBtn) {
            const catId = catFollowBtn.dataset.toggleFollowCategory;
            const cat = state.categories.find((c) => c.id === catId);
            const idx = state.followedCategoryIds.indexOf(catId);
            if (idx >= 0) {
                state.followedCategoryIds.splice(idx, 1);
                showToast(`Unfollowed ${cat ? cat.name : 'category'}`);
            } else {
                state.followedCategoryIds.push(catId);
                showToast(`Following ${cat ? cat.name : 'category'}`);
            }
            saveState();
            renderCategories();
            return;
        }

        const catFilterChip = e.target.closest('.ew-chip[data-cat-filter]');
        if (catFilterChip) {
            categoryFilterMode = catFilterChip.dataset.catFilter;
            document.querySelectorAll('.ew-chip[data-cat-filter]').forEach((c) => {
                c.classList.toggle('active', c === catFilterChip);
            });
            renderCategories();
            return;
        }

        const actorFollowBtn = e.target.closest('[data-toggle-follow-actor]');
        if (actorFollowBtn) {
            const actor = state.otherActors.find((a) => a.id === actorFollowBtn.dataset.toggleFollowActor);
            if (actor) {
                actor.following = !actor.following;
                saveState();
                renderActorSearch();
                showToast(actor.following ? `Following ${actor.name}` : `Unfollowed ${actor.name}`);
            }
            return;
        }

        const resolveTicketBtn = e.target.closest('[data-resolve-complaint]');
        if (resolveTicketBtn) {
            const ticket = state.complaints.find((c) => c.id === resolveTicketBtn.dataset.resolveComplaint);
            if (ticket) {
                ticket.status = 'Resolved';
                ticket.badgeClass = 'shortlisted';
                saveState();
                renderComplaints();
                showToast(`Marked ${ticket.id} as resolved`);
            }
            return;
        }

        const teammateFilterChip = e.target.closest('.ew-chip[data-teammate-filter]');
        if (teammateFilterChip) {
            teammateFilterMode = teammateFilterChip.dataset.teammateFilter;
            document.querySelectorAll('.ew-chip[data-teammate-filter]').forEach((c) => {
                c.classList.toggle('active', c === teammateFilterChip);
            });
            renderTeammateSuggestions();
            return;
        }

        const addTeammateBtn = e.target.closest('[data-add-teammate]');
        if (addTeammateBtn) {
            const tm = state.teammateSuggestions.find((t) => t.id === addTeammateBtn.dataset.addTeammate);
            if (tm && !tm.added) {
                tm.added = true;
                state.network.unshift({
                    id: `net-${Date.now()}`,
                    name: tm.name,
                    role: `${tm.profession} • ${tm.city}`,
                    connected: true
                });
                saveState();
                renderNetwork();
                renderTeammateSuggestions();
                showToast(`Added ${tm.name} to your network`);
            }
            return;
        }

        const deleteVideoBtn = e.target.closest('[data-delete-video]');
        if (deleteVideoBtn) {
            const vidId = deleteVideoBtn.dataset.deleteVideo;
            const idx = state.videos.findIndex((v) => v.id === vidId);
            if (idx >= 0) {
                state.videos.splice(idx, 1);
                const objUrl = videoObjectUrls.get(vidId);
                if (objUrl) {
                    URL.revokeObjectURL(objUrl);
                    videoObjectUrls.delete(vidId);
                }
                saveState();
                renderVideos();
                renderPlanUsage();
                showToast('Video removed from profile');
            }
            return;
        }

        const compareBtn = e.target.closest('.ew-chip[data-compare-track]');
        if (compareBtn) {
            compareTrack = compareBtn.dataset.compareTrack;
            document.querySelectorAll('.ew-chip[data-compare-track]').forEach((c) => {
                c.classList.toggle('active', c === compareBtn);
            });
            renderPlanComparison();
            return;
        }

        const openToolBtn = e.target.closest('[data-open-tool]');
        if (openToolBtn) {
            showToast(`${openToolBtn.dataset.openTool} launched for your ${state.currentPlan.toUpperCase()} plan`);
        }
    });

    document.getElementById('mark-notifs-read')?.addEventListener('click', () => {
        state.notifications.forEach((n) => { n.unread = false; });
        saveState();
        renderNotifications();
        showToast('All notifications marked as read');
    });

    document.getElementById('opp-search')?.addEventListener('input', renderOpportunities);
    document.getElementById('opp-category')?.addEventListener('change', renderOpportunities);
    document.getElementById('opp-advanced-filter')?.addEventListener('change', renderOpportunities);
    document.getElementById('category-search')?.addEventListener('input', renderCategories);
    document.getElementById('actor-search-input')?.addEventListener('input', renderActorSearch);
    document.getElementById('actor-plan-filter')?.addEventListener('change', renderActorSearch);
    document.getElementById('teammate-profession-filter')?.addEventListener('change', renderTeammateSuggestions);

    document.getElementById('profile-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const user = getSessionUser();
        const fullName = document.getElementById('profile-full-name').value.trim();
        const category = document.getElementById('profile-category-input').value.trim();

        if (user) {
            if (fullName) user.fullName = fullName;
            if (category) user.professionalCategory = category;
            sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));

            const userNameEl = document.getElementById('user-name');
            const topbarNameEl = document.getElementById('topbar-user-name');
            const catEl = document.getElementById('professional-category');
            const viewNameEl = document.getElementById('profile-view-name');
            const viewCatEl = document.getElementById('profile-view-category');
            if (userNameEl && fullName) userNameEl.textContent = fullName;
            if (topbarNameEl && fullName) topbarNameEl.textContent = fullName;
            if (catEl && category) catEl.textContent = category;
            if (viewNameEl && fullName) viewNameEl.textContent = fullName;
            if (viewCatEl && category) viewCatEl.textContent = category;
        }

        state.profileExtras = {
            phone: document.getElementById('profile-phone').value.trim(),
            city: document.getElementById('profile-city').value.trim(),
            dateOfBirth: document.getElementById('profile-dob').value,
            gender: document.getElementById('profile-gender').value,
            experienceCategory: document.getElementById('profile-experience').value,
            bio: document.getElementById('profile-bio').value.trim(),
            showreelUrl: document.getElementById('profile-showreel').value.trim(),
            availability: document.getElementById('profile-availability').value
        };
        saveState();
        showToast('Profile updated successfully');
    });

    ['setting-email-alerts', 'setting-audition-reminders', 'setting-public-profile'].forEach((id) => {
        document.getElementById(id)?.addEventListener('change', () => {
            state.settings = {
                emailAlerts: document.getElementById('setting-email-alerts').checked,
                auditionReminders: document.getElementById('setting-audition-reminders').checked,
                publicProfile: document.getElementById('setting-public-profile').checked
            };
            saveState();
            showToast('Preferences saved');
        });
    });

    document.getElementById('profile-photo-upload')?.addEventListener('change', (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => {
            const dataUrl = typeof reader.result === 'string' ? reader.result : '';
            if (!dataUrl) return;
            const user = getSessionUser();
            if (user) {
                user.profilePhotoUrl = dataUrl;
                sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
                if (user.email) {
                    try {
                        localStorage.setItem(`ew_profile_photo_ACTOR_${user.email}`, dataUrl);
                    } catch {
                        // Ignore storage quota errors
                    }
                }
            }
            syncProfilePhoto(dataUrl);
            showToast('Profile photo updated');
        };
        reader.readAsDataURL(file);
    });

    document.getElementById('profile-video-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const limits = getActiveLimits();
        if (state.videos.length >= limits.maxVideos) {
            showToast(`Video upload limit reached (${limits.maxVideos}). Upgrade your plan to upload more.`);
            return;
        }

        const title = document.getElementById('profile-video-title').value.trim();
        const category = document.getElementById('profile-video-category').value;
        const fileInput = document.getElementById('profile-video-file');
        const file = fileInput.files && fileInput.files[0];
        if (!title || !file) return;

        const vidId = `vid-${Date.now()}`;
        const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
        videoObjectUrls.set(vidId, URL.createObjectURL(file));

        state.videos.unshift({
            id: vidId,
            title,
            category,
            fileName: file.name,
            sizeLabel: `${sizeMb} MB`,
            uploadedAt: 'Just now'
        });
        saveState();
        renderVideos();
        renderPlanUsage();
        e.target.reset();
        showToast(`Uploaded "${title}"`);
    });

    document.getElementById('complaint-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const category = document.getElementById('complaint-category').value;
        const priority = document.getElementById('complaint-priority').value;
        const subject = document.getElementById('complaint-subject').value.trim();
        const description = document.getElementById('complaint-description').value.trim();
        if (!subject || !description) return;

        const ticketId = `CMP-${Math.floor(1000 + Math.random() * 9000)}`;
        state.complaints.unshift({
            id: ticketId,
            category,
            priority,
            subject,
            description,
            status: 'Open',
            badgeClass: 'audition',
            createdAt: 'Just now'
        });
        saveState();
        renderComplaints();
        e.target.reset();
        showToast(`Complaint ${ticketId} submitted`);
    });

    // Profile Photo Lightbox
    const photoModal = document.getElementById('ew-photo-modal');
    const photoModalImg = document.getElementById('ew-photo-modal-img');
    const photoModalCaption = document.getElementById('ew-photo-modal-caption');
    const photoModalClose = document.getElementById('ew-photo-modal-close');

    function closePhotoModal() {
        if (photoModal) photoModal.hidden = true;
    }

    document.addEventListener('click', (e) => {
        const clickablePhoto = e.target.closest('.ew-photo-clickable');
        if (clickablePhoto && clickablePhoto.src && !clickablePhoto.hidden && photoModal && photoModalImg) {
            e.stopPropagation();
            photoModalImg.src = clickablePhoto.src;
            const user = getSessionUser();
            if (photoModalCaption) {
                photoModalCaption.textContent = user?.fullName ? `${user.fullName} — Profile Photo` : 'Profile Photo';
            }
            photoModal.hidden = false;
            return;
        }
        if (e.target === photoModal) {
            closePhotoModal();
        }
    });

    photoModalClose?.addEventListener('click', closePhotoModal);
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closePhotoModal();
    });

    // Initial render
    renderKpis();
    renderRecentApplications();
    renderAllApplications();
    renderOpportunities();
    renderWishlist();
    renderNetwork();
    renderTeammateSuggestions();
    renderNotifications();
    renderPlans();
    renderCategories();
    renderActorSearch();
    renderComplaints();
    populateProfileForm();
    populateSettings();
})();
