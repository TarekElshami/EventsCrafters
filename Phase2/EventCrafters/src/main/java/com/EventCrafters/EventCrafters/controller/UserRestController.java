package com.EventCrafters.EventCrafters.controller;

import com.EventCrafters.EventCrafters.DTO.CensoredUserDTO;
import com.EventCrafters.EventCrafters.DTO.FullUserDTO;
import com.EventCrafters.EventCrafters.DTO.NewUserDTO;
import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	private static Map<String, TokenService> tokens = new HashMap<>();
	public static void addToken(String user, TokenService token){tokens.put(user, token);}

	@Operation(summary = "Gets the currently authenticated user",
			description = "Returns all information associated to the authenticated user. If no user is authenticated, returns 404 not found")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User found",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = FullUserDTO.class)) }),
			@ApiResponse(responseCode = "404", description = "No user currently authenticated", content = @Content)
	})
	@GetMapping("/me")
	public ResponseEntity<FullUserDTO> currentUser(HttpServletRequest request){
		Principal principal = request.getUserPrincipal();
		if(principal != null) {
			return ResponseEntity.ok(new FullUserDTO(userService.findByUserName(principal.getName()).orElseThrow()));
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Gets a specific user by their id",
			description = "Returns the user with the id specified in the URL. If it is the currently authenticated user, all information about them will be returned. If it is some other user, a censored version will be returned, omitting some information. If there is no user with the specified id, returns 404 not found.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User found",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(oneOf = {FullUserDTO.class, CensoredUserDTO.class})) }),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content)
	})
	@GetMapping("/{id}")
	public ResponseEntity<CensoredUserDTO> getUser(HttpServletRequest request, @PathVariable Long id){
		Optional<User> optionalUser = userService.findById(id);
		if (optionalUser.isEmpty()) {
			return ResponseEntity.status(404).build();
		}

		User user = (optionalUser.get());
		Principal principal = request.getUserPrincipal();
		if (principal != null){
			Optional<User> userPrincipalOptional = userService.findByUserName(principal.getName());
			if (userPrincipalOptional.isPresent()) {
				User userPrincipal = userPrincipalOptional.get();
				if (userPrincipal.hasRole("ADMIN")){
					return ResponseEntity.ok(new FullUserDTO(user));
				}
			}
		}

		if (principal != null && user.getUsername().equals(principal.getName())) {
			return ResponseEntity.ok(new FullUserDTO(user));
		} else {
			return ResponseEntity.ok(new CensoredUserDTO(user));
		}
	}

	@Operation(summary = "Creates a new user",
			description = "Creates a new user from the data specified in the request body.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User created",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = FullUserDTO.class)) }),
			@ApiResponse(responseCode = "409",
					description = "Conflict. Username is taken.", content = @Content),
			@ApiResponse(responseCode = "400",
					description = "Bad Request. Body must not have a photo attribute.", content = @Content)
	})
	@PostMapping
	public ResponseEntity<FullUserDTO> newUser(@RequestBody NewUserDTO userDTO){
		User user = new User(userDTO);
		if (userService.findByUserName(user.getUsername()).isPresent()) {
			return ResponseEntity.status(409).build(); //409 conflict
		}

		if (!userService.isValidUser(user)){
			return ResponseEntity.badRequest().build();
		}

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		user.clearRoles();
		user.setRole("USER");
		user.setDefaultPhoto();

		userService.save(user);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", "/api/users/"+user.getId());

		FullUserDTO fullUserDTO = new FullUserDTO(user);
		return new ResponseEntity<>(fullUserDTO, headers, HttpStatus.CREATED);
	}

	@Operation(summary = "Gets the image associated with a user.",
			description = "Returns the image associated to the user with the specified id.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Image found",
					content = { @Content(mediaType = "image/jpeg")}),
			@ApiResponse(responseCode = "404",
					description = "Image not found", content = @Content),
			@ApiResponse(responseCode = "500",
					description = "Internal Server Error", content = @Content)
	})
	@GetMapping ("/img/{id}")
	public ResponseEntity<byte[]> showUserImage(@PathVariable long id){
		Optional<User> userOptional = userService.findById(id);
		if (userOptional.isPresent()) {
			User user = userOptional.get();

			try {
				Blob photoBlob = user.getPhoto();
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

	@Operation(summary = "Substitutes a user with the one provided",
			description = "Substitutes the user with the specified id with a user created from the provided data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User modified",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = FullUserDTO.class))}),
			@ApiResponse(responseCode = "400",
					description = "Bad Request. Provided user must not have a id attribute.", content = @Content),
			@ApiResponse(responseCode = "403",
					description = "Forbidden. Current user lacks authority to modify specified user", content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Not Found. No user found with provided id", content = @Content),
			@ApiResponse(responseCode = "409",
					description = "Conflict. Invalid Username.", content = @Content)
	})
	@PutMapping("/{id}")
	public ResponseEntity<FullUserDTO> modifyUser(@RequestBody FullUserDTO userDTO, @PathVariable Long id, Principal principal){
		if (userService.findByUserName(userDTO.getUsername()).isPresent()) return ResponseEntity.status(409).build(); //conflict

		User user = new User(userDTO);
		user.setPassword("temporaryPass");
		if (!userService.isValidUser(user)){
			return ResponseEntity.badRequest().build();
		}

		Optional<User> optUser = userService.findById(id);
		if (optUser.isPresent()) {
			if (optUser.get().hasRole("ADMIN")) return ResponseEntity.status(403).build();

			if (!checkUserPrivileges(principal, optUser)) return ResponseEntity.status(403).build();

			user.setId(optUser.get().getId());
			user.setEncodedPassword(optUser.get().getEncodedPassword());
			//String encodedPassword = passwordEncoder.encode(user.getPassword());
			//user.setPassword(encodedPassword);
			user.clearRoles();
			user.setRole("USER");
			user.setBanned(false);

			if (user.getPhoto() == null) {
				user.setDefaultPhoto();
			}
			userService.save(user);
			return ResponseEntity.ok(new FullUserDTO(user));
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Deletes user with specified id.",
			description = "Deletes user with specified id. If no such user exists, return 404. If current user has no permissions to delete specified user, return 403.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User deleted",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = FullUserDTO.class))}),
			@ApiResponse(responseCode = "403",
					description = "Forbidden. Current user lacks authority to delete specified user", content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Not Found. No user found with provided id", content = @Content)
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<FullUserDTO> deleteUser(@PathVariable Long id, Principal principal){
		Optional<User> optUser = userService.findById(id);
		if (!checkUserPrivileges(principal, optUser)) return ResponseEntity.status(403).build(); //403 forbidden

		if (optUser.isPresent()) {
			if (optUser.get().hasRole("ADMIN")) return ResponseEntity.status(403).build();
			return ResponseEntity.ok(new FullUserDTO(userService.deleteUserById(id)));
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Changes the profile picture of the specified user",
			description = "Changes the profile picture of the user with the given id to a picture created from the data provided in the body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Ok. Profile picture changed"),
			@ApiResponse(responseCode = "400", description = "Bad Request. No data provided, or data provided was empty", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user lacks permission to edit this resource", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found. No user found with provided id",content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error. Couldn't create a Blob from provided data.",content = @Content)
	})
	@PostMapping("/img/{id}")
	public ResponseEntity<String> changeProfilePicture(@PathVariable("id") Long id, @RequestPart("photo") MultipartFile pfpData, Principal principal) {
		if (pfpData==null) return ResponseEntity.badRequest().build();
		Optional<User> userOptional = userService.findById(id);
		if (userOptional.isEmpty()) return ResponseEntity.notFound().build();
		User user = userOptional.get();

		if (user.hasRole("ADMIN")) return ResponseEntity.status(403).build();
		if (!checkUserPrivileges(principal, userOptional)) return ResponseEntity.status(403).build();

        try {
            Blob pfp = new SerialBlob(pfpData.getBytes());
			user.setPhoto(pfp);
			userService.save(user);
			return ResponseEntity.ok().build();
		} catch (SQLException e) {
            return ResponseEntity.status(500).build();
        } catch (IOException e) {
			return ResponseEntity.status(500).build();
        }
    }

	@Operation(summary = "Sends an email for password recovery.",
			description = "Sends a one time email to the user with specified id for them to recover their password through the web app.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Email sent"),
			@ApiResponse(responseCode = "404", description = "Not Found. No user found with provided id", content = @Content)
	})
	@PostMapping("/{id}/recoverPassword")
	public ResponseEntity<String> recoverPassword(@PathVariable Long id) {
		Optional<User> userOptional = userService.findById(id);
		if (userOptional.isPresent()) {
			TokenService tokenService = new TokenService(userOptional.get());
			UserWebController.addToken(userOptional.get().getUsername(), tokenService);
			String link = "https://localhost:8443/recoverPassword/" + userOptional.get().getUsername() +"/randomToken?token=" + tokenService.getToken();
			new MailService().sendEmail(
					userOptional.get(),
					"Recuperación de contraseña de Event Crafters",
					"He aquí un enlace de un solo uso para que restablezcas tu contraseña" + "\n\n" + link,
					false
			);
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build();
	}

	@Operation(summary = "Bans specified user",
			description = "Bans user with specified id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User banned"),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
	})
	@PostMapping("/{id}/ban")
	public ResponseEntity<String> banUser(@PathVariable Long id, Principal principal) {
		if (principal==null) return ResponseEntity.status(403).build();
		Optional<User> principalUser = userService.findByUserName(principal.getName());
		if (principalUser.isPresent()){
			if (!principalUser.get().hasRole("ADMIN")){
				return ResponseEntity.status(403).build();
			}
			Optional<User> userOp = userService.findById(id);
			if (userOp.isEmpty()){ return ResponseEntity.notFound().build();}
			if (userOp.get().hasRole("ADMIN")) { return ResponseEntity.status(403).build();}
			userOp.get().setBanned(true);
			userService.save(userOp.get());
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.status(500).build();
	}

	@Operation(summary = "Unbans specified user",
			description = "Unbans user with specified id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User unbanned"),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
	})
	@PostMapping("/{id}/unban")
	public ResponseEntity<String> unbanUser(@PathVariable Long id, Principal principal) {
		if (principal==null) return ResponseEntity.status(403).build();
		Optional<User> principalUser = userService.findByUserName(principal.getName());
		if (principalUser.isPresent()){
			if (!principalUser.get().hasRole("ADMIN")){
				return ResponseEntity.status(403).build();
			}
			Optional<User> userOp = userService.findById(id);
			if (userOp.isEmpty()){ return ResponseEntity.notFound().build();}
			userOp.get().setBanned(false);
			userService.save(userOp.get());
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.status(500).build();
	}


	@Operation(summary = "Check banned users",
			description = "Check is users is banned. The response entity will provide the boolean comprobation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User banned/User not banned")
	})
	@GetMapping("/IsUserBanned")
	@ResponseBody
	public ResponseEntity<Boolean> isUserBanned(@RequestParam("username") String username) {
		Optional<User> userOptional = userService.findByUserName(username);
		if (userOptional.isPresent()) {
			return ResponseEntity.ok(userOptional.get().isBanned());
		}
		return ResponseEntity.ok(false);
	}



	@Operation(summary = "Check username taken",
			description = "Check if username is taken")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Username taken /not taken")
	})
	@GetMapping("/IsUsernameTaken")
	@ResponseBody
	public ResponseEntity<Boolean> isUserNameTaken(@RequestParam("username") String username) {
		Optional<User> userOptional = userService.findByUserName(username);
		if (userOptional.isPresent()){
			return ResponseEntity.ok(true);
		} else {
			return ResponseEntity.ok(false);
		}
	}


	@Operation(summary = "recover password",
			description = "recover pssword")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "email sent"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad request")

	})

	@PostMapping("/recoverPassword")
	@ResponseBody
	public ResponseEntity<String> recoverPassword(@RequestBody String user) {
		Optional<User> userOptional = userService.findByUserName(user);
		if (userOptional.isPresent()) {
			TokenService tokenService = new TokenService(userOptional.get());
			tokens.put(userOptional.get().getUsername(), tokenService);
			String link = "http://localhost:4200/recoverPassword/" + userOptional.get().getUsername() +"/randomToken?token=" + tokenService.getToken();
			String status = new MailService().sendEmail(
					userOptional.get(),
					"Recuperación de contraseña de Event Crafters",
					"He aquí un enlace de un solo uso para que restablezcas tu contraseña" + "\n\n" + link,
					false
			);
			if ("emailSent".equals(status)) {
				return ResponseEntity.ok().build();
			} else {
				return ResponseEntity.status(500).build();
			}
		}
		return ResponseEntity.badRequest().build();
	}



	@Operation(summary = "change password",
			description = "change password")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "changed password"),
			@ApiResponse(responseCode = "403", description = "Invalid link")
	})
	@PostMapping("/changePassword")
	@ResponseBody
	public ResponseEntity<String> changePassword(@RequestBody Map<String, String> body) {
		String username = body.get("username");
		String password = body.get("password");
		String token = body.get("token");
		Optional<User> userOptional = userService.findByUserName(username);
		if (userOptional.isPresent()) {
			TokenService tokenService = tokens.get(userOptional.get().getUsername());
			if (!tokenService.isValid() || !token.equals(tokenService.getToken())){
				return ResponseEntity.status(403).build(); // invalid link
			}
			tokens.remove(username);
			userOptional.get().setEncodedPassword(passwordEncoder.encode(password));
			//tell the user the operation was successful
			userService.save(userOptional.get());
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build();
	}




	@Operation(summary = "ban user",
			description = "ban user ")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been successfully banned"),
			@ApiResponse(responseCode = "403", description = "User is not authorized to perform this action")
	})
	@PostMapping("/ban")
	public ResponseEntity<String> banUser(@RequestBody String body, Principal principal) {
		if (principal==null) return ResponseEntity.status(403).build();
		Optional<User> principalUser = userService.findByUserName(principal.getName());
		if (principalUser.isPresent()){
			if (!principalUser.get().hasRole("ADMIN")){
				return ResponseEntity.status(403).build();
			}
			Optional<User> userOp = userService.findByUserName(body);
			if (userOp.isEmpty()){ return ResponseEntity.notFound().build();}
			if (userOp.get().hasRole("ADMIN")) { return ResponseEntity.status(403).build();}
			userOp.get().setBanned(true);
			userService.save(userOp.get());
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.status(500).build();
	}




	@Operation(summary = "unban user",
			description = "unban user ")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been successfully unbanned"),
			@ApiResponse(responseCode = "403", description = "User is not authorized to perform this action")
	})
	@PostMapping("/unban")
	public ResponseEntity<String> unbanUser(@RequestBody String body, Principal principal) {
		if (principal==null) return ResponseEntity.status(403).build();
		Optional<User> principalUser = userService.findByUserName(principal.getName());
		if (principalUser.isPresent()){
			if (!principalUser.get().hasRole("ADMIN")){
				return ResponseEntity.status(403).build();
			}
			Optional<User> userOp = userService.findByUserName(body);
			if (userOp.isEmpty()){ return ResponseEntity.notFound().build();}
			userOp.get().setBanned(false);
			userService.save(userOp.get());
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.status(500).build();
	}

	private boolean checkUserPrivileges(Principal principal, Optional<User> optUser) {
		if (optUser.isEmpty()) return false;
		if (principal==null) return false;
		Optional<User> principalUser = userService.findByUserName(principal.getName());
		if (principalUser.isPresent()){
			if (principalUser.get().hasRole("ADMIN") || principal.getName().equals(optUser.get().getUsername())){
				return true;
			}
		}
		return false;
	}


	@Operation(summary = "Update user profile",
			description = "Update user profile information.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User profile successfully updated"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@PostMapping("/updateProfile")
	@ResponseBody
	public ResponseEntity<?> updateProfile(@RequestBody FullUserDTO updatedUser) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> userOptional = userService.findByUserName(currentUsername);
		if (userOptional.isPresent()) {

			User user = userOptional.get();
			user.setName(updatedUser.getName());
			user.setEmail(updatedUser.getEmail());
			user.setUsername(updatedUser.getUsername());
			userService.save(user);
			//SecurityContextHolder.clearContext();
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().build();
	}


	@Operation(summary = "Set profile picture",
			description = "Set the profile picture of the current user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Profile picture successfully set"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@PostMapping("/setProfilePicture")
	@ResponseBody
	public ResponseEntity<?> setProfilePicture(@RequestParam("profilePicture") MultipartFile pfp) {
		return userService.changeCurrentUserProfilePicture(pfp);
	}

}
