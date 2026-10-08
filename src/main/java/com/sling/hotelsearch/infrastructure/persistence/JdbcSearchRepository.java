package com.sling.hotelsearch.infrastructure.persistence;

import com.sling.hotelsearch.application.port.out.SearchRepository;
import com.sling.hotelsearch.domain.model.HotelSearch;
import com.sling.hotelsearch.domain.model.Search;
import com.sling.hotelsearch.domain.model.SearchCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JdbcSearchRepository implements SearchRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcSearchRepository.class);

    private static final String INSERT = """
            INSERT INTO hotel_searches (search_id, hotel_id, check_in, check_out, ages)
            VALUES (:searchId, :hotelId, :checkIn, :checkOut, :ages)""";

    private static final String COUNT = """
            SELECT s.search_id, s.hotel_id, s.check_in, s.check_out, s.ages,
                   (SELECT COUNT(*) FROM hotel_searches x
                     WHERE x.hotel_id = s.hotel_id
                       AND x.check_in = s.check_in
                       AND x.check_out = s.check_out
                       AND x.ages = s.ages) AS total
              FROM hotel_searches s
             WHERE s.search_id = :searchId""";

    private final JdbcClient jdbc;

    public JdbcSearchRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Search search) {
        HotelSearch hs = search.hotelSearch();
        try {
            jdbc.sql(INSERT)
                    .param("searchId", search.searchId())
                    .param("hotelId", hs.hotelId())
                    .param("checkIn", hs.checkIn())
                    .param("checkOut", hs.checkOut())
                    .param("ages", agesToText(hs.ages()))
                    .update();
        } catch (DuplicateKeyException e) {
            // Kafka puede entregar un mensaje mas de una vez: ignorarlo mantiene el guardado idempotente.
            log.debug("La busqueda {} ya estaba guardada", search.searchId());
        }
    }

    @Override
    public Optional<SearchCount> findCountBySearchId(String searchId) {
        return jdbc.sql(COUNT)
                .param("searchId", searchId)
                .query((rs, rowNum) -> new SearchCount(
                        rs.getString("search_id"),
                        new HotelSearch(
                                rs.getString("hotel_id"),
                                rs.getObject("check_in", LocalDate.class),
                                rs.getObject("check_out", LocalDate.class),
                                textToAges(rs.getString("ages"))),
                        rs.getLong("total")))
                .optional();
    }

    private static String agesToText(List<Integer> ages) {
        return ages.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static List<Integer> textToAges(String text) {
        return Arrays.stream(text.split(",")).map(Integer::valueOf).toList();
    }
}
