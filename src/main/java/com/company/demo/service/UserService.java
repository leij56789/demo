package com.company.demo.service;

import com.company.demo.common.BusinessException;
import com.company.demo.entity.User;
import com.company.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    public List<User> getAllUsers(){
        return userMapper.selectList(null);
    }
    public User getUserById(Long id){
        User user = userMapper.selectById(id);
        if(user==null){
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    public boolean addUser(User user){
        return userMapper.insert(user)>0;
    }
    public boolean updateUser(User user){
        if(user.getId()==null){
            throw new BusinessException("用户id不能为空");
        }
        if(userMapper.selectById(user.getId())==null){
            throw new BusinessException("用户不存在");
        }
        return userMapper.updateById(user)>0;
    }
    public boolean deleteUser(Long id){
        if(userMapper.selectById(id)==null){
            throw new BusinessException("用户不存在");
        }
        return userMapper.deleteById(id)>0;
    }

    public boolean existUser(Long id) {
        return userMapper.selectById(id)!=null;
    }
}
