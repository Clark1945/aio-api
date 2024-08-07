package org.clarkproject.aioapi.api.obj;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

public class LoginResponse {
    private String jwt;
    private String username;
    private List<String> authorities;

//    public static LoginResponse of(String jwt, UserDetails user) {
//        LoginResponse res = new LoginResponse();
//        res.jwt = jwt;
//        res.username = user.getUsername();
//        res.authorities = user.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//
//        return res;
//    }

    // getter ...
}