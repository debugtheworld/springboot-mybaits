package com.xw.test.dao;

import com.xw.test.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface UserDao
{

        long insertUser(User user);

        List<User> selectAll();
}
