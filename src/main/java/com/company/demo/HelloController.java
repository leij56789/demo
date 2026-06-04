package com.company.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/apii")
public class HelloController {
    @GetMapping("/hello")
    public String sayHello(){
        return "Hello,我回来了，环境搭建成功";
    }

    @GetMapping("/users")
    public List<User> getUsers(){
        User user1=new User(1L,"张三",25);
        User user2=new User(2L,"李四",30);
        User user3=new User(3L,"王五",28);

        return Arrays.asList(user1,user2,user3);
    }
    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable Long id){

        List<User> list = Arrays.asList(
                new User(1L, "张三", 25),
                new User(2L, "李四", 30),
                new User(3L, "王五", 28)
        );
        return list.stream()
                .filter(user->user.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->new RuntimeException("用户不存在，id:"+id));

//        User user1=new User(1L,"张三",25);
//        User user2=new User(2L,"李四",30);
//        User user3=new User(3L,"王五",28);
//        ArrayList<User> users = new ArrayList<User>();
//        users.add(user1);
//        users.add(user2);
//        users.add(user3);
//        User user=null;
//        for (int i = 0; i < users.size(); i++) {
//            user = users.get(i);
//            if(user.getId().equals(id)) break;
//        }
//        return user;

    }

}
