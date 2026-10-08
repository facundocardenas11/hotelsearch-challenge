package com.sling.hotelsearch.infrastructure.persistence;

import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.domain.model.SearchCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba el repositorio contra H2 en modo Oracle, sin necesidad de levantar una base real. */
@JdbcTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:repotest;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=never"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/schema.sql")
class JdbcSearchRepositoryTest {

    private static final LocalDate IN = LocalDate.of(2023, 12, 29);
    private static final LocalDate OUT = LocalDate.of(2023, 12, 31);

    @Autowired
    private DataSource dataSource;

    private JdbcSearchRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcSearchRepository(JdbcClient.create(dataSource));
    }

    private static Search search(String id, String hotel, List<Integer> ages) {
        return new Search(id, new HotelSearch(hotel, IN, OUT, ages));
    }

    @Test
    void guardaYRecuperaUnaBusquedaRespetandoElOrdenDeEdades() {
        repository.save(search("id-1", "h1", List.of(30, 29, 1, 3)));

        SearchCount result = repository.findCountBySearchId("id-1").orElseThrow();

        assertAll(
                () -> assertEquals("id-1", result.searchId()),
                () -> assertEquals("h1", result.hotelSearch().hotelId()),
                () -> assertEquals(IN, result.hotelSearch().checkIn()),
                () -> assertEquals(OUT, result.hotelSearch().checkOut()),
                () -> assertEquals(List.of(30, 29, 1, 3), result.hotelSearch().ages()),
                () -> assertEquals(1, result.count()));
    }

    @Test
    void cuentaSoloBusquedasIgualesIncluyendoElOrdenDeEdades() {
        repository.save(search("a", "h2", List.of(30, 29, 1, 3)));
        repository.save(search("b", "h2", List.of(30, 29, 1, 3)));
        repository.save(search("c", "h2", List.of(3, 29, 30, 1)));
        repository.save(search("d", "otro-hotel", List.of(30, 29, 1, 3)));

        assertAll(
                () -> assertEquals(2, repository.findCountBySearchId("a").orElseThrow().count()),
                () -> assertEquals(2, repository.findCountBySearchId("b").orElseThrow().count()),
                () -> assertEquals(1, repository.findCountBySearchId("c").orElseThrow().count()),
                () -> assertEquals(1, repository.findCountBySearchId("d").orElseThrow().count()));
    }

    @Test
    void devuelveVacioSiElIdNoExiste() {
        Optional<SearchCount> result = repository.findCountBySearchId("no-existe");

        assertTrue(result.isEmpty());
    }

    @Test
    void guardarDosVecesElMismoIdEsIdempotente() {
        repository.save(search("dup", "h3", List.of(1)));

        assertDoesNotThrow(() -> repository.save(search("dup", "h3", List.of(1))));
        assertEquals(1, repository.findCountBySearchId("dup").orElseThrow().count());
    }

    @Test
    void unIdConIntentoDeInyeccionSqlSeTrataComoTextoPlano() {
        assertTrue(repository.findCountBySearchId("' OR '1'='1").isEmpty());
    }
}
