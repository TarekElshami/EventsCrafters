package com.EventCrafters.EventCrafters.controller;

import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.EventCrafters.EventCrafters.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.EventCrafters.EventCrafters.service.EventService;

@Controller
public class EventWebController {
/*
    @Autowired
    private EventService service;

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal != null) {

            model.addAttribute("logged", true);
            model.addAttribute("userName", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));

        } else {
            model.addAttribute("logged", false);
        }
    }

    @GetMapping("/home")
    public String home(Model model) {
        //To-do: implement the whole thing. Maybe take inspiration from this
        //model.addAttribute("books", service.findAll());
        return "index";
    }
    @GetMapping("/home/search")
    public String search(Model model) {
        //To-do: implement the whole thing.
        return "index";
    }
    @GetMapping("/home/{tag}")
    public String filter(Model model) {
        //To-do: implement the whole thing.
        return "index";
    }

    @GetMapping("/events/{id}")
    public String showEvent(Model model, @PathVariable long id) {
        //To-do: implement the whole thing. Maybe take inspiration from this
        /*Optional<Event> book = service.findById(id);
        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            return "book";
        } else {
            return "books";
        }*/
/*        return "event_info";
    }

    @PostMapping("/removeEvent/{id}")
    public String removeBook(Model model, @PathVariable long id) {
        //To-do: implement the whole thing. Maybe take inspiration from this
        /*
        Optional<Event> book = service.findById(id);
        if (book.isPresent()) {
            service.delete(id);
            model.addAttribute("book", book.get());
        }*/
/*        return "profile"; //or a dedicated page to tell the user the operation went through
    }

    @GetMapping("/newEvent")
    public String newEvent(Model model) {
        //To-do: Give model neccesary params
        return "create_event";
    }

    @PostMapping("/newEvent")
    public String newEventProcess(Model model, Event event) {
        //To-do: save the event. Maybe take inspiration from this
        /* service.save(event);
        model.addAttribute("bookId", event.getId()); */
/*        return "profile"; //or event_info for this event, or a dedicated page to tell the user the operation went through
    }

    @GetMapping("/editEvent/{id}")
    public String editBook(Model model, @PathVariable long id) {
        //To-do: implement the whole thing. Maybe take inspiration from this
        /*
        Optional<Event> book = service.findById(id);
        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            return "editBookPage";
        } else {
            return "books";
        }
        */
/*        return "create_event"; //?
    }

    @PostMapping("/editEvent") //wouldn't we take the id as well?
    public String editBookProcess(Model model, Event event) {
        //To-do: implement the whole thing. Maybe take inspiration from this
        /*service.save(event);
        model.addAttribute("bookId", event.getId());*/

/*        return "event_info"; //or profile for this event, or a dedicated page to tell the user the operation went through
    }
*/
}