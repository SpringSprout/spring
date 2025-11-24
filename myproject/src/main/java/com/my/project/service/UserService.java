package com.my.project.service;

import static java.util.stream.Collectors.toList;

import com.my.project.domain.User;
import com.my.project.dto.Id;
import com.my.project.dto.UserInfo;
import com.my.project.repository.UserRepository;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Service;
import com.spring.sprout.global.annotation.db.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void join(UserInfo userInfo) {
        User user = new User(userInfo.name(), userInfo.age());
        userRepository.save(user);
    }

    public UserInfo findInfo(Id id) {
        User user = userRepository.findById(id.id());
        UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getAge());
        return userInfo;
    }

    public List<UserInfo> findAll() {
        return userRepository.findAll().stream()
            .map(u -> new UserInfo(u.getId(), u.getName(), u.getAge())).collect(toList());
    }
}