package com.crazy.rain.controller;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crazy.rain.annotation.AuthCheck;
import com.crazy.rain.common.BaseResponse;
import com.crazy.rain.common.DeleteRequest;
import com.crazy.rain.common.ErrorCode;
import com.crazy.rain.common.ResultUtil;
import com.crazy.rain.constant.UserConstant;
import com.crazy.rain.converter.UserConverter;
import com.crazy.rain.exception.BusinessException;
import com.crazy.rain.exception.ThrowUtils;
import com.crazy.rain.model.dto.user.*;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.vo.LoginUserVO;
import com.crazy.rain.model.vo.SignatureAuthenticationVo;
import com.crazy.rain.model.vo.UserVO;
import com.crazy.rain.service.UserService;
import com.crazy.rain.utils.UserInfoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.crazy.rain.constant.UserConstant.REQUEST_PARAMETER_IS_EMPTY;


@RestController
@RequestMapping("/user")
@Slf4j
@AllArgsConstructor
@Tag(name = "用户接口")
public class UserController {


    private final UserService userService;
    private final UserConverter userConverter;

    private final RedisTemplate<String, Object> redisTemplate;


    // region 登录相关

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = userRegisterRequest.getEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String verificationCode = userRegisterRequest.getVerificationCode();
        if (StringUtils.isAnyBlank(email, userPassword, verificationCode)) {
            return null;
        }
        long result = userService.userRegister(email, userPassword, verificationCode);
        return ResultUtil.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = userLoginRequest.getEmail();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(email, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(email, userPassword, request);
        return ResultUtil.success(loginUserVO);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtil.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser() {
        return ResultUtil.success(userService.getLoginUser());
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @Operation(summary = "创建用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(userService.addUser(userAddRequest));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtil.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userConverter.userAddRequestConverter(userUpdateRequest);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @Operation(summary = "根据 id 获取用户", description = "仅限管理员")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtil.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 id 获取脱敏后的用户信息")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtil.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @Operation(summary = "分页获取用户列表", description = "仅管理员")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtil.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     */
    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取用户封装列表")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtil.success(userVOPage);
    }
    // endregion

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    @Operation(summary = "更新个人信息")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updateMyUser(userUpdateMyRequest);
        return ResultUtil.success(true);
    }

    /**
     * 发送邮箱验证码
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "发送验证码")
    public BaseResponse<Integer> sendVerificationCode(@PathVariable("email") String email) {
        ThrowUtils.throwIf(StringUtils.isBlank(email), ErrorCode.NOT_FOUND_ERROR, "邮箱为空");
        return ResultUtil.success(userService.sendVerificationCode(email));
    }

    @PostMapping("/forgotPassword")
    @Operation(summary = "忘记密码")
    public BaseResponse<Void> forgotPasswordDto(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        ThrowUtils.throwIf(forgotPasswordDto == null, ErrorCode.PARAMS_ERROR, REQUEST_PARAMETER_IS_EMPTY);
        userService.ForgotPasswordDto(forgotPasswordDto);
        return ResultUtil.success();
    }

    @PostMapping("/changePassword")
    @Operation(summary = "修改密码")
    public BaseResponse<Void> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        ThrowUtils.throwIf(changePasswordDto == null, ErrorCode.PARAMS_ERROR, REQUEST_PARAMETER_IS_EMPTY);
        String userPassword = changePasswordDto.getUserPassword();
        String verifyPassword = changePasswordDto.getVerifyPassword();
        String verificationCode = changePasswordDto.getVerificationCode();
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userPassword, verifyPassword, verificationCode),
                ErrorCode.PARAMS_ERROR, REQUEST_PARAMETER_IS_EMPTY);
        ThrowUtils.throwIf(!verifyPassword.equals(userPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        String email = UserInfoUtil.getUserInfo().getEmail();
        ThrowUtils.throwIf(!verificationCode.equals(String.valueOf(redisTemplate.opsForValue().get(email))),
                ErrorCode.PARAMS_ERROR,
                "验证码不正确");
        ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto();
        forgotPasswordDto.setEmail(email);
        forgotPasswordDto.setUserPassword(userPassword);
        forgotPasswordDto.setVerificationCode(verificationCode);
        userService.ForgotPasswordDto(forgotPasswordDto);
        return ResultUtil.success();
    }

    @GetMapping("/generateAccessKey")
    @Operation(summary = "生成访问秘钥")
    public BaseResponse<SignatureAuthenticationVo> generateAccessKey() {
        User userInfo = UserInfoUtil.getUserInfo();
        User user = userService.getById(userInfo.getId());
        ThrowUtils.throwIf(!StringUtils.isAnyBlank(user.getSecretId(), user.getSecretKey()),
                ErrorCode.SYSTEM_ERROR, "用户访问凭证已存在!,请联系管理员获取。");
        String secretId = UUID.randomUUID().toString().replace("-", "");
        SignatureAuthenticationVo signatureAuthenticationVo = new SignatureAuthenticationVo();
        signatureAuthenticationVo.setSecretId(secretId);
        String secretKey = DigestUtils.md5DigestAsHex((user.getEmail() + secretId).getBytes());
        signatureAuthenticationVo.setSecretKey(secretKey);
        user.setSecretKey(secretKey);
        user.setSecretId(secretId);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "添加访问凭证失败");
        return ResultUtil.success(signatureAuthenticationVo);
    }

}
