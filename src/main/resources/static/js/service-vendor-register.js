(() => {
    const CATEGORY_SCHEMAS = {
        'Production & Camera Services': [
            { key: 'productionServiceType', label: 'Production Service Type', placeholder: 'Feature Film, Ad Shoot, Wedding Cinema' },
            { key: 'cinematographyServices', label: 'Cinematography Services', placeholder: 'DOP, Multi-cam setup, Steadicam' },
            { key: 'cameraTypes', label: 'Camera Types and Equipment', placeholder: 'ARRI Alexa, RED V-Raptor, Sony FX6' },
            { key: 'droneAvailability', label: 'Drone Shooting Availability', type: 'select', options: ['Yes', 'No'] },
            { key: 'cameraProfessionals', label: 'Number of Camera Professionals', type: 'number', placeholder: '5' },
            { key: 'servicePackages', label: 'Service Packages', placeholder: 'Half-Day / Full-Day / Feature Package' },
            { key: 'pricing', label: 'Pricing (₹)', placeholder: '25000' },
            { key: 'portfolioMedia', label: 'Portfolio Images / Videos URL', placeholder: 'https://...' }
        ],
        'Video Editing & Post-Production': [
            { key: 'editingServices', label: 'Editing Services', placeholder: 'Offline/Online Cut, Trailer, Reels' },
            { key: 'vfxServices', label: 'VFX Services', placeholder: 'Compositing, Roto, CGI' },
            { key: 'animationServices', label: 'Animation Services', placeholder: '2D/3D Motion Graphics' },
            { key: 'colorGrading', label: 'Color Grading', type: 'select', options: ['Yes - DaVinci Resolve', 'Basic Grading', 'No'] },
            { key: 'softwareTools', label: 'Software / Tools Used', placeholder: 'Premiere Pro, DaVinci, After Effects, Nuke' },
            { key: 'teamSize', label: 'Team Size', type: 'number', placeholder: '4' },
            { key: 'projectPricing', label: 'Project Pricing (₹)', placeholder: '15000' },
            { key: 'portfolioMedia', label: 'Previous Work Portfolio URL', placeholder: 'https://...' }
        ],
        'Audio & Music Production': [
            { key: 'recordingStudioAvailable', label: 'Recording Studio Availability', type: 'select', options: ['Yes', 'No'] },
            { key: 'dubbingServices', label: 'Dubbing Services', type: 'select', options: ['Yes', 'No'] },
            { key: 'soundRecording', label: 'Sound Recording', placeholder: 'Sync Sound, Foley, Voice-over' },
            { key: 'mixingMastering', label: 'Audio Mixing and Mastering', placeholder: '5.1 Surround, Dolby Atmos, Stereo' },
            { key: 'equipmentDetails', label: 'Equipment Details', placeholder: 'Neumann U87, Pro Tools HDX, SSL Console' },
            { key: 'studioFacilities', label: 'Studio Facilities', placeholder: 'Vocal Booth, Acoustic Control Room' },
            { key: 'pricingPackages', label: 'Pricing Packages (₹)', placeholder: '12000' },
            { key: 'audioPortfolio', label: 'Audio Portfolio URL', placeholder: 'https://...' }
        ],
        'Lighting & Equipment Rental': [
            { key: 'equipmentCategories', label: 'Equipment Categories', placeholder: 'Stage Lights, Truss, Audio, Camera' },
            { key: 'lightingEquipment', label: 'Lighting Equipment', placeholder: 'ARRI SkyPanel, Aputure 1200d, Moving Heads' },
            { key: 'ledScreens', label: 'LED Screens', placeholder: 'P3 Indoor / Outdoor LED Wall' },
            { key: 'soundSystems', label: 'Sound Systems', placeholder: 'Line Array, Subwoofers, Digital Mixers' },
            { key: 'cameraRental', label: 'Camera Equipment Rental', placeholder: 'Cinema Lenses, Gimbals, Cranes' },
            { key: 'availableQuantity', label: 'Available Equipment Quantity', type: 'number', placeholder: '25' },
            { key: 'rentalPricing', label: 'Rental Pricing (₹/day)', placeholder: '18000' },
            { key: 'deliverySetup', label: 'Delivery and Setup Availability', type: 'select', options: ['Yes - Full Delivery & Setup', 'Pickup Only'] }
        ],
        'Art, Set, Costume & Makeup': [
            { key: 'serviceCategories', label: 'Service Categories', placeholder: 'Set Design, Art Direction, Costume, Makeup' },
            { key: 'setDesign', label: 'Set Design', placeholder: 'Film Sets, Wedding Mandap, Stage Backdrops' },
            { key: 'artDirection', label: 'Art Direction', placeholder: 'Concept Art, Period Props' },
            { key: 'propsCostume', label: 'Props and Costume Services', placeholder: 'Period Costumes, Bridal & Show Wardrobe' },
            { key: 'makeupStyling', label: 'Makeup and Styling Services', placeholder: 'Prosthetics, Bridal, Editorial Makeup' },
            { key: 'teamSize', label: 'Team Size', type: 'number', placeholder: '10' },
            { key: 'servicePackages', label: 'Service Packages (₹)', placeholder: '30000' },
            { key: 'portfolioImages', label: 'Portfolio Images URL', placeholder: 'https://...' }
        ],
        'Event & Logistics Services': [
            { key: 'serviceCategories', label: 'Service Categories', placeholder: 'Setup, Catering, Transport, Security' },
            { key: 'eventSetup', label: 'Event Setup', placeholder: 'Stages, Tents, Seating, Barricades' },
            { key: 'cateringServices', label: 'Catering Services', placeholder: 'Crew Catering, VIP Buffet' },
            { key: 'transportationServices', label: 'Transportation Services', placeholder: 'Vanity Vans, Equipment Trucks, Fleet' },
            { key: 'eventSupport', label: 'Event Support Services', placeholder: 'Bouncers, Housekeeping, Power Generators' },
            { key: 'staffSize', label: 'Available Staff / Team Size', type: 'number', placeholder: '30' },
            { key: 'serviceLocations', label: 'Service Locations', placeholder: 'Hyderabad, Bengaluru, Pan-India' },
            { key: 'pricingPackages', label: 'Pricing Packages (₹)', placeholder: '40000' }
        ],
        'Marketing & Promotion Services': [
            { key: 'digitalMarketing', label: 'Digital Marketing Services', placeholder: 'SEO, Performance Ads, PR' },
            { key: 'entertainmentCampaign', label: 'Entertainment Campaign Management', placeholder: 'Movie Releases, Music Launches' },
            { key: 'influencerCampaign', label: 'Influencer Campaign Management', placeholder: 'Creator Seeding, Reels Campaigns' },
            { key: 'socialMediaPromo', label: 'Social Media Promotion', placeholder: 'Instagram, YouTube, X Promotions' },
            { key: 'advertisingServices', label: 'Advertising Services', placeholder: 'OOH Hoardings, Print, OTT Ads' },
            { key: 'platformsSupported', label: 'Platforms Supported', placeholder: 'YouTube, Instagram, Meta, Google' },
            { key: 'campaignPackages', label: 'Campaign Packages (₹)', placeholder: '50000' },
            { key: 'portfolioMedia', label: 'Previous Campaign Portfolio URL', placeholder: 'https://...' }
        ],
        'Other Entertainment Services': [
            { key: 'customServiceName', label: 'Custom Service Name', placeholder: 'Fireworks, Stunt Rigging, Drone Show' },
            { key: 'serviceDescription', label: 'Service Description', placeholder: 'Detailed overview of your specialty service' },
            { key: 'serviceCategory', label: 'Service Category', placeholder: 'Special Effects / Talent Support' },
            { key: 'pricing', label: 'Pricing (₹)', placeholder: '20000' },
            { key: 'portfolioMedia', label: 'Portfolio Images / Videos URL', placeholder: 'https://...' }
        ]
    };

    const catSel = document.getElementById('cr-category');
    const box = document.getElementById('cat-specific-fields');
    const banner = document.getElementById('cat-reg-banner') || document.getElementById('status-banner');
    const form = document.getElementById('cat-reg-form');
    if (!catSel || !box || !banner || !form) return;

    function showBanner(msg, type = 'error') {
        banner.textContent = msg;
        banner.className = `status-banner ${type}`;
        banner.hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    catSel.addEventListener('change', () => {
        const schema = CATEGORY_SCHEMAS[catSel.value];
        if (!schema) {
            box.hidden = true;
            box.innerHTML = '';
            return;
        }
        box.hidden = false;
        box.innerHTML = `<h3 style="margin:0 0 0.75rem;font-size:1rem;color:#be123c;">${catSel.value} — Category Details</h3>`
            + `<div class="field-row" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:0.75rem;">`
            + schema.map((f) => {
                if (f.type === 'select') {
                    return `<div class="field-group"><label for="cf-${f.key}">${f.label}</label><select id="cf-${f.key}" data-cf="${f.key}">${f.options.map((o) => `<option value="${o}">${o}</option>`).join('')}</select></div>`;
                }
                return `<div class="field-group"><label for="cf-${f.key}">${f.label}</label><input type="${f.type || 'text'}" id="cf-${f.key}" data-cf="${f.key}" placeholder="${f.placeholder || ''}"></div>`;
            }).join('')
            + `</div>`;
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        banner.hidden = true;
        const cat = catSel.value;
        if (!cat) return showBanner('Please select a Vendor Type.');
        const pass = document.getElementById('cr-pass').value;
        const cpass = document.getElementById('cr-cpass').value;
        if (pass !== cpass) return showBanner('Password and Confirm Password must match.');

        const details = {};
        box.querySelectorAll('[data-cf]').forEach((el) => { details[el.dataset.cf] = el.value; });

        const fd = new FormData();
        fd.append('vendorCategory', cat);
        fd.append('businessName', document.getElementById('cr-biz').value);
        fd.append('contactPerson', document.getElementById('cr-person').value);
        fd.append('email', document.getElementById('cr-email').value);
        fd.append('phone', document.getElementById('cr-phone').value);
        fd.append('password', pass);
        fd.append('confirmPassword', cpass);
        fd.append('country', document.getElementById('cr-country').value);
        fd.append('state', document.getElementById('cr-state').value);
        fd.append('city', document.getElementById('cr-city').value);
        fd.append('areaLocality', document.getElementById('cr-area').value);
        fd.append('address', document.getElementById('cr-addr').value);
        fd.append('pincode', document.getElementById('cr-pin').value);
        fd.append('mapLink', document.getElementById('cr-map').value);
        fd.append('aboutBusiness', document.getElementById('cr-about').value);
        fd.append('categoryDetailsJson', JSON.stringify(details));
        const logo = document.getElementById('cr-logo').files[0];
        if (logo) fd.append('logoFile', logo);

        try {
            const res = await fetch('/api/category-vendors/register', { method: 'POST', body: fd });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Registration failed.');
            showBanner(`${data.message} You can now sign in from Vendor Login.`, 'success');
            setTimeout(() => { window.location.href = '/vendors-login.html'; }, 1400);
        } catch (err) {
            showBanner(err.message, 'error');
        }
    });
})();
