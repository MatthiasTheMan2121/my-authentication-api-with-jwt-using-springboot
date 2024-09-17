package mathias.security.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mathias.security.services.TokenService;

@Component
public class SecurityFilter extends OncePerRequestFilter {
	
	@Autowired
	private TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = extractToken(request);
		
		if (token != null && tokenService.isTokenValid(token)) {
			String email = tokenService.extractEmail(token);
			Collection<? extends GrantedAuthority> roles = tokenService.extractRoles(token).stream()
					.map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
			
			var authToken = new UsernamePasswordAuthenticationToken(email, null, roles);
			
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}
		
		filterChain.doFilter(request, response);
	}
	
	
	private String extractToken(HttpServletRequest request) {
		if (request.getHeader("Authorization") == null) {
			return null;
		}
		
		return (request.getHeader("Authorization").contains("Bearer ")) ? 
				request.getHeader("Authorization").replace("Bearer ", "") : null; 
	}
}
