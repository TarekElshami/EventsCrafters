package com.EventCrafters.EventCrafters.service;

import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.repository.ReviewRepository;
import com.EventCrafters.EventCrafters.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private ReviewRepository reviewRepository;


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
	public void deleteUserById(Long userId) {
		repository.deleteRegistrationsByUserId(userId);
		reviewRepository.deleteReviewsByUserId(userId);
		repository.deleteById(userId);

	}

	public void banUserByUsername(String username) {
		Optional<User> userOptional = repository.findByUsername(username);
		User user = userOptional.orElse(null);
		if (user != null) {
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
}
