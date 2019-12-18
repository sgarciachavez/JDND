package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
import com.example.demo.TestUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SareetaApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserController_LoggedInUserTests {
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
    public void findByIdTest() throws Exception{
        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/user/id/1")
                        .contentType("application/json")
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Object id = jsonParser.parseMap(resultString).get("id");
        Object username = jsonParser.parseMap(resultString).get("username");

        assertNotNull(id);
        assertNotNull(username);
        assertEquals("1", id.toString()); //ID will be 1 for the 1st user.
        assertEquals(TestUtils.getUSER(), username.toString());

    }

    @Test
    public void findByUserNameTest() throws Exception {
        //1st create, login and get a token
        String token = TestUtils.getTOKEN();
        if(token == null){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/user/" + TestUtils.getUSER())
                        .contentType("application/json")
                        .header("Authorization", TestUtils.getTOKEN())
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Object id = jsonParser.parseMap(resultString).get("id");
        Object username = jsonParser.parseMap(resultString).get("username");

        assertNotNull(id);
        assertNotNull(username);
        assertEquals("1", id.toString()); //ID will be 1 for the 1st user.
        assertEquals(TestUtils.getUSER(), username.toString());
    }
}
