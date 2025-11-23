package com.my.project.mvc;

import com.my.project.domain.User;
import com.my.project.repository.UserRepository;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Service;
import com.spring.sprout.global.annotation.db.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void join(User user) {
        userRepository.save(user);
    }

    public User find(int id) {
        return userRepository.findById(id);
    }
}