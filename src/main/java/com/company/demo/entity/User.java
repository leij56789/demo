package com.company.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@TableName("user")
public class User {
    @TableId(type= IdType.AUTO)
    private Long id;

    @NotBlank(message="姓名不能为空")
    private String name;
    
    @NotNull(message = "年龄不能为空")
    @Min(value=1,message="年龄必须大于0")
    private Integer age;

    public Long getId(){return id;}
    public void setId(Long id) {this.id=id;}
    public String getName(){return name;}
    public void setName(String name) {this.name=name;}
    public Integer getAge() {return age;}
    public void setAge(Integer age) {this.age=age;}

}
