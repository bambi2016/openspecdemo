package com.enterprise.permission;

import com.enterprise.arch.exception.GlobalExceptionHandler;
import com.enterprise.permission.controller.PermissionController;
import com.enterprise.permission.service.PermissionService;
import com.enterprise.permission.vo.RoleVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermissionControllerTest {

    private final PermissionService permissionService = mock(PermissionService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PermissionController(permissionService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void createRoleAndValidationBranches() throws Exception {
        RoleVO roleVO = new RoleVO();
        roleVO.setId(1L);
        roleVO.setRoleCode("ADMIN");
        roleVO.setRoleName("管理员");
        when(permissionService.createRole(any())).thenReturn(roleVO);

        mockMvc.perform(post("/api/permission/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ADMIN\",\"roleName\":\"管理员\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode", is("ADMIN")));

        mockMvc.perform(post("/api/permission/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"bad-code\",\"roleName\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(10004)));
    }

    @Test
    void bindEndpointsDelegateToService() throws Exception {
        mockMvc.perform(put("/api/permission/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));
        verify(permissionService).bindRolePermissions(eq(1L), any());

        mockMvc.perform(put("/api/permission/users/2/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));
        verify(permissionService).bindUserRoles(eq(2L), any());
    }

    @Test
    void currentPermissionsReturnCodes() throws Exception {
        when(permissionService.currentPermissionCodes()).thenReturn(List.of("role:create"));

        mockMvc.perform(get("/api/permission/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]", is("role:create")));
    }
}
