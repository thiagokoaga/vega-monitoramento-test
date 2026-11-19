package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;

import java.math.BigDecimal;

public record RegistroMovimentacao(
	OperacaoMovimentacao operacao,
	StatusMovimentacao status,
	BigDecimal quantidade,
	TipoIngrediente tipo,
	String responsavel,
	Long compartimentoId,
	Long ingredienteId,
	String metadata
) {
}
