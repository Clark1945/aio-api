package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.obj.MemberRole;
import org.clarkproject.aioapi.api.obj.MemberStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {
    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @Mapping(source = "phone",target = "phoneNumber")
    @Mapping(source = "birthday",target = "birthdate")
    MemberPO memberToMemberPo(Member member);
    @Mapping(source = "phoneNumber",target = "phone")
    @Mapping(source = "birthdate",target = "birthday")
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

}
