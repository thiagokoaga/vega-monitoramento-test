package dev.vegamonitoramento.vega_monitoramento_test.comum;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Quantidades {

	public static final int ESCALA = 3;

	private Quantidades() {
	}

	public static BigDecimal normalizar(BigDecimal valor) {
		return valor.setScale(ESCALA, RoundingMode.HALF_UP);
	}

	public static BigDecimal zeroSeNulo(BigDecimal valor) {
		if (valor == null) {
			return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
		}
		return valor.setScale(ESCALA, RoundingMode.HALF_UP);
	}

}
