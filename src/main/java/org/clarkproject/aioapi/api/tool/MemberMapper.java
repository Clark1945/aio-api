package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.obj.MemberRole;
import org.clarkproject.aioapi.api.obj.MemberStatus;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 物件轉型用，可以理解物件之間有一個代理處理兩者間不同的地方後再轉型
 */
@Mapper
public interface MemberMapper {
    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @Mapping(source = "phone",target = "phoneNumber")
    @Mapping(source = "birthday",target = "birthdate")
    @Mapping(source = "ip", target = "ip", qualifiedByName = "stringToInetAddress")
    MemberPO memberToMemberPo(Member member);
    @Mapping(source = "phoneNumber",target = "phone")
    @Mapping(source = "birthdate",target = "birthday")
    @Mapping(source = "ip", target = "ip", qualifiedByName = "inetAddressToString")
    Member memberPOToMember(MemberPO memberPO);

    @AfterMapping
    default void setDefaultValues(@MappingTarget MemberPO memberPO, Member member) {
        if (memberPO.getRole() == null) {
            memberPO.setRole(MemberRole.USER.name()); // 设置默认值
        }
        if (memberPO.getStatus() == null) {
            memberPO.setStatus(MemberStatus.ACTIVE.name());
        }
    }

    @Named("stringToInetAddress")
    default InetAddress stringToInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid IP address: " + ip, e);
        }
    }

    @Named("inetAddressToString")
    default String inetAddressToString(InetAddress inetAddress) {
        return inetAddress != null ? inetAddress.getHostAddress() : null;
    }

}
