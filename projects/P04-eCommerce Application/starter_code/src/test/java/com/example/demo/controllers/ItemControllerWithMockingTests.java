package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerWithMockingTests {

    private ItemController itemController;
    private ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void setUp(){
        itemController = new ItemController();
        TestUtils.injectObject(itemController, "itemRepository", itemRepository);
    }

    @Test
    public void getItemsTest(){

        List<Item> testList = getTestItemsSetup();

        when(itemRepository.findAll()).thenReturn(testList);
        final ResponseEntity<List<Item>> listResponseEntity = itemController.getItems();
        assertNotNull(listResponseEntity);
        assertEquals(200, listResponseEntity.getStatusCodeValue());

        List<Item>  actualList = listResponseEntity.getBody();
        assertEquals(2, actualList.size());
        assertEquals(testList,actualList);
        assertEquals(testList.get(0), actualList.get(0));
        assertEquals(testList.get(0).getName(), actualList.get(0).getName());
        assertEquals(testList.get(0).getId(), actualList.get(0).getId());
    }

    @Test
    public void getItemsByIdTest(){
        List<Item> testList = getTestItemsSetup();
        Item anItem = testList.get(0);

        when(itemRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(anItem));
        final ResponseEntity<Item> responseEntity = itemController.getItemById((long)1);
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        Item actualItem = responseEntity.getBody();
        assertEquals(anItem.getId(), actualItem.getId());
        assertEquals(anItem.getName(), actualItem.getName());
        assertEquals(anItem.getPrice(), actualItem.getPrice());
        assertEquals(anItem.getDescription(), actualItem.getDescription());
    }

    @Test
    public void getItemsByNameTest(){
        List<Item> testList = getTestItemsSetup();
        Item anItem = testList.get(0);

        when(itemRepository.findByName((anItem.getName()))).thenReturn((List<Item>) testList);
        final ResponseEntity<List<Item>> listResponseEntity = itemController.getItemsByName(anItem.getName());
        assertNotNull(listResponseEntity);
        assertEquals(200, listResponseEntity.getStatusCodeValue());

        List<Item>  actualList = listResponseEntity.getBody();
        Item actualItem = actualList.get(0);
        assertEquals(anItem.getId(), actualItem.getId());
        assertEquals(anItem.getName(), actualItem.getName());
        assertEquals(anItem.getPrice(), actualItem.getPrice());
        assertEquals(anItem.getDescription(), actualItem.getDescription());
    }

    private List<Item> getTestItemsSetup(){

        //SETUP Mocking
        List<Item> list = new ArrayList<Item>();
        Item one = new Item();
        one.setId((long) 1);
        one.setName("Round Widget");
        BigDecimal price1 = new BigDecimal(2.99);
        one.setPrice(price1);
        one.setDescription("A widget that is round");

        Item two = new Item();
        two.setId((long) 2);
        two.setName("Square Widget");
        BigDecimal price2 = new BigDecimal(1.99);
        two.setPrice(price2);
        two.setDescription("A widget that is square");

        list.add(one);
        list.add(two);

        return list;
    }

}
