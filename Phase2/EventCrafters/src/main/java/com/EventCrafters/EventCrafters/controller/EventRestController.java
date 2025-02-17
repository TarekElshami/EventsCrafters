package com.EventCrafters.EventCrafters.controller;

import com.EventCrafters.EventCrafters.DTO.*;
import com.EventCrafters.EventCrafters.model.Category;
import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.service.CategoryService;
import com.EventCrafters.EventCrafters.service.EventService;
import com.EventCrafters.EventCrafters.service.ReviewService;
import com.EventCrafters.EventCrafters.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    @Operation(summary = "Gets an event by its ID",
            description = "Returns basic event details. Once the event has finished, additional information is included in the response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {EventDTO.class, EventFinishedDTO.class}))}),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
    })
    public ResponseEntity<EventDTO> showEvent(@PathVariable long id) {
        Optional<Event> eventOptional = eventService.findById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            EventDTO eventDTO = transformDTO(event);
            return ResponseEntity.ok(eventDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Creates a new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventManipulationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid event data", content = @Content),
            @ApiResponse(responseCode = "403", description = "The operation is not allowed without registration", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventManipulationDTO eventManipulationDTO) {
        // Check for empty fields in the event
        if (eventService.eventHasValidFields(eventManipulationDTO)) {
            return ResponseEntity.badRequest().build();
        }

        // Check if category exists
        Optional<Category> categoryOpt = categoryService.findById(eventManipulationDTO.getCategoryId());
        if (!categoryOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        Event event = transformEvent(eventManipulationDTO);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> userOpt = userService.findByUserName(currentUsername);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        event.setCreator(userOpt.get());

        // Set the category
        event.setCategory(categoryOpt.get());

        // Save the event
        Event savedEvent = eventService.save(event);

        // Transform the saved event to EventDTO
        EventDTO eventDTO = transformDTO(savedEvent);

        // Build the URL created event
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/events/{id}")
                .buildAndExpand(savedEvent.getId())
                .toUri();

        return ResponseEntity.created(location).body(eventDTO);
    }


    @PutMapping("/{eventId}")
    @Operation(summary = "Modifies an existing event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Event successfully modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventManipulationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid event data", content = @Content),
            @ApiResponse(responseCode = "403", description = "The operation is not allowed without registration", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event or category not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "The operation is not allowed when event has finished", content = @Content)
    })
    public ResponseEntity<EventDTO> editEvent(@PathVariable Long eventId, @RequestBody EventManipulationDTO eventManipulationDTO) {
        // Check for empty fields in the event
        if (eventService.eventHasValidFields(eventManipulationDTO)) {
            return ResponseEntity.badRequest().build();
        }

        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Event existingEvent = eventOptional.get();

        //Check if event has not finished yet
        boolean eventFinished = LocalDateTime.now().isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (eventFinished) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        //Check if current user is the event creator or an admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> userOpt = userService.findByUserName(currentUsername);
        if (!userOpt.isPresent() || !isUserAdminOrCreator(currentUsername, eventOptional.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        //Check that, in case category is modified, the new category already exists
        Optional<Category> categoryOpt = categoryService.findById(eventManipulationDTO.getCategoryId());
        if (!categoryOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        eventService.assignEventProperties(existingEvent, eventManipulationDTO.getName(), eventManipulationDTO.getDescription(), eventManipulationDTO.getMaxCapacity(), eventManipulationDTO.getPrice(), eventManipulationDTO.getLocation(), eventManipulationDTO.getMap(), eventManipulationDTO.getStartDate(), eventManipulationDTO.getEndDate(), eventManipulationDTO.getAdditionalInfo());

        // Set the category
        existingEvent.setCategory(categoryOpt.get());

        // Save the event
        Event savedEvent = eventService.update(existingEvent);

        // Transform the saved event to EventDTO
        EventDTO eventDTO = transformDTO(savedEvent);

        return ResponseEntity.accepted().body(eventDTO);
    }

    @PostMapping("/registrations/{eventId}")
    @Operation(summary = "Registers current user for selected event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered in the event", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is already registered in the event, or is the event creator or event is full", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Event has already finished", content = @Content),
            @ApiResponse(responseCode = "500", description = "User is not registered", content = @Content)
    })
    public ResponseEntity<String> registerToEvent(HttpServletRequest request, @PathVariable("eventId") Long eventId) {
        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Event existingEvent = eventOptional.get();

        //Check if event has not finished yet
        boolean eventFinished = LocalDateTime.now().isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (eventFinished) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        //Check if user is registered
        Principal principal = request.getUserPrincipal();
        User userPrincipal;
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            Optional<User> userPrincipalOptional = userService.findByUserName(principal.getName());
            if (!userPrincipalOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userPrincipal = userPrincipalOptional.get();
        }

        boolean userAlreadyRegistered = existingEvent.getRegisteredUsers().contains(userPrincipal);
        boolean userIsCreator = existingEvent.getCreator().equals(userPrincipal);
        boolean eventNotFull = existingEvent.getRegisteredUsers().size() >= existingEvent.getMaxCapacity();
        //Check if user is not registered in the event yet, is not the event creator and event is not full
        if (userAlreadyRegistered || userIsCreator || eventNotFull) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        existingEvent.getRegisteredUsers().add(userPrincipal);
        eventService.save(existingEvent);

        return ResponseEntity.ok("");
    }


    @DeleteMapping("/registrations/{eventId}")
    @Operation(summary = "Unregisters current user for selected event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully unregistered in the event", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not registered in the event yet", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Event has already finished", content = @Content),
            @ApiResponse(responseCode = "500", description = "User is not registered", content = @Content)
    })
    public ResponseEntity<String> leaveAnEvent(HttpServletRequest request, @PathVariable("eventId") Long eventId) {
        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Event existingEvent = eventOptional.get();

        //Check if event has not finished yet
        boolean eventFinished = LocalDateTime.now().isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (eventFinished) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        //Check if user is registered
        Principal principal = request.getUserPrincipal();
        User userPrincipal;
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            Optional<User> userPrincipalOptional = userService.findByUserName(principal.getName());
            if (!userPrincipalOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userPrincipal = userPrincipalOptional.get();
        }

        //Check if user is registered in the event
        if (!existingEvent.getRegisteredUsers().contains(userPrincipal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        existingEvent.getRegisteredUsers().remove(userPrincipal);
        eventService.save(existingEvent);

        return ResponseEntity.ok("");
    }

    @GetMapping("/ticket/{eventId}")
    @Operation(summary = "Generates an event ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket successfully generated", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not registered in the event yet", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Event has already finished", content = @Content),
            @ApiResponse(responseCode = "500", description = "User is not registered", content = @Content)
    })
    public ResponseEntity<TicketDTO> getEventTicket(HttpServletRequest request, @PathVariable Long eventId) {
        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Event existingEvent = eventOptional.get();

        //Check if event has not finished yet
        boolean eventFinished = LocalDateTime.now().isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (eventFinished) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        //Check if user is registered
        Principal principal = request.getUserPrincipal();
        User userPrincipal;
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            Optional<User> userPrincipalOptional = userService.findByUserName(principal.getName());
            if (!userPrincipalOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userPrincipal = userPrincipalOptional.get();
        }

        //Check if user is registered in the event
        if (!existingEvent.getRegisteredUsers().contains(userPrincipal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User eventCreator = existingEvent.getCreator();

        String priceDisplay = existingEvent.getPrice() == 0.0 ? "Gratis" : String.format("%.2f €", existingEvent.getPrice());
        String startDateFormatted = eventService.formatDate(existingEvent.getStartDate());
        String endDateFormatted = eventService.formatDate(existingEvent.getEndDate());
        Duration duration = Duration.between(existingEvent.getStartDate().toInstant(), existingEvent.getEndDate().toInstant());
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        String durationFormatted = String.format("%d horas y %d minutos", hours, minutes);

        TicketDTO ticketDTO = new TicketDTO(existingEvent.getName(), userPrincipal.getName(), userPrincipal.getUsername(), userPrincipal.getEmail(), eventCreator.getName(), eventCreator.getUsername(), eventCreator.getEmail(), existingEvent.getMaxCapacity(), existingEvent.getLocation(), startDateFormatted, endDateFormatted, durationFormatted, existingEvent.getAdditionalInfo(), priceDisplay);

        return ResponseEntity.accepted().body(ticketDTO);
    }


    @PostMapping("/{eventId}/photo")
    @Operation(summary = "Uploads a photo for an existing event", description = "Allows uploading a photo for an existing event. Only the event creator or an admin can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded",
                    content = {@Content(mediaType = "image/jpeg")}),
            @ApiResponse(responseCode = "403", description = "Current user not is not the creator or an admin"),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error retrieving the image", content = @Content)
    })
    public ResponseEntity<EventDTO> uploadEventPhoto(@PathVariable Long eventId, @RequestPart("photo") MultipartFile photo) {
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Event event = eventOptional.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        if (!isUserAdminOrCreator(currentUsername, event)) {
            // Unauthorized to update the event
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            event.setPhoto(new javax.sql.rowset.serial.SerialBlob(photo.getBytes()));
            eventService.save(event);
            EventDTO eventDTO = transformDTO(event);

            // Build the URL created event
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/events/{id}")
                    .buildAndExpand(event.getId())
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(location);

            return new ResponseEntity<>(eventDTO, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/image/{id}")
    @Operation(summary = "Gets the image of an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image found",
                    content = {@Content(mediaType = "image/jpeg")}),
            @ApiResponse(responseCode = "404", description = "Event or image not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error retrieving the image", content = @Content)
    })
    public ResponseEntity<byte[]> showEventImage(@PathVariable long id) {
        Optional<Event> eventOptional = eventService.findById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            try {
                Blob photoBlob = event.getPhoto();
                byte[] photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(photoBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{eventId}/attendees")
    @Operation(summary = "Sets the number of attendees for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number of attendees updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventFinishedDTO.class))),
            @ApiResponse(responseCode = "400", description = "Provided information not valid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Operation not permitted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
    })
    public ResponseEntity<EventDTO> updateEventAttendees(@PathVariable Long eventId, @RequestBody AttendeesUpdateDTO attendeesUpdate) {

        Optional<Event> eventOpt = eventService.findById(eventId);
        if (!eventOpt.isPresent()) {
            // Event not found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Event event = eventOpt.get();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!isUserAdminOrCreator(currentUsername, event)) {
            // Unauthorized to update the event
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Integer attendeesCount = attendeesUpdate.getAttendeesCount();
        if (event.getEndDate().before(new Date()) && attendeesCount != null && attendeesCount >= 0 && attendeesCount <= event.getNumRegisteredUsers() && event.getAttendeesCount() == -1) {
            // Update the attendees count (the event has already ended)
            event.setAttendeesCount(attendeesCount);
            eventService.save(event);

            EventDTO eventDTO = transformDTO(event);
            // Define the "Location" header pointing to the event's URL using a relative path
            String eventUrl = "/api/events/" + eventId;
            return ResponseEntity.ok().header("Location", eventUrl).body(eventDTO);
        } else {
            // Event has not ended yet or other validation errors
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/graph/{eventId}")
    @Operation(summary = "Gets graph data of the event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Graph data obtained",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", description = "Operation not permitted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
    })
    public ResponseEntity<Map<String, Integer>> getEventGraphData(@PathVariable Long eventId) {

        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.notFound().build();

        }
        Event event = eventOptional.get();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!isUserAdminOrCreator(currentUsername, event)) {
            // Unauthorized to update the event
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Map<String, Integer> graphData = new HashMap<>();
        if (event.getAttendeesCount() == -1) {
            // If the attendees count has not been set returning -1 for both registeredUsers and didNotAttend to indicate data is not available
            graphData.put("registeredUsers", -1);
            graphData.put("attendeesCount", -1);
            graphData.put("didNotAttend", -1);
        } else {
            // If attendees count is available, populate the map with actual data
            graphData.put("registeredUsers", event.getNumRegisteredUsers());
            graphData.put("attendeesCount", event.getAttendeesCount());
            int didNotAttend = event.getNumRegisteredUsers() - event.getAttendeesCount();
            graphData.put("didNotAttend", didNotAttend);
        }

        return ResponseEntity.ok(graphData);
    }

    @GetMapping("/profile/graph")
    @Operation(summary = "Gets graph data of categories in relation to events")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Graph data obtained",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", description = "Operation not permitted", content = @Content),
    })
    public ResponseEntity<Map<String, List<String>>> getAdminProfileGraphData() {

        Map<String, List<String>> graphData = new HashMap<>();
        List<String> labels = categoryService.findAllNames();
        List<Integer> data = categoryService.categoriesNumbers();
        List<String> aux = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            aux.add(String.valueOf(data.get(i)));
        }
        graphData.put("data", aux);
        graphData.put("labels", labels);


        return ResponseEntity.ok(graphData);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event created if user is registered as event creator or as admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Operation not permitted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "The operation is not allowed when event has finished", content = @Content)
    })
    public ResponseEntity<EventDTO> deleteEvent(@PathVariable long eventId) {
        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Event existingEvent = eventOptional.get();

        //Check if event has not finished yet
        boolean eventFinished = LocalDateTime.now().isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (eventFinished) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        //Check if current user is the event creator or an admin
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!isUserAdminOrCreator(currentUsername, existingEvent)) {
            // Unauthorized to delete the event
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        eventService.delete(eventId);

        // Transform the saved event to EventDTO
        EventDTO eventDTO = transformDTO(existingEvent);

        return ResponseEntity.status(200).body(eventDTO);
    }


    @GetMapping("/stats/{eventId}")
    @Operation(summary = "Retrieves event basic stats for properly visualization, taking into account the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Event not found"),
    })
    public ResponseEntity<EventLiveStatsDTO> getEventLiveStats(HttpServletRequest request, @PathVariable long eventId) {
        //Check if event exists
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Event existingEvent = eventOptional.get();

        EventLiveStatsDTO stats = new EventLiveStatsDTO();

        //Check if user is registered
        Principal principal = request.getUserPrincipal();
        User userPrincipal;
        boolean isUserRegistered;
        boolean hasUserReviewed = false;
        if (principal == null || !userService.findByUserName(principal.getName()).isPresent()) {
            isUserRegistered = false;
        } else {
            userPrincipal = userService.findByUserName(principal.getName()).get();
            isUserRegistered = existingEvent.getRegisteredUsers().contains(userPrincipal);
            hasUserReviewed = reviewService.hasUserReviewedEvent(eventId, userPrincipal.getId());
        }

        stats.setHasUserJoined(isUserRegistered);
        stats.setHasUserReviewed(hasUserReviewed);


        LocalDateTime now = LocalDateTime.now();
        boolean eventFinished = now.isAfter(existingEvent.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        stats.setEventFinished(eventFinished);

        return ResponseEntity.status(200).body(stats);
    }

    @GetMapping("/filter")
    @Operation(summary = "Retrieves events filtered by category, with the category's ID specified in the URL, by input from the search bar or depending on our recommendation algorithm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events obtained",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
    })
    public ResponseEntity<PageEventDTO> filterByCategory(@RequestParam("page") int page, @RequestParam(value = "id", required = false) Long id, @RequestParam("type") String type, @RequestParam(value = "input", required = false) String input, Principal principal) {
        int pageSize = 3;
        Page<Event> events = new PageImpl<>(new ArrayList<>());
        List<EventDTO> eventDTOS = new ArrayList<>();
        switch (type) {
            case "category":

                try {
                    events = eventService.findByCategory(id, page, pageSize);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case "searchBar":
                try {
                    events = eventService.findBySearchBar(input, page, pageSize);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case "recommended":
                boolean adminOrUnregistered = false;
                if (principal != null) {
                    Optional<User> userOp = userService.findByUserName(principal.getName());
                    if (userOp.isPresent()) {
                        if (userOp.get().hasRole("USER")) {
                            User user = userOp.get();
                            events = userService.getUserCategoryPreferences(user.getId(), page, pageSize);

                        } else {
                            adminOrUnregistered = true;
                        }
                    }
                } else {
                    adminOrUnregistered = true;
                }
                if (adminOrUnregistered) {
                    events = eventService.eventsOrderedByPopularity(page, pageSize);
                }
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        for (Event e : events.getContent()) {
            eventDTOS.add(transformDTO(e));
        }

        return ResponseEntity.ok(new PageEventDTO(eventDTOS, page, events.getTotalPages()));
    }

    @GetMapping("/user")
    @Operation(summary = "Returns the events in which the user is a participant or creator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events obtained",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Operation not permitted", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)

    })
    public ResponseEntity<PageEventDTO> userEvents(@RequestParam("page") int page, Principal principal,
                                                   @RequestParam(value = "time", required = false) String time,
                                                   @RequestParam(value = "type", required = false) String type) {
        int pageSize = 3;
        List<EventDTO> eventDTOS = new ArrayList<>();
        Page<Event> events;

        if (principal == null || principal.getName().equals("anonymousUser")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> userOp = userService.findByUserName(principal.getName());
        if (!userOp.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        User currentUser = userOp.get();

        if (currentUser.hasRole("ADMIN")) {
            events = eventService.findAll(page, pageSize);
        } else if (type != null && time != null) {
            switch (type) {
                case "created":
                    if ("present".equals(time)) {
                        events = eventService.findByCreatorIdCurrentCreatedEvents(currentUser.getId(), page, pageSize);
                    } else if ("past".equals(time)) {
                        events = eventService.findByCreatorIdPastCreatedEvents(currentUser.getId(), page, pageSize);
                    } else {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    break;
                case "registered":
                    if ("present".equals(time)) {
                        events = eventService.findByRegisteredUserIdCurrentEvents(currentUser.getId(), page, pageSize);
                    } else if ("past".equals(time)) {
                        events = eventService.findByRegisteredUserIdPastEvents(currentUser.getId(), page, pageSize);
                    } else {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        for (Event e : events.getContent()) {
            eventDTOS.add(transformDTO(e));
        }

        PageEventDTO pageEventDTO = new PageEventDTO(eventDTOS, page, events.getTotalPages());

        return ResponseEntity.ok(pageEventDTO);
    }

    private EventDTO transformDTO(Event event) {
        if (event.getEndDate().before(new Date())) {
            return new EventFinishedDTO(event.getId(), event.getName(), event.getDescription(), event.getMaxCapacity(), event.getPrice(), event.getLocation(), event.getMap(), event.getStartDate(), event.getEndDate(), event.getAdditionalInfo(), event.getCreator().getId(), event.getNumRegisteredUsers(), event.getCategory().getId(), event.getAttendeesCount(), reviewService.calculateAverageRatingForEvent(event.getId()), reviewService.countReviewsForEvent(event.getId()));
        }
        return new EventDTO(event.getId(), event.getName(), event.getDescription(), event.getMaxCapacity(), event.getPrice(), event.getLocation(), event.getMap(), event.getStartDate(), event.getEndDate(), event.getAdditionalInfo(), event.getCreator().getId(), event.getNumRegisteredUsers(), event.getCategory().getId());
    }

    private boolean isUserAdminOrCreator(String username, Event event) {
        Optional<User> userOpt = userService.findByUserName(username);
        if (!userOpt.isPresent()) {
            return false;
        }
        User user = userOpt.get();

        // Check if the user has ROLE_ADMIN authority
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Check if the user is the event creator
        boolean isCreator = event.getCreator().equals(user);

        return isAdmin || isCreator;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

    public Event transformEvent(EventManipulationDTO dto) {
        Event event = new Event(dto.getName(), null, dto.getDescription(), dto.getMaxCapacity(),
                dto.getPrice(), dto.getLocation(), dto.getMap(), dto.getStartDate(), dto.getEndDate(), dto.getAdditionalInfo());
        return event;
    }
}

