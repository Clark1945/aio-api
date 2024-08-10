package org.clarkproject.aioapi.api.obj;

import lombok.Getter;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MemberUserDetails implements UserDetails {
    private MemberPO memberPO;

    public MemberUserDetails() {
    }
    public MemberUserDetails(MemberPO memberPO) {
        this.memberPO = memberPO;
    }

    public String getEmail() {
        return memberPO.getEmail();
    }

    // 實作介面規範的方法
    public String getUsername() {
        return memberPO.getAccount();
    }

    public void setUsername(String username) {
        memberPO.setAccount(username);
    }

    public String getPassword() {
        return memberPO.getPassword();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(memberPO.getRole()));
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public void setMemberAuthorities(List<MemberRole> memberAuthorities) {
        // 将List<MemberRole> 转换为以逗号分割的字符串
        String roles = memberAuthorities.stream()
                .map(MemberRole::name) // 假设 MemberRole 有 getRoleName 方法
                .collect(Collectors.joining(","));
        memberPO.setRole(roles);
    }
}
