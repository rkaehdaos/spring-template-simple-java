package dev.haja.springtemplatesimplejava.dto;

import lombok.Builder;
import lombok.Value;

/**
 * {@code SampleEntity}의 DTO.
 * <ul>
 *     <li>{@code fullName}은 엔티티의 {@code name}과 이름이 달라, {@code unmappedTargetPolicy = ERROR}
 *         정책 하에서 구체 매퍼가 {@code @Mapping} 오버라이드를 강제로 선언해야 함을 증명한다.</li>
 *     <li>{@code age}는 boxed {@link Integer}라 {@code partialUpdate}의 null 스킵이 동작한다.</li>
 * </ul>
 */
@Value
@Builder
public class SampleEntityDto {
    Long id;
    String fullName;
    Integer age;
}
