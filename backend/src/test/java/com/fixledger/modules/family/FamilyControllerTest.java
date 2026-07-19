package com.fixledger.modules.family;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.request.InviteFamilyMemberRequest;
import com.fixledger.modules.family.request.UpdateFamilyMemberRoleRequest;
import com.fixledger.modules.family.service.FamilyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FamilyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Test
  @DisplayName("家庭所有者可以通过接口邀请、调整和移除成员")
  void ownerCanManageMembersByApi() throws Exception {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "familyapiowner",
        null,
        "123456",
        "Owner"
    ));
    authService.register(new RegisterRequest(
        "familyapimember",
        null,
        "123456",
        "Member"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());
    LoginResponse login = authService.login(new LoginRequest("familyapiowner", "123456"));
    String token = "Bearer " + login.accessToken();

    MvcResult inviteResult = mockMvc.perform(post("/api/families/{familyId}/members", familyId)
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new InviteFamilyMemberRequest("familyapimember", "MEMBER")
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("MEMBER"))
        .andReturn();
    Long memberId = readDataId(inviteResult);

    mockMvc.perform(put("/api/families/{familyId}/members/{memberId}/role", familyId, memberId)
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new UpdateFamilyMemberRoleRequest("OWNER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("OWNER"));

    mockMvc.perform(delete("/api/families/{familyId}/members/{memberId}", familyId, memberId)
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(true));

    mockMvc.perform(get("/api/system/operation-logs")
            .header(HttpHeaders.AUTHORIZATION, token)
            .param("familyId", String.valueOf(familyId))
            .param("module", "FAMILY"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(3));
  }

  private Long readDataId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("id").asLong();
  }
}
