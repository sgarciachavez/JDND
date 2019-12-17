package com.example.demo.controllers;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/item")
public class ItemController {

	@Autowired
	private ItemRepository itemRepository;
	private static final Logger log = LoggerFactory.getLogger(ItemController.class);
	
	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		log.info("Get All Items");
		return ResponseEntity.ok(itemRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		String theId = id.toString();
		Optional<Item> item = itemRepository.findById(id);
		if(!item.isPresent()){
			log.warn("Item NOT found itemID = " + id.toString());
		}else{
			log.info("Item found itemID = " + id.toString());
		}

		//return ResponseEntity.of(itemRepository.findById(id));
		return ResponseEntity.of(item);
	}
	
	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		List<Item> items = itemRepository.findByName(name);
		if(items == null || items.isEmpty()){
			log.warn("Item NOT found get itemName = " + name);
		}else{
			log.info("Item found get itemName = " + name);
		}

		return items == null || items.isEmpty() ? ResponseEntity.notFound().build()
				: ResponseEntity.ok(items);
			
	}
	
}
