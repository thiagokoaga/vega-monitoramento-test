package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import dev.vegamonitoramento.vega_monitoramento_test.comum.Quantidades;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;

import java.math.BigDecimal;

public record CompartimentoDisponivelResponse(
	Long id,
	String codigo,
	TipoIngrediente tipoAtual,
	BigDecimal saldo,
	BigDecimal capacidade,
	BigDecimal espacoLivre
) {

	public static CompartimentoDisponivelResponse de(Compartimento compartimento, BigDecimal saldo, TipoIngrediente tipoDaCapacidade) {
		BigDecimal saldoNormalizado = Quantidades.zeroSeNulo(saldo);
		BigDecimal capacidade = tipoDaCapacidade.getCapacidade();
		BigDecimal livre = capacidade.subtract(saldoNormalizado).max(BigDecimal.ZERO);
		return new CompartimentoDisponivelResponse(
			compartimento.getId(),
			compartimento.getCodigo(),
			compartimento.getTipoAtual(),
			saldoNormalizado,
			capacidade,
			Quantidades.normalizar(livre));
	}

}
