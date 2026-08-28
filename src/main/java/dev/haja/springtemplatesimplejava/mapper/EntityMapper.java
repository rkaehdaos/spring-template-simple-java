package dev.haja.springtemplatesimplejava.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

/**
 * 양방향(엔티티 ↔ DTO) 제네릭 매퍼. {@link DtoMapper}의 읽기 방향에 쓰기 방향과
 * 부분 갱신을 더한다.
 * <p>
 * <strong>주의</strong>: 구체 매퍼가 여기의 메서드를 {@code @Override}하면 이 인터페이스에 붙은
 * {@link Named}/{@link Mapping} 어노테이션은 <em>병합되지 않고 무시된다</em>.
 * 따라서 비대칭 필드 때문에 {@code partialUpdate}를 오버라이드할 때는 {@code @Named}와
 * {@code @Mapping(target = "id", ignore = true)}를 구체 매퍼에서 다시 선언해야 한다.
 * null 스킵 자체는 {@link CentralMapperConfig}에 중앙 선언되어 있어 재선언이 필요 없다.
 *
 * @param <D> DTO 타입
 * @param <E> 엔티티 타입
 */
public interface EntityMapper<D, E> extends DtoMapper<E, D> {

    E toEntity(D dto);

    List<E> toEntity(List<D> dtoList);

    /**
     * DTO의 null이 아닌 필드만 기존 엔티티에 덮어써 부분 갱신한다.
     * null 스킵은 {@link CentralMapperConfig}의 중앙 정책({@code nullValuePropertyMappingStrategy = IGNORE})이
     * 담당하며, boxed 타입 필드에만 동작한다(primitive는 null이 될 수 없어 항상 복사됨).
     * {@code id}는 항상 무시하므로 {@code E}는 {@code id} 프로퍼티를 가져야 한다(JPA 엔티티 전제).
     */
    @Named("partialUpdate")
    @Mapping(target = "id", ignore = true)
    void partialUpdate(@MappingTarget E entity, D dto);
}
