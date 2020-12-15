package com.matchi;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MatchiTokenBasedRememberMeServices extends TokenBasedRememberMeServices {
    private String cookieName = SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
    private Boolean useSecureCookie = null;

    public MatchiTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    public void setUseSecureCookie(boolean useSecureCookie) {
        super.setUseSecureCookie(useSecureCookie);
        this.useSecureCookie = useSecureCookie;
    }

    protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Cancelling cookie");
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);

        cookie.setPath(getCookiePath(request));
        if (useSecureCookie == null) {
            cookie.setSecure(request.isSecure());
        } else {
            cookie.setSecure(useSecureCookie);
        }

        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }

    public void setCookieName(String cookieName) {
        super.setCookieName(cookieName);
        this.cookieName = cookieName;
    }
}