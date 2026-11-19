package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.comum.Quantidades;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponse(
	Long id,
	LocalDateTime dataHora,
	OperacaoMovimentacao operacao,
	StatusMovimentacao status,
	BigDecimal quantidade,
	TipoIngrediente tipo,
	String responsavel,
	Long compartimentoId,
	String compartimentoCodigo,
	Long ingredienteId,
	String ingredienteNome,
	String metadata
) {

	public static MovimentacaoResponse de(Movimentacao movimentacao) {
		return new MovimentacaoResponse(
			movimentacao.getId(),
			movimentacao.getDataHora(),
			movimentacao.getOperacao(),
			movimentacao.getStatus(),
			Quantidades.normalizar(movimentacao.getQuantidade()),
			movimentacao.getTipo(),
			movimentacao.getResponsavel(),
			movimentacao.getCompartimento() == null ? null : movimentacao.getCompartimento().getId(),
			movimentacao.getCompartimento() == null ? null : movimentacao.getCompartimento().getCodigo(),
			movimentacao.getIngrediente() == null ? null : movimentacao.getIngrediente().getId(),
			movimentacao.getIngrediente() == null ? null : movimentacao.getIngrediente().getNome(),
			movimentacao.getMetadata());
	}

}
