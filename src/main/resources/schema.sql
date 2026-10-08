-- Requiere Oracle 23ai (soporta IF NOT EXISTS). La imagen gvenzl/oracle-free es 23ai.
CREATE TABLE IF NOT EXISTS hotel_searches (
    search_id VARCHAR2(36)  PRIMARY KEY,
    hotel_id  VARCHAR2(100) NOT NULL,
    check_in  DATE          NOT NULL,
    check_out DATE          NOT NULL,
    ages      VARCHAR2(500) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hotel_searches_eq
    ON hotel_searches (hotel_id, check_in, check_out, ages);
