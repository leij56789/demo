package com.company.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
