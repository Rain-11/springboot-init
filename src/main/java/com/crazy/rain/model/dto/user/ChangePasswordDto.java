package com.crazy.rain.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: ForgotPasswordDto
 * @Description: 忘记密码Dto
 * @author: CrazyRain
 * @date: 2024/4/23 下午9:47
 */
@Data
public class ChangePasswordDto implements Serializable {


    /**
     * 新密码
     */
    private String userPassword;

    /**
     * 新密码
     */
    private String verifyPassword;

    /**
     * 邮箱验证码
     */
    private String verificationCode;

}
