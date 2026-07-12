package dev.haja.springtemplatesimplejava.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MapStruct 매핑 검증용 JPA 엔티티.
 * <p>
 * {@code @Setter} + no-arg 생성자를 쓰는 이유:
 * <ul>
 *     <li>{@code partialUpdate}의 {@code @MappingTarget}은 setter로 기존 인스턴스를 갱신한다.</li>
 *     <li>MapStruct가 setter로 값을 채우므로 lombok-mapstruct-binding 동작이 setter 방향으로도 증명된다.</li>
 * </ul>
 * 엔티티 identity/lazy-loading 함정을 피하려 {@code @Data}/{@code @EqualsAndHashCode}/{@code @Builder}는 쓰지 않는다.
 * {@code age}는 {@code partialUpdate}의 null 스킵을 증명하려 boxed {@link Integer}로 둔다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer age;
}
