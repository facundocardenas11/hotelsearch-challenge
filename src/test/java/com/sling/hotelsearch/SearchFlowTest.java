package com.sling.hotelsearch;

import com.jayway.jsonpath.JsonPath;
import com.sling.hotelsearch.infrastructure.kafka.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo: POST /search -> Kafka (embebido) -> consumer -> base (H2 en modo Oracle) -> GET /count.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flowtest;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = KafkaTopics.HOTEL_AVAILABILITY_SEARCHES,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class SearchFlowTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private MockMvc mockMvc;

    private static String body(String ages) {
        LocalDate in = LocalDate.now().plusDays(30);
        return """
                {"hotelId":"1234aBc","checkIn":"%s","checkOut":"%s","ages":%s}"""
                .formatted(FMT.format(in), FMT.format(in.plusDays(2)), ages);
    }

    private String registrar(String ages) throws Exception {
        String response = mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(ages)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.searchId");
    }

    @Test
    void registraYCuentaBusquedasIgualesDistinguiendoElOrdenDeEdades() throws Exception {
        String first = registrar("[30,29,1,3]");
        registrar("[30,29,1,3]");
        String reordered = registrar("[3,29,30,1]");

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            mockMvc.perform(get("/count").param("searchId", first))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2))
                    .andExpect(jsonPath("$.search.ages[0]").value(30));
            mockMvc.perform(get("/count").param("searchId", reordered))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1));
        });
    }
}