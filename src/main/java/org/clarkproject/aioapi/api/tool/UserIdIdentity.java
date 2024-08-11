package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserIdIdentity {

    private final MemberUserDetails ANONYMOUS_USER = new MemberUserDetails(new MemberPO());


    private MemberUserDetails getMemberUserDetails() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        Object principal = auth.getPrincipal();

        return "anonymousUser".equals(principal)
                ? ANONYMOUS_USER
                : (MemberUserDetails) principal;
    }

    public boolean isAnonymous() {
        return getMemberUserDetails() == ANONYMOUS_USER;
    }

    public String getUsername() {
        return getMemberUserDetails().getUsername();
    }


    public String getEmail() {
        return getMemberUserDetails().getEmail();
    }

    public String getAuthority() {
        return getMemberUserDetails().getMemberPO().getRole();
    }
}
