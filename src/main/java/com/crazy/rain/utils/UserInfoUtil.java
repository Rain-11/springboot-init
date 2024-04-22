package com.crazy.rain.utils;

import com.crazy.rain.common.ErrorCode;
import com.crazy.rain.exception.BusinessException;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.enums.UserRoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.crazy.rain.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @ClassName: UserInfoUtil
 * @Description: 获取用户信息
 * @author: CrazyRain
 * @date: 2024/4/18 下午9:20
 */
@Slf4j
public class UserInfoUtil {
    private UserInfoUtil() {
    }

    private static final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    private static final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

    public static User getUserInfo() {
        // 先判断是否已登录
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    public static User getLoginUserPermitNull() {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return currentUser;
    }

    public static boolean isAdmin() {
        return isAdmin(getUserInfo());
    }

    public static boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}
