package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SareetaApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CartController_LoggedInUserTests {
    //More integration testing and less Mocking

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain).build();
    }

    @Test
    public void createUser_Login_getAccessToken() throws Exception {

        //Only run this if user does not exist
        String token = TestUtils.getTOKEN();
        if(token == null){
            ResultActions createUserResult =
                    mockMvc.perform(post("/api/user/create")
                            .contentType("application/json")
                            .content(TestUtils.getUserContent(false))
                            .accept("application/json"))
                            .andExpect(status().isOk());

            ResultActions result
                    = mockMvc.perform(post("/login")
                    .contentType("application/json")
                    .content(TestUtils.getUserContent(true))
                    .accept("application/json"))
                    .andExpect(status().isOk());


            TestUtils.setTOKEN(result.andReturn().getResponse().getHeader("Authorization"));
        }
    }

    @Test
    public void d_addTocartTest() throws Exception{

        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(post("/api/cart/addToCart")
                        .contentType("application/json")
                        .content(getCartContent())
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Cart cart = objectMapper.readValue(resultString, Cart.class);
        assertNotNull(cart);
        Long lg = new Long(1);
        assertEquals(lg, cart.getId());

        User theUser = cart.getUser();
        assertNotNull(theUser);
        assertEquals(TestUtils.getUSER(), theUser.getUsername());
        assertEquals((long)1, theUser.getId());

        List<Item> items = cart.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        for(int i = 0; i < 2; i++){
            Item item = items.get(i);
            assertNotNull(item);
            assertEquals(lg, item.getId());
            assertEquals("Round Widget", item.getName());
            assertEquals("A widget that is round", item.getDescription());

            BigDecimal price1 = new BigDecimal(2.99);
            price1 = price1.setScale(2, RoundingMode.HALF_UP);
            assertEquals(price1, item.getPrice());
        }
    }

    @Test
    public void removeFromCartTest() throws Exception{

        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
            d_addTocartTest();
        }

        ResultActions result =
                mockMvc.perform(post("/api/cart/removeFromCart")
                        .contentType("application/json")
                        .content(getRemoveFromCartContent())
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Cart cart = objectMapper.readValue(resultString, Cart.class);
        assertNotNull(cart);
        Long lg = new Long(1);
        assertEquals(lg, cart.getId());

        User theUser = cart.getUser();
        assertNotNull(theUser);
        assertEquals(TestUtils.getUSER(), theUser.getUsername());
        assertEquals((long)1, theUser.getId());

        List<Item> items = cart.getItems();
        assertNotNull(items);
        assertEquals(1, items.size());

        Item item = items.get(0);
        assertNotNull(item);
        assertEquals(lg, item.getId());
        assertEquals("Round Widget", item.getName());
        assertEquals("A widget that is round", item.getDescription());

        BigDecimal price1 = new BigDecimal(2.99);
        price1 = price1.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price1, item.getPrice());
    }

    private String getCartContent(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + TestUtils.getUSER() + "\",");
        sb.append("\"itemId\" :  \"" + 1 + "\",");
        sb.append("\"quantity\" :  \"" + 2 + "\"");
        sb.append("}");
        return sb.toString();
    }

    private String getRemoveFromCartContent(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + TestUtils.getUSER() + "\",");
        sb.append("\"itemId\" :  \"" + 1 + "\",");
        sb.append("\"quantity\" :  \"" + 1 + "\"");
        sb.append("}");
        return sb.toString();
    }
}
