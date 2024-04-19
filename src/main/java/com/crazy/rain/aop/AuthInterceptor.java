package com.crazy.rain.aop;

import com.crazy.rain.annotation.AuthCheck;
import com.crazy.rain.common.ErrorCode;
import com.crazy.rain.exception.BusinessException;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.enums.UserRoleEnum;
import com.crazy.rain.utils.UserInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName: AuthInterceptor
 * @Description: 权限校验
 * @author: CrazyRain
 * @date: 2024/4/18 下午5:50
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserInfoUtil userInfoUtil;

    /**
     * 执行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 当前登录用户
        User loginUser = userInfoUtil.getUserInfo();
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum) && !mustRole.equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

