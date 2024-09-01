package org.clarkproject.aioapi;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    public void testOptionalTest3() {
//        Student st1 = new Student(1,"Joe",17);
//        Student st2 = new Student(2,"Sean",10,null);
//        Student st3 = new Student(3,null,43);
//        Student st4 = new Student(4,"Alan",88);
//        Student st5 = new Student(5,"Michael",0);
//        ArrayList<Student> studentList = new ArrayList<>();
//        studentList.add(st1);
//        studentList.add(st2);
//        studentList.add(st3);
//        studentList.add(st4);
//        studentList.add(st5);

//        Optional.ofNullable(st1)
//                .filter(student -> student.getAge()> 18)
//                .filter(student -> student.getName().equals("Joe"))
//                .isPresent();
//        Family motherName = Optional.ofNullable(st2)
//                .map(s -> s.getFamily())
//                .filter( f -> f.getMotherName().equals("Mary"))
//                .orElseThrow(() -> new IllegalObjectStatusException("NotFound"));
//        System.out.println(motherName);
//        Student student = Optional.of(st).orElse(new Student());
    }

    void printFail() {
        System.out.println("Faile");
    }




}
