package application.adapters.in.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.out.memory.InMemoryDeviceReadingAdapter;
import application.domain.valueobjects.SurveillanceDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class RestApiIntegrationTest {

    private static final String STAR_LABS = "{\"distance\":485.41,\"message\":[\"savitar\",\"\",\"\",\"the\",\"lab\"]}";
    private static final String CCPD = "{\"distance\":265.75,\"message\":[\"\",\"returns\",\"\",\"the\",\"\"]}";
    private static final String SATELLITE = "{\"distance\":600.52,\"message\":[\"\",\"\",\"returns\",\"at\",\"\",\"\"]}";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InMemoryDeviceReadingAdapter storage;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            storage.deleteIfPresent(device);
        }
    }

    // ---- F1 ----

    @Test
    void f1ProcessesTheThreeDevicesAtOnce() throws Exception {
        String body = "{\"devices\":["
                + "{\"name\":\"STAR Labs\"," + STAR_LABS.substring(1) + ","
                + "{\"name\":\"ccpd\"," + CCPD.substring(1) + ","
                + "{\"name\":\"S.T.A.R. Satellite\"," + SATELLITE.substring(1) + "]}";

        send(post("/api/interceptions"), body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.position.x").value(-100.0))
                .andExpect(jsonPath("$.data.position.y").value(75.0))
                .andExpect(jsonPath("$.data.message").value("savitar returns at the lab"));

        mvc.perform(get("/api/devices/readings")).andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void f1RejectsIncompleteOrInvalidRequests() throws Exception {
        send(post("/api/interceptions"), "{\"devices\":[{\"name\":\"ccpd\"," + CCPD.substring(1) + "]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Missing: star-labs, star-satellite")));
        send(post("/api/interceptions"), "{\"devices\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.devices").exists());
        send(post("/api/interceptions"), "{\"devices\":[{\"name\":\"kenobi\"," + CCPD.substring(1) + "]}")
                .andExpect(status().isNotFound());
        send(post("/api/interceptions"), "{\"devices\":[{\"name\":\"ccpd\",\"distance\":-1,\"message\":[]}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data['devices[0].distance']").exists());
    }

    @Test
    void f1ReportsInconsistentDistancesAsUnprocessable() throws Exception {
        String body = "{\"devices\":["
                + "{\"name\":\"star-labs\",\"distance\":100,\"message\":[\"a\"]},"
                + "{\"name\":\"ccpd\",\"distance\":100,\"message\":[\"a\"]},"
                + "{\"name\":\"star-satellite\",\"distance\":100,\"message\":[\"a\"]}]}";
        send(post("/api/interceptions"), body).andExpect(status().isUnprocessableContent());
    }

    // ---- F2 / F3 ----

    @Test
    void f2AndF3AccumulateReadingsInAnyOrder() throws Exception {
        mvc.perform(get("/api/interceptions/accumulated"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.data.missingDevices", hasSize(3)));

        send(post("/api/devices/star-satellite/readings"), SATELLITE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.device").value("star-satellite"))
                .andExpect(jsonPath("$.data.devicePosition.x").value(500.0));
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isCreated());

        mvc.perform(get("/api/interceptions/accumulated"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.data.missingDevices", containsInAnyOrder("star-labs")));

        send(post("/api/devices/star-labs/readings"), STAR_LABS).andExpect(status().isCreated());

        mvc.perform(get("/api/devices/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].device").value("star-labs"));
        mvc.perform(get("/api/devices/ccpd/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.distance").value(265.75));
        mvc.perform(get("/api/interceptions/accumulated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.position.x").value(-100.0))
                .andExpect(jsonPath("$.data.message").value("savitar returns at the lab"));
    }

    @Test
    void f2RejectsDuplicatesUnknownDevicesAndInvalidBodies() throws Exception {
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isCreated());
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isConflict());
        send(post("/api/devices/kenobi/readings"), CCPD).andExpect(status().isNotFound());
        send(post("/api/devices/star-labs/readings"), "{\"message\":[\"a\"]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.distance").exists());
        send(post("/api/devices/star-labs/readings"), "{\"distance\":1,\"message\":[\"a\",null]}")
                .andExpect(status().isBadRequest());
        send(post("/api/devices/star-labs/readings"), "{\"distance\":\"far\",\"message\":[]}")
                .andExpect(status().isBadRequest());
        send(post("/api/devices/star-labs/readings"), "{not json")
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/devices/star-labs/readings")).andExpect(status().isNotFound());
    }

    // ---- F4 ----

    @Test
    void f4ReplacesTheWholeReadingOfOneDevice() throws Exception {
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isCreated());
        send(post("/api/devices/star-labs/readings"), STAR_LABS).andExpect(status().isCreated());

        send(put("/api/devices/ccpd/readings"), "{\"distance\":10,\"message\":[\"new\"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.distance").value(10.0))
                .andExpect(jsonPath("$.data.message", hasSize(1)));

        mvc.perform(get("/api/devices/star-labs/readings")).andExpect(jsonPath("$.data.distance").value(485.41));
        send(put("/api/devices/ccpd/readings"), "{\"distance\":10}").andExpect(status().isBadRequest());
        send(put("/api/devices/star-satellite/readings"), SATELLITE).andExpect(status().isNotFound());
    }

    // ---- F5 ----

    @Test
    void f5UpdatesOnlyTheProvidedFields() throws Exception {
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isCreated());

        send(patch("/api/devices/ccpd/readings"), "{\"distance\":300}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.distance").value(300.0))
                .andExpect(jsonPath("$.data.message[1]").value("returns"));

        send(patch("/api/devices/ccpd/readings"), "{\"message\":[\"only\"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.distance").value(300.0))
                .andExpect(jsonPath("$.data.message", hasSize(1)));

        send(patch("/api/devices/ccpd/readings"), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("At least one field")));
        send(patch("/api/devices/ccpd/readings"), "{\"distance\":-5}").andExpect(status().isBadRequest());
        send(patch("/api/devices/star-labs/readings"), "{\"distance\":1}").andExpect(status().isNotFound());
    }

    // ---- F6 ----

    @Test
    void f6DeletesOnlyTheSelectedDevice() throws Exception {
        send(post("/api/devices/ccpd/readings"), CCPD).andExpect(status().isCreated());
        send(post("/api/devices/star-labs/readings"), STAR_LABS).andExpect(status().isCreated());

        mvc.perform(delete("/api/devices/ccpd/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mvc.perform(delete("/api/devices/ccpd/readings")).andExpect(status().isNotFound());
        mvc.perform(get("/api/devices/readings"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].device").value("star-labs"));
    }

    private ResultActions send(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String body)
            throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
