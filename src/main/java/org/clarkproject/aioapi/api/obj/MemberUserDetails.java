package org.clarkproject.aioapi.api.obj;

import lombok.Getter;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class MemberUserDetails implements UserDetails {
    private MemberPO memberPO;
    private String username;

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
        this.username = username;
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
    private String role;
    public void setMemberAuthorities(List<String> memberAuthorities) {
        // 将List<MemberRole> 转换为以逗号分割的字符串
        // 假设 MemberRole 有 getRoleName 方法
        String rolesWithComon = String.join(",", memberAuthorities);
        this.role = rolesWithComon;
    }
}
