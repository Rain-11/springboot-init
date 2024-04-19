package com.crazy.rain.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: DeleteRequest
 * @Description: 删除请求
 * @author: CrazyRain
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}