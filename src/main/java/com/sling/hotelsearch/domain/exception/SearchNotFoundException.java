package com.sling.hotelsearch.domain.exception;

public class SearchNotFoundException extends RuntimeException {

    public SearchNotFoundException(String searchId) {
        super("No existe una busqueda con searchId: " + searchId);
    }
}
