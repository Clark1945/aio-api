package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.enums.MemberRole;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.service.MemberService;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class MemberMapper {

    /**
     * 將 Member 轉換為 MemberPO
     */
    public static MemberPO memberToMemberPo(Member member,String accessIp) {
        if (member == null) {
            return null;
        }

        MemberPO memberPO = new MemberPO();
        memberPO.setName(member.getName());
        memberPO.setAccount(member.getAccount());
        memberPO.setPassword(member.getPassword());
        memberPO.setPhoneNumber(member.getPhone());
        memberPO.setEmail(member.getEmail());
        memberPO.setAddress(member.getAddress());
        memberPO.setBirthdate(member.getBirthday());

        setDefaultValues(memberPO,accessIp);

        return memberPO;
    }

    /**
     * 將 MemberPO 轉換為 Member
     */
    public static Member memberPOToMember(MemberPO memberPO) {
        if (memberPO == null) {
            return null;
        }

        Member member = new Member();
        member.setName(memberPO.getName());
        member.setAccount(memberPO.getAccount());
        member.setPassword(memberPO.getPassword());
        member.setPhone(memberPO.getPhoneNumber());
        member.setEmail(memberPO.getEmail());
        member.setAddress(memberPO.getAddress());
        member.setBirthday(memberPO.getBirthdate());

        return member;
    }

    /**
     * 設置默認值的方法
     */
    public static void setDefaultValues(MemberPO memberPO, String accessIp) {
        memberPO.setRole(MemberRole.USER.name());
        memberPO.setStatus(MemberStatus.ACTIVE.name());
        memberPO.setIp(MemberService.stringToInetAddress(accessIp));
    }

    /**
     * IP 地址的轉換方法
     */
    public InetAddress stringToInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid IP address: " + ip, e);
        }
    }

    public String inetAddressToString(InetAddress inetAddress) {
        return inetAddress != null ? inetAddress.getHostAddress() : null;
    }
}


//import org.clarkproject.aioapi.api.obj.dto.Member;
//import org.clarkproject.aioapi.api.obj.po.MemberPO;
//import org.clarkproject.aioapi.api.obj.enums.MemberRole;
//import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
//import org.mapstruct.*;
//import org.mapstruct.factory.Mappers;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
///**
// * 物件轉型用，可以理解物件之間有一個代理處理兩者間不同的地方後再轉型
// */
//@Mapper
//public interface MemberMapper {
//    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);
//
////    @Mapping(source = "phone",target = "phoneNumber")
//    @Mapping(source = "birthday",target = "birthdate")
//    @Mapping(source = "phone", target = "phoneNumber", qualifiedByName = "transferPhone")
////    @Mapping(source = "ip", target = "ip", qualifiedByName = "stringToInetAddress")
//    MemberPO memberToMemberPo(Member member);
//    @Mapping(source = "phoneNumber",target = "phone")
//    @Mapping(source = "birthdate",target = "birthday")
////    @Mapping(source = "ip", target = "ip", qualifiedByName = "inetAddressToString")
//    Member memberPOToMember(MemberPO memberPO);
//
//
//    @Named("transferPhone")
//    default void transferPhone(@MappingTarget MemberPO memberPO, Member member) {
//        memberPO.setPhoneNumber(member.getPhone());
//    }
//
//    /**
//     * 轉型後設定預設值
//     * @param memberPO 轉換對象
//     * @param member 轉換來源
//     */
//    @AfterMapping
//    default void setDefaultValues(@MappingTarget MemberPO memberPO, Member member) {
//        if (memberPO.getRole() == null) {
//            memberPO.setRole(MemberRole.USER.name());
//        }
//        if (memberPO.getStatus() == null) {
//            memberPO.setStatus(MemberStatus.ACTIVE.name());
//        }
//    }
//
//    /**
//     * 屬性型別轉換
//     * @param ip 請求IP
//     * @return
//     */
//    @Deprecated
//    @Named("stringToInetAddress")
//    default InetAddress stringToInetAddress(String ip) {
//        try {
//            return InetAddress.getByName(ip);
//        } catch (UnknownHostException e) {
//            throw new RuntimeException("Invalid IP address: " + ip, e);
//        }
//    }
//
//    @Named("inetAddressToString")
//    default String inetAddressToString(InetAddress inetAddress) {
//        return inetAddress != null ? inetAddress.getHostAddress() : null;
//    }
//
//}
