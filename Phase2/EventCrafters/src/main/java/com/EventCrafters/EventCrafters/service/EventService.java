package com.EventCrafters.EventCrafters.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.EventCrafters.EventCrafters.DTO.EventManipulationDTO;
import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.EventCrafters.EventCrafters.repository.EventRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

	@Autowired
	private MailService mailService;

	@Autowired
	private EventRepository repository;

	public Optional<Event> findById(long id) {
		return repository.findById(id);
	}

	public boolean exist(long id) {
		return repository.existsById(id);
	}

	public List<Event> findAll() { return repository.findAll(); }
	public Page<Event> findAll(int page,int pageSize) {
		return repository.findAll(PageRequest.of(page, pageSize));
	}

	public Event save(Event event) {
		return repository.save(event);
	}

	public Event update(Event event){
		Set<User> registeredUsers = event.getRegisteredUsers();
		String subject = "Actualización del Evento: " + event.getName();
		for (User user : registeredUsers) {
			String content = generateUpdateEmailContent(event.getName());
			mailService.sendEmail(user, subject, content, true);
		}

		return repository.save(event);
	}

	private String generateUpdateEmailContent(String eventName) {
		return String.format(
				"<html>" +
						"<head>" +
						"<style>" +
						"body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }" +
						".container { background-color: #f8f8f8; border-radius: 10px; padding: 20px; text-align: center; }" +
						"h2 { color: #4CAF50; }" +
						"p { margin: 10px 0; }" +
						"</style>" +
						"</head>" +
						"<body>" +
						"<div class='container'>" +
						"<h2>Uno de los eventos en los que estás inscrito ha sido modificado</h2>" +
						"<p>El evento '%s' ha recibido importantes actualizaciones.</p>" +
						"<p>Te invitamos a iniciar sesión en tu cuenta para descubrir todas las novedades.</p>" +
						"</div>" +
						"</body>" +
						"</html>", eventName);
	}

	@Transactional
	public void delete(Long eventId) {
		Optional<Event> eventOptional = repository.findById(eventId);
		Date now = new Date();
		if(eventOptional.isPresent()) {
			Event event = eventOptional.get();
			if(event.getStartDate().after(now)) {
				for (User notifiedUser : event.getRegisteredUsers()) {
					String subject = "Información Importante Sobre Tu Evento Inscrito";
					String content = generateEmailContent(event.getName());
					mailService.sendEmail(notifiedUser, subject, content, true);
				}
			}
		}
		repository.deleteReviewsByEventId(eventId);
		repository.deleteEventUserByEventId(eventId);
		repository.deleteEventByIdCustom(eventId);

	}

	private String generateEmailContent(String eventName) {
		return String.format(
				"<html>" +
						"<head>" +
						"<style>" +
						"body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }" +
						".container { background-color: #f8f8f8; border: 1px solid #ddd; padding: 20px; }" +
						"h2 { color: #f40; }" +
						"p { margin: 10px 0; }" +
						"</style>" +
						"</head>" +
						"<body>" +
						"<div class='container'>" +
						"<h2>Evento Cancelado: '%s'</h2>" +
						"<p>Querido participante,</p>" +
						"<p>Lamentamos informarte que el evento '%s', en el cual estabas inscrito, ha sido cancelado.</p>" +
						"<p>Entendemos que esto puede ser decepcionante y agradecemos tu comprensión. Estamos comprometidos a ofrecerte la mejor experiencia y te invitamos a explorar otros eventos emocionantes en nuestra plataforma.</p>" +
						"<p>Gracias por tu apoyo continuo.</p>" +
						"</div>" +
						"</body>" +
						"</html>", eventName, eventName);
	}


	public Page<Event> findByCategory(long id, int page, int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByCategory(id, pageable);
	}


	public Page<Event> findBySearchBar(String input, int page, int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findBySearchBar(input, pageable);
	}


	public Page<Event> findByCreatorIdCurrentCreatedEvents(Long id, int page, int  pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByCreatorIdCurrentCreatedEvents(id, pageable);
	}

	public Page<Event> findByCreatorIdPastCreatedEvents(Long id, int page, int  pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByCreatorIdPastCreatedEvents(id, pageable);
	}


	public Page<Event> findByRegisteredUserIdCurrentEvents(Long id, int page, int  pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByRegisteredUserIdCurrentEvents(id, pageable);
	}


	public Page<Event> findByRegisteredUserIdPastEvents(Long id, int page, int  pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByRegisteredUserIdPastEvents(id, pageable);
	}

	@Transactional
	public void updateAttendeesCount(Long eventId, int attendeesCount) {
		Event event = repository.findById(eventId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));
		event.setAttendeesCount(attendeesCount);
		repository.save(event);
	}

	public Page<Event> eventsOrderedByPopularity(int page, int pageSize){
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findByRegisteredUsersCount(pageable);
	}

	public String getShortDescription(String desc) {
		int limit = 40;
		if (desc.length() > limit) {
			return desc.substring(0, limit - 3) + "...";
		} else {
			return desc;
		}
	}

	public String formatDate(Date date) {
		if (date == null) return null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(date);
	}

	public static boolean eventHasValidFields(EventManipulationDTO event) {
		if (
				event.getName() == null || event.getName().trim().isEmpty() ||
						event.getDescription() == null || event.getDescription().trim().isEmpty() ||
						event.getLocation() == null || event.getLocation().trim().isEmpty() ||
						event.getMap() == null || event.getMap().trim().isEmpty() ||
						event.getCategoryId() == null ||
						event.getAdditionalInfo() == null || event.getAdditionalInfo().trim().isEmpty()) {
			return true;
		}

		if (event.getMaxCapacity() <= 0 || event.getPrice() < 0 || !isPriceValid(event.getPrice())) {
			return true;
		}

		Date now = new Date();

		if (event.getStartDate() == null || event.getStartDate().before(now)) {
			return true;
		}

		if (event.getEndDate() == null || event.getEndDate().before(event.getStartDate())) {
			return true;
		}

		if (isMapIframeInvalid(event.getMap())) return true;

		return false;
	}

	public static boolean isMapIframeInvalid(String map) {
		String mapIframeRegex = "<iframe.*src=\"https?.*\".*></iframe>";
		Pattern pattern = Pattern.compile(mapIframeRegex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(map);

		if (!matcher.find()) {
			return true;
		}
		return false;
	}

	public static boolean isPriceValid(double price) {
		// Convert the price to a string
		String priceText = String.valueOf(price);

		// Find the position of the decimal point
		int decimalPointIndex = priceText.indexOf(".");

		// If there's no decimal point, the number is an integer and valid
		if (decimalPointIndex == -1) {
			return true;
		}

		// Calculate the number of digits after the decimal point
		int decimalDigits = priceText.length() - decimalPointIndex - 1;

		// Check that there are no more than two digits after the decimal point
		return decimalDigits <= 2;
	}


	public static void assignEventProperties(Event event, String name, String description,
									  int maxCapacity, double price, String location,
									  String map, Date startDate, Date endDate,
									  String additionalInfo) {
		event.setName(name);
		event.setDescription(description);
		event.setMaxCapacity(maxCapacity);
		event.setPrice(price);
		event.setLocation(location);
		event.setMap(map);
		event.setStartDate(startDate);
		event.setEndDate(endDate);
		event.setAdditionalInfo(additionalInfo);
	}

}
