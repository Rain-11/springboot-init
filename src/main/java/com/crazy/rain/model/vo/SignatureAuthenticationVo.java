package com.crazy.rain.model.vo;

import lombok.Data;

/**
 * @ClassName: SignatureAuthenticationVo
 * @Description: 签名认证视图
 * @author: CrazyRain
 * @date: 2024/4/26 上午8:54
 */
@Data
public class SignatureAuthenticationVo {


    /**
     * 签名秘钥
     */
    private String secretKey;


    /**
     * 签名id
     */
    private String secretId;

}
