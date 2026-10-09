package com.sling.hotelsearch.infrastructure.rest;

import com.sling.hotelsearch.application.exception.EventPublishException;
import com.sling.hotelsearch.application.port.in.CountSearchUseCase;
import com.sling.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.sling.hotelsearch.domain.exception.SearchNotFoundException;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.SearchCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    private static final String VALID_BODY = """
            {"hotelId":"1234aBc","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[30,29,1,3]}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterSearchUseCase registerSearch;

    @MockitoBean
    private CountSearchUseCase countSearch;

    @Test
    void searchDevuelveElIdYPasaLaBusquedaAlCasoDeUso() throws Exception {
        when(registerSearch.register(any())).thenReturn("abc-123");

        mockMvc.perform(post("/search").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.searchId").value("abc-123"));

        verify(registerSearch).register(new HotelSearch(
                "1234aBc", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("payloadsInvalidos")
    void searchRechazaPayloadsInvalidosConBadRequest(String caso, String body, String mensaje) throws Exception {
        mockMvc.perform(post("/search").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(mensaje));

        verifyNoInteractions(registerSearch);
    }

    static Stream<Arguments> payloadsInvalidos() {
        return Stream.of(
                Arguments.of("hotelId vacio",
                        """
                        {"hotelId":"","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[1]}""",
                        "hotelId es obligatorio"),
                Arguments.of("hotelId ausente",
                        """
                        {"checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[1]}""",
                        "hotelId es obligatorio"),
                Arguments.of("checkIn ausente",
                        """
                        {"hotelId":"h","checkOut":"31/12/2023","ages":[1]}""",
                        "checkIn es obligatorio"),
                Arguments.of("checkOut vacio",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"","ages":[1]}""",
                        "checkOut es obligatorio"),
                Arguments.of("checkIn con formato incorrecto",
                        """
                        {"hotelId":"h","checkIn":"2023-12-29","checkOut":"31/12/2023","ages":[1]}""",
                        "checkIn debe tener formato dd/MM/yyyy"),
                Arguments.of("fecha inexistente",
                        """
                        {"hotelId":"h","checkIn":"31/02/2023","checkOut":"31/12/2023","ages":[1]}""",
                        "checkIn debe tener formato dd/MM/yyyy"),
                Arguments.of("checkIn igual a checkOut",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"29/12/2023","ages":[1]}""",
                        "checkIn debe ser anterior a checkOut"),
                Arguments.of("checkIn posterior a checkOut",
                        """
                        {"hotelId":"h","checkIn":"31/12/2023","checkOut":"29/12/2023","ages":[1]}""",
                        "checkIn debe ser anterior a checkOut"),
                Arguments.of("ages ausente",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"31/12/2023"}""",
                        "ages es obligatorio"),
                Arguments.of("ages vacio",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[]}""",
                        "ages es obligatorio"),
                Arguments.of("edad negativa",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[5,-1]}""",
                        "Las edades deben ser >= 0"),
                Arguments.of("edad nula",
                        """
                        {"hotelId":"h","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":[5,null]}""",
                        "Las edades deben ser >= 0"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "{",
            "",
            """
            {"hotelId":"h","checkIn":"29/12/2023","checkOut":"31/12/2023","ages":["a"]}"""})
    void searchRechazaJsonRotoOTiposIncorrectos(String body) throws Exception {
        mockMvc.perform(post("/search").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la peticion es invalido o esta mal formado"));
    }

    @Test
    void searchDevuelve503SiNoSePuedePublicar() throws Exception {
        when(registerSearch.register(any())).thenThrow(new EventPublishException("fallo", new RuntimeException()));

        mockMvc.perform(post("/search").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("No se pudo registrar la busqueda, intente nuevamente"));
    }

    @Test
    void countDevuelveLaBusquedaYElConteo() throws Exception {
        HotelSearch hs = new HotelSearch("1234aBc", LocalDate.of(2023, 12, 29), LocalDate.of(2023, 12, 31), List.of(30, 29, 1, 3));
        when(countSearch.count("abc-123")).thenReturn(new SearchCount("abc-123", hs, 100));

        mockMvc.perform(get("/count").param("searchId", "abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchId").value("abc-123"))
                .andExpect(jsonPath("$.search.hotelId").value("1234aBc"))
                .andExpect(jsonPath("$.search.checkIn").value("29/12/2023"))
                .andExpect(jsonPath("$.search.checkOut").value("31/12/2023"))
                .andExpect(jsonPath("$.search.ages[0]").value(30))
                .andExpect(jsonPath("$.count").value(100));
    }

    @Test
    void countDevuelve404SiNoExiste() throws Exception {
        when(countSearch.count("nope")).thenThrow(new SearchNotFoundException("nope"));

        mockMvc.perform(get("/count").param("searchId", "nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe una busqueda con searchId: nope"));
    }

    @Test
    void countSinParametroDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/count"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("searchId es obligatorio"));
    }
}
