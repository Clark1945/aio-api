package org.clarkproject.aioapi;

import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AioApiApplicationTests {

    @MockBean
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;

    // Optional API 功能測試
    @Test
    void optionalTest1() {
        Mockito.when(memberRepository.findByAccount(Mockito.isNull())).thenReturn(null);
        Optional<MemberPO> memberPO1 = memberService.findActiveAccount(null);
        assert !memberPO1.isPresent();
    }

    // Optional API 功能測試
    @Test
    void optionalTest2() {
        MemberPO memberPOTest = new MemberPO();
        memberPOTest.setStatus("INACTIVE");
        Mockito.when(memberRepository.findByAccount(Mockito.isNull())).thenReturn(memberPOTest);
        Optional<MemberPO> memberPO1 = memberService.findActiveAccount(null);
        assert !memberPO1.isPresent();
    }

//    @Autowired 此測試適用Reactive 應用程式
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

    // Mockito 功能測試
    @Test
    void testGetMember() throws Exception {
        MemberPO memberPO = new MemberPO();
        memberPO.setStatus("ACTIVE");
        memberPO.setName("Clark");
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.ofNullable(memberPO));
    }

    // Mockito 功能測試
    @Test
    public void testMockito() {
        MemberPO memberPO = new MemberPO();
        memberPO.setStatus("ACTIVE");
        memberPO.setName("Clark");

        assert Objects.equals(memberPO.getStatus(), "ACTIVE");
        assert memberPO.getName().equals("Clark");
    }
}
