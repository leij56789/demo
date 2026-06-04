package com.company.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
/**
 * @author jiaolei
 * @date 2026/6/4 15:58
 * @description 
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
