package mathias.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import mathias.security.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
	public User findByEmail(String email);
	public boolean existsByEmail(String email);
}
