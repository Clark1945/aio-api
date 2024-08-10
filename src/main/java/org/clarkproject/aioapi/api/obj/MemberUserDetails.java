package org.clarkproject.aioapi.api.obj;

import lombok.Getter;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class MemberUserDetails implements UserDetails {
    private MemberPO memberPO;

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
}
