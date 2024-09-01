package org.clarkproject.aioapi.api.service;

import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

public class UserDetailsServiceImpl implements UserDetailsService {
    private final Map<String, MemberPO> memberPOMap = new HashMap<>();

    public UserDetailsServiceImpl(List<MemberPO> memberPOList) {
        memberPOList.forEach(m -> memberPOMap.put(m.getAccount(), m));
    }

    /**
     * 傳入帳號，若查詢成功，則包裝成驗證物件回傳
     * 進入API時自動執行
     *
     * @param username
     * @param
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberPO memberPO = memberPOMap.get(username);
        if (memberPO == null) {
            throw new UsernameNotFoundException("Can't find username: " + username);
        }
//        return User
//                .withUsername(username)
//                .password(memberPO.getPassword())
//                .authorities(authorities)
//                .build();
        // 用自定義類別替代 實作UserDetail的User類別
        return new MemberUserDetails(memberPO);
    }
}
