package in.sp.main;

import com.example.demo.EntertainmentWorldApplication;
import in.sp.main.entity.ArtistProfile;
import in.sp.main.entity.CastingCompany;
import in.sp.main.repository.ArtistProfileRepository;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.service.SessionAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = EntertainmentWorldApplication.class)
class CastingDashboardEndpointsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CastingCompanyRepository companyRepo;

    @Autowired
    private ArtistProfileRepository artistRepo;

    @Test
    @Transactional
    void verifyStaticAssetsAndDashboardEndpoints() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 1. Static dashboard resources resolve cleanly
        mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ew-dashboard-ops.js")));
        mockMvc.perform(get("/js/ew-dashboard-ops.js")).andExpect(status().isOk());
        mockMvc.perform(get("/js/ew-dashboard.js")).andExpect(status().isOk());

        // 2. Authenticate seeded Casting Company session
        MockHttpSession session = new MockHttpSession();
        Optional<CastingCompany> demoCompany = companyRepo.findByCompanyEmailIgnoreCase("studio@moonlightcinema.in");
        if (demoCompany.isPresent()) {
            MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"studio@moonlightcinema.in\",\"password\":\"Demo@123\",\"portal\":\"COMPANY\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value(SessionAuthHelper.ROLE_COMPANY))
                    .andReturn();
            session = (MockHttpSession) loginRes.getRequest().getSession(false);
            assertNotNull(session);
        } else {
            CastingCompany activeCompany = companyRepo.findAll().stream()
                    .filter(CastingCompany::isSubscriptionActive)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No active paid CastingCompany found in database"));
            session.setAttribute(SessionAuthHelper.SESSION_ROLE, SessionAuthHelper.ROLE_COMPANY);
            session.setAttribute(SessionAuthHelper.SESSION_USER_ID, activeCompany.getId());
        }

        // 3. Verify RecruiterDashboardController endpoints
        mockMvc.perform(get("/api/dashboard/overview").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActorsCount").isNumber())
                .andExpect(jsonPath("$.activeProjectsCount").isNumber())
                .andExpect(jsonPath("$.totalApplicationsCount").isNumber())
                .andExpect(jsonPath("$.upcomingAuditionsCount").isNumber())
                .andExpect(jsonPath("$.shortlistedCount").isNumber())
                .andExpect(jsonPath("$.selectedCount").isNumber())
                .andExpect(jsonPath("$.recentActivities").isArray())
                .andExpect(jsonPath("$.chartData.monthlyActivity").isArray())
                .andExpect(jsonPath("$.chartData.applicationsByStatus").exists());

        mockMvc.perform(get("/api/dashboard/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/dashboard/applications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications").isArray())
                .andExpect(jsonPath("$.statusCounts").exists());

        mockMvc.perform(post("/api/dashboard/artists/search")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":\"Acting\",\"verifiedOnly\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isArray());

        mockMvc.perform(get("/api/dashboard/artists/recommended").param("type", "AI_MATCH").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isArray())
                .andExpect(jsonPath("$.projects").isArray());

        // 4. Verify CastingOperationsController endpoints
        mockMvc.perform(get("/api/dashboard/ops/auditions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditions").isArray())
                .andExpect(jsonPath("$.stats").exists());

        mockMvc.perform(get("/api/dashboard/ops/compare").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compared").isArray())
                .andExpect(jsonPath("$.availableArtists").isArray());

        mockMvc.perform(get("/api/dashboard/ops/selection/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlisted").isArray())
                .andExpect(jsonPath("$.selected").isArray())
                .andExpect(jsonPath("$.backups").isArray());

        mockMvc.perform(get("/api/dashboard/ops/messages").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray());

        mockMvc.perform(get("/api/dashboard/ops/reports").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actorReport").exists())
                .andExpect(jsonPath("$.projectReport").isArray())
                .andExpect(jsonPath("$.auditionReport").exists())
                .andExpect(jsonPath("$.selectionReport").isArray())
                .andExpect(jsonPath("$.financialSummary").exists());

        mockMvc.perform(get("/api/dashboard/ops/reports/export-excel").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".xls")));

        MvcResult pdfRes = mockMvc.perform(get("/api/dashboard/ops/reports/export-pdf").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        String pdfHeader = new String(pdfRes.getResponse().getContentAsByteArray(), 0,
                Math.min(5, pdfRes.getResponse().getContentAsByteArray().length), StandardCharsets.US_ASCII);
        assertTrue(pdfHeader.startsWith("%PDF"), "Expected valid PDF binary header");

        mockMvc.perform(get("/api/dashboard/ops/team").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").isArray());

        mockMvc.perform(get("/api/dashboard/ops/contracts").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contracts").isArray());

        mockMvc.perform(get("/api/dashboard/ops/expenses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.summary").exists());

        // 5. Verify Project CRUD lifecycle (rolled back cleanly by @Transactional)
        MvcResult createProjectRes = mockMvc.perform(multipart("/api/dashboard/projects")
                        .session(session)
                        .param("projectTitle", "Integration Verification Call")
                        .param("projectType", "Feature Film")
                        .param("productionHouseName", "Moonlight Pictures")
                        .param("castingCompanyName", "Moonlight Cinema Casting")
                        .param("projectDetails", "Automated endpoint verification call")
                        .param("projectLanguage", "Hindi")
                        .param("projectStage", "Casting Open")
                        .param("targetCategory", "Actor"))
                .andExpect(status().isCreated())
                .andReturn();
        long createdProjectId = extractFirstId(createProjectRes.getResponse().getContentAsString());

        mockMvc.perform(put("/api/dashboard/projects/" + createdProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectTitle\":\"Updated Verification Call\",\"budget\":\"Rs. 25,000/day\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.projectTitle").value("Updated Verification Call"));

        mockMvc.perform(patch("/api/dashboard/projects/" + createdProjectId + "/status")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.status").value("PAUSED"));

        // Verify audition schedule + team CRUD + expense + verification roundtrips against temporary project
        List<ArtistProfile> artists = artistRepo.findAll();
        assertFalse(artists.isEmpty(), "Seeded artists must exist");
        long sampleArtistId = artists.get(0).getId();

        MvcResult audCreateRes = mockMvc.perform(post("/api/dashboard/ops/auditions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":" + createdProjectId + ",\"artistId\":" + sampleArtistId
                                + ",\"auditionMode\":\"Online\",\"auditionTime\":\"02:30 PM\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long audId = extractFirstId(audCreateRes.getResponse().getContentAsString());

        mockMvc.perform(delete("/api/dashboard/ops/auditions/" + audId).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/dashboard/projects/" + createdProjectId).session(session))
                .andExpect(status().isOk());

        MvcResult teamCreateRes = mockMvc.perform(post("/api/dashboard/ops/team")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Coordinator\",\"email\":\"coord-test@example.in\",\"roleTitle\":\"Casting Coordinator\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long teamId = extractFirstId(teamCreateRes.getResponse().getContentAsString());

        mockMvc.perform(put("/api/dashboard/ops/team/" + teamId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/dashboard/ops/team/" + teamId).session(session))
                .andExpect(status().isOk());
    }

    private long extractFirstId(String json) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(m.find(), "Expected numeric id in response JSON: " + json);
        return Long.parseLong(m.group(1));
    }
}
