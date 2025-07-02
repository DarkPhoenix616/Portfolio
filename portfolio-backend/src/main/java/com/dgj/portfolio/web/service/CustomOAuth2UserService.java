package com.dgj.portfolio.web.service;

import com.dgj.portfolio.web.model.entity.User;
import com.dgj.portfolio.web.model.enums.Role;
import com.dgj.portfolio.web.repository.UserRepository;
import com.dgj.portfolio.web.service.CustomOAuth2UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository repo;

    public CustomOAuth2UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();
        OAuth2User oauthUser = delegate.loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // 1) Upsert your local User entity (no password needed)
        String username = repo.findUsernameByEmail(email);
        User user = repo.findByUsername(username);

        // 2) Map your roles into GrantedAuthorities
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toList());

        // 3) Return a DefaultOAuth2User
        //    Keycloak uses "sub", Google uses "sub" or "email"—here we pick "sub"
        return new DefaultOAuth2User(
                authorities,
                oauthUser.getAttributes(),
                "sub"  // the attribute key to use as principal name
        );
    }
}

