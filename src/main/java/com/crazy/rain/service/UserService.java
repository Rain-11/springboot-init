package com.crazy.rain.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crazy.rain.model.dto.user.UserAddRequest;
import com.crazy.rain.model.dto.user.UserQueryRequest;
import com.crazy.rain.model.dto.user.UserUpdateMyRequest;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.vo.LoginUserVO;
import com.crazy.rain.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 更新个人信息
     */
    void updateMyUser(UserUpdateMyRequest userUpdateMyRequest);

    /**
     * 获取当前登录用户
     */
    LoginUserVO getLoginUser();

    /**
     * 创建用户
     * @param userAddRequest 添加用户参数
     */
    Long addUser(UserAddRequest userAddRequest);
}
