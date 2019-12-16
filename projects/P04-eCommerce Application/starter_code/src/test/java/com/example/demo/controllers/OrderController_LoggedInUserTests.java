package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SareetaApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrderController_LoggedInUserTests {
    //More integration testing and less Mocking

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    private String USER = "testuser";
    private String PASSWORD = "password123";
    private static boolean CREATE = true;
    private static boolean ADD2CART = true;
    private static String TOKEN;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain).build();
    }

    @Test
    public void createUser_Login_getAccessToken() throws Exception {

        ResultActions createUserResult =
                mockMvc.perform(post("/api/user/create")
                        .contentType("application/json")
                        .content(getUserContent(false))
                        .accept("application/json"))
                        .andExpect(status().isOk());

        ResultActions result
                = mockMvc.perform(post("/login")
                .contentType("application/json")
                .content(getUserContent(true))
                .accept("application/json"))
                .andExpect(status().isOk());

        TOKEN = result.andReturn().getResponse().getHeader("Authorization");
        CREATE = false;
    }

    @Test
    public void getOrdersForUserTest() throws Exception{

        if(CREATE){
            createUser_Login_getAccessToken();
        }

        //Cart is empty check for that 1st!
        ResultActions emptyCartResult =
                mockMvc.perform(get("/api/order/history/" + USER)
                        .contentType("application/json")
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String emptyCartResultString = emptyCartResult.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        UserOrder[] orders  = objectMapper.readValue(emptyCartResultString, UserOrder[].class);
        assertNotNull(orders);
        assertEquals(0, orders.length);

        //Now add items to cart
        ResultActions addtocartResult =
                mockMvc.perform(post("/api/cart/addToCart")
                        .contentType("application/json")
                        .content(getCartContent())
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        //Submit Order
        ResultActions submitResult =
                mockMvc.perform(post("/api/order/submit/" + USER)
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        //Cart is NOT empty
        ResultActions nonEmptyCartResult =
                mockMvc.perform(get("/api/order/history/" + USER)
                        .contentType("application/json")
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String nonEmptyCartResultString = nonEmptyCartResult.andReturn().getResponse().getContentAsString();

        objectMapper = new ObjectMapper();
        UserOrder[] nonEmptyOrders  = objectMapper.readValue(nonEmptyCartResultString, UserOrder[].class);
        assertNotNull(nonEmptyOrders);
        assertEquals(1, nonEmptyOrders.length);
        UserOrder order = nonEmptyOrders[0];
        assertNotNull(order);
        Long lg = new Long(1);
        assertEquals(lg, order.getId());

        BigDecimal price1 = new BigDecimal(5.98);
        price1 = price1.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price1, order.getTotal());

        List<Item> items = order.getItems();
        assertEquals(2, items.size());

        Item item = items.get(0);
        assertNotNull(item);
        Long long2 = new Long(1);
        assertEquals(long2, item.getId());
        assertEquals("Round Widget", item.getName());
        assertEquals("A widget that is round", item.getDescription());
        BigDecimal price2 = new BigDecimal(2.99);
        price2 = price2.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price2, item.getPrice());

        User user = order.getUser();
        assertNotNull(user);
        assertEquals(USER, user.getUsername());
        assertEquals((long) 1, user.getId());
    }

    @Test
    public void submitOrderTest() throws Exception{

        if(CREATE){
            createUser_Login_getAccessToken();
        }

        //Now add items to cart
        ResultActions addtocartResult =
                mockMvc.perform(post("/api/cart/addToCart")
                        .contentType("application/json")
                        .content(getCartContent2())
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        //Submit Order
        ResultActions submitResult =
                mockMvc.perform(post("/api/order/submit/" + USER)
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());


        String submitResultString = submitResult.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        UserOrder order  = objectMapper.readValue(submitResultString, UserOrder.class);
        assertNotNull(order);

        List<Item> items = order.getItems();

        Item item = items.get(items.size()-1); //Get last item in the list should for unit and class test
        assertNotNull(item);
        Long long2 = new Long(2);
        assertEquals(long2, item.getId());
        assertEquals("Square Widget", item.getName());
        assertEquals("A widget that is square", item.getDescription());
        BigDecimal price2 = new BigDecimal(1.99);
        price2 = price2.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price2, item.getPrice());

        User user = order.getUser();
        assertNotNull(user);
        assertEquals(USER, user.getUsername());
        assertEquals((long) 1, user.getId());
    }

    private String getUserContent(boolean login){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + USER + "\",");
        sb.append("\"password\" :  \"" + PASSWORD + "\"");

        if(login){
            sb.append("}");
            return sb.toString();
        }

        sb.append(", \"confirmPassword\" :  \"" + PASSWORD + "\"");
        sb.append("}");
        return sb.toString();
    }

    private String getCartContent(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + USER + "\",");
        sb.append("\"itemId\" :  \"" + 1 + "\",");  //Round Widget
        sb.append("\"quantity\" :  \"" + 2 + "\"");
        sb.append("}");
        return sb.toString();
    }
    private String getCartContent2(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + USER + "\",");
        sb.append("\"itemId\" :  \"" + 2 + "\","); //Square Widget
        sb.append("\"quantity\" :  \"" + 1 + "\"");
        sb.append("}");
        return sb.toString();
    }
}
