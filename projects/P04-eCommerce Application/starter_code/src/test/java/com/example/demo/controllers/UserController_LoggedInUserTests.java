package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
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

    private String USER = "testuser";
    private String PASSWORD = "password123";
    private static boolean CREATE = true;
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
    public void findByIdTest() throws Exception{
        if(CREATE){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/user/id/1")
                        .contentType("application/json")
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Object id = jsonParser.parseMap(resultString).get("id");
        Object username = jsonParser.parseMap(resultString).get("username");

        assertNotNull(id);
        assertNotNull(username);
        assertEquals("1", id.toString()); //ID will be 1 for the 1st user.
        assertEquals(USER, username.toString());

    }

    @Test
    public void findByUserNameTest() throws Exception {
        //1st create, login and get a token
        if(CREATE){
            createUser_Login_getAccessToken();
        }

        ResultActions result =
                mockMvc.perform(get("/api/user/" + USER)
                        .contentType("application/json")
                        .header("Authorization", TOKEN)
                        .accept("application/json"))
                        .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Object id = jsonParser.parseMap(resultString).get("id");
        Object username = jsonParser.parseMap(resultString).get("username");

        assertNotNull(id);
        assertNotNull(username);
        assertEquals("1", id.toString()); //ID will be 1 for the 1st user.
        assertEquals(USER, username.toString());
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
}
