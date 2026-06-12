package com.enterprise.user;

import com.enterprise.arch.exception.GlobalExceptionHandler;
import com.enterprise.user.controller.UserController;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.service.UserService;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerAndLoginReturnSuccess() throws Exception {
        UserVO user = userVO();
        when(userService.register(any())).thenReturn(user);
        when(userService.login(any(LoginDTO.class))).thenReturn(new LoginVO("token", 60, user));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"neo\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.username", is("neo")));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"neo\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", is("token")));
    }

    @Test
    void validationFailureReturnsParamError() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(10004)));
    }

    @Test
    void currentUserAndPasswordUpdateDelegateToService() throws Exception {
        when(userService.currentUser()).thenReturn(userVO());

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("neo")));

        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"old123\",\"newPassword\":\"new123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));
        verify(userService).updatePassword(any());
    }

    private UserVO userVO() {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUsername("neo");
        vo.setNickname("Neo");
        return vo;
    }
}
