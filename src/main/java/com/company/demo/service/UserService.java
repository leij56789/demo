package com.company.demo.service;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.common.BusinessException;
import com.company.demo.entity.User;
import com.company.demo.mapper.UserMapper;
import com.company.demo.mq.UserMessageProducer;
import com.company.demo.utils.RedisUtil;
import com.company.demo.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiaolei
 * @date 2026/6/4 18:03
 * @description 
 */
@Slf4j
@Service
public class UserService {

    private static final String USER_CACHE_KEY="user";
    private static final String USER_CACHE_KEY_NULL="user:null";
    private static final long CACHE_EXPIRE_SECONDS=3600;
    private static final long CACHE_EMPTY_SECONDS=300;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserMessageProducer userMessageProducer;

    public List<User> getAllUsers(){
        return userMapper.selectList(null);
    }
    public User getUserById(Long id){

        String key = USER_CACHE_KEY + id;
        String nullKey = USER_CACHE_KEY_NULL + id;

        Object nullCached = redisUtil.get(nullKey);
        if(nullCached!=null){
            log.debug("缓存穿透防护，用户不存在，id={}",id);
            throw new BusinessException("用户不存在");
        }

        Object cached = redisUtil.get(key);
        if(cached!=null){
            log.debug("redis获取用户 id="+id);
            return (User)cached;
        }

        User user = userMapper.selectById(id);
        if(user==null){
            redisUtil.set(nullKey,"null",CACHE_EMPTY_SECONDS);
            throw new BusinessException("用户不存在");
        }
        redisUtil.set(key,user,CACHE_EXPIRE_SECONDS);
        log.debug("数据库获取用户 id="+id);

        return user;
    }
    public boolean addUser(User user){
        boolean result = userMapper.insert(user) > 0;
//        sendWelcomeMessage(user);//测试异步
        System.out.println("mq->result="+result);
//        log.debug("mq->result="+result);
//        log.info("mq->result="+result);
        if(result){
            log.debug("执行mq");
            //异步发送用户创建消息
            userMessageProducer.sendUserCreatedMessage(user);
            //发送邮件通知
            userMessageProducer.sendEmailMessage("admin@example.com","新用户注册","用户："+user.getName());
        }
        return result;
    }
    public boolean updateUser(User user){
        if(user.getId()==null){
            throw new BusinessException("用户id不能为空");
        }
        String key = USER_CACHE_KEY + user.getId();
        String nullKey = USER_CACHE_KEY_NULL + user.getId();
        if(userMapper.selectById(user.getId())==null){
            redisUtil.delete(nullKey);
            redisUtil.delete(key);
            throw new BusinessException("用户不存在");
        }
        return userMapper.updateById(user)>0;
    }
    public boolean deleteUser(Long id){
        String key = USER_CACHE_KEY + id;
        String nullKey = USER_CACHE_KEY_NULL + id;
        if(userMapper.selectById(id)==null){
            redisUtil.delete(nullKey);
            redisUtil.delete(key);
            log.debug("删除缓存"+nullKey);
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
    /**
     * 新增用户（使用雪花算法生成ID）
     */
    public boolean addUserWithSnow(User user) {
        // 如果前端没传 ID，自动生成
        if (user.getId() == null) {
            long id = snowflakeIdWorker.nextId();
            user.setId(id);
            log.debug("生成雪花ID: {}", id);
        }
        return userMapper.insert(user) > 0;
    }

    /**
     * 异步发送欢迎消息（邮件/短信）
     */
    @Async("taskExecutor")
    public void sendWelcomeMessage(User user) {
        try {
            // 模拟发送邮件/短信，耗时操作
            Thread.sleep(1000);
            log.info("发送欢迎消息给用户: {}", user.getName());
        } catch (Exception e) {
            log.error("发送欢迎消息失败: {}", e.getMessage());
        }
    }

    /**
     * 异步批量处理用户数据
     */
    @Async("taskExecutor")
    public CompletableFuture<List<User>> processUsersAsync(List<Long> userIds) {
        log.info("开始批量处理用户: {}, 线程: {}", userIds.size(), Thread.currentThread().getName());

        List<User> users = new ArrayList<>();
        for (Long id : userIds) {
            User user = userMapper.selectById(id);
            if (user != null) {
                // 模拟业务处理
                users.add(user);
            }
        }

        log.info("批量处理完成: {} 条", users.size());
        return CompletableFuture.completedFuture(users);
    }

    /**
     * 批量查询（使用 CompletableFuture）
     */
    public List<User> batchGetUsers(List<Long> userIds) throws Exception {
        // 分批处理，每批 100 个
        List<List<Long>> batches = partition(userIds, 100);

        // 并行处理每一批
        List<CompletableFuture<List<User>>> futures = batches.stream()
                .map(batch -> processUsersAsync(batch))
                .toList();

        // 等待所有任务完成，合并结果
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allFutures.join();

        List<User> allUsers = new ArrayList<>();
        for (CompletableFuture<List<User>> future : futures) {
            allUsers.addAll(future.get());
        }

        return allUsers;
    }

    /**
     * 分批工具方法
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

}
