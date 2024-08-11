package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.enums.MemberRole;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
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
//    @Mapping(source = "ip", target = "ip", qualifiedByName = "stringToInetAddress")
    MemberPO memberToMemberPo(Member member);
    @Mapping(source = "phoneNumber",target = "phone")
    @Mapping(source = "birthdate",target = "birthday")
//    @Mapping(source = "ip", target = "ip", qualifiedByName = "inetAddressToString")
    Member memberPOToMember(MemberPO memberPO);

    /**
     * 轉型後設定預設值
     * @param memberPO 轉換對象
     * @param member 轉換來源
     */
    @AfterMapping
    default void setDefaultValues(@MappingTarget MemberPO memberPO, Member member) {
        if (memberPO.getRole() == null) {
            memberPO.setRole(MemberRole.USER.name());
        }
        if (memberPO.getStatus() == null) {
            memberPO.setStatus(MemberStatus.ACTIVE.name());
        }
    }

    /**
     * 屬性型別轉換
     * @param ip 請求IP
     * @return
     */
    @Deprecated
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
