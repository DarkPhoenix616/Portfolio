package com.dgj.portfolio.web.config;

import com.dgj.portfolio.web.model.entity.User;
import com.dgj.portfolio.web.model.enums.Role;
import com.dgj.portfolio.web.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

//import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    public OAuth2SuccessHandler(JwtTokenProvider tokenProvider, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email    = oauthUser.getAttribute("email");
            String fullName = oauthUser.getAttribute("name");
            String token    = tokenProvider.createToken(email, fullName);

            String check = userService.findUsernameByEmail(email);

            if(check == null) {
                String username = tokenProvider.generateUniqueUsername(email, fullName);
                User user = new User();
                user.setEmail(email);
                Role role = Role.ROLE_USER;
                user.setRoles(Collections.singleton(role));
                user.setUsername(username);
                userService.saveUser(user);
            }



            // Set cookie
            Cookie cookie = new Cookie("DANIES_JWT_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge((int)(tokenProvider.getValidity() / 1000));
            response.addCookie(cookie);

            // Write JSON
            Map<String,Object> payload = Map.of(
                    "token", token,
                    "email", email,
                    "name",  fullName
            );
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), payload);
            response.getWriter().flush();

        } catch (Exception ex) {
            // log the error so you can see it in your server console
            ex.printStackTrace();
            // send a 500 so you at least get an error message in the browser
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
//        // 3) redirect to your frontend (or write a JSON response)
//        response.sendRedirect("https://your-frontend-app.com/");