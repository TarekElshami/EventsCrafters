package com.EventCrafters.EventCrafters.service;

import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.Secondary.PageUserRecc;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.repository.EventRepository;
import com.EventCrafters.EventCrafters.repository.ReviewRepository;
import com.EventCrafters.EventCrafters.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private EventService eventService;

	public Optional<User> findById(long id) {
		return repository.findById(id);
	}

	public Optional<User> findByUserName(String name) {
		return repository.findByUsername(name);
	}
	public boolean exist(long id) {
		return repository.existsById(id);
	}

	public List<User> findAll() {
		return repository.findAll();
	}

	public void save(User user) {
		if (user.getPhoto()==null) user.setDefaultPhoto();
		repository.save(user);
	}

	public void delete(long id) {
		repository.deleteById(id);
	}

	public Long findUserIdByUsername(String name) {
		Optional<User> userOptional = repository.findByUsername(name);
		User user = userOptional.orElse(null);
		return user != null ? user.getId() : null;
	}

	@Transactional
	public User deleteUserById(Long userId) {
		Optional<User> userOptional = repository.findById(userId);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			repository.deleteRegistrationsByUserId(userId);
			reviewRepository.deleteReviewsByUserId(userId);
			for (Event event : eventRepository.findByCreatorId(user.getId())) {
				eventService.delete(event.getId());
			}
			repository.deleteById(userId);
			return user;
		}
		return null;
	}

	public void banUserByUsername(String username) {
		Optional<User> userOptional = repository.findByUsername(username);
		User user = userOptional.orElse(null);
		if (user != null) {
			repository.deleteRegistrationsByUserId(user.getId());
			reviewRepository.deleteReviewsByUserId(user.getId());
			for (Event event : eventRepository.findByCreatorId(user.getId())) {
				eventService.delete(event.getId());
			}
			user.setBanned(true);
			repository.save(user);
		}
	}

	public User authenticateUser(String username, String password) throws UsernameNotFoundException {
		Optional<User> userOptional = repository.findByUsername(username);
		if (!userOptional.isPresent()) {
			throw new UsernameNotFoundException("User not found");
		}

		User user = userOptional.get();

		if (user.isBanned()) {
			throw new DisabledException("User is banned"); // Or any other appropriate exception
		}

		// Perform password check here if needed

		return user;
	}

	public void unbanUserByUsername(String username) {
		Optional<User> userOptional = repository.findByUsername(username);
		User user = userOptional.orElse(null);
		if (user != null) {
			user.setBanned(false);
			repository.save(user);
		}
	}

	public Page<Event> getUserCategoryPreferences(Long id, int page, int pageSize){
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<BigInteger> ids = repository.getUserCategoryPreferences(id, pageable);
		List<Event> result = new ArrayList<>();
		for(BigInteger aux : ids.getContent()){
			result.add(eventService.findById(aux.longValue()).get());
		};
		PageUserRecc P = new PageUserRecc(result, ids.getTotalPages());
		return P;

	}

	public boolean isValidUser(User user) {
		Long id = user.getId();
		String name = user.getName();
		String username = user.getUsername();
		String email = user.getEmail();
		String password = user.getEncodedPassword();

		if (id!=null) {
			return false;
		} else if (name==null || name.isEmpty()){
			return false;
		} else if (username==null || username.isEmpty()){
			return false;
		} else if (email==null || email.isEmpty()){
			return false;
		} else if (password==null || password.isEmpty()){
			return false;
		}
		return true;
	}

	public ResponseEntity<?> changeCurrentUserProfilePicture(@RequestParam("profilePicture") MultipartFile pfp) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> userOptional = this.findByUserName(currentUsername);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			try {
				// Get the file content as a byte array
				byte[] fileContent = pfp.getBytes();

				// Convert the byte array to a Blob
				Blob pfpBlob = new SerialBlob(fileContent);

				// Process the Blob as needed
				// For example, you can save it to a database or use it in your application
				user.setPhoto(pfpBlob);
				this.save(user);

				return ResponseEntity.ok().build();
			} catch (IOException | SQLException e) {
				// Handle exceptions
				e.printStackTrace();
				return ResponseEntity.status(500).build();
			}
			//SecurityContextHolder.clearContext();
		}
		return ResponseEntity.notFound().build();
	}
}
