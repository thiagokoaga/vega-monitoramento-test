package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import dev.vegamonitoramento.vega_monitoramento_test.comum.Quantidades;
import dev.vegamonitoramento.vega_monitoramento_test.erro.RegraDeNegocioException;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.IngredienteRepository;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompartimentoService {

	private final CompartimentoRepository compartimentoRepository;
	private final IngredienteRepository ingredienteRepository;
	private final Relogio relogio;

	@Transactional(readOnly = true)
	public List<CompartimentoDisponivelResponse> disponiveis(BigDecimal quantidade, TipoIngrediente tipo) {
		if (tipo == null) {
			throw new RegraDeNegocioException("Tipo do ingrediente é obrigatório.");
		}
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraDeNegocioException("Quantidade deve ser maior que zero.");
		}
		BigDecimal quantidadeNormalizada = Quantidades.normalizar(quantidade);
		Map<Long, BigDecimal> saldos = saldosPorCompartimento();
		return compartimentoRepository.findAllByOrderByCodigoAsc().stream()
			.filter(compartimento -> compartimento.aceitaEntrada(
				tipo,
				quantidadeNormalizada,
				saldos.getOrDefault(compartimento.getId(), BigDecimal.ZERO),
				relogio.hoje()))
			.map(compartimento -> CompartimentoDisponivelResponse.de(
				compartimento,
				saldos.getOrDefault(compartimento.getId(), BigDecimal.ZERO),
				tipo))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CompartimentoDisponivelResponse> disponiveisParaVenda(TipoIngrediente tipo) {
		if (tipo == null) {
			throw new RegraDeNegocioException("Tipo do ingrediente é obrigatório.");
		}
		Map<Long, BigDecimal> saldos = saldosPorCompartimento();
		return compartimentoRepository.findAllByOrderByCodigoAsc().stream()
			.filter(compartimento -> compartimento.getTipoAtual() == tipo)
			.filter(compartimento -> saldos.getOrDefault(compartimento.getId(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
			.map(compartimento -> CompartimentoDisponivelResponse.de(
				compartimento,
				saldos.get(compartimento.getId()),
				tipo))
			.toList();
	}

	private Map<Long, BigDecimal> saldosPorCompartimento() {
		Map<Long, BigDecimal> saldos = new HashMap<>();
		for (Object[] linha : ingredienteRepository.somarSaldoAgrupado()) {
			Long compartimentoId = ((Number) linha[0]).longValue();
			saldos.put(compartimentoId, Quantidades.zeroSeNulo(new BigDecimal(linha[1].toString())));
		}
		return saldos;
	}

}
