package com.dgj.portfolio.web.service;

import com.dgj.portfolio.web.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    public void saveUser(User user);

    public User findByUsername(String username);

    public String findUsernameByEmail(String email);
}
