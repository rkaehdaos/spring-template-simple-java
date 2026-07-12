package dev.haja.springtemplatesimplejava.dto;

import lombok.Builder;
import lombok.Value;

/**
 * MapStruct 매핑의 타깃 DTO.
 * Lombok {@code @Builder}로 생성된 builder를 MapStruct가 사용해 객체를 만든다.
 * {@code fullName}은 소스의 {@code name}과 이름이 달라 {@code @Mapping} 동작 검증에 쓰인다.
 */
@Value
@Builder
public class SampleDto {
    Long id;
    String fullName;
    int age;
}
