package dev.vegamonitoramento.vega_monitoramento_test.erro;

import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.RegistroMovimentacao;

public class RegraDeNegocioException extends RuntimeException {

	private final RegistroMovimentacao registro;

	public RegraDeNegocioException(String message) {
		this(message, null);
	}

	public RegraDeNegocioException(String message, RegistroMovimentacao registro) {
		super(message);
		this.registro = registro;
	}

	public RegistroMovimentacao getRegistro() {
		return registro;
	}

}
