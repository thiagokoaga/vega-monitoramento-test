package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import java.math.BigDecimal;

public enum TipoIngrediente {

	SECO(new BigDecimal("600.000"), "kg"),
	LIQUIDO(new BigDecimal("500.000"), "L"),
	REFRIGERADO(new BigDecimal("400.000"), "kg");

	private final BigDecimal capacidade;
	private final String unidade;

	TipoIngrediente(BigDecimal capacidade, String unidade) {
		this.capacidade = capacidade;
		this.unidade = unidade;
	}

	public BigDecimal getCapacidade() {
		return capacidade;
	}

	public String getUnidade() {
		return unidade;
	}

}
