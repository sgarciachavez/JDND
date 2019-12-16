package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerWithMockingTests {

    private UserController userController;
    private UserRepository userRepository = mock(UserRepository.class);
    private CartRepository cartRepository = mock(CartRepository.class);
    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private CreateUserRequest testUser = new CreateUserRequest();

    @Before
    public void setUp(){
       userController = new UserController();
        TestUtils.injectObject(userController, "userRepository", userRepository);
        TestUtils.injectObject(userController, "cartRepository", cartRepository);
        TestUtils.injectObject(userController, "bCryptPasswordEncoder", encoder);
    }

    @Before
    public void setUpTestUser(){
        testUser.setUsername("user");
        testUser.setPassword("password");
        testUser.setConfirmPassword("password");
    }

    @Test
    public void createUserTest() throws Exception{
        when(encoder.encode(testUser.getPassword())).thenReturn("hashedPassword");

        final ResponseEntity<User> responseEntity = userController.createUser(testUser);
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        User actualuser = responseEntity.getBody();
        assertNotNull(actualuser);
        assertEquals(testUser.getUsername(), actualuser.getUsername());
        assertEquals(0, actualuser.getId());  //id of 1st  user created
        assertEquals("hashedPassword", actualuser.getPassword());

        Cart cart = actualuser.getCart();
        assertNotNull(cart);  //Cart will be created but everyting in the cart is NULL!
        assertEquals(null, cart.getTotal());
        assertEquals(null, cart.getItems());
        assertEquals(null, cart.getUser());
        assertEquals(null,cart.getId());
    }

    @Test
    public void findByUserNameTest() throws Exception {
        //This test uses Mocking for the Respository

        User user = new User();
        user.setUsername(testUser.getUsername());
        user.setId(1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        final ResponseEntity<User> responseEntity = userController.findByUserName(user.getUsername());
        assertNotNull(responseEntity);

        User actualUser = responseEntity.getBody();
        assertEquals(user.getUsername(), actualUser.getUsername());
        assertEquals(user.getId(), user.getId());
    }

    @Test
    public void findByIdTest() throws Exception {
        //This test uses Mocking for the Respository

        User user = new User();
        user.setUsername(testUser.getUsername());
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        final ResponseEntity<User> responseEntity = userController.findById(user.getId());
        assertNotNull(responseEntity);

        User actualUser = responseEntity.getBody();
        assertEquals(user.getUsername(), actualUser.getUsername());
        assertEquals(user.getId(), actualUser.getId());
    }
}
