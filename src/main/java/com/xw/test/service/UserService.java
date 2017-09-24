package com.xw.test.service;

import com.xw.test.model.User;

import java.util.List;


public interface UserService
{

        long register(User user);

        List<User> showAll();
}
