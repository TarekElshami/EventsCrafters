package com.EventCrafters.EventCrafters.controller;

import com.EventCrafters.EventCrafters.model.Category;
import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.security.jwt.AuthResponse;
import com.EventCrafters.EventCrafters.security.jwt.UserLoginService;
import com.EventCrafters.EventCrafters.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.Optional;

@Controller
public class UserWebController {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserLoginService userLoginService;
	@Autowired
	private AjaxService ajaxService;

	private ObjectMapper objectMapper;

	@Autowired
	private void ChartController(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	private static Map<String, TokenService> tokens = new HashMap<>();
	public static void addToken(String user, TokenService token){tokens.put(user, token);}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	@RequestMapping("/loginerror")
	public String loginerror() {
		return "loginerror";
	}

	@GetMapping("/logout")
	public String logout() {
		return "redirect:/login";
	}

	@GetMapping("/profile")
	public String newReview(Model model, HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> user = userService.findByUserName(currentUsername);
		Long id;
		if (user.isPresent()) {
			model.addAttribute("user", user.get());
			id = user.get().getId();

			if (request.isUserInRole("USER")) {
				List<Event> currentCreatedE = ajaxService.findAjax(id, 1);
				List<Event> pastCreatedE = ajaxService.findAjax(id, 2);
				List<Event> userRegisteredCurrentEvents = ajaxService.findAjax(id, 3);
				List<Event> userRegisteredPastEvents = ajaxService.findAjax(id, 4);
				int i;
				for (i = 1; i < 5; i++){
					if (ajaxService.getMaxPageNum(i) <= 1){
						String aux = "thereAreNoMore" + i;
						model.addAttribute(aux, "");
					}
				}

				model.addAttribute("events",currentCreatedE);
				model.addAttribute("pastEvents",pastCreatedE);
				model.addAttribute("registeredCurrentEvents", userRegisteredCurrentEvents);
				model.addAttribute("registeredPastEvents", userRegisteredPastEvents);
				model.addAttribute("showWhenAdmin", "d-none");
				model.addAttribute("showWhenUser", "");
				model.addAttribute("eventsText", "Mis Eventos Creados");
				model.addAttribute("createdEvents",1);
			} else {
				List<Category> c = categoryService.findAjax();
				List<Event> e = ajaxService.findAjax(0);
				if (ajaxService.getMaxPageNum(0)  <= 1){
					model.addAttribute("thereAreNoMore1", "");
				}
				if (categoryService.getMaxPageNum() <= 2){
					model.addAttribute("thereAreNoMore0", "");
				}
				model.addAttribute("adminOnly", "");
				model.addAttribute("category",c);
				model.addAttribute("events",e);
				model.addAttribute("showWhenAdmin","");
				model.addAttribute("showWhenUser", "d-none");
				model.addAttribute("eventsText", "Todos los eventos");
				model.addAttribute("createdEvents",0);
			}

			return "profile";
		}
		return "error";
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String createAccount(Model model, User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		user.setRole("USER");

		if (user.getPhoto()==null){
			user.setDefaultPhoto();
		}
		userService.save(user);
		return "redirect:/login";
	}

	@GetMapping("/IsUsernameTaken")
	public ResponseEntity<String> isUserNameTaken(@RequestParam("username") String username) {
		Optional<User> userOptional = userService.findByUserName(username);
		if (userOptional.isPresent()){
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
		} else {
			return ResponseEntity.ok("Username is available");
		}
	}

	@GetMapping("/IsUserBanned")
	@ResponseBody
	public ResponseEntity<Boolean> isUserBanned(@RequestParam("username") String username) {
		Optional<User> userOptional = userService.findByUserName(username);
		if (userOptional.isPresent()) {
			return ResponseEntity.ok(userOptional.get().isBanned());
		}
		return ResponseEntity.ok(false);
	}

	@GetMapping("/recoverPassword/{user}")
	public String recoverPassword(@PathVariable String user) {
		Optional<User> userOptional = userService.findByUserName(user);
        if (userOptional.isPresent()) {
            TokenService tokenService = new TokenService(userOptional.get());
			tokens.put(userOptional.get().getUsername(), tokenService);
			String link = "https://localhost:8443/recoverPassword/" + userOptional.get().getUsername() +"/randomToken?token=" + tokenService.getToken();
			return new MailService().sendEmail(
					userOptional.get(),
					"Recuperación de contraseña de Event Crafters",
					"He aquí un enlace de un solo uso para que restablezcas tu contraseña" + "\n\n" + link,
					false
			);
        }
		return "error";
	}

	@GetMapping("/recoverPassword/{user}/randomToken")
	public String recoverPasswordWithToken(Model model, @PathVariable String user, @RequestParam("token") String token) {
		Optional<User> userOptional = userService.findByUserName(user);
		if (userOptional.isPresent()) {
			TokenService tokenService = tokens.get(userOptional.get().getUsername());
			if (!tokenService.isValid() || !tokenService.getToken().equals(token)){
				return "invalidLink";
			}
			model.addAttribute("recover", true);
			model.addAttribute("recoverToken", tokenService.getToken());
			model.addAttribute("username",userOptional.get().getUsername());
		}
		return "change_password";
	}

	@PostMapping("/recoverPassword/{user}/randomToken")
	public String sendRecoverPasswordWithToken(Model model,
											   @PathVariable String user,
											   @RequestParam("token") String token,
											   @RequestParam("password") String password) {
		Optional<User> userOptional = userService.findByUserName(user);
		if (userOptional.isPresent()) {
			TokenService tokenService = tokens.get(userOptional.get().getUsername());
			if (!tokenService.isValid() || !token.equals(tokenService.getToken())){
				model.addAttribute("valid", false);
				return "invalidLink";
			}
			tokens.remove(user);
			userOptional.get().setEncodedPassword(passwordEncoder.encode(password));
			//tell the user the operation was successful
			userService.save(userOptional.get());
		}
		return "redirect:/login";
	}


	@PostMapping("/delete-account")
	public String deleteAccount(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> userOptional = userService.findByUserName(currentUsername);
		User user = userOptional.get();
		Long currentUserId = user.getId();
		userService.deleteUserById(currentUserId);
		//this is problematic, as it sends a GET request to /logout, which only accepts
		//POST requests.
		new AuthResponse(AuthResponse.Status.SUCCESS, userLoginService.logout(request, response));
		return "redirect:/logout";
	}


	@PostMapping("/updateProfile")
	public String updateProfile(Model model, @ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> userOptional = userService.findByUserName(currentUsername);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setName(updatedUser.getName());
			user.setEmail(updatedUser.getEmail());
			user.setUsername(updatedUser.getUsername());
			userService.save(user);
			SecurityContextHolder.clearContext();
			return "redirect:/login";
		}

		return "error";
	}

	@PostMapping("/setProfilePicture")
	public ResponseEntity<?> setProfilePicture(@RequestParam("profilePicture") MultipartFile pfp) {
		return userService.changeCurrentUserProfilePicture(pfp);
	}

	@Hidden
	@GetMapping("/profile/img/{username}")
	@ResponseBody
	public byte[] showPFP(@PathVariable String username) throws SQLException, IOException {
		Optional<User> userOptional = userService.findByUserName(username);

		if (userOptional.isPresent()) {
			Blob photoBlob = userOptional.get().getPhoto();
            int blobLength = (int) photoBlob.length();
			byte[] blobAsBytes = photoBlob.getBytes(1, blobLength);
			photoBlob.free();
			return blobAsBytes;

		} else {
			return new byte[0];
		}
	}

	@GetMapping("/chart-page")
	public String chart(Model model) throws JsonProcessingException {

		List<String> labels = categoryService.findAllNames();
		List<Integer> data = categoryService.categoriesNumbers();

		model.addAttribute("labels", objectMapper.writeValueAsString(labels));
		model.addAttribute("data", objectMapper.writeValueAsString(data));

		return "chart-page";
	}

	@PostMapping("/ban")
	public String banUser(@RequestParam("usernameBan") String username) {
		userService.banUserByUsername(username);
		return "redirect:/profile";
	}

	@PostMapping("/unban")
	public String unbanUser(@RequestParam("usernameUnBan") String username) {
		userService.unbanUserByUsername(username);
		return "redirect:/profile";
	}

}
