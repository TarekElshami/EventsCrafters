package com.EventCrafters.EventCrafters.service;

import com.EventCrafters.EventCrafters.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class AjaxService {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    private final int pageSize = 3;
    private List<Integer> maxPageNum;

    private List<Long> ids;

    private String input;

    // number of possible events ajax at the same time
    // if the number is changed go to getPageNum and add til that number in the not default case
    // and be careful with the findAjax function
    private int maxEventAjaxSameTime = 4;
    public AjaxService() {
        this.maxPageNum = new ArrayList<>();
        this.ids = new ArrayList<>();
        int i;
        for (i = 0; i<maxEventAjaxSameTime; i++){
            this.maxPageNum.add(0);
            this.ids.add(0L);
        }
    }
    public List<Event> findAjax(int i){
        return findAjax(-1L, i);
    }

    public List<Event> findAjax(Long id, int i){
        return findAjax(id, i, "", 0);
    }

    public List<Event> findAjax(Long id, int i, int dest){
        return findAjax(id, i, "", dest);
    }

    public List<Event> findAjax(int i, String input, int dest){
        return findAjax(-1L, i, input, dest);
    }

    public List<Event> findAjax(Long id, int i, String input, int dest){
        int aux;
        List<Event> result = new ArrayList<>();
        Page<Event> pageEvent;

        switch (i){
            case 0 :
                aux = 0;
                pageEvent = eventService.findAll(0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 1 :
                aux = 0;
                pageEvent = eventService.findByCreatorIdCurrentCreatedEvents(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 2 :
                aux = 1;
                pageEvent = eventService.findByCreatorIdPastCreatedEvents(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 3:
                aux = 2;
                pageEvent = eventService.findByRegisteredUserIdCurrentEvents(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
                //return new ArrayList<>();
            case 4:
                aux = 3;
                pageEvent = eventService.findByRegisteredUserIdPastEvents(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
                //return new ArrayList<>();
            case 5:
                aux = 0;
                pageEvent = eventService.eventsOrderedByPopularity(0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 6:
                aux = 0;
                pageEvent = userService.getUserCategoryPreferences(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 7: // Using dest to save space as it can´t be both at the same time
                aux = 0;
                pageEvent = eventService.findByCategory(id, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();
            case 8:
                aux = 0;
                pageEvent = eventService.findBySearchBar(input, 0, pageSize);
                this.maxPageNum.set(aux, pageEvent.getTotalPages());
                this.input= input;
                this.ids.set(aux, id);
                if(maxPageNum.get(aux) == 0){
                    return result;
                }
                return pageEvent.getContent();

        }
        return result;
    }

    public List<Event> loadMore(int i, int page){
        List<Event> result = new ArrayList<>();
        switch (i){
            case 0 :
                return eventService.findAll(page, pageSize).getContent();
            case 1 :
                return eventService.findByCreatorIdCurrentCreatedEvents(ids.get(0), page, pageSize).getContent();
            case 2 :
                return eventService.findByCreatorIdPastCreatedEvents(ids.get(1), page, pageSize).getContent();
            case 3:
                return eventService.findByRegisteredUserIdCurrentEvents(ids.get(2), page, pageSize).getContent();
            case 4:
                return eventService.findByRegisteredUserIdPastEvents(ids.get(3), page, pageSize).getContent();
            case 5:
                return eventService.eventsOrderedByPopularity(page, pageSize).getContent();
            case 6:
                return userService.getUserCategoryPreferences(ids.get(0), page, pageSize).getContent();
            case 7: // Using dest to save space as it can´t be both at the same time
                return eventService.findByCategory(ids.get(0), page, pageSize).getContent();
            case 8:
                return eventService.findBySearchBar(input, page, pageSize).getContent();

        }
        return result;
    }

    public Integer getMaxPageNum(int i) {
        return switch (i) {
            case 1, 2, 3, 4 -> maxPageNum.get(i - 1);
            default -> maxPageNum.get(0);
        };

    }
}
