package com.EventCrafters.EventCrafters.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.Review;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.service.EventService;
import com.EventCrafters.EventCrafters.service.ReviewService;
import com.EventCrafters.EventCrafters.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class ReviewWebController {

    @Autowired
    private ReviewService service;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @PostMapping("/review/event/{eventId}")
    public String saveReview(@PathVariable long eventId, @RequestParam("rating") int rating, @RequestParam("text") String text, Authentication authentication) {
        Optional<Event> eventOptional = eventService.findById(eventId);
        String currentUsername = authentication.getName();
        Optional<User> currentUser = userService.findByUserName(currentUsername);

        if (eventOptional.isPresent() && currentUser.isPresent()) {
            Event event = eventOptional.get();
            User user = currentUser.get();

            // Check if the user has already reviewed this event
            if (service.hasUserReviewedEvent(eventId, user.getId())) {
                // Redirect to an error page if the user has already reviewed the event
                return "redirect:/error";
            }

            Review review = new Review();
            review.setEvent(event);
            review.setUser(user);
            review.setRating(rating);
            review.setText(text);

            service.save(review);
            return "redirect:/event/" + eventId;
        } else {
            return "redirect:/";
        }
    }



    @GetMapping("/review/event/{id}")
    public String showReviewForm(@PathVariable long id, Model model, Authentication authentication) {
        Optional<Event> eventOptional = eventService.findById(id);
        boolean isLoggedIn = isAuthenticated(authentication);
        if (eventOptional.isPresent() && isLoggedIn) {
            Event event = eventOptional.get();
            LocalDateTime now = LocalDateTime.now();
            boolean eventFinished = now.isAfter(event.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            String currentUsername = authentication.getName();
            Optional<User> currentUser = userService.findByUserName(currentUsername);
            boolean isUserRegistered = currentUser.isPresent() && event.getRegisteredUsers().contains(currentUser.get());
            boolean hasReviewed = currentUser.isPresent() && service.findByUserAndEvent(currentUser.get(), event).isPresent();

            if (eventFinished && isUserRegistered && !hasReviewed) {
                model.addAttribute("event", event);
                return "review";
            }
        }
        return "redirect:/";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}
