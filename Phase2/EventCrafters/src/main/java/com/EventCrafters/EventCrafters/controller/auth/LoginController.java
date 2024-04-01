package com.EventCrafters.EventCrafters.controller.auth;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import com.EventCrafters.EventCrafters.model.User;
import com.EventCrafters.EventCrafters.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EventCrafters.EventCrafters.security.jwt.AuthResponse;
import com.EventCrafters.EventCrafters.security.jwt.LoginRequest;
import com.EventCrafters.EventCrafters.security.jwt.UserLoginService;
import com.EventCrafters.EventCrafters.security.jwt.AuthResponse.Status;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

	@Autowired
	private UserLoginService userLoginService;
	@Autowired
	private UserService userService;

	@PostMapping("/login")
	@Operation(summary = "User login", description = "Authenticates a user with username and password.")
	@ApiResponse(responseCode = "200", description = "Authentication successful",
			content = @Content(schema = @Schema(implementation = AuthResponse.class)))
	public ResponseEntity<AuthResponse> login(
			@CookieValue(name = "accessToken", required = false) String accessToken,
			@CookieValue(name = "refreshToken", required = false) String refreshToken,
			@RequestBody LoginRequest loginRequest) {
		Optional<User> user = userService.findByUserName(loginRequest.getUsername());
		if (user.isPresent() && user.get().isBanned()){
			return ResponseEntity.status(403).build();
		}
		return userLoginService.login(loginRequest, accessToken, refreshToken);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh token", description = "Refreshes authentication tokens.")
	@ApiResponse(responseCode = "200", description = "Token refreshed successfully",
			content = @Content(schema = @Schema(implementation = AuthResponse.class)))
	public ResponseEntity<AuthResponse> refreshToken(
			@CookieValue(name = "refreshToken", required = false) String refreshToken) {

		return userLoginService.refresh(refreshToken);
	}

	@PostMapping("/logout")
	@Operation(summary = "User logout", description = "Logs out the user and clears the tokens.")
	@ApiResponse(responseCode = "200", description = "Logout successful",
			content = @Content(schema = @Schema(implementation = AuthResponse.class)))
	public ResponseEntity<AuthResponse> logOut(HttpServletRequest request, HttpServletResponse response) {

		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(request, response)));
	}
}
