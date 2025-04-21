package dev.gunho.payment.model.mapper;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserPayload.Response toDto(UserEntity user);

    // DTO -> Entity 변환
    @Mapping(target = "idx", ignore = true)
    UserEntity toEntity(UserPayload.Request dto);

}
