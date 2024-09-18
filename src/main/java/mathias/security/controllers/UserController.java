package mathias.security.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mathias.security.services.UserService;
import mathias.security.models.User;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@GetMapping("{id}")
	public ResponseEntity<User> findById(@PathVariable("id") Long id) {
		return ResponseEntity.ok(userService.findById(id));
	}
}
