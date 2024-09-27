package com.gokulraj.localstorage.repository;

import com.gokulraj.localstorage.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}
