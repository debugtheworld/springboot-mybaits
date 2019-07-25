package com.xw.test.service.impl;

import com.xw.test.dao.UserDao;
import com.xw.test.model.User;
import com.xw.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl extends User implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public long register(User user) {
        long id = userDao.insertUser(user);
        int i = 1 / 0;//测试回滚
        return id;
    }

    @Override
    public List<User> showAll() {
        List<User> users = userDao.selectAll();
        return users;
    }

}
