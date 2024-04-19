package com.crazy.rain.utils;

import com.crazy.rain.common.ErrorCode;
import com.crazy.rain.exception.BusinessException;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.enums.UserRoleEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static com.crazy.rain.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @ClassName: UserInfoUtil
 * @Description: 获取用户信息
 * @author: CrazyRain
 * @date: 2024/4/18 下午9:20
 */
@Component
@AllArgsConstructor
public class UserInfoUtil {

    private final HttpServletRequest request;

    public User getUserInfo() {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    public User getLoginUserPermitNull() {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return currentUser;
    }

    public boolean isAdmin() {
        return isAdmin(getUserInfo());
    }

    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}
