package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SareetaApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ItemController_LoggedInUserTests {
    //More integration testing and less Mocking

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    private TestUtils utils = new TestUtils();

//    private String USER = "testuser";
//    private String PASSWORD = "password123";
//    private static boolean CREATE = true;
//    private static String TOKEN;

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
    public void getItemsTest() throws Exception{

        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/item")
                        .contentType("application/json")
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Item[] items = objectMapper.readValue(resultString, Item[].class);
        assertNotNull(items);
        assertEquals(2, items.length);

        Item actualItem = items[0];
        assertEquals("Round Widget", actualItem.getName());
        assertEquals("A widget that is round", actualItem.getDescription());

        Long lg = new Long(1);
        assertEquals(lg, actualItem.getId());

        BigDecimal price1 = new BigDecimal(2.99);
        price1 = price1.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price1, actualItem.getPrice());
    }

    @Test
    public void getItemsByIdTest() throws Exception {
        //1st create, login and get a token
        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/item/2")
                        .contentType("application/json")
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Item actualItem = objectMapper.readValue(resultString, Item.class);

        assertEquals("Square Widget", actualItem.getName());
        assertEquals("A widget that is square", actualItem.getDescription());

        Long lg = new Long(2);
        assertEquals(lg, actualItem.getId());

        BigDecimal price1 = new BigDecimal(1.99);
        price1 = price1.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price1, actualItem.getPrice());
    }

    //getItemsByNameTest
    @Test
    public void getItemsByNameTest() throws Exception {
        //1st create, login and get a token
        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/item/name/Round Widget")
                        .contentType("application/json")
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Item[] items = objectMapper.readValue(resultString, Item[].class);
        assertNotNull(items);
        assertEquals(1, items.length);

        Item actualItem = items[0];
        assertEquals("Round Widget", actualItem.getName());
        assertEquals("A widget that is round", actualItem.getDescription());

        Long lg = new Long(1);
        assertEquals(lg, actualItem.getId());

        BigDecimal price1 = new BigDecimal(2.99);
        price1 = price1.setScale(2, RoundingMode.HALF_UP);
        assertEquals(price1, actualItem.getPrice());
    }

//    private String getUserContent(boolean login){
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        sb.append("\"username\" :  \"" + USER + "\",");
//        sb.append("\"password\" :  \"" + PASSWORD + "\"");
//
//        if(login){
//            sb.append("}");
//            return sb.toString();
//        }
//
//        sb.append(", \"confirmPassword\" :  \"" + PASSWORD + "\"");
//        sb.append("}");
//        return sb.toString();
//    }
}
