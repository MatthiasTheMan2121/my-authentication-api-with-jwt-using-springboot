package mathias.security.controllers;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import mathias.security.dtos.LoginDTO;
import mathias.security.dtos.TokenDTO;
import mathias.security.dtos.UserDTO;
import mathias.security.exceptions.ResourceAlreadyExistsException;
import mathias.security.exceptions.ResourceNotFoundException;
import mathias.security.models.User;
import mathias.security.services.TokenService;
import mathias.security.services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TokenService tokenService;
	
	@PostMapping("/login")
	public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO data) {
		UserDetails user = userService.loadUserByUsername(data.email());
		
		String token = tokenService.generateToken(data.email(), user.getAuthorities().stream(
				).map(authority -> authority.getAuthority()).collect(Collectors.toList()));
		
		return ResponseEntity.ok(new TokenDTO(token));
	}
	
	@PostMapping("/register")
	public ResponseEntity<User> register(@Valid @RequestBody UserDTO data) {
		userService.userExists(data.getEmail());
		
		data.setPassword(new BCryptPasswordEncoder().encode(data.getPassword()));
		
		User savedUser = userService.saveUser(data);
		
		URI location = ServletUriComponentsBuilder.fromCurrentServletMapping()
				.path("/user/{id}")
				.buildAndExpand(savedUser.getId())
				.toUri();
		
		return ResponseEntity.created(location).body(savedUser);
	}
}
