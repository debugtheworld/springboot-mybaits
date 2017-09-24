package com.xw.test.controller;

import java.util.List;

import com.xw.test.model.User;
import com.xw.test.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController
{

        private Logger log = Logger.getLogger(UserController.class);

        @Autowired
        private UserService userService;

        @RequestMapping("/register")
        @ResponseBody
        private long register(User user)
        {
                userService.register(user);
                return user.getId();
        }

        @RequestMapping("/showAll")
        @ResponseBody
        private List<User> showAll()
        {
                List<User> users = userService.showAll();
                for (User u : users)
                {
                        log.info(u);
                }
                return users;
        }
}
