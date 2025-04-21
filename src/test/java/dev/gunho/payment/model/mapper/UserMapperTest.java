package dev.gunho.payment.model.mapper;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Request DTO를 Entity로 변환")
    void toEntity_ShouldMapRequestToEntity() {
        // given
        String userId = "testUser123";
        UserPayload.Request request = UserPayload.Request.builder()
                .userId(userId)
                .build();

        // when
        UserEntity entity = userMapper.toEntity(request);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getIdx()).isNull(); // idx는 ignore되므로 null
        assertThat(entity.getStatus()).isNull(); // withStatus 메서드 호출 전
        assertThat(entity.getPayKey()).isNull(); // withPayKey 메서드 호출 전
    }

    @Test
    @DisplayName("Entity를 Response DTO로 변환")
    void toDto_ShouldMapEntityToResponse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = UserEntity.builder()
                .userId("testUser123")
                .status(Status.ACTIVE)
                .payKey("20240520093000abc")
                .regDate(now)
                .build();

        // when
        UserPayload.Response response = userMapper.toDto(entity);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(entity.getUserId());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
        assertThat(response.getPayKey()).isEqualTo(entity.getPayKey());
        assertThat(response.getRegDate()).isEqualTo(entity.getRegDate());
    }
}