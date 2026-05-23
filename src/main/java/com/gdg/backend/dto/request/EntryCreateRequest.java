package com.gdg.backend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class EntryCreateRequest {

    private String text;
    private Map<String, Object> context;
}
