package org.clarkproject.aioapi.api.obj;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class LoginResponse {
    private String jwt;
    private String username;
    private List<String> authorities;

    public static LoginResponse of(String jwt, UserDetails user) {
        LoginResponse res = new LoginResponse();
        res.setJwt(jwt);;
        res.setUsername(user.getUsername());
        res.setAuthorities(user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return res;
    }

    // getter ...
}