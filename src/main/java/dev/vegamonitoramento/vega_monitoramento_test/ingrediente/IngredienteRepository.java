package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

	@Query("select i from Ingrediente i join fetch i.compartimento order by i.nome")
	List<Ingrediente> findAllComCompartimento();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Ingrediente i where i.nome = :nome")
	Optional<Ingrediente> findByNomeComBloqueio(@Param("nome") String nome);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Ingrediente i where i.id = :id")
	Optional<Ingrediente> findByIdComBloqueio(@Param("id") Long id);

	@Query("select i.compartimento.id from Ingrediente i where i.id = :id")
	Optional<Long> findCompartimentoIdByIngredienteId(@Param("id") Long id);

	@Query("select coalesce(sum(i.quantidade), 0) from Ingrediente i where i.compartimento.id = :compartimentoId")
	BigDecimal somarSaldoPorCompartimento(@Param("compartimentoId") Long compartimentoId);

	@Query("select i.compartimento.id, coalesce(sum(i.quantidade), 0) from Ingrediente i group by i.compartimento.id")
	List<Object[]> somarSaldoAgrupado();

	@Query("select i.tipo, coalesce(sum(i.quantidade), 0) from Ingrediente i group by i.tipo")
	List<Object[]> somarPorTipo();

}
