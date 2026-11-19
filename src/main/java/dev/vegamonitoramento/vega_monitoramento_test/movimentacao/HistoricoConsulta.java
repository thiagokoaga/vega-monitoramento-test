package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.erro.RegraDeNegocioException;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;

import java.time.LocalDate;

public record HistoricoConsulta(
	String sortBy,
	String order,
	TipoIngrediente tipo,
	OperacaoMovimentacao operacao,
	StatusMovimentacao status,
	Long compartimentoId,
	String responsavel,
	LocalDate dataInicio,
	LocalDate dataFim
) {

	public void validar() {
		String campo = campoOrdenacao();
		if (!campo.equals("data") && !campo.equals("date") && !campo.equals("compartimento")) {
			throw new RegraDeNegocioException("Parâmetro sortBy inválido. Use data ou compartimento.");
		}
		if (order != null && !order.isBlank() && !order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
			throw new RegraDeNegocioException("Parâmetro order inválido. Use asc ou desc.");
		}
		if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
			throw new RegraDeNegocioException("dataFim não pode ser anterior a dataInicio.");
		}
	}

	public boolean ordenarPorCompartimento() {
		return "compartimento".equals(campoOrdenacao());
	}

	public boolean ascendente() {
		return "asc".equalsIgnoreCase(order);
	}

	private String campoOrdenacao() {
		if (sortBy == null || sortBy.isBlank()) {
			return "data";
		}
		return sortBy.trim().toLowerCase();
	}

}
