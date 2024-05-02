package com.crazy.rain.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crazy.rain.common.ErrorCode;
import com.crazy.rain.constant.CommonConstant;
import com.crazy.rain.converter.UserConverter;
import com.crazy.rain.exception.BusinessException;
import com.crazy.rain.exception.ThrowUtils;
import com.crazy.rain.mapper.UserMapper;
import com.crazy.rain.model.dto.user.ForgotPasswordDto;
import com.crazy.rain.model.dto.user.UserAddRequest;
import com.crazy.rain.model.dto.user.UserQueryRequest;
import com.crazy.rain.model.dto.user.UserUpdateMyRequest;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.vo.LoginUserVO;
import com.crazy.rain.model.vo.UserVO;
import com.crazy.rain.service.UserService;
import com.crazy.rain.utils.EmailUtils;
import com.crazy.rain.utils.SqlUtils;
import com.crazy.rain.utils.UserInfoUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.crazy.rain.constant.UserConstant.USER_LOGIN_STATE;


@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "CrazyRain";
    private static final String DEFAULT_PASSWORD = "12345678";
    private final UserConverter userConverter;
    private final EmailUtils emailUtils;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public long userRegister(String email, String userPassword, String verificationCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(email, userPassword, verificationCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!emailUtils.isValidEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        String code = String.valueOf(redisTemplate.opsForValue().get(email));
        ThrowUtils.throwIf(!code.equals(verificationCode), ErrorCode.PARAMS_ERROR, "验证码错误");
        synchronized (email.intern()) {
            // 账户不能重复
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getEmail, email);
            long count = this.baseMapper.selectCount(userLambdaQueryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserName(String.valueOf(UUID.randomUUID().getLeastSignificantBits()));
            user.setEmail(email);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String email, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(email, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!emailUtils.isValidEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, email).eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(userLambdaQueryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("用户不存在");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    /**
     * 用户注销
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        return userConverter.loginUserVOConverter(user);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return userConverter.userVOConverter(user);
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void updateMyUser(UserUpdateMyRequest userUpdateMyRequest) {
        User user = userConverter.userUpdateMyRequestConverter(userUpdateMyRequest);
        user.setId(UserInfoUtil.getUserInfo().getId());
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public LoginUserVO getLoginUser() {
        return getLoginUserVO(UserInfoUtil.getUserInfo());
    }

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, userAddRequest.getEmail());
        ThrowUtils.throwIf(count(userLambdaQueryWrapper) != 0, ErrorCode.OPERATION_ERROR, "邮箱已存在");
        User user = userConverter.userAddRequestConverter(userAddRequest);
        // 默认密码 12345678
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + DEFAULT_PASSWORD).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    @Override
    public Integer sendVerificationCode(String email) {
        int code = emailUtils.sendVerificationCode(email);
        //缓存邮箱验证码
        ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
        stringObjectValueOperations.set(email, code, 5, TimeUnit.MINUTES);
        return code;
    }

    @Override
    public void ForgotPasswordDto(ForgotPasswordDto forgotPasswordDto) {
        String email = forgotPasswordDto.getEmail();
        String userPassword = forgotPasswordDto.getUserPassword();
        String verificationCode = forgotPasswordDto.getVerificationCode();
        ThrowUtils.throwIf(StringUtils.isAnyBlank(email, userPassword, verificationCode), ErrorCode.PARAMS_ERROR,
                "请求参数为空");
        Integer code = (Integer) redisTemplate.opsForValue().get(email);
        ThrowUtils.throwIf(code == null, ErrorCode.SYSTEM_ERROR, "验证码不存在");
        ThrowUtils.throwIf(!verificationCode.equals(String.valueOf(code)), ErrorCode.PARAMS_ERROR, "验证码异常");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "新密码格式不正确");
        forgotPasswordDto.setUserPassword(DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
        User user = userConverter.forgotPasswordDtoConverter(forgotPasswordDto);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, email);
        int insert = baseMapper.update(user, userLambdaQueryWrapper);
        ThrowUtils.throwIf(insert != 1, ErrorCode.SYSTEM_ERROR, "修改密码失败,请联系管理员!");
        redisTemplate.delete(email);
    }
}
