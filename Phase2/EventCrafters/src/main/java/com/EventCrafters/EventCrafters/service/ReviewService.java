package com.EventCrafters.EventCrafters.service;

import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.Review;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

	@Autowired
	private ReviewRepository repository;

	@Autowired
	private MailService mailService;

	public Optional<Review> findById(long id) {
		return repository.findById(id);
	}

	public boolean exist(long id) {
		return repository.existsById(id);
	}

	public List<Review> findAll() {
		return repository.findAll();
	}

	public void save(Review review) {
		repository.save(review);

		User eventCreator = review.getEvent().getCreator();
		String subject = "Nueva reseña publicada para tu evento";
		String content = String.format(
				"<html>" +
						"<head>" +
						"<style>" +
						"body { font-family: Arial, sans-serif; line-height: 1.6; }" +
						".header { background: #f4f4f4; padding: 10px; text-align: center; }" +
						".content { margin: 20px; }" +
						".rating { color: #f4b400; font-size: 18px; }" +
						".review-text { margin-top: 20px; }" +
						"</style>" +
						"</head>" +
						"<body>" +
						"<div class='header'><h2>Reseña para el Evento: '%s'</h2></div>" +
						"<div class='content'>" +
						"<p><strong>El usuario:</strong> %s</p>" +
						"<p><strong>ha calificado al evento con:</strong> <span class='rating'>%d/5</span></p>" +
						"<div class='review-text'><strong>y ha puesto este mensaje:</strong> %s</div>" +
						"</div>" +
						"</body>" +
						"</html>",
				review.getEvent().getName(), review.getUser().getUsername(), review.getRating(), review.getText().replace("\n", "<br>")
		);

		mailService.sendEmail(eventCreator, subject, content, true);
	}

	public void delete(long id) {
		repository.deleteById(id);
	}

	public double calculateAverageRatingForEvent(Long eventId) {
		return repository.findAverageRatingByEvent(eventId)
				.orElse(0.0);
	}

	public boolean hasUserReviewedEvent(Long eventId, Long Id) {
		Optional<Review> review = repository.findByEventIdAndUserId(eventId, Id);
		return review.isPresent();
	}

	public Optional<Review> findByUserAndEvent(User user, Event event) {
		return repository.findByEventIdAndUserId(user.getId(), event.getId());
	}

	public int countReviewsForEvent(Long eventId) {
		return repository.countByEventId(eventId);
	}
}
