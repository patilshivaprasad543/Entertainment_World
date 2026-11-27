(() => {
    const STORAGE_KEY = 'ew_professional_dashboard_state_v1';
    const SESSION_KEY = 'ew_session_ACTOR';

    const PLAN_LIMITS = {
        free: {
            maxVideos: 1,
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
            perksLabel: '1 Portfolio Video • 1 Audition/Event Application'
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
                'Upload up to 1 portfolio video',
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
        planHistory: [
            {
                id: 'ph-1',
                planName: 'Basic (Free)',
                price: '₹0',
                period: 'Current • Started 01 Nov 2026',
                paymentMethod: 'Free Tier • #INV-2026-004',
                status: 'Active'
            },
            {
                id: 'ph-2',
                planName: 'Premium',
                price: '₹199 / mo',
                period: '01 Oct 2026 – 31 Oct 2026',
                paymentMethod: 'UPI • #INV-2026-003',
                status: 'Expired'
            },
            {
                id: 'ph-3',
                planName: 'Pro Elite',
                price: '₹399 / mo',
                period: '01 Sep 2026 – 30 Sep 2026',
                paymentMethod: 'Card • #INV-2026-002',
                status: 'Expired'
            },
            {
                id: 'ph-4',
                planName: 'Basic (Free)',
                price: '₹0',
                period: '15 Aug 2026 – 31 Aug 2026',
                paymentMethod: 'Free Tier • #INV-2026-001',
                status: 'Completed'
            }
        ],
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
            {
                id: 'act-1',
                name: 'Aarav Mehta',
                category: 'Lead Actor • Web Series',
                city: 'Mumbai',
                planId: 'pro',
                credits: '14 Credits • Verified Showreel',
                followers: 1240,
                following: true,
                photoUrl: '/images/avatars/actor-male-1.jpg',
                videos: [
                    { id: 'act1-v1', title: 'Crime Thriller Showreel 2026', category: 'Showreel', fileName: 'aarav-thriller-reel.mp4', sizeLabel: '18.4 MB • 2:15', uploadedAt: '4 days ago' },
                    { id: 'act1-v2', title: 'Hindi & English Monologue', category: 'Monologue', fileName: 'aarav-monologue.mp4', sizeLabel: '9.6 MB • 1:20', uploadedAt: '2 weeks ago' }
                ]
            },
            {
                id: 'act-2',
                name: 'Diya Sharma',
                category: 'Screen & Theatre Actor',
                city: 'Delhi NCR',
                planId: 'pro',
                credits: '11 Credits • Award Nominee',
                followers: 980,
                following: false,
                photoUrl: '/images/avatars/actor-female-1.jpg',
                videos: [
                    { id: 'act2-v1', title: 'Stage & Screen Showreel', category: 'Showreel', fileName: 'diya-stage-screen.mp4', sizeLabel: '15.1 MB • 1:55', uploadedAt: '5 days ago' },
                    { id: 'act2-v2', title: 'Period Drama Audition Clip', category: 'Audition Clip', fileName: 'diya-period-audition.mp4', sizeLabel: '11.2 MB • 1:35', uploadedAt: '3 weeks ago' }
                ]
            },
            {
                id: 'act-3',
                name: 'Kabir Malhotra',
                category: 'Commercial & Voice Actor',
                city: 'Bengaluru',
                planId: 'premium',
                credits: '9 Credits • 4 Brand Shoots',
                followers: 640,
                following: false,
                photoUrl: '/images/avatars/producer-male-1.jpg',
                videos: [
                    { id: 'act3-v1', title: 'Brand Commercials Montage', category: 'Commercial', fileName: 'kabir-brand-montage.mp4', sizeLabel: '12.8 MB • 1:40', uploadedAt: '1 week ago' }
                ]
            },
            {
                id: 'act-4',
                name: 'Neha Kulkarni',
                category: 'Character Actor & Host',
                city: 'Pune',
                planId: 'premium',
                credits: '7 Credits • Bilingual',
                followers: 520,
                following: true,
                photoUrl: '/images/talent-actor.jpg',
                videos: [
                    { id: 'act4-v1', title: 'Bilingual Hosting & Acting Reel', category: 'Showreel', fileName: 'neha-bilingual-reel.mp4', sizeLabel: '13.5 MB • 1:50', uploadedAt: '6 days ago' }
                ]
            },
            {
                id: 'act-5',
                name: 'Rishi Verma',
                category: 'Indie Film Actor',
                city: 'Hyderabad',
                planId: 'free',
                credits: '3 Short Films',
                followers: 215,
                following: false,
                photoUrl: '/images/talent-singer.jpg',
                videos: [
                    { id: 'act5-v1', title: 'Indie Short Film Scene', category: 'Short Film', fileName: 'rishi-indie-scene.mp4', sizeLabel: '10.4 MB • 1:15', uploadedAt: '2 weeks ago' }
                ]
            },
            {
                id: 'act-6',
                name: 'Tara Singhania',
                category: 'Model & Screen Performer',
                city: 'Mumbai',
                planId: 'free',
                credits: '2 Music Videos',
                followers: 310,
                following: false,
                photoUrl: '/images/talent-model.jpg',
                videos: [
                    { id: 'act6-v1', title: 'Music Video Performance Cut', category: 'Music Video', fileName: 'tara-mv-cut.mp4', sizeLabel: '16.0 MB • 2:05', uploadedAt: '1 week ago' }
                ]
            }
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
        auditionInvitations: [
            {
                id: 'ainv-1',
                projectTitle: 'Shadows of Bombay — OTT Thriller',
                castingHouse: 'Moonlight Cinema Casting',
                roleName: 'Lead Detective (Kabir)',
                auditionMode: 'In-Person',
                slotDate: '04 Dec 2026 • 11:30 AM',
                location: 'Andheri West Studio 4, Mumbai',
                compensation: '₹1,50,000 / episode',
                rsvpDeadline: 'Confirm in 2 days',
                status: 'Pending',
                scriptNote: '2-page interrogation scene sides attached. Prepare both intense and understated takes.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'ainv-2',
                projectTitle: 'Monsoon Echoes — Feature Film',
                castingHouse: 'SilverScreen Casting',
                roleName: 'Supporting Lead (Vikram)',
                auditionMode: 'Self-Tape',
                slotDate: 'Submit by 07 Dec 2026 • 6:00 PM',
                location: 'Online Portal Upload (HD 1080p)',
                compensation: '₹2,20,000 (Full Shoot)',
                rsvpDeadline: 'Due in 4 days',
                status: 'Pending',
                scriptNote: 'Record 1 dramatic monologue (90 sec) + 1 slate intro in natural daylight.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'ainv-3',
                projectTitle: 'Katha — National TV Commercial',
                castingHouse: 'Prism Ad Films',
                roleName: 'Young Entrepreneur (Featured)',
                auditionMode: 'Live Video',
                slotDate: '09 Dec 2026 • 3:00 PM',
                location: 'Zoom Casting Room (Link on Confirm)',
                compensation: '₹75,000 (1-Day Shoot)',
                rsvpDeadline: 'Confirm in 5 days',
                status: 'Pending',
                scriptNote: 'Live chemistry read with director and brand creative lead; casual smart wardrobe.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'ainv-4',
                projectTitle: 'Raag & Rhythm — Stage Musical',
                castingHouse: 'NCPA Performing Arts',
                roleName: 'Ensemble Vocalist & Actor',
                auditionMode: 'In-Person',
                slotDate: '12 Dec 2026 • 10:00 AM',
                location: 'NCPA Experimental Theatre, Mumbai',
                compensation: '₹60,000 (Run of Show)',
                rsvpDeadline: 'Confirmed',
                status: 'Accepted',
                scriptNote: 'Prepare 1 classical/semi-classical verse and 1 contemporary theatre monologue.',
                counterFee: '',
                counterNote: ''
            }
        ],
        eventInvitations: [
            {
                id: 'einv-1',
                eventTitle: 'Filmfare Spotlight Gala Night',
                organizer: 'Apex Live Events',
                eventType: 'Award Show',
                date: '14 Dec 2026 • 7:00 PM',
                venue: 'Jio World Convention Centre',
                city: 'Mumbai',
                role: 'VIP Red-Carpet Guest & Award Presenter',
                fee: '₹85,000',
                dressCode: 'Black Tie / Designer Ethnic',
                travelCovered: 'Business Class + 5-Star Stay',
                rsvpDeadline: 'RSVP in 3 days',
                status: 'Pending',
                notes: 'Includes 15-min red-carpet media interaction and presenting Best Debut Director award.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'einv-2',
                eventTitle: 'Echoes of Monsoon — Grand Premiere',
                organizer: 'SilverScreen Live',
                eventType: 'Film Premiere',
                date: '21 Dec 2026 • 6:30 PM',
                venue: 'PVR ICON, Phoenix Palladium',
                city: 'Mumbai',
                role: 'Celebrity Panelist & Press Q&A',
                fee: '₹45,000',
                dressCode: 'Smart Formal',
                travelCovered: 'Chauffeur Pick-up & Drop',
                rsvpDeadline: 'RSVP in 5 days',
                status: 'Pending',
                notes: 'Post-screening Q&A with director and lead cast, followed by VIP dinner.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'einv-3',
                eventTitle: 'Sunburn Arena — NYE Countdown',
                organizer: 'VibeNation Entertainment',
                eventType: 'Music Festival',
                date: '31 Dec 2026 • 8:00 PM',
                venue: 'Vagator Hilltop Arena',
                city: 'Goa',
                role: 'Main Stage Host / Emcee',
                fee: '₹1,10,000',
                dressCode: 'Festival Couture (Styled)',
                travelCovered: 'Return Flights + Villa Stay',
                rsvpDeadline: 'RSVP in 6 days',
                status: 'Pending',
                notes: '4-hour live stage hosting across headliner sets and midnight countdown.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'einv-4',
                eventTitle: 'TechVista Annual Leadership Summit',
                organizer: 'Crestline Corporate Events',
                eventType: 'Corporate Gala',
                date: '10 Jan 2027 • 7:30 PM',
                venue: 'ITC Grand Chola',
                city: 'Chennai',
                role: 'Keynote Fireside Moderator',
                fee: '₹70,000',
                dressCode: 'Business Formal',
                travelCovered: 'Flights + 2-Night Suite',
                rsvpDeadline: 'Confirmed',
                status: 'Accepted',
                notes: '45-minute moderated fireside chat on AI in Cinema & Entertainment.',
                counterFee: '',
                counterNote: ''
            }
        ],
        brandInvitations: [
            {
                id: 'binv-1',
                campaignTitle: 'Chronos Heritage Watch Launch',
                brandName: 'Chronos Geneve India',
                industry: 'Luxury Accessories',
                format: 'Product Launch',
                deliverables: '2 Instagram Reels + 4 Stories + Launch Event Appearance',
                compensation: '₹1,40,000',
                commissionNote: 'Direct Brand Invite • Zero agency middleman',
                timeline: '15 Dec – 28 Dec 2026',
                exclusivity: '30 days (Watches)',
                location: 'Mumbai / Studio + Social',
                responseDeadline: 'Respond in 2 days',
                status: 'Pending',
                brief: 'Showcase the new brushed-titanium dress watch in an editorial evening reel and attend the flagship boutique unveiling.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'binv-2',
                campaignTitle: 'Lumina Glow Serum — Winter Campaign',
                brandName: 'Lumina Botanicals',
                industry: 'Skincare & Beauty',
                format: 'Reels & Stories',
                deliverables: '3 Skincare Routine Reels + 1 YouTube Short',
                compensation: '₹65,000',
                commissionNote: 'Includes ₹15,000 PR hamper',
                timeline: '10 Dec – 24 Dec 2026',
                exclusivity: '45 days (Facial Serums)',
                location: 'Remote / Home Studio',
                responseDeadline: 'Respond in 4 days',
                status: 'Pending',
                brief: 'Authentic morning-to-set skincare routine highlighting hydration under studio lighting.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'binv-3',
                campaignTitle: 'StrideX Flagship Store Opening',
                brandName: 'StrideX Athleisure',
                industry: 'Sportswear & Fashion',
                format: 'Store Appearance',
                deliverables: '2-Hour Store Ribbon Cutting + Meet & Greet + 2 Reels',
                compensation: '₹90,000',
                commissionNote: 'Travel & styling wardrobe provided',
                timeline: '19 Dec 2026',
                exclusivity: '15 days (Athleisure)',
                location: 'DLF Avenue, New Delhi',
                responseDeadline: 'Respond in 5 days',
                status: 'Pending',
                brief: 'Lead the flagship store opening in South Delhi with fan photo-ops and a styling reel.',
                counterFee: '',
                counterNote: ''
            },
            {
                id: 'binv-4',
                campaignTitle: 'Veloce Studio ANC — Creator Series',
                brandName: 'Veloce Audio',
                industry: 'Consumer Tech',
                format: 'TV / Digital Ad',
                deliverables: '1-Day Commercial Shoot + 30s Digital Cut',
                compensation: '₹1,25,000',
                commissionNote: '6-month digital usage rights included',
                timeline: '06 Jan 2027',
                exclusivity: '60 days (Audio)',
                location: 'Bengaluru Studio',
                responseDeadline: 'Confirmed',
                status: 'Accepted',
                brief: 'Feature as the lead creator in Veloce Audio’s Q1 digital commercial for noise-cancelling studio headphones.',
                counterFee: '',
                counterNote: ''
            }
        ],
        network: [
            {
                id: 'net-1',
                name: 'Meera Nair',
                role: 'Casting Director • Mumbai',
                connected: true,
                status: 'mutual',
                chatContext: 'Audition: Lead Role',
                unreadCount: 1,
                blocked: false,
                reported: false,
                photoUrl: '/images/avatars/director-female-1.jpg',
                videos: [
                    { id: 'net1-v1', title: 'Casting Process & Audition Tips', category: 'Casting Brief', fileName: 'meera-audition-tips.mp4', sizeLabel: '14.8 MB • 2:10', uploadedAt: '3 days ago' },
                    { id: 'net1-v2', title: 'Q1 Web Series Casting Call Intro', category: 'Project Reel', fileName: 'meera-q1-casting.mp4', sizeLabel: '8.9 MB • 1:05', uploadedAt: '1 week ago' }
                ]
            },
            {
                id: 'net-2',
                name: 'Rohan Kapoor',
                role: 'Executive Producer • RedFrame',
                connected: true,
                status: 'mutual',
                chatContext: 'Campaign: Brand Collab',
                unreadCount: 0,
                blocked: false,
                reported: false,
                photoUrl: '/images/avatars/producer-male-1.jpg',
                videos: [
                    { id: 'net2-v1', title: 'RedFrame Originals 2026 Slate', category: 'Production Reel', fileName: 'redframe-2026-slate.mp4', sizeLabel: '22.4 MB • 2:45', uploadedAt: '5 days ago' }
                ]
            },
            {
                id: 'net-3',
                name: 'Ananya Verma',
                role: 'Talent Manager • Starlight',
                connected: false,
                status: 'pending_incoming',
                chatContext: 'Booking: Live Event',
                unreadCount: 1,
                blocked: false,
                reported: false,
                photoUrl: '/images/avatars/actor-female-1.jpg',
                videos: [
                    { id: 'net3-v1', title: 'Starlight Talent Showcase', category: 'Showcase', fileName: 'starlight-showcase.mp4', sizeLabel: '17.3 MB • 2:20', uploadedAt: '1 week ago' }
                ]
            },
            {
                id: 'net-4',
                name: 'Vikram Sethi',
                role: 'Director of Photography',
                connected: false,
                status: 'not_connected',
                chatContext: 'General Networking',
                unreadCount: 0,
                blocked: false,
                reported: false,
                photoUrl: '/images/avatars/actor-male-1.jpg',
                videos: [
                    { id: 'net4-v1', title: 'Anamorphic Cinematography Reel', category: 'Showreel', fileName: 'vikram-dop-reel.mp4', sizeLabel: '26.1 MB • 3:00', uploadedAt: '4 days ago' },
                    { id: 'net4-v2', title: 'Night Shoot Lighting Breakdown', category: 'Behind the Scenes', fileName: 'vikram-lighting-bts.mp4', sizeLabel: '11.5 MB • 1:30', uploadedAt: '2 weeks ago' }
                ]
            }
        ],
        chatSettings: {
            readReceipts: true
        },
        networkChats: {
            'net-1': [
                { sender: 'peer', text: 'Hi! Loved your latest showreel. Let us stay in touch for upcoming casting calls.', time: 'Yesterday, 4:15 PM', status: 'Read' }
            ],
            'net-2': [
                { sender: 'peer', text: 'Thanks for connecting! We are casting for Q1 web series roles soon.', time: '2 days ago', status: 'Read' }
            ]
        },
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
                added: false,
                videos: [
                    { id: 'tm1-v1', title: 'OTT Thriller Teaser Cut', category: 'Director Reel', fileName: 'siddharth-thriller-cut.mp4', sizeLabel: '19.2 MB • 2:15', uploadedAt: '2 days ago' },
                    { id: 'tm1-v2', title: 'Indie Short Film Trailer', category: 'Trailer', fileName: 'siddharth-indie-trailer.mp4', sizeLabel: '12.0 MB • 1:25', uploadedAt: '2 weeks ago' }
                ]
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
                added: false,
                videos: [
                    { id: 'tm2-v1', title: 'Dramatic Screen Showreel 2026', category: 'Showreel', fileName: 'priya-screen-reel.mp4', sizeLabel: '15.6 MB • 1:50', uploadedAt: '4 days ago' },
                    { id: 'tm2-v2', title: 'Theatre Monologue Live', category: 'Monologue', fileName: 'priya-theatre-mono.mp4', sizeLabel: '10.1 MB • 1:18', uploadedAt: '3 weeks ago' }
                ]
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
                added: false,
                videos: [
                    { id: 'tm3-v1', title: 'Commercial & Music Video Reel', category: 'Showreel', fileName: 'arjun-dop-commercial.mp4', sizeLabel: '24.3 MB • 2:40', uploadedAt: '6 days ago' }
                ]
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
                added: false,
                videos: [
                    { id: 'tm4-v1', title: 'Actor Showreel Editing Samples', category: 'Edit Reel', fileName: 'kavya-edit-samples.mp4', sizeLabel: '16.8 MB • 2:05', uploadedAt: '3 days ago' },
                    { id: 'tm4-v2', title: 'Action Trailer Pacing Cut', category: 'Trailer', fileName: 'kavya-action-cut.mp4', sizeLabel: '13.4 MB • 1:35', uploadedAt: '1 week ago' }
                ]
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
                added: false,
                videos: [
                    { id: 'tm5-v1', title: 'Brand Campaign Highlights 2026', category: 'Campaign Reel', fileName: 'devansh-brand-reel.mp4', sizeLabel: '14.0 MB • 1:45', uploadedAt: '5 days ago' }
                ]
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
                added: false,
                videos: [
                    { id: 'tm6-v1', title: 'Animation & Dubbing Voice Reel', category: 'Voice & Video', fileName: 'zoya-dubbing-reel.mp4', sizeLabel: '9.8 MB • 1:30', uploadedAt: '1 week ago' }
                ]
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
        availabilityDates: {
            '2026-11-28': { status: 'available', note: 'Open for Auditions' },
            '2026-11-29': { status: 'available', note: 'Full Day Available' },
            '2026-12-02': { status: 'blocked', note: 'Commercial Shoot' },
            '2026-12-05': { status: 'blocked', note: 'Out of City' }
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
            const parsed = JSON.parse(raw);
            return {
                ...structuredClone(DEFAULT_STATE),
                ...parsed,
                auditionInvitations: Array.isArray(parsed.auditionInvitations) && parsed.auditionInvitations.length
                    ? parsed.auditionInvitations
                    : structuredClone(DEFAULT_STATE.auditionInvitations),
                eventInvitations: Array.isArray(parsed.eventInvitations) && parsed.eventInvitations.length
                    ? parsed.eventInvitations
                    : structuredClone(DEFAULT_STATE.eventInvitations),
                brandInvitations: Array.isArray(parsed.brandInvitations) && parsed.brandInvitations.length
                    ? parsed.brandInvitations
                    : structuredClone(DEFAULT_STATE.brandInvitations),
                planHistory: Array.isArray(parsed.planHistory) && parsed.planHistory.length
                    ? parsed.planHistory
                    : structuredClone(DEFAULT_STATE.planHistory),
                availabilityDates: parsed.availabilityDates && typeof parsed.availabilityDates === 'object'
                    ? parsed.availabilityDates
                    : structuredClone(DEFAULT_STATE.availabilityDates)
            };
        } catch {
            return structuredClone(DEFAULT_STATE);
        }
    }

    let state = loadState();
    let appStatusFilter = 'All';
    let auditionInvFilter = 'All';
    let eventInvFilter = 'All';
    let brandInvFilter = 'All';
    let activeInvModal = null;
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

    function getInvitationBadgeClass(status) {
        if (status === 'Accepted') return 'accepted';
        if (status === 'Negotiating') return 'negotiating';
        if (status === 'Declined') return 'declined';
        return 'pending';
    }

    function updateInvitationBadges() {
        const pendingAuditions = state.auditionInvitations.filter((i) => i.status === 'Pending').length;
        const pendingEvents = state.eventInvitations.filter((i) => i.status === 'Pending').length;
        const pendingBrands = state.brandInvitations.filter((i) => i.status === 'Pending').length;

        const auditionBadge = document.getElementById('audition-inv-nav-badge');
        const eventBadge = document.getElementById('event-inv-nav-badge');
        const brandBadge = document.getElementById('brand-inv-nav-badge');
        const auditionMeta = document.getElementById('audition-inv-header-meta');
        const eventMeta = document.getElementById('event-inv-header-meta');
        const brandMeta = document.getElementById('brand-inv-header-meta');

        if (auditionBadge) auditionBadge.textContent = String(pendingAuditions);
        if (eventBadge) eventBadge.textContent = String(pendingEvents);
        if (brandBadge) brandBadge.textContent = String(pendingBrands);
        if (auditionMeta) auditionMeta.textContent = `${pendingAuditions} Pending Audition${pendingAuditions === 1 ? '' : 's'}`;
        if (eventMeta) eventMeta.textContent = `${pendingEvents} Pending RSVP${pendingEvents === 1 ? '' : 's'}`;
        if (brandMeta) brandMeta.textContent = `${pendingBrands} Pending Offer${pendingBrands === 1 ? '' : 's'}`;
    }

    function renderInvitationSummary(containerId, items, acceptedLabel) {
        const container = document.getElementById(containerId);
        if (!container) return;
        const total = items.length;
        const pending = items.filter((i) => i.status === 'Pending').length;
        const accepted = items.filter((i) => i.status === 'Accepted').length;
        const negotiating = items.filter((i) => i.status === 'Negotiating').length;

        container.innerHTML = `
            <div class="ew-inv-stat">
                <div class="ew-inv-stat-val">${total}</div>
                <div class="ew-inv-stat-label">Total Invitations</div>
            </div>
            <div class="ew-inv-stat">
                <div class="ew-inv-stat-val">${pending}</div>
                <div class="ew-inv-stat-label">Pending Response</div>
            </div>
            <div class="ew-inv-stat">
                <div class="ew-inv-stat-val">${accepted}</div>
                <div class="ew-inv-stat-label">${escapeHtml(acceptedLabel)}</div>
            </div>
            <div class="ew-inv-stat">
                <div class="ew-inv-stat-val">${negotiating}</div>
                <div class="ew-inv-stat-label">In Negotiation</div>
            </div>
        `;
    }

    function renderAuditionInvitations() {
        updateInvitationBadges();
        renderInvitationSummary('audition-inv-summary', state.auditionInvitations, 'Confirmed Slots');
        const container = document.getElementById('audition-invitations-list');
        if (!container) return;

        const query = (document.getElementById('audition-inv-search')?.value || '').trim().toLowerCase();
        const modeFilter = document.getElementById('audition-inv-mode')?.value || 'All';

        const filtered = state.auditionInvitations.filter((inv) => {
            const matchesStatus = auditionInvFilter === 'All' || inv.status === auditionInvFilter;
            const matchesMode = modeFilter === 'All' || inv.auditionMode === modeFilter;
            const matchesQuery = !query ||
                inv.projectTitle.toLowerCase().includes(query) ||
                inv.castingHouse.toLowerCase().includes(query) ||
                inv.roleName.toLowerCase().includes(query) ||
                inv.location.toLowerCase().includes(query);
            return matchesStatus && matchesMode && matchesQuery;
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No audition invitations match your current filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((inv) => {
            const badgeCls = getInvitationBadgeClass(inv.status);
            const badgeLabel = inv.status === 'Negotiating' ? 'Reschedule Requested' : inv.status;
            const isDecided = inv.status === 'Accepted' || inv.status === 'Declined';
            return `
                <article class="ew-panel ew-inv-card">
                    <div class="ew-inv-header">
                        <div>
                            <span class="ew-badge ${badgeCls}">${escapeHtml(badgeLabel)}</span>
                            <h3 class="ew-inv-title">${escapeHtml(inv.projectTitle)}</h3>
                            <div class="ew-app-meta">${escapeHtml(inv.castingHouse)} • ${escapeHtml(inv.auditionMode)}</div>
                        </div>
                        <span class="ew-app-meta">${escapeHtml(inv.rsvpDeadline)}</span>
                    </div>
                    <p style="margin: 0;">${escapeHtml(inv.scriptNote)}</p>
                    <div class="ew-opp-specs" style="margin-bottom: 4px;">
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Role</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.roleName)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Compensation</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.compensation)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Audition Slot</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.slotDate)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Location / Mode</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.location)}</span>
                        </div>
                    </div>
                    ${inv.counterFee ? `<div class="ew-inv-counter-note"><strong>Reschedule / Note Sent:</strong> ${escapeHtml(inv.counterFee)} — ${escapeHtml(inv.counterNote)}</div>` : ''}
                    <div class="ew-inv-actions">
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-inv-accept="audition:${escapeHtml(inv.id)}" ${inv.status === 'Accepted' ? 'disabled' : ''}>
                            ${inv.status === 'Accepted' ? 'Slot Confirmed' : 'Confirm Slot'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-negotiate="audition:${escapeHtml(inv.id)}" ${isDecided ? 'disabled' : ''}>
                            Reschedule
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-decline="audition:${escapeHtml(inv.id)}" ${inv.status === 'Declined' ? 'disabled' : ''}>
                            ${inv.status === 'Declined' ? 'Declined' : 'Decline'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-view="audition:${escapeHtml(inv.id)}">
                            Audition Brief
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderEventInvitations() {
        updateInvitationBadges();
        renderInvitationSummary('event-inv-summary', state.eventInvitations, 'Confirmed Events');
        const container = document.getElementById('event-invitations-list');
        if (!container) return;

        const query = (document.getElementById('event-inv-search')?.value || '').trim().toLowerCase();
        const typeFilter = document.getElementById('event-inv-type')?.value || 'All';

        const filtered = state.eventInvitations.filter((inv) => {
            const matchesStatus = eventInvFilter === 'All' || inv.status === eventInvFilter;
            const matchesType = typeFilter === 'All' || inv.eventType === typeFilter;
            const matchesQuery = !query ||
                inv.eventTitle.toLowerCase().includes(query) ||
                inv.organizer.toLowerCase().includes(query) ||
                inv.venue.toLowerCase().includes(query) ||
                inv.city.toLowerCase().includes(query) ||
                inv.role.toLowerCase().includes(query);
            return matchesStatus && matchesType && matchesQuery;
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No event invitations match your current filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((inv) => {
            const badgeCls = getInvitationBadgeClass(inv.status);
            const isDecided = inv.status === 'Accepted' || inv.status === 'Declined';
            return `
                <article class="ew-panel ew-inv-card">
                    <div class="ew-inv-header">
                        <div>
                            <span class="ew-badge ${badgeCls}">${escapeHtml(inv.status)}</span>
                            <h3 class="ew-inv-title">${escapeHtml(inv.eventTitle)}</h3>
                            <div class="ew-app-meta">${escapeHtml(inv.organizer)} • ${escapeHtml(inv.eventType)}</div>
                        </div>
                        <span class="ew-app-meta">${escapeHtml(inv.rsvpDeadline)}</span>
                    </div>
                    <p style="margin: 0;">${escapeHtml(inv.notes)}</p>
                    <div class="ew-opp-specs" style="margin-bottom: 4px;">
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Invited Role</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.role)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Appearance Fee</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.fee)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Date &amp; Time</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.date)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Venue &amp; City</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.venue)}, ${escapeHtml(inv.city)}</span>
                        </div>
                    </div>
                    ${inv.counterFee ? `<div class="ew-inv-counter-note"><strong>Counter Offer Sent:</strong> ${escapeHtml(inv.counterFee)} — ${escapeHtml(inv.counterNote)}</div>` : ''}
                    <div class="ew-inv-actions">
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-inv-accept="event:${escapeHtml(inv.id)}" ${inv.status === 'Accepted' ? 'disabled' : ''}>
                            ${inv.status === 'Accepted' ? 'Accepted' : 'Accept Invite'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-negotiate="event:${escapeHtml(inv.id)}" ${isDecided ? 'disabled' : ''}>
                            Propose Fee
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-decline="event:${escapeHtml(inv.id)}" ${inv.status === 'Declined' ? 'disabled' : ''}>
                            ${inv.status === 'Declined' ? 'Declined' : 'Decline'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-view="event:${escapeHtml(inv.id)}">
                            Event Brief
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderBrandInvitations() {
        updateInvitationBadges();
        renderInvitationSummary('brand-inv-summary', state.brandInvitations, 'Active Campaigns');
        const container = document.getElementById('brand-invitations-list');
        if (!container) return;

        const query = (document.getElementById('brand-inv-search')?.value || '').trim().toLowerCase();
        const formatFilter = document.getElementById('brand-inv-format')?.value || 'All';

        const filtered = state.brandInvitations.filter((inv) => {
            const matchesStatus = brandInvFilter === 'All' || inv.status === brandInvFilter;
            const matchesFormat = formatFilter === 'All' || inv.format === formatFilter;
            const matchesQuery = !query ||
                inv.campaignTitle.toLowerCase().includes(query) ||
                inv.brandName.toLowerCase().includes(query) ||
                inv.industry.toLowerCase().includes(query) ||
                inv.deliverables.toLowerCase().includes(query);
            return matchesStatus && matchesFormat && matchesQuery;
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No brand promotion invitations match your current filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((inv) => {
            const badgeCls = getInvitationBadgeClass(inv.status);
            const isDecided = inv.status === 'Accepted' || inv.status === 'Declined';
            return `
                <article class="ew-panel ew-inv-card">
                    <div class="ew-inv-header">
                        <div>
                            <span class="ew-badge ${badgeCls}">${escapeHtml(inv.status)}</span>
                            <h3 class="ew-inv-title">${escapeHtml(inv.campaignTitle)}</h3>
                            <div class="ew-app-meta">${escapeHtml(inv.brandName)} • ${escapeHtml(inv.industry)}</div>
                        </div>
                        <span class="ew-app-meta">${escapeHtml(inv.responseDeadline)}</span>
                    </div>
                    <p style="margin: 0;">${escapeHtml(inv.brief)}</p>
                    <div class="ew-opp-specs" style="margin-bottom: 4px;">
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Campaign Format</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.format)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Compensation</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.compensation)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Deliverables</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.deliverables)}</span>
                        </div>
                        <div class="ew-opp-spec">
                            <span class="ew-opp-spec-label">Timeline &amp; Exclusivity</span>
                            <span class="ew-opp-spec-val">${escapeHtml(inv.timeline)} (${escapeHtml(inv.exclusivity)})</span>
                        </div>
                    </div>
                    <div class="ew-app-meta">${escapeHtml(inv.commissionNote)} • ${escapeHtml(inv.location)}</div>
                    ${inv.counterFee ? `<div class="ew-inv-counter-note"><strong>Counter Offer Sent:</strong> ${escapeHtml(inv.counterFee)} — ${escapeHtml(inv.counterNote)}</div>` : ''}
                    <div class="ew-inv-actions">
                        <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-inv-accept="brand:${escapeHtml(inv.id)}" ${inv.status === 'Accepted' ? 'disabled' : ''}>
                            ${inv.status === 'Accepted' ? 'Accepted' : 'Accept Campaign'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-negotiate="brand:${escapeHtml(inv.id)}" ${isDecided ? 'disabled' : ''}>
                            Counter Offer
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-decline="brand:${escapeHtml(inv.id)}" ${inv.status === 'Declined' ? 'disabled' : ''}>
                            ${inv.status === 'Declined' ? 'Declined' : 'Decline'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-inv-view="brand:${escapeHtml(inv.id)}">
                            Campaign Brief
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function findInvitation(ref) {
        const [kind, id] = String(ref || '').split(':');
        const list = kind === 'brand'
            ? state.brandInvitations
            : kind === 'audition'
                ? state.auditionInvitations
                : state.eventInvitations;
        const item = list.find((entry) => entry.id === id);
        return item ? { kind, item } : null;
    }

    function respondToInvitation(ref, newStatus) {
        const found = findInvitation(ref);
        if (!found) return;
        const { kind, item } = found;
        item.status = newStatus;
        if (newStatus === 'Accepted') {
            if (kind === 'event' || kind === 'audition') item.rsvpDeadline = 'Confirmed';
            else item.responseDeadline = 'Confirmed';
        }
        const label = kind === 'event'
            ? item.eventTitle
            : kind === 'audition'
                ? item.projectTitle
                : item.campaignTitle;
        const kindLabel = kind === 'event' ? 'Event' : kind === 'audition' ? 'Audition' : 'Brand';
        state.notifications.unshift({
            id: `n-${Date.now()}`,
            text: `${kindLabel} invitation "${label}" marked as ${newStatus}.`,
            time: 'Just now',
            unread: true
        });
        saveState();
        renderNotifications();
        renderAuditionInvitations();
        renderEventInvitations();
        renderBrandInvitations();
        showToast(`${newStatus}: ${label}`);
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

    function getConnectionStatus(person) {
        if (person.status) return person.status;
        return person.connected ? 'mutual' : 'not_connected';
    }

    let inboxFilterMode = 'all';

    function getUnreadTotal() {
        return state.network.reduce((sum, p) => {
            if (p.blocked) return sum;
            const status = getConnectionStatus(p);
            const msgUnread = typeof p.unreadCount === 'number' ? p.unreadCount : 0;
            const reqUnread = status === 'pending_incoming' ? 1 : 0;
            return sum + Math.max(msgUnread, reqUnread);
        }, 0);
    }

    function renderChatInbox() {
        const unreadTotal = getUnreadTotal();
        const navBadge = document.getElementById('network-nav-badge');
        if (navBadge) {
            navBadge.textContent = String(unreadTotal);
            navBadge.hidden = unreadTotal === 0;
        }

        const unreadPill = document.getElementById('inbox-unread-pill');
        if (unreadPill) {
            unreadPill.textContent = `${unreadTotal} Unread`;
        }

        const receiptsBtn = document.getElementById('chat-read-receipts-toggle');
        if (receiptsBtn) {
            const on = state.chatSettings?.readReceipts !== false;
            receiptsBtn.textContent = on ? 'Read Receipts: On' : 'Read Receipts: Off';
        }

        const container = document.getElementById('network-inbox-list');
        if (!container) return;

        const query = (document.getElementById('inbox-search')?.value || '').trim().toLowerCase();
        const filtered = state.network.filter((person) => {
            const status = getConnectionStatus(person);
            const ctx = person.chatContext || 'General Networking';
            const msgs = (state.networkChats && state.networkChats[person.id]) || [];
            const lastMsg = msgs.length ? msgs[msgs.length - 1].text : '';

            if (inboxFilterMode === 'unread' && !(person.unreadCount > 0 || status === 'pending_incoming')) return false;
            if (inboxFilterMode === 'requests' && status !== 'pending_incoming' && status !== 'pending_outgoing') return false;
            if (inboxFilterMode === 'blocked' && !person.blocked) return false;
            if (['Audition', 'Campaign', 'Booking'].includes(inboxFilterMode) && !ctx.toLowerCase().includes(inboxFilterMode.toLowerCase())) return false;
            if (inboxFilterMode !== 'blocked' && person.blocked) return false;

            if (!query) return true;
            return person.name.toLowerCase().includes(query)
                || person.role.toLowerCase().includes(query)
                || ctx.toLowerCase().includes(query)
                || lastMsg.toLowerCase().includes(query);
        });

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No conversations match your current search or filter.</p></div>`;
            return;
        }

        container.innerHTML = filtered.map((person) => {
            const status = getConnectionStatus(person);
            const msgs = (state.networkChats && state.networkChats[person.id]) || [];
            const lastMsg = msgs.length ? msgs[msgs.length - 1] : null;
            const unread = !person.blocked && (person.unreadCount > 0 || status === 'pending_incoming');
            const photo = person.photoUrl || DEFAULT_STATE.network.find((n) => n.id === person.id || n.name === person.name)?.photoUrl || '/images/avatars/director-female-1.jpg';
            const ctx = person.chatContext || 'General Networking';

            let actionBtns = '';
            if (person.blocked) {
                actionBtns = `<button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-unblock-chat="${escapeHtml(person.id)}">Unblock</button>`;
            } else if (status === 'mutual') {
                actionBtns = `<button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-open-chat="${escapeHtml(person.id)}">Open Chat</button>`;
            } else if (status === 'pending_incoming') {
                actionBtns = `
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-approve-incoming="${escapeHtml(person.id)}">Accept</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-decline-incoming="${escapeHtml(person.id)}">Reject</button>
                `;
            } else if (status === 'pending_outgoing') {
                actionBtns = `<span class="ew-badge pending">Request Sent</span>`;
            } else {
                actionBtns = `<button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-start-conversation="${escapeHtml(person.id)}">Start Conversation</button>`;
            }

            return `
                <article class="ew-inbox-item ${unread ? 'unread' : ''}">
                    <div class="ew-inbox-left">
                        <img class="ew-network-avatar" src="${escapeHtml(photo)}" alt="${escapeHtml(person.name)}">
                        <div style="min-width: 0;">
                            <div class="ew-inbox-meta-row">
                                <strong>${escapeHtml(person.name)}</strong>
                                <span class="ew-context-pill">${escapeHtml(ctx)}</span>
                                ${unread ? `<span class="ew-unread-pill">${person.unreadCount || 1} New</span>` : ''}
                                ${person.blocked ? `<span class="ew-badge closed">Blocked</span>` : ''}
                            </div>
                            <div class="ew-app-meta">${lastMsg ? `${escapeHtml(lastMsg.text)} • ${escapeHtml(lastMsg.time)}` : escapeHtml(person.role)}</div>
                        </div>
                    </div>
                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                        ${actionBtns}
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderNetwork() {
        renderChatInbox();
        const container = document.getElementById('network-list');
        if (!container) return;

        const mutualCount = state.network.filter((p) => getConnectionStatus(p) === 'mutual').length;
        const pendingOutCount = state.network.filter((p) => getConnectionStatus(p) === 'pending_outgoing').length;
        const pendingInCount = state.network.filter((p) => getConnectionStatus(p) === 'pending_incoming').length;

        const summaryBadge = document.getElementById('network-connections-summary');
        if (summaryBadge) {
            summaryBadge.textContent = `${mutualCount} Mutually Connected`;
        }

        const summaryStrip = document.getElementById('network-summary-strip');
        if (summaryStrip) {
            summaryStrip.innerHTML = `
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">${state.network.length}</div>
                    <div class="ew-inv-stat-label">Total People</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">${mutualCount}</div>
                    <div class="ew-inv-stat-label">Mutually Approved</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">${pendingOutCount + pendingInCount}</div>
                    <div class="ew-inv-stat-label">Pending Approval</div>
                </div>
                <div class="ew-inv-stat">
                    <div class="ew-inv-stat-val">${mutualCount}</div>
                    <div class="ew-inv-stat-label">Chat Unlocked</div>
                </div>
            `;
        }

        container.innerHTML = state.network.map((person) => {
            const status = getConnectionStatus(person);
            let badgeClass = 'closed';
            let badgeText = 'Not Connected';
            let actionsHtml = '';

            if (status === 'mutual') {
                badgeClass = 'accepted';
                badgeText = 'Mutually Connected';
                actionsHtml = `
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-open-chat="${escapeHtml(person.id)}">Chat</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-connect="${escapeHtml(person.id)}">Disconnect</button>
                `;
            } else if (status === 'pending_outgoing') {
                badgeClass = 'pending';
                badgeText = 'Pending Their Approval';
                actionsHtml = `
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" disabled title="Chat unlocks after both ends approve">Chat Locked</button>
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-approve-peer="${escapeHtml(person.id)}">Approve (Peer)</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-toggle-connect="${escapeHtml(person.id)}">Cancel</button>
                `;
            } else if (status === 'pending_incoming') {
                badgeClass = 'negotiating';
                badgeText = 'Incoming Request';
                actionsHtml = `
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-approve-incoming="${escapeHtml(person.id)}">Approve Connection</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-decline-incoming="${escapeHtml(person.id)}">Decline</button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" disabled title="Approve connection to unlock chat">Chat Locked</button>
                `;
            } else {
                actionsHtml = `
                    <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" data-start-conversation="${escapeHtml(person.id)}">Start Conversation</button>
                `;
            }

            return `
                <article class="ew-panel ew-card-clickable" data-open-profile="network:${escapeHtml(person.id)}">
                    <div class="ew-section-head" style="margin-bottom: 10px;">
                        <span class="ew-badge ${badgeClass}">${badgeText}</span>
                    </div>
                    <div class="ew-network-head">
                        <img class="ew-network-avatar" src="${escapeHtml(person.photoUrl || DEFAULT_STATE.network.find((n) => n.id === person.id || n.name === person.name)?.photoUrl || '/images/avatars/director-female-1.jpg')}" alt="${escapeHtml(person.name)}">
                        <div>
                            <h3 style="margin-bottom: 2px;">${escapeHtml(person.name)}</h3>
                            <p style="margin: 0;">${escapeHtml(person.role)}</p>
                        </div>
                    </div>
                    <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px;">
                        ${actionsHtml}
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-open-profile="network:${escapeHtml(person.id)}">View Profile</button>
                    </div>
                </article>
            `;
        }).join('');
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

        container.innerHTML = filtered.map((tm, idx) => {
            const fallbackPhotos = [
                '/images/avatars/producer-male-1.jpg',
                '/images/avatars/actor-female-1.jpg',
                '/images/avatars/actor-male-1.jpg',
                '/images/avatars/director-female-1.jpg',
                '/images/talent-actor.jpg',
                '/images/talent-model.jpg'
            ];
            const tmPhoto = tm.photoUrl || fallbackPhotos[idx % fallbackPhotos.length];
            return `
            <article class="ew-panel ew-card-clickable" data-open-profile="teammate:${escapeHtml(tm.id)}">
                <div class="ew-suggestion-meta">
                    <span class="ew-reason-tag ${escapeHtml(tm.reasonType)}">${escapeHtml(tm.reasonLabel)}</span>
                    <span class="ew-badge closed">${escapeHtml(tm.profession)}</span>
                </div>
                <div class="ew-network-head" style="margin-bottom: 8px;">
                    <img class="ew-network-avatar" src="${escapeHtml(tmPhoto)}" alt="${escapeHtml(tm.name)}">
                    <div>
                        <h3 style="margin-bottom: 2px;">${escapeHtml(tm.name)}</h3>
                        <div class="ew-app-meta">${escapeHtml(tm.city)} • Interests: ${escapeHtml(tm.interests)}</div>
                    </div>
                </div>
                <p style="margin-bottom: 14px;">${escapeHtml(tm.detail)}</p>
                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                    <button type="button" class="ew-btn ${tm.added ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-add-teammate="${escapeHtml(tm.id)}" ${tm.added ? 'disabled' : ''}>
                        ${tm.added ? 'Added to Network' : 'Add Teammate'}
                    </button>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-open-profile="teammate:${escapeHtml(tm.id)}">View Profile</button>
                </div>
            </article>
        `;
        }).join('');
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
        const showreelEl = document.getElementById('profile-showreel');
        if (showreelEl) showreelEl.value = state.profileExtras.showreelUrl || '';
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
        const titleInput = document.getElementById('profile-video-title');
        const categorySelect = document.getElementById('profile-video-category');
        const fileInput = document.getElementById('profile-video-file');

        if (alertBox && alertText) {
            alertBox.hidden = !limitReached;
            if (limitReached) {
                alertText.textContent = `Your ${activePlan.name} (${activePlan.shortLabel}) allows up to ${maxVideos} ${maxVideos === 1 ? 'video' : 'videos'}. Upgrade to ${state.currentPlan === 'free' ? 'Premium (₹199/mo) for 20 videos or Pro Plan (₹399/mo) for 50' : 'Pro Plan (₹399/mo) for 50'} uploads.`;
            }
        }
        if (submitBtn) submitBtn.disabled = limitReached;
        if (titleInput) titleInput.disabled = limitReached;
        if (categorySelect) categorySelect.disabled = limitReached;
        if (fileInput) fileInput.disabled = limitReached;

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
        const followingCount = state.otherActors.filter((a) => a.following).length;
        const navBadge = document.getElementById('categories-nav-badge');
        if (navBadge) navBadge.textContent = String(followingCount);

        const container = document.getElementById('categories-grid');
        if (!container) return;

        const query = (document.getElementById('category-search')?.value || '').trim().toLowerCase();
        const cityFilter = document.getElementById('actor-dir-city-filter')?.value || 'all';
        const planFilter = document.getElementById('actor-dir-plan-filter')?.value || 'all';

        const filtered = state.otherActors
            .filter((actor) => {
                const matchesMode = categoryFilterMode === 'all' || actor.following;
                const matchesCity = cityFilter === 'all' || actor.city === cityFilter;
                const matchesPlan = planFilter === 'all' || actor.planId === planFilter;
                const matchesQuery = !query ||
                    actor.name.toLowerCase().includes(query) ||
                    actor.category.toLowerCase().includes(query) ||
                    actor.city.toLowerCase().includes(query) ||
                    actor.credits.toLowerCase().includes(query);
                return matchesMode && matchesCity && matchesPlan && matchesQuery;
            })
            .sort((a, b) => a.name.localeCompare(b.name));

        const summary = document.getElementById('categories-following-summary');
        if (summary) {
            summary.textContent = `${filtered.length} ${filtered.length === 1 ? 'actor' : 'actors'} • ${followingCount} Following`;
        }

        if (filtered.length === 0) {
            container.innerHTML = `<div class="ew-panel"><p>No actors match your current name search or filter.</p></div>`;
            return;
        }

        const planLabels = {
            free: 'Basic (Free)',
            premium: 'Premium (₹199)',
            pro: 'Pro Elite (₹399)'
        };

        container.innerHTML = filtered.map((actor) => {
            const fallbackActor = DEFAULT_STATE.otherActors.find((a) => a.id === actor.id);
            const hasRealPhoto = actor.photoUrl && !String(actor.photoUrl).startsWith('data:image/svg+xml');
            const photoSrc = hasRealPhoto ? actor.photoUrl : (fallbackActor ? fallbackActor.photoUrl : '');
            const followerCount = typeof actor.followers === 'number' ? actor.followers : (fallbackActor?.followers ?? 0);
            return `
                <article class="ew-actor-card ew-card-clickable" data-open-profile="actor:${escapeHtml(actor.id)}">
                    <div class="ew-actor-left">
                        <img class="ew-actor-avatar ew-photo-clickable" src="${escapeHtml(photoSrc)}" alt="${escapeHtml(actor.name)}" title="Click to enlarge">
                        <div>
                            <h3>
                                ${escapeHtml(actor.name)}
                                <span class="ew-plan-badge-inline ${escapeHtml(actor.planId)}">${escapeHtml(planLabels[actor.planId] || actor.planId)}</span>
                            </h3>
                            <div class="ew-app-meta">${escapeHtml(actor.category)} • ${escapeHtml(actor.city)} • ${escapeHtml(actor.credits)} • <strong>${followerCount.toLocaleString('en-IN')} Followers</strong></div>
                        </div>
                    </div>
                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                        <button type="button" class="ew-btn ${actor.following ? 'ew-btn-outline' : 'ew-btn-primary'} ew-btn-sm" data-toggle-follow-actor="${escapeHtml(actor.id)}">
                            ${actor.following ? 'Following' : 'Follow'}
                        </button>
                        <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-open-profile="actor:${escapeHtml(actor.id)}">View Profile</button>
                    </div>
                </article>
            `;
        }).join('');
    }

    function renderPlanHistory() {
        const tbody = document.getElementById('plan-history-tbody');
        const countEl = document.getElementById('plan-history-count');
        if (!tbody) return;

        const history = Array.isArray(state.planHistory) ? state.planHistory : [];
        if (countEl) {
            countEl.textContent = `${history.length} ${history.length === 1 ? 'record' : 'records'}`;
        }

        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 18px;">No previous plan history available.</td></tr>`;
            return;
        }

        const badgeClassMap = {
            Active: 'shortlisted',
            Completed: 'review',
            Expired: 'closed'
        };

        tbody.innerHTML = history.map((item) => `
            <tr>
                <td><strong>${escapeHtml(item.planName)}</strong></td>
                <td>${escapeHtml(item.price)}</td>
                <td>${escapeHtml(item.period)}</td>
                <td>${escapeHtml(item.paymentMethod)}</td>
                <td><span class="ew-badge ${escapeHtml(badgeClassMap[item.status] || 'review')}">${escapeHtml(item.status)}</span></td>
            </tr>
        `).join('');
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

                if (opp.backendProjectId) {
                    const user = getSessionUser() || {};
                    const latestVideo = state.videos && state.videos[0];
                    fetch('/api/workflow/professional/apply', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            projectId: opp.backendProjectId,
                            email: user.email || 'actor@entertainmentworld.com',
                            fullName: user.fullName || 'Entertainment Professional',
                            category: user.professionalCategory || opp.category,
                            city: state.profileExtras?.city || 'Mumbai',
                            phone: state.profileExtras?.phone || '',
                            roleName: opp.roleName || opp.title,
                            auditionSubmissionTitle: latestVideo ? latestVideo.title : '',
                            auditionVideoUrl: latestVideo ? latestVideo.fileName : ''
                        })
                    }).catch(() => {});
                }
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
                const status = getConnectionStatus(person);
                if (status === 'not_connected') {
                    person.status = 'pending_outgoing';
                    person.connected = false;
                    showToast(`Connection request sent to ${person.name}. Waiting for their approval.`);
                } else {
                    person.status = 'not_connected';
                    person.connected = false;
                    showToast(status === 'mutual' ? `Disconnected from ${person.name}` : `Cancelled request to ${person.name}`);
                }
                saveState();
                renderNetwork();
            }
            return;
        }

        const approvePeerBtn = e.target.closest('[data-approve-peer]');
        if (approvePeerBtn) {
            const person = state.network.find((n) => n.id === approvePeerBtn.dataset.approvePeer);
            if (person) {
                person.status = 'mutual';
                person.connected = true;
                state.notifications.unshift({
                    id: `n-${Date.now()}`,
                    text: `${person.name} approved your connection request. Chat is now unlocked!`,
                    time: 'Just now',
                    unread: true
                });
                saveState();
                renderNotifications();
                renderNetwork();
                showToast(`${person.name} approved! You can now chat.`);
            }
            return;
        }

        const approveIncomingBtn = e.target.closest('[data-approve-incoming]');
        if (approveIncomingBtn) {
            const person = state.network.find((n) => n.id === approveIncomingBtn.dataset.approveIncoming);
            if (person) {
                person.status = 'mutual';
                person.connected = true;
                state.notifications.unshift({
                    id: `n-${Date.now()}`,
                    text: `You approved ${person.name}'s connection request. Chat is now unlocked!`,
                    time: 'Just now',
                    unread: true
                });
                saveState();
                renderNotifications();
                renderNetwork();
                showToast(`Mutually connected with ${person.name}! Chat unlocked.`);
            }
            return;
        }

        const declineIncomingBtn = e.target.closest('[data-decline-incoming]');
        if (declineIncomingBtn) {
            const person = state.network.find((n) => n.id === declineIncomingBtn.dataset.declineIncoming);
            if (person) {
                person.status = 'not_connected';
                person.connected = false;
                saveState();
                renderNetwork();
                showToast(`Declined connection request from ${person.name}`);
            }
            return;
        }

        const chatBtn = e.target.closest('[data-open-chat]');
        if (chatBtn) {
            openChatModal(chatBtn.dataset.openChat);
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
                if (!Array.isArray(state.planHistory)) state.planHistory = [];
                state.planHistory.forEach((entry) => {
                    if (entry.status === 'Active') {
                        entry.status = 'Completed';
                    }
                });
                const invNum = String(state.planHistory.length + 1).padStart(3, '0');
                state.planHistory.unshift({
                    id: `ph-${Date.now()}`,
                    planName: chosen.name,
                    price: chosen.price === '₹0' ? '₹0' : `${chosen.price} / mo`,
                    period: 'Current • Switched just now',
                    paymentMethod: chosen.id === 'free' ? `Free Tier • #INV-2026-${invNum}` : `Online • #INV-2026-${invNum}`,
                    status: 'Active'
                });
                saveState();
                renderPlans();
                renderPlanHistory();
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
                const fallbackActor = DEFAULT_STATE.otherActors.find((a) => a.id === actor.id);
                const currentFollowers = typeof actor.followers === 'number' ? actor.followers : (fallbackActor?.followers ?? 0);
                actor.following = !actor.following;
                actor.followers = Math.max(0, currentFollowers + (actor.following ? 1 : -1));
                saveState();
                renderCategories();
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

        const auditionInvChip = e.target.closest('.ew-chip[data-audition-inv-filter]');
        if (auditionInvChip) {
            auditionInvFilter = auditionInvChip.dataset.auditionInvFilter;
            document.querySelectorAll('.ew-chip[data-audition-inv-filter]').forEach((c) => {
                c.classList.toggle('active', c === auditionInvChip);
            });
            renderAuditionInvitations();
            return;
        }

        const eventInvChip = e.target.closest('.ew-chip[data-event-inv-filter]');
        if (eventInvChip) {
            eventInvFilter = eventInvChip.dataset.eventInvFilter;
            document.querySelectorAll('.ew-chip[data-event-inv-filter]').forEach((c) => {
                c.classList.toggle('active', c === eventInvChip);
            });
            renderEventInvitations();
            return;
        }

        const brandInvChip = e.target.closest('.ew-chip[data-brand-inv-filter]');
        if (brandInvChip) {
            brandInvFilter = brandInvChip.dataset.brandInvFilter;
            document.querySelectorAll('.ew-chip[data-brand-inv-filter]').forEach((c) => {
                c.classList.toggle('active', c === brandInvChip);
            });
            renderBrandInvitations();
            return;
        }

        const invAcceptBtn = e.target.closest('[data-inv-accept]');
        if (invAcceptBtn) {
            respondToInvitation(invAcceptBtn.dataset.invAccept, 'Accepted');
            return;
        }

        const invDeclineBtn = e.target.closest('[data-inv-decline]');
        if (invDeclineBtn) {
            respondToInvitation(invDeclineBtn.dataset.invDecline, 'Declined');
            return;
        }

        const invNegotiateBtn = e.target.closest('[data-inv-negotiate]');
        if (invNegotiateBtn) {
            openInvitationModal(invNegotiateBtn.dataset.invNegotiate, true);
            return;
        }

        const invViewBtn = e.target.closest('[data-inv-view]');
        if (invViewBtn) {
            openInvitationModal(invViewBtn.dataset.invView, false);
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
                    connected: false,
                    status: 'pending_outgoing',
                    videos: structuredClone(tm.videos || [])
                });
                saveState();
                renderNetwork();
                renderTeammateSuggestions();
                showToast(`Connection request sent to ${tm.name}. Waiting for mutual approval.`);
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
            if (openToolBtn.dataset.openTool === 'Availability Calendar') {
                switchView('profile');
                document.getElementById('availability-calendar-panel')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                return;
            }
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
    document.getElementById('audition-inv-search')?.addEventListener('input', renderAuditionInvitations);
    document.getElementById('audition-inv-mode')?.addEventListener('change', renderAuditionInvitations);
    document.getElementById('event-inv-search')?.addEventListener('input', renderEventInvitations);
    document.getElementById('event-inv-type')?.addEventListener('change', renderEventInvitations);
    document.getElementById('brand-inv-search')?.addEventListener('input', renderBrandInvitations);
    document.getElementById('brand-inv-format')?.addEventListener('change', renderBrandInvitations);
    document.getElementById('category-search')?.addEventListener('input', renderCategories);
    document.getElementById('actor-dir-city-filter')?.addEventListener('change', renderCategories);
    document.getElementById('actor-dir-plan-filter')?.addEventListener('change', renderCategories);
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
            showreelUrl: document.getElementById('profile-showreel')?.value.trim() || state.profileExtras.showreelUrl || '',
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

    const INAPPROPRIATE_VIDEO_TERMS = [
        'nsfw', 'porn', 'xxx', 'nude', 'nudity', 'explicit', 'gore', 'violence',
        'violent', 'abuse', 'hate', 'terror', 'scam', 'pirated', '18+', 'adult',
        'sex', 'sexual', 'weapon', 'drugs', 'harassment', 'vulgar', 'obscene'
    ];

    let videoAnalysisHideTimer = null;

    function setVideoAnalysisStatus(mode, titleText, detailText) {
        const box = document.getElementById('profile-video-analysis-status');
        if (!box) return;
        clearTimeout(videoAnalysisHideTimer);
        if (!mode) {
            box.hidden = true;
            box.className = 'ew-video-analysis-box';
            box.innerHTML = '';
            return;
        }
        box.hidden = false;
        box.className = `ew-video-analysis-box ${mode}`;
        box.innerHTML = `<strong>${escapeHtml(titleText)}</strong><span>${escapeHtml(detailText)}</span>`;
        if (mode === 'approved') {
            videoAnalysisHideTimer = setTimeout(() => {
                setVideoAnalysisStatus(null);
            }, 4000);
        }
    }

    function checkTextForInappropriateTerms(text) {
        const normalized = String(text || '').toLowerCase();
        return INAPPROPRIATE_VIDEO_TERMS.find((term) => {
            if (term === '18+') return normalized.includes('18+');
            const escaped = term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            return new RegExp(`(^|[^a-z0-9])${escaped}([^a-z0-9]|$)`, 'i').test(normalized);
        }) || null;
    }

    function sampleAndAnalyzeVideoFrames(objectUrl) {
        return new Promise((resolve) => {
            const video = document.createElement('video');
            video.muted = true;
            video.playsInline = true;
            video.preload = 'auto';

            let settled = false;
            const finish = (result) => {
                if (settled) return;
                settled = true;
                clearTimeout(timeoutId);
                video.removeAttribute('src');
                video.load();
                resolve(result);
            };

            const timeoutId = setTimeout(() => {
                finish({ ok: false, reason: 'Video analysis timed out. Please upload a standard MP4, WEBM, or MOV file.' });
            }, 8000);

            video.onerror = () => {
                finish({ ok: false, reason: 'Unable to decode video stream. The file may be corrupted or in an unsupported format.' });
            };

            video.onloadedmetadata = async () => {
                if (!video.duration || !Number.isFinite(video.duration) || video.duration <= 0 || !video.videoWidth || !video.videoHeight) {
                    finish({ ok: false, reason: 'Invalid video stream: missing duration or video dimensions.' });
                    return;
                }

                const canvas = document.createElement('canvas');
                canvas.width = 160;
                canvas.height = 90;
                const ctx = canvas.getContext('2d', { willReadFrequently: true });
                if (!ctx) {
                    finish({ ok: true });
                    return;
                }

                const sampleFractions = [0.15, 0.5, 0.85];
                let highExposureFrames = 0;
                let blankFrames = 0;

                for (const frac of sampleFractions) {
                    const targetTime = Math.min(Math.max(0, video.duration - 0.05), Math.max(0, video.duration * frac));
                    await new Promise((res) => {
                        let done = false;
                        const onSeeked = () => {
                            if (done) return;
                            done = true;
                            clearTimeout(seekTimer);
                            video.removeEventListener('seeked', onSeeked);
                            res();
                        };
                        const seekTimer = setTimeout(onSeeked, 1500);
                        video.addEventListener('seeked', onSeeked, { once: true });
                        try {
                            video.currentTime = targetTime;
                        } catch {
                            onSeeked();
                        }
                    });

                    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
                    const { data } = ctx.getImageData(0, 0, canvas.width, canvas.height);
                    const totalPixels = canvas.width * canvas.height;
                    let skinPixels = 0;
                    let lumaSum = 0;
                    let lumaSqSum = 0;

                    for (let i = 0; i < data.length; i += 4) {
                        const r = data[i];
                        const g = data[i + 1];
                        const b = data[i + 2];
                        const y = 0.299 * r + 0.587 * g + 0.114 * b;
                        lumaSum += y;
                        lumaSqSum += y * y;

                        const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
                        const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;
                        if (y > 60 && cb >= 77 && cb <= 127 && cr >= 133 && cr <= 173) {
                            skinPixels += 1;
                        }
                    }

                    const meanLuma = lumaSum / totalPixels;
                    const variance = (lumaSqSum / totalPixels) - (meanLuma * meanLuma);
                    if (variance < 8) {
                        blankFrames += 1;
                    }
                    if (skinPixels / totalPixels > 0.68) {
                        highExposureFrames += 1;
                    }
                }

                if (highExposureFrames >= 2) {
                    finish({
                        ok: false,
                        reason: 'Visual content check flagged potentially explicit or inappropriate exposure in sampled frames.'
                    });
                    return;
                }

                if (blankFrames === sampleFractions.length) {
                    finish({
                        ok: false,
                        reason: 'Video appears completely blank or static across all sampled frames.'
                    });
                    return;
                }

                finish({ ok: true });
            };

            video.src = objectUrl;
        });
    }

    async function analyzeUploadedVideo(file, title) {
        const validExt = /\.(mp4|webm|mov|m4v|avi|mkv)$/i.test(file.name);
        if (!file.type.startsWith('video/') && !validExt) {
            return { ok: false, reason: 'Unsupported file type. Please upload a valid video file (MP4, WEBM, or MOV).' };
        }

        if (file.size <= 0) {
            return { ok: false, reason: 'Uploaded video file is empty.' };
        }

        if (file.size > 200 * 1024 * 1024) {
            return { ok: false, reason: 'Video exceeds the 200 MB maximum file size limit.' };
        }

        const matchedTerm = checkTextForInappropriateTerms(`${title} ${file.name}`);
        if (matchedTerm) {
            return {
                ok: false,
                reason: `Inappropriate or restricted keyword ("${matchedTerm}") detected in video title or filename.`
            };
        }

        const tempUrl = URL.createObjectURL(file);
        const frameResult = await sampleAndAnalyzeVideoFrames(tempUrl);
        if (!frameResult.ok) {
            URL.revokeObjectURL(tempUrl);
            return frameResult;
        }

        return { ok: true, objectUrl: tempUrl };
    }

    document.getElementById('profile-video-file')?.addEventListener('change', (e) => {
        const file = e.target.files && e.target.files[0];
        const titleInput = document.getElementById('profile-video-title');
        if (file && titleInput && !titleInput.value.trim()) {
            titleInput.value = file.name.replace(/\.[^/.]+$/, '').replace(/[_-]+/g, ' ').trim();
        }
    });

    document.getElementById('profile-video-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const limits = getActiveLimits();
        if (state.videos.length >= limits.maxVideos) {
            showToast(`Video upload limit reached (${limits.maxVideos}). Upgrade your plan to upload more.`);
            return;
        }

        const title = document.getElementById('profile-video-title').value.trim();
        const category = document.getElementById('profile-video-category').value;
        const fileInput = document.getElementById('profile-video-file');
        const submitBtn = document.getElementById('profile-video-submit');
        const file = fileInput.files && fileInput.files[0];
        if (!title || !file) return;

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Analyzing Video...';
        }
        setVideoAnalysisStatus(
            'analyzing',
            'Analyzing Uploaded Video...',
            `Scanning "${file.name}" metadata, stream integrity, and sampled frames for content safety.`
        );

        const analysis = await analyzeUploadedVideo(file, title);

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Upload & Analyze Video';
        }

        if (!analysis.ok) {
            setVideoAnalysisStatus(
                'rejected',
                'Video Rejected — Content Policy Violation',
                analysis.reason
            );
            fileInput.value = '';
            renderVideos();
            showToast(`Rejected "${title}": ${analysis.reason}`);
            return;
        }

        const vidId = `vid-${Date.now()}`;
        const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
        videoObjectUrls.set(vidId, analysis.objectUrl);

        state.videos.unshift({
            id: vidId,
            title,
            category,
            fileName: file.name,
            sizeLabel: `${sizeMb} MB`,
            uploadedAt: 'Just now • Verified Safe'
        });
        saveState();
        renderVideos();
        renderPlanUsage();
        e.target.reset();
        setVideoAnalysisStatus(
            'approved',
            'Video Approved & Uploaded',
            `"${title}" passed content analysis and has been added to your portfolio.`
        );
        showToast(`Uploaded "${title}" (Verified Safe)`);
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

    // Person / Actor Profile Details Modal
    const personModal = document.getElementById('ew-person-modal');
    const personAvatar = document.getElementById('ew-person-modal-avatar');
    const personName = document.getElementById('ew-person-modal-name');
    const personRole = document.getElementById('ew-person-modal-role');
    const personBody = document.getElementById('ew-person-modal-body');
    const personClose = document.getElementById('ew-person-modal-close');

    function closePersonModal() {
        if (personModal) personModal.hidden = true;
    }

    function renderProfileVideosSection(videos) {
        const list = Array.isArray(videos) ? videos : [];
        if (list.length === 0) {
            return `
                <div class="ew-profile-videos-section">
                    <div class="ew-profile-videos-header">
                        <strong>Uploaded Videos</strong>
                        <span class="ew-app-meta">0 Videos</span>
                    </div>
                    <p class="ew-app-meta" style="margin: 0;">No videos uploaded by this profile yet.</p>
                </div>
            `;
        }

        const cardsHtml = list.map((vid) => {
            const mediaHtml = vid.videoUrl
                ? `<video class="ew-video-player" src="${escapeHtml(vid.videoUrl)}" controls preload="metadata"></video>`
                : `<div class="ew-video-placeholder">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
                        <strong>${escapeHtml(vid.fileName)}</strong>
                        <span>${escapeHtml(vid.sizeLabel || '')}</span>
                   </div>`;

            return `
                <article class="ew-video-card">
                    ${mediaHtml}
                    <div class="ew-video-body">
                        <div>
                            <h4>${escapeHtml(vid.title)}</h4>
                            <div class="ew-app-meta">${escapeHtml(vid.category)} • ${escapeHtml(vid.uploadedAt)}</div>
                        </div>
                    </div>
                </article>
            `;
        }).join('');

        return `
            <div class="ew-profile-videos-section">
                <div class="ew-profile-videos-header">
                    <strong>Uploaded Videos</strong>
                    <span class="ew-app-meta">${list.length} ${list.length === 1 ? 'Video' : 'Videos'}</span>
                </div>
                <div class="ew-profile-videos-grid">${cardsHtml}</div>
            </div>
        `;
    }

    function openPersonModal(ref) {
        if (!personModal || !ref) return;
        const [kind, id] = ref.split(':');
        let title = '';
        let subtitle = '';
        let avatar = '';
        let bodyHtml = '';

        if (kind === 'network') {
            const p = state.network.find((x) => x.id === id);
            if (!p) return;
            const fallback = DEFAULT_STATE.network.find((n) => n.id === p.id || n.name === p.name)
                || DEFAULT_STATE.teammateSuggestions.find((t) => t.name === p.name);
            const videos = Array.isArray(p.videos) && p.videos.length ? p.videos : (fallback?.videos || []);
            const initials = p.name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
            title = p.name;
            subtitle = p.role;
            avatar = p.photoUrl || fallback?.photoUrl || makeActorAvatar(initials, '#1f2937', '#b45309');
            bodyHtml = `
                <div><strong>Connection Status:</strong> ${p.connected ? 'Connected' : 'Not Connected'}</div>
                <div><strong>Collaboration Focus:</strong> Casting calls, production coordination, and talent referrals.</div>
                <div><strong>Location &amp; Availability:</strong> Active on Entertainment World • Open to project inquiries.</div>
                ${renderProfileVideosSection(videos)}
            `;
        } else if (kind === 'teammate') {
            const tm = state.teammateSuggestions.find((x) => x.id === id);
            if (!tm) return;
            const fallback = DEFAULT_STATE.teammateSuggestions.find((t) => t.id === tm.id);
            const videos = Array.isArray(tm.videos) && tm.videos.length ? tm.videos : (fallback?.videos || []);
            const initials = tm.name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
            title = tm.name;
            subtitle = `${tm.profession} • ${tm.city}`;
            avatar = tm.photoUrl || '/images/avatars/actor-male-1.jpg' || makeActorAvatar(initials, '#312e81', '#db2777');
            bodyHtml = `
                <div><strong>Match Reason:</strong> ${escapeHtml(tm.reasonLabel)}</div>
                <div><strong>Profile Summary:</strong> ${escapeHtml(tm.detail)}</div>
                <div><strong>Shared Interests:</strong> ${escapeHtml(tm.interests)}</div>
                <div><strong>Network Status:</strong> ${tm.added ? 'Added to Your Connections' : 'Suggested Teammate'}</div>
                ${renderProfileVideosSection(videos)}
            `;
        } else if (kind === 'actor') {
            const actor = state.otherActors.find((x) => x.id === id);
            if (!actor) return;
            const fallback = DEFAULT_STATE.otherActors.find((a) => a.id === actor.id);
            const videos = Array.isArray(actor.videos) && actor.videos.length ? actor.videos : (fallback?.videos || []);
            const followerCount = typeof actor.followers === 'number' ? actor.followers : (fallback?.followers ?? 0);
            const hasRealPhoto = actor.photoUrl && !String(actor.photoUrl).startsWith('data:image/svg+xml');
            title = actor.name;
            subtitle = `${actor.category} • ${actor.city}`;
            avatar = hasRealPhoto ? actor.photoUrl : (fallback ? fallback.photoUrl : '');
            bodyHtml = `
                <div><strong>Plan Tier:</strong> ${actor.planId === 'pro' ? 'Pro Elite (₹399)' : actor.planId === 'premium' ? 'Premium (₹199)' : 'Basic (Free)'}</div>
                <div><strong>Followers:</strong> ${followerCount.toLocaleString('en-IN')}</div>
                <div><strong>Credits &amp; Portfolio:</strong> ${escapeHtml(actor.credits)}</div>
                <div><strong>City:</strong> ${escapeHtml(actor.city)}</div>
                <div><strong>Follow Status:</strong> ${actor.following ? 'Following' : 'Not Following'}</div>
                ${renderProfileVideosSection(videos)}
            `;
        } else if (kind === 'category') {
            const cat = state.categories.find((c) => c.id === id);
            if (!cat) return;
            const initials = cat.name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
            title = cat.name;
            subtitle = `${cat.followers.toLocaleString('en-IN')} followers • Category Talent`;
            avatar = makeActorAvatar(initials, '#0f766e', '#1d4ed8');
            const sampleTalent = state.otherActors.slice(0, 3).map((a) => `
                <div class="ew-app-card" style="padding: 10px 12px;">
                    <div>
                        <strong>${escapeHtml(a.name)}</strong>
                        <div class="ew-app-meta">${escapeHtml(a.category)} • ${escapeHtml(a.city)}</div>
                    </div>
                    <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" data-open-profile="actor:${escapeHtml(a.id)}">View Profile</button>
                </div>
            `).join('');
            bodyHtml = `
                <div>${escapeHtml(cat.description)}</div>
                <div><strong>Featured Profiles in ${escapeHtml(cat.name)}:</strong></div>
                <div class="ew-card-stack">${sampleTalent}</div>
            `;
        }

        if (personAvatar) personAvatar.src = avatar;
        if (personName) personName.textContent = title;
        if (personRole) personRole.textContent = subtitle;
        if (personBody) personBody.innerHTML = bodyHtml;
        personModal.hidden = false;
    }

    document.addEventListener('click', (e) => {
        if (e.target.closest('button:not([data-open-profile]), a, input, select')) return;
        const trigger = e.target.closest('[data-open-profile]');
        if (trigger) {
            openPersonModal(trigger.dataset.openProfile);
            return;
        }
        if (e.target === personModal) {
            closePersonModal();
        }
    });

    const invModal = document.getElementById('ew-inv-modal');
    const invModalClose = document.getElementById('ew-inv-modal-close');
    const invModalBadge = document.getElementById('ew-inv-modal-badge');
    const invModalTitle = document.getElementById('ew-inv-modal-title');
    const invModalSubtitle = document.getElementById('ew-inv-modal-subtitle');
    const invModalBody = document.getElementById('ew-inv-modal-body');
    const invNegotiateForm = document.getElementById('ew-inv-negotiate-form');
    const invCounterFeeInput = document.getElementById('ew-inv-counter-fee');
    const invCounterNoteInput = document.getElementById('ew-inv-counter-note');
    const invCancelNegotiate = document.getElementById('ew-inv-cancel-negotiate');
    const invModalActions = document.getElementById('ew-inv-modal-actions');

    function closeInvitationModal() {
        if (invModal) invModal.hidden = true;
        if (invNegotiateForm) invNegotiateForm.hidden = true;
        activeInvModal = null;
    }

    function openInvitationModal(ref, startNegotiate = false) {
        const found = findInvitation(ref);
        if (!found || !invModal) return;
        const { kind, item } = found;
        activeInvModal = ref;

        const badgeCls = getInvitationBadgeClass(item.status);
        invModalBadge.className = `ew-badge ${badgeCls}`;
        invModalBadge.textContent = kind === 'audition' && item.status === 'Negotiating' ? 'Reschedule Requested' : item.status;

        if (kind === 'audition') {
            invModalTitle.textContent = item.projectTitle;
            invModalSubtitle.textContent = `${item.castingHouse} • ${item.auditionMode}`;
            invModalBody.innerHTML = `
                <p style="margin:0;">${escapeHtml(item.scriptNote)}</p>
                <div class="ew-opp-specs" style="margin-bottom:0;">
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Role</span><span class="ew-opp-spec-val">${escapeHtml(item.roleName)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Compensation</span><span class="ew-opp-spec-val">${escapeHtml(item.compensation)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Audition Slot</span><span class="ew-opp-spec-val">${escapeHtml(item.slotDate)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Location / Mode</span><span class="ew-opp-spec-val">${escapeHtml(item.location)}</span></div>
                </div>
                ${item.counterFee ? `<div class="ew-inv-counter-note"><strong>Your Reschedule / Note:</strong> ${escapeHtml(item.counterFee)} — ${escapeHtml(item.counterNote)}</div>` : ''}
            `;
        } else if (kind === 'event') {
            invModalTitle.textContent = item.eventTitle;
            invModalSubtitle.textContent = `${item.organizer} • ${item.eventType}`;
            invModalBody.innerHTML = `
                <p style="margin:0;">${escapeHtml(item.notes)}</p>
                <div class="ew-opp-specs" style="margin-bottom:0;">
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Invited Role</span><span class="ew-opp-spec-val">${escapeHtml(item.role)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Appearance Fee</span><span class="ew-opp-spec-val">${escapeHtml(item.fee)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Date &amp; Time</span><span class="ew-opp-spec-val">${escapeHtml(item.date)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Venue</span><span class="ew-opp-spec-val">${escapeHtml(item.venue)}, ${escapeHtml(item.city)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Dress Code</span><span class="ew-opp-spec-val">${escapeHtml(item.dressCode)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Travel &amp; Stay</span><span class="ew-opp-spec-val">${escapeHtml(item.travelCovered)}</span></div>
                </div>
                ${item.counterFee ? `<div class="ew-inv-counter-note"><strong>Your Counter Offer:</strong> ${escapeHtml(item.counterFee)} — ${escapeHtml(item.counterNote)}</div>` : ''}
            `;
        } else {
            invModalTitle.textContent = item.campaignTitle;
            invModalSubtitle.textContent = `${item.brandName} • ${item.industry}`;
            invModalBody.innerHTML = `
                <p style="margin:0;">${escapeHtml(item.brief)}</p>
                <div class="ew-opp-specs" style="margin-bottom:0;">
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Format</span><span class="ew-opp-spec-val">${escapeHtml(item.format)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Compensation</span><span class="ew-opp-spec-val">${escapeHtml(item.compensation)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Deliverables</span><span class="ew-opp-spec-val">${escapeHtml(item.deliverables)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Timeline</span><span class="ew-opp-spec-val">${escapeHtml(item.timeline)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Exclusivity</span><span class="ew-opp-spec-val">${escapeHtml(item.exclusivity)}</span></div>
                    <div class="ew-opp-spec"><span class="ew-opp-spec-label">Location &amp; Terms</span><span class="ew-opp-spec-val">${escapeHtml(item.location)} (${escapeHtml(item.commissionNote)})</span></div>
                </div>
                ${item.counterFee ? `<div class="ew-inv-counter-note"><strong>Your Counter Offer:</strong> ${escapeHtml(item.counterFee)} — ${escapeHtml(item.counterNote)}</div>` : ''}
            `;
        }

        const isDecided = item.status === 'Accepted' || item.status === 'Declined';
        invNegotiateForm.hidden = !startNegotiate;
        if (startNegotiate) {
            invCounterFeeInput.value = item.counterFee || (kind === 'event' ? item.fee : kind === 'audition' ? item.slotDate : item.compensation);
            invCounterNoteInput.value = item.counterNote || '';
        }

        invModalActions.innerHTML = `
            <button type="button" class="ew-btn ew-btn-primary ew-btn-sm" id="ew-inv-modal-accept" ${item.status === 'Accepted' ? 'disabled' : ''}>
                ${item.status === 'Accepted' ? 'Accepted' : kind === 'audition' ? 'Confirm Slot' : 'Accept'}
            </button>
            <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" id="ew-inv-modal-negotiate" ${isDecided ? 'disabled' : ''}>
                ${kind === 'audition' ? 'Request Reschedule' : 'Propose Counter Offer'}
            </button>
            <button type="button" class="ew-btn ew-btn-outline ew-btn-sm" id="ew-inv-modal-decline" ${item.status === 'Declined' ? 'disabled' : ''}>
                ${item.status === 'Declined' ? 'Declined' : 'Decline'}
            </button>
        `;

        document.getElementById('ew-inv-modal-accept')?.addEventListener('click', () => {
            respondToInvitation(ref, 'Accepted');
            closeInvitationModal();
        });
        document.getElementById('ew-inv-modal-decline')?.addEventListener('click', () => {
            respondToInvitation(ref, 'Declined');
            closeInvitationModal();
        });
        document.getElementById('ew-inv-modal-negotiate')?.addEventListener('click', () => {
            invNegotiateForm.hidden = false;
            invCounterFeeInput.value = item.counterFee || (kind === 'event' ? item.fee : kind === 'audition' ? item.slotDate : item.compensation);
            invCounterNoteInput.value = item.counterNote || '';
            invCounterFeeInput.focus();
        });

        invModal.hidden = false;
    }

    invNegotiateForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!activeInvModal) return;
        const found = findInvitation(activeInvModal);
        if (!found) return;
        const feeVal = invCounterFeeInput.value.trim();
        const noteVal = invCounterNoteInput.value.trim();
        if (!feeVal || !noteVal) return;

        found.item.counterFee = feeVal;
        found.item.counterNote = noteVal;
        found.item.status = 'Negotiating';
        const title = found.kind === 'event'
            ? found.item.eventTitle
            : found.kind === 'audition'
                ? found.item.projectTitle
                : found.item.campaignTitle;
        state.notifications.unshift({
            id: `n-${Date.now()}`,
            text: `${found.kind === 'audition' ? 'Reschedule request' : 'Counter offer'} (${feeVal}) sent for "${title}".`,
            time: 'Just now',
            unread: true
        });
        saveState();
        renderNotifications();
        renderAuditionInvitations();
        renderEventInvitations();
        renderBrandInvitations();
        closeInvitationModal();
        showToast(`${found.kind === 'audition' ? 'Reschedule request' : 'Counter offer'} sent for "${title}"`);
    });

    invCancelNegotiate?.addEventListener('click', () => {
        if (invNegotiateForm) invNegotiateForm.hidden = true;
    });

    invModalClose?.addEventListener('click', closeInvitationModal);
    invModal?.addEventListener('click', (e) => {
        if (e.target === invModal) closeInvitationModal();
    });

    const chatModal = document.getElementById('ew-chat-modal');
    const chatClose = document.getElementById('ew-chat-modal-close');
    const chatNameEl = document.getElementById('ew-chat-name');
    const chatRoleEl = document.getElementById('ew-chat-role');
    const chatMessagesEl = document.getElementById('ew-chat-messages');
    const chatForm = document.getElementById('ew-chat-form');
    const chatInput = document.getElementById('ew-chat-input');
    const chatContextSelect = document.getElementById('ew-chat-context-select');
    const chatAttachInput = document.getElementById('ew-chat-attach');
    const chatAttachPreview = document.getElementById('ew-chat-attach-preview');
    const chatBlockBtn = document.getElementById('ew-chat-block-btn');
    const chatReportBtn = document.getElementById('ew-chat-report-btn');

    let activeChatPersonId = null;
    let pendingChatAttachment = null;
    const chatSendTimestamps = [];

    const SPAM_PATTERNS = [/bit\.ly\//i, /tinyurl\.com/i, /free[- ]money/i, /crypto[- ]airdrop/i, /win[- ]prize/i, /click[- ]here[- ]now/i];

    function clearPendingAttachment() {
        pendingChatAttachment = null;
        if (chatAttachInput) chatAttachInput.value = '';
        if (chatAttachPreview) {
            chatAttachPreview.hidden = true;
            chatAttachPreview.innerHTML = '';
        }
    }

    function closeChatModal() {
        if (chatModal) chatModal.hidden = true;
        activeChatPersonId = null;
        clearPendingAttachment();
    }

    function renderChatMessages(personId) {
        if (!chatMessagesEl) return;
        if (!state.networkChats) state.networkChats = {};
        const messages = state.networkChats[personId] || [];
        const readReceiptsOn = state.chatSettings?.readReceipts !== false;

        if (messages.length === 0) {
            chatMessagesEl.innerHTML = `<div class="ew-app-meta" style="text-align:center; margin:auto;">Permission granted! Start your conversation below.</div>`;
            return;
        }
        chatMessagesEl.innerHTML = messages.map((m) => {
            const receiptLabel = m.sender === 'me'
                ? (readReceiptsOn ? (m.status || 'Delivered') : 'Sent')
                : '';
            let attachHtml = '';
            if (m.attachment) {
                if (m.attachment.isImage && m.attachment.previewUrl) {
                    attachHtml = `<div class="ew-chat-attachment"><strong>${escapeHtml(m.attachment.name)}</strong><img src="${escapeHtml(m.attachment.previewUrl)}" alt="Attachment"></div>`;
                } else {
                    attachHtml = `<div class="ew-chat-attachment">📎 <strong>${escapeHtml(m.attachment.name)}</strong> (${escapeHtml(m.attachment.sizeLabel)})</div>`;
                }
            }
            return `
                <div class="ew-chat-bubble ${m.sender === 'me' ? 'me' : 'peer'}">
                    ${m.text ? `<div>${escapeHtml(m.text)}</div>` : ''}
                    ${attachHtml}
                    <div class="ew-chat-meta-line">
                        <span>${escapeHtml(m.time)}</span>
                        ${receiptLabel ? `<span>✓✓ ${escapeHtml(receiptLabel)}</span>` : ''}
                    </div>
                </div>
            `;
        }).join('');
        chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
    }

    function openChatModal(personId) {
        const person = state.network.find((n) => n.id === personId);
        if (!person) return;
        if (person.blocked) {
            showToast(`Access blocked: Unblock ${person.name} in the Chat Inbox to message.`);
            return;
        }
        if (getConnectionStatus(person) !== 'mutual') {
            showToast('Access denied: Chat unlocks only after recipient accepts your request.');
            return;
        }
        activeChatPersonId = person.id;
        person.unreadCount = 0;
        saveState();
        renderChatInbox();

        if (chatNameEl) chatNameEl.textContent = person.name;
        if (chatRoleEl) chatRoleEl.textContent = person.role;
        if (chatContextSelect) chatContextSelect.value = person.chatContext || 'General Networking';
        if (chatBlockBtn) chatBlockBtn.textContent = person.blocked ? 'Unblock' : 'Block';

        renderChatMessages(person.id);
        if (chatModal) chatModal.hidden = false;
        chatInput?.focus();
    }

    chatContextSelect?.addEventListener('change', () => {
        if (!activeChatPersonId) return;
        const person = state.network.find((n) => n.id === activeChatPersonId);
        if (person) {
            person.chatContext = chatContextSelect.value;
            saveState();
            renderChatInbox();
            showToast(`Linked chat to ${person.chatContext}`);
        }
    });

    chatAttachInput?.addEventListener('change', () => {
        const file = chatAttachInput.files && chatAttachInput.files[0];
        if (!file) return;
        const allowedExt = /\.(jpg|jpeg|png|webp|pdf|doc|docx|txt|mp4|webm|mp3|wav)$/i.test(file.name);
        if (!allowedExt) {
            showToast('Security: Unsupported attachment format. Use images, PDF/DOC, or MP4/MP3.');
            clearPendingAttachment();
            return;
        }
        if (file.size > 10 * 1024 * 1024) {
            showToast('Security: Attachment exceeds 10 MB limit.');
            clearPendingAttachment();
            return;
        }
        const isImage = /\.(jpg|jpeg|png|webp)$/i.test(file.name);
        const sizeLabel = `${(file.size / 1024).toFixed(0)} KB`;
        const finishAttach = (previewUrl) => {
            pendingChatAttachment = { name: file.name, sizeLabel, isImage, previewUrl };
            if (chatAttachPreview) {
                chatAttachPreview.hidden = false;
                chatAttachPreview.innerHTML = `<span>📎 ${escapeHtml(file.name)} (${sizeLabel})</span><button type="button" id="ew-chat-remove-attach" class="ew-btn ew-btn-outline ew-btn-sm">Remove</button>`;
                document.getElementById('ew-chat-remove-attach')?.addEventListener('click', clearPendingAttachment);
            }
        };
        if (isImage) {
            const reader = new FileReader();
            reader.onload = () => finishAttach(typeof reader.result === 'string' ? reader.result : '');
            reader.readAsDataURL(file);
        } else {
            finishAttach('');
        }
    });

    chatBlockBtn?.addEventListener('click', () => {
        if (!activeChatPersonId) return;
        const person = state.network.find((n) => n.id === activeChatPersonId);
        if (!person) return;
        person.blocked = true;
        saveState();
        closeChatModal();
        renderNetwork();
        showToast(`Blocked ${person.name}. Conversation hidden from active inbox.`);
    });

    chatReportBtn?.addEventListener('click', () => {
        if (!activeChatPersonId) return;
        const person = state.network.find((n) => n.id === activeChatPersonId);
        if (!person) return;
        person.reported = true;
        state.notifications.unshift({
            id: `n-${Date.now()}`,
            text: `Safety report submitted for conversation with ${person.name}.`,
            time: 'Just now',
            unread: true
        });
        saveState();
        renderNotifications();
        showToast(`Reported conversation with ${person.name} to Trust & Safety.`);
    });

    chatForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!activeChatPersonId || !chatInput) return;
        const person = state.network.find((n) => n.id === activeChatPersonId);
        if (!person || person.blocked || getConnectionStatus(person) !== 'mutual') {
            showToast('Security: You do not have permission to message this user.');
            return;
        }

        const text = chatInput.value.trim();
        if (!text && !pendingChatAttachment) return;

        // Rate limiting: max 5 messages per 15 seconds
        const now = Date.now();
        while (chatSendTimestamps.length && now - chatSendTimestamps[0] > 15000) {
            chatSendTimestamps.shift();
        }
        if (chatSendTimestamps.length >= 5) {
            showToast('Rate limit: Please wait a few seconds before sending more messages.');
            return;
        }

        // Spam protection
        if (SPAM_PATTERNS.some((rx) => rx.test(text))) {
            showToast('Security: Message blocked by spam/phishing filter.');
            return;
        }

        if (!state.networkChats) state.networkChats = {};
        if (!state.networkChats[activeChatPersonId]) state.networkChats[activeChatPersonId] = [];
        const history = state.networkChats[activeChatPersonId];
        const recentSame = history.slice(-2).filter((m) => m.sender === 'me' && m.text === text).length;
        if (text && recentSame >= 2) {
            showToast('Spam protection: Repeated identical messages are blocked.');
            return;
        }

        chatSendTimestamps.push(now);
        const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        const readReceiptsOn = state.chatSettings?.readReceipts !== false;

        history.push({
            sender: 'me',
            text,
            attachment: pendingChatAttachment ? { ...pendingChatAttachment } : null,
            time: timeStr,
            status: readReceiptsOn ? 'Read' : 'Delivered'
        });

        saveState();
        chatInput.value = '';
        clearPendingAttachment();
        renderChatMessages(activeChatPersonId);
        renderChatInbox();
    });

    document.getElementById('inbox-search')?.addEventListener('input', renderChatInbox);
    document.getElementById('chat-read-receipts-toggle')?.addEventListener('click', () => {
        if (!state.chatSettings) state.chatSettings = { readReceipts: true };
        state.chatSettings.readReceipts = !state.chatSettings.readReceipts;
        saveState();
        renderChatInbox();
        if (activeChatPersonId) renderChatMessages(activeChatPersonId);
        showToast(`Read receipts ${state.chatSettings.readReceipts ? 'enabled' : 'disabled'}`);
    });

    document.addEventListener('click', (e) => {
        const inboxChip = e.target.closest('.ew-chip[data-inbox-filter]');
        if (inboxChip) {
            inboxFilterMode = inboxChip.dataset.inboxFilter;
            document.querySelectorAll('.ew-chip[data-inbox-filter]').forEach((c) => {
                c.classList.toggle('active', c === inboxChip);
            });
            renderChatInbox();
            return;
        }

        const startConvBtn = e.target.closest('[data-start-conversation]');
        if (startConvBtn) {
            const person = state.network.find((n) => n.id === startConvBtn.dataset.startConversation);
            if (person) {
                person.status = 'pending_outgoing';
                person.connected = false;
                if (!person.chatContext) person.chatContext = 'Audition: Lead Role';
                state.notifications.unshift({
                    id: `n-${Date.now()}`,
                    text: `Chat request sent to ${person.name} (${person.chatContext}). Waiting for approval.`,
                    time: 'Just now',
                    unread: true
                });
                saveState();
                renderNotifications();
                renderNetwork();
                showToast(`Conversation request sent to ${person.name}. Permission required.`);
            }
            return;
        }

        const unblockBtn = e.target.closest('[data-unblock-chat]');
        if (unblockBtn) {
            const person = state.network.find((n) => n.id === unblockBtn.dataset.unblockChat);
            if (person) {
                person.blocked = false;
                saveState();
                renderNetwork();
                showToast(`Unblocked ${person.name}`);
            }
            return;
        }
    });

    chatClose?.addEventListener('click', closeChatModal);
    chatModal?.addEventListener('click', (e) => {
        if (e.target === chatModal) closeChatModal();
    });

    personClose?.addEventListener('click', closePersonModal);
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closePhotoModal();
            closePersonModal();
            closeInvitationModal();
            closeChatModal();
        }
    });

    async function syncWorkflowFromBackend() {
        const user = getSessionUser();
        try {
            const oppRes = await fetch('/api/workflow/opportunities');
            if (oppRes.ok) {
                const liveOpps = await oppRes.json();
                if (Array.isArray(liveOpps) && liveOpps.length) {
                    const existingIds = new Set(state.opportunities.map((o) => o.id));
                    const merged = [...state.opportunities];
                    liveOpps.forEach((item) => {
                        if (!existingIds.has(item.id)) {
                            merged.unshift(item);
                        }
                    });
                    state.opportunities = merged;
                    renderOpportunities();
                }
            }
        } catch {
            // Keep offline/demo opportunities if backend is unavailable
        }

        if (!user || !user.email) return;

        try {
            await fetch('/api/workflow/professional/sync-profile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: user.email,
                    fullName: user.fullName || '',
                    category: user.professionalCategory || 'Actor',
                    city: state.profileExtras?.city || 'Mumbai',
                    phone: state.profileExtras?.phone || '',
                    bio: state.profileExtras?.bio || ''
                })
            });

            const appRes = await fetch(`/api/workflow/professional/applications?email=${encodeURIComponent(user.email)}`);
            if (appRes.ok) {
                const liveApps = await appRes.json();
                if (Array.isArray(liveApps) && liveApps.length) {
                    const badgeMap = {
                        APPLIED: 'review',
                        SHORTLISTED: 'shortlisted',
                        INVITED: 'audition',
                        AUDITION_SCHEDULED: 'audition',
                        ACCEPTED: 'shortlisted',
                        REJECTED: 'closed',
                        DECLINED: 'closed'
                    };
                    liveApps.forEach((item) => {
                        const existing = state.applications.find((a) => a.title === item.title && a.company === item.company);
                        if (existing) {
                            existing.status = item.status;
                            existing.badgeClass = badgeMap[item.status] || 'review';
                        } else {
                            state.applications.unshift({
                                id: `wf-app-${item.applicationId}`,
                                title: item.title,
                                company: item.company,
                                location: item.location,
                                appliedDate: item.appliedAt || 'Recently',
                                status: item.status,
                                badgeClass: badgeMap[item.status] || 'review',
                                compensation: 'As per project'
                            });
                        }
                    });
                    renderKpis();
                    renderRecentApplications();
                    renderAllApplications();
                }
            }
        } catch {
            // Keep local state if backend sync fails
        }
    }

    let availCalDate = new Date();

    function renderAvailabilityCalendar() {
        const grid = document.getElementById('avail-calendar-grid');
        const monthLabel = document.getElementById('avail-cal-month-label');
        const summaryPill = document.getElementById('availability-summary-pill');
        const availList = document.getElementById('avail-dates-list');
        const blockedList = document.getElementById('blocked-dates-list');
        const dateInput = document.getElementById('avail-date-input');
        if (!grid) return;

        if (dateInput && !dateInput.min) {
            const now = new Date();
            dateInput.min = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        }

        const datesMap = state.availabilityDates || {};
        const year = availCalDate.getFullYear();
        const month = availCalDate.getMonth();
        const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        if (monthLabel) monthLabel.textContent = `${monthNames[month]} ${year}`;

        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        const cells = [];
        for (let i = 0; i < firstDay; i++) {
            cells.push(`<div class="ew-cal-day empty"></div>`);
        }
        for (let day = 1; day <= daysInMonth; day++) {
            const iso = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const entry = datesMap[iso];
            const cls = entry ? entry.status : '';
            const title = entry ? `${entry.status.toUpperCase()}${entry.note ? ' — ' + entry.note : ''}` : 'Click to mark Available or Blocked';
            cells.push(`<button type="button" class="ew-cal-day ${escapeHtml(cls)}" data-toggle-avail-date="${iso}" title="${escapeHtml(title)}">${day}</button>`);
        }
        grid.innerHTML = cells.join('');

        const entries = Object.entries(datesMap).sort(([a], [b]) => a.localeCompare(b));
        const availEntries = entries.filter(([, v]) => v.status === 'available');
        const blockedEntries = entries.filter(([, v]) => v.status === 'blocked');

        if (summaryPill) {
            summaryPill.textContent = `${availEntries.length} Available • ${blockedEntries.length} Blocked`;
        }
        if (availList) {
            availList.innerHTML = availEntries.length
                ? availEntries.map(([iso, v]) => `
                    <span class="ew-date-pill available">
                        ${escapeHtml(iso)}${v.note ? ` • ${escapeHtml(v.note)}` : ''}
                        <button type="button" data-remove-avail-date="${escapeHtml(iso)}" aria-label="Remove">&times;</button>
                    </span>
                `).join('')
                : `<span class="ew-app-meta">No available dates marked.</span>`;
        }
        if (blockedList) {
            blockedList.innerHTML = blockedEntries.length
                ? blockedEntries.map(([iso, v]) => `
                    <span class="ew-date-pill blocked">
                        ${escapeHtml(iso)}${v.note ? ` • ${escapeHtml(v.note)}` : ''}
                        <button type="button" data-remove-avail-date="${escapeHtml(iso)}" aria-label="Remove">&times;</button>
                    </span>
                `).join('')
                : `<span class="ew-app-meta">No blocked dates marked.</span>`;
        }
    }

    document.getElementById('avail-cal-prev')?.addEventListener('click', () => {
        availCalDate = new Date(availCalDate.getFullYear(), availCalDate.getMonth() - 1, 1);
        renderAvailabilityCalendar();
    });

    document.getElementById('avail-cal-next')?.addEventListener('click', () => {
        availCalDate = new Date(availCalDate.getFullYear(), availCalDate.getMonth() + 1, 1);
        renderAvailabilityCalendar();
    });

    document.getElementById('avail-date-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const iso = document.getElementById('avail-date-input')?.value;
        const status = document.getElementById('avail-date-status')?.value || 'available';
        const note = document.getElementById('avail-date-note')?.value.trim() || '';
        if (!iso) return;
        if (!state.availabilityDates) state.availabilityDates = {};
        state.availabilityDates[iso] = { status, note };
        saveState();
        renderAvailabilityCalendar();
        e.target.reset();
        showToast(`Marked ${iso} as ${status}`);
    });

    document.addEventListener('click', (e) => {
        const toggleDateBtn = e.target.closest('[data-toggle-avail-date]');
        if (toggleDateBtn) {
            const iso = toggleDateBtn.dataset.toggleAvailDate;
            if (!state.availabilityDates) state.availabilityDates = {};
            const current = state.availabilityDates[iso];
            if (!current) {
                state.availabilityDates[iso] = { status: 'available', note: '' };
                showToast(`${iso} marked Available`);
            } else if (current.status === 'available') {
                state.availabilityDates[iso] = { status: 'blocked', note: current.note || '' };
                showToast(`${iso} marked Blocked`);
            } else {
                delete state.availabilityDates[iso];
                showToast(`${iso} unmarked`);
            }
            saveState();
            renderAvailabilityCalendar();
            return;
        }

        const removeDateBtn = e.target.closest('[data-remove-avail-date]');
        if (removeDateBtn) {
            const iso = removeDateBtn.dataset.removeAvailDate;
            if (state.availabilityDates && state.availabilityDates[iso]) {
                delete state.availabilityDates[iso];
                saveState();
                renderAvailabilityCalendar();
                showToast(`Removed ${iso}`);
            }
            return;
        }
    });

    // Initial render
    renderKpis();
    renderRecentApplications();
    renderAllApplications();
    renderOpportunities();
    renderAuditionInvitations();
    renderEventInvitations();
    renderBrandInvitations();
    renderWishlist();
    renderNetwork();
    renderTeammateSuggestions();
    renderNotifications();
    renderPlans();
    renderPlanHistory();
    renderCategories();
    renderComplaints();
    populateProfileForm();
    renderAvailabilityCalendar();
    populateSettings();
    syncWorkflowFromBackend();
})();
