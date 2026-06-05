package com.company.demo.service;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.common.BusinessException;
import com.company.demo.entity.User;
import com.company.demo.mapper.UserMapper;
import com.company.demo.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 18:03
 * @description 
 */
@Slf4j
@Service
public class UserService {

    private static final String USER_CACHE_KEY="user";
    private static final long CACHE_EXPIRE_SECONDS=3600;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;

    public List<User> getAllUsers(){
        return userMapper.selectList(null);
    }
    public User getUserById(Long id){

        String key = USER_CACHE_KEY + id;
        Object cached = redisUtil.get(key);
        if(cached!=null){
            log.debug("redis获取用户 id="+id);
            return (User)cached;
        }


        User user = userMapper.selectById(id);
        if(user==null){
            throw new BusinessException("用户不存在");
        }
        redisUtil.set(key,user,CACHE_EXPIRE_SECONDS);
        log.debug("数据库获取用户 id="+id);

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
    //分页查询用户
    public Page<User> getUsersPage(int pageNum,int pageSize,String keyword){
        Page<User> page = new Page<>(pageNum, pageSize);
        //条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //name like '%keyword%'
        wrapper.like(StrUtil.isNotBlank(keyword),User::getName,keyword);
        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(page,wrapper);
    }

    public boolean deleteBatch(Long[] ids) {
        if(ids==null||ids.length==0){
            throw new BusinessException("请至少选择一个用户");
        }
        List<Long> list = Arrays.asList(ids);
        return userMapper.deleteByIds(list)>0;
    }
}
