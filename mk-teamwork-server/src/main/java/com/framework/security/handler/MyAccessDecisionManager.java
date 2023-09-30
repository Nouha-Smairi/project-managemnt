package com.framework.security.handler;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.security.handler
 * @description: Custom permission decider
 * @create: 2020-06-25 14:42
 **/
@Component
public class MyAccessDecisionManager implements AccessDecisionManager {
    /**
     * @Description: Compare the permissions of the current user with the permissions required by the url requested this time, and decide whether to let it go
     * auth contains the current user information, including the permissions it owns, that is, the user object stored when the UserDetailsService logged in before
     * object is the FilterInvocation object, which can get web resources such as request.
     * configAttributes is the permission required for this access. That is, the permission list obtained by querying and checking in MyFilterInvocationSecurityMetadataSource in the previous step
     * @Date: 2019/3/27-17:18
     * @Param: [auth, object, cas]
     * @return: void
     **/
    @Override
    public void decide(Authentication auth, Object object, Collection<ConfigAttribute> cas) {
        Iterator<ConfigAttribute> iterator = cas.iterator();
        while (iterator.hasNext()) {
            if (auth == null) {
                throw new AccessDeniedException("Current access does not have permission");
            }
            ConfigAttribute ca = iterator.next();
            //Permissions required by the current request
            String needRole = ca.getAttribute();
            if ("ROLE_LOGIN".equals(needRole)) {
                if (auth instanceof AnonymousAuthenticationToken) {
                    throw new BadCredentialsException("not logged in");
                } else {
                    return;
                }
            }
            //Permissions of the current user
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(needRole)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("Insufficient permissions!");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
