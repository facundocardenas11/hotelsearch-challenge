package com.sling.hotelsearch.application.port.in;

import com.sling.hotelsearch.domain.model.SearchCount;

public interface CountSearchUseCase {

    SearchCount count(String searchId);
}
