package com.EventCrafters.EventCrafters.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageEventDTO {
    private List<EventDTO> events;
    private int page;
    private int totalPages;

    public PageEventDTO(List<EventDTO> events, int page, int totalPages) {
        this.events = events;
        this.page = page;
        this.totalPages = totalPages;
    }
}
