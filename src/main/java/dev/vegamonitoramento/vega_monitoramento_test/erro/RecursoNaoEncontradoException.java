package dev.vegamonitoramento.vega_monitoramento_test.erro;

import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.RegistroMovimentacao;

public class RecursoNaoEncontradoException extends RuntimeException {

	private final RegistroMovimentacao registro;

	public RecursoNaoEncontradoException(String message) {
		this(message, null);
	}

	public RecursoNaoEncontradoException(String message, RegistroMovimentacao registro) {
		super(message);
		this.registro = registro;
	}

	public RegistroMovimentacao getRegistro() {
		return registro;
	}

}
