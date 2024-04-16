package com.EventCrafters.EventCrafters.model.Secondary;

import com.EventCrafters.EventCrafters.model.Event;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class PageUserRecc implements Page {
    private List<Event> events;
    private int totalPages;

    public PageUserRecc(List<Event> events,  int totalPages) {
        this.events = events;
        this.totalPages = totalPages;
    }

    @Override
    public Pageable getPageable() {
        return Page.super.getPageable();
    }

    @Override
    public Pageable nextOrLastPageable() {
        return Page.super.nextOrLastPageable();
    }

    @Override
    public Pageable previousOrFirstPageable() {
        return Page.super.previousOrFirstPageable();
    }

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getNumberOfElements() {
        return 0;
    }

    @Override
    public List<Event> getContent() {
        return events;
    }

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public Pageable nextPageable() {
        return null;
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public long getTotalElements() {
        return 0;
    }

    @Override
    public Page<Event> map(Function function) {
        return null;
    }
}
