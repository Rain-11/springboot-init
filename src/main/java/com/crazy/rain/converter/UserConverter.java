package com.crazy.rain.converter;

import com.crazy.rain.model.dto.user.ForgotPasswordDto;
import com.crazy.rain.model.dto.user.UserAddRequest;
import com.crazy.rain.model.dto.user.UserUpdateMyRequest;
import com.crazy.rain.model.dto.user.UserUpdateRequest;
import com.crazy.rain.model.entity.User;
import com.crazy.rain.model.vo.LoginUserVO;
import com.crazy.rain.model.vo.UserVO;
import org.mapstruct.Mapper;

/**
 * @ClassName: UserConverter
 * @Description: 用户相关转换器
 * @author: CrazyRain
 * @date: 2024/4/19 上午9:32
 */
@Mapper(componentModel = "spring")
public interface UserConverter {
    User userAddRequestConverter(UserAddRequest userAddRequest);

    User userAddRequestConverter(UserUpdateRequest userUpdateRequest);

    User userUpdateMyRequestConverter(UserUpdateMyRequest userUpdateMyRequest);

    LoginUserVO loginUserVOConverter(User user);

    UserVO userVOConverter(User user);

    User forgotPasswordDtoConverter(ForgotPasswordDto forgotPasswordDto);
}
