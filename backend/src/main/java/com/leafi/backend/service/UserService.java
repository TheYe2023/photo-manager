package com.leafi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leafi.backend.model.dto.user.UserQueryRequest;
import com.leafi.backend.model.entity.User;
import com.leafi.backend.model.vo.LoginUserVO;
import com.leafi.backend.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leafi.backend.service.impl.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userEmail     用户邮箱
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount ,String userEmail ,String userPassword, String checkPassword);

    /**
     * 获取加密后的密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前用户
     * 
     * @param request 请求
     * @return 当前用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户视图
     */
    public UserVO getUserVO(User user);

    /**
     * 获取用户视图列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构建用户查询包装器
     */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**  
     * 是否为管理员  
     *  
     * @param user  
     * @return  
     */  
    boolean isAdmin(User user);

}
