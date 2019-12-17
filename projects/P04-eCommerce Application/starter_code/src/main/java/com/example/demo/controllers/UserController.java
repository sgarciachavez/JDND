package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

	//private static final Logger log = Logger.getLogger(UserController.class);
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {

		Optional<User> user = userRepository.findById(id);
		if(!user.isPresent()){
			log.warn("NOT found NULL user userId = " + id.toString());
		}else{
			log.info("Found user userId = " + id.toString());
		}

		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {

		User user = userRepository.findByUsername(username);
		if(user == null ){
			log.warn("NOT found NULL user userName = " + username);
		}else{
			log.info("Found user userName = " + username);
		}

		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		log.info("Create user attempt userName = " + createUserRequest.getUsername());
//		log.debug("Debug Message");
//		log.error("Error Message");
//		log.warn("Warn Message");

		if(createUserRequest.getPassword().length() < 7 ||
				!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
			log.error("Invalid password length for userName = " + createUserRequest.getUsername());
			return ResponseEntity.badRequest().build();
		}
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);

		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
		userRepository.save(user);
		log.info("User created userName = "+ createUserRequest.getUsername());

		return ResponseEntity.ok(user);
	}

}
