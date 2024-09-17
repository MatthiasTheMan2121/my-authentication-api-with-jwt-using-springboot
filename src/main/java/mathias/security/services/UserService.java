package mathias.security.services;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import mathias.security.dtos.UserDTO;
import mathias.security.models.Role;
import mathias.security.models.User;
import mathias.security.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService{
	@Autowired
	private UserRepository repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repo.findByEmail(username);
		if (user == null) {
			throw new UsernameNotFoundException("User Not Found");
		}
		return user;
	}
	
	public User saveUser(UserDTO data) {
		User user = new User(null, data.getEmail(), data.getPassword());
		user.getRoles().addAll(data.getRoles().stream().map(role -> Role.valueOf(role)).collect(Collectors.toList()));
		return repo.save(user);
	}
	
	public boolean userExists(String email) {
		return repo.existsByEmail(email);
	}
}
