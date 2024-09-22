package org.clarkproject.aioapi;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clarkproject.aioapi.api.controller.MemberControllerImpl;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.dto.APIResponse;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AioApiApplicationTests {

    @MockBean
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;

    @Test
    void optionalTest1() {
        Mockito.when(memberRepository.findByAccount(Mockito.isNull())).thenReturn(null);
        Optional<MemberPO> memberPO1 = memberService.findActiveAccount(null);
        assert !memberPO1.isPresent();
    }

    @Test
    void optionalTest2() {
        MemberPO memberPOTest = new MemberPO();
        memberPOTest.setStatus("INACTIVE");
        Mockito.when(memberRepository.findByAccount(Mockito.isNull())).thenReturn(memberPOTest);
        Optional<MemberPO> memberPO1 = memberService.findActiveAccount(null);
        assert !memberPO1.isPresent();
    }

//    @Autowired
//    private ApplicationContext applicationContext;
//    @Mock
//    MemberRepository memberRepository;
//    @Mock
//    MemberService memberService;
//
//    @Test
//    void webTestClient() {
//        MemberPO memberPO = new MemberPO();
//        memberPO.setStatus("ACTIVE");
//        memberPO.setName("Clark");
//        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.ofNullable(memberPO));
//
//        WebTestClient client = WebTestClient.bindToApplicationContext(applicationContext).build();
//        client.get().uri(uriBuilder ->
//                        uriBuilder.path("/api/1.0/member")
//                                .queryParam("id", "1")
//                                .build())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(Member.class)
//                .consumeWith(response -> {
//                    Member member = response.getResponseBody();
//                    assertNotNull(member);
//                    assertEquals("Clark", member.getName());
//                });
//    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetMember() throws Exception {
        MemberPO memberPO = new MemberPO();
        memberPO.setStatus("ACTIVE");
        memberPO.setName("Clark");
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.ofNullable(memberPO));

        mockMvc.perform(get("/api/1.0/member")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.name").value("Clark"));
    }

}
