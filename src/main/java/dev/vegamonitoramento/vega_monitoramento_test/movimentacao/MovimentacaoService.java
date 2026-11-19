package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.compartimento.Compartimento;
import dev.vegamonitoramento.vega_monitoramento_test.compartimento.CompartimentoRepository;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.Ingrediente;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.IngredienteRepository;
import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

	private final MovimentacaoRepository movimentacaoRepository;
	private final CompartimentoRepository compartimentoRepository;
	private final IngredienteRepository ingredienteRepository;
	private final Relogio relogio;

	@Transactional
	public void registrarSucesso(RegistroMovimentacao registro, Compartimento compartimento, Ingrediente ingrediente) {
		movimentacaoRepository.save(montar(registro, compartimento, ingrediente));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registrarFalha(RegistroMovimentacao registro) {
		Compartimento compartimento = registro.compartimentoId() == null
			? null
			: compartimentoRepository.findById(registro.compartimentoId()).orElse(null);
		Ingrediente ingrediente = registro.ingredienteId() == null
			? null
			: ingredienteRepository.findById(registro.ingredienteId()).orElse(null);
		movimentacaoRepository.save(montar(registro, compartimento, ingrediente));
	}

	@Transactional(readOnly = true)
	public List<MovimentacaoResponse> listar(HistoricoConsulta consulta) {
		consulta.validar();
		return movimentacaoRepository.findAll(filtrar(consulta))
			.stream()
			.map(MovimentacaoResponse::de)
			.toList();
	}

	private Movimentacao montar(RegistroMovimentacao registro, Compartimento compartimento, Ingrediente ingrediente) {
		return new Movimentacao(
			relogio.agora(),
			registro.operacao(),
			registro.status(),
			registro.quantidade(),
			registro.tipo(),
			registro.responsavel(),
			compartimento,
			ingrediente,
			registro.metadata());
	}

	private Specification<Movimentacao> filtrar(HistoricoConsulta consulta) {
		return (root, query, criteriaBuilder) -> {
			if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
				var compartimento = root.join("compartimento", JoinType.LEFT);
				var dataHora = root.get("dataHora");
				var codigo = compartimento.get("codigo");
				if (consulta.ordenarPorCompartimento()) {
					query.orderBy(
						consulta.ascendente() ? criteriaBuilder.asc(codigo) : criteriaBuilder.desc(codigo),
						criteriaBuilder.desc(dataHora),
						criteriaBuilder.asc(root.get("id")));
				}
				else {
					query.orderBy(
						consulta.ascendente() ? criteriaBuilder.asc(dataHora) : criteriaBuilder.desc(dataHora),
						criteriaBuilder.asc(codigo),
						criteriaBuilder.asc(root.get("id")));
				}
			}

			List<Predicate> predicados = new ArrayList<>();
			if (consulta.tipo() != null) {
				predicados.add(criteriaBuilder.equal(root.get("tipo"), consulta.tipo()));
			}
			if (consulta.operacao() != null) {
				predicados.add(criteriaBuilder.equal(root.get("operacao"), consulta.operacao()));
			}
			if (consulta.status() != null) {
				predicados.add(criteriaBuilder.equal(root.get("status"), consulta.status()));
			}
			if (consulta.compartimentoId() != null) {
				predicados.add(criteriaBuilder.equal(root.get("compartimento").get("id"), consulta.compartimentoId()));
			}
			if (consulta.responsavel() != null && !consulta.responsavel().isBlank()) {
				predicados.add(criteriaBuilder.equal(root.get("responsavel"), consulta.responsavel().trim()));
			}
			if (consulta.dataInicio() != null) {
				predicados.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataHora"), consulta.dataInicio().atStartOfDay()));
			}
			if (consulta.dataFim() != null) {
				predicados.add(criteriaBuilder.lessThan(root.get("dataHora"), consulta.dataFim().plusDays(1).atStartOfDay()));
			}
			if (predicados.isEmpty()) {
				return criteriaBuilder.conjunction();
			}
			return criteriaBuilder.and(predicados.toArray(Predicate[]::new));
		};
	}

}
