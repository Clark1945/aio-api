package org.clarkproject.aioapi;

import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
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

    @Test
    void optionalTest3() {

    }
}
