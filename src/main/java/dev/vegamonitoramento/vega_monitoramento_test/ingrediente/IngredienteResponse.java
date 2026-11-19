package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import dev.vegamonitoramento.vega_monitoramento_test.comum.Quantidades;

import java.math.BigDecimal;

public record IngredienteResponse(
	Long id,
	String nome,
	TipoIngrediente tipo,
	BigDecimal quantidade,
	Long compartimentoId,
	String compartimentoCodigo
) {

	public static IngredienteResponse de(Ingrediente ingrediente) {
		return new IngredienteResponse(
			ingrediente.getId(),
			ingrediente.getNome(),
			ingrediente.getTipo(),
			Quantidades.normalizar(ingrediente.getQuantidade()),
			ingrediente.getCompartimento().getId(),
			ingrediente.getCompartimento().getCodigo());
	}

}
