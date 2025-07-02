package com.dgj.portfolio.web.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.dgj.portfolio.web.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u  FROM User u WHERE u.username = :username")
    public User findByUsername(String username);

    @Query("SELECT u.username  FROM User u WHERE u.email = :email")
    public String findUsernameByEmail(String email);
}
