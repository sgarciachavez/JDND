package com.example.demo.controllers;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	private static final Logger log = LoggerFactory.getLogger(OrderController.class);
	
	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			log.error("Error order submitted NULL userName = " + username);
			return ResponseEntity.notFound().build();
		}
		UserOrder order = UserOrder.createFromCart(user.getCart());
		orderRepository.save(order);
		log.info("Submmited order userName = " + username + " orderID = " + order.getId().toString());
		return ResponseEntity.ok(order);
	}
	
	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			log.error("Error order history NULL userName = " + username);
			return ResponseEntity.notFound().build();
		}
		List<UserOrder> orders = orderRepository.findByUser(user);
		String orderid = "EMPTY";
		if (orders.size() > 0){
			List<String> ids = new ArrayList<>();
			orders.forEach((order) -> {
				ids.add(order.getId().toString());
			} );
			orderid = String.join(", ", ids);
		}
		log.info("Retrieved order(s) userName = " + username + " orderIDs = " + orderid);
		//return ResponseEntity.ok(orderRepository.findByUser(user));
		return ResponseEntity.ok(orders);
	}
}
