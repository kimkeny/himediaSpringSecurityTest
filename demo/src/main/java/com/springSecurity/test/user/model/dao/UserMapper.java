package com.springSecurity.test.user.model.dao;

import com.springSecurity.test.user.model.dto.SignupDTO;
import com.springSecurity.test.user.model.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    int regist(SignupDTO newUserInfo);

    UserDTO findByUsername(String username);
}
