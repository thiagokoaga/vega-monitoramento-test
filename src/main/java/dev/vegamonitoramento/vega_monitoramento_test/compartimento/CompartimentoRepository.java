package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompartimentoRepository extends JpaRepository<Compartimento, Long> {

	List<Compartimento> findAllByOrderByCodigoAsc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Compartimento c where c.id = :id")
	Optional<Compartimento> findByIdComBloqueio(@Param("id") Long id);

}
