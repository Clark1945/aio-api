package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private List<MemberPO> memberPOList;
    private final MemberRepository memberRepository;
    public UserDetailsServiceImpl(MemberRepository memberRepository,List<MemberPO> memberPOList) {
        this.memberRepository = memberRepository;
        this.memberPOList = memberPOList;
    }

    /**
     * 傳入帳號，若查詢成功，則包裝成驗證物件回傳
     * 進入API時自動執行
     * @param username
     * @param
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberPO memberPO = Optional.ofNullable(memberRepository.findByAccount(username))
                .orElseThrow(() -> new UsernameNotFoundException("Can't find member: " + username));
//        return User
//                .withUsername(username)
//                .password(memberPO.getPassword())
//                .authorities(authorities)
//                .build();
        // 用自定義類別替代 實作UserDetail的User類別
        return new MemberUserDetails(memberPO);
    }
}
