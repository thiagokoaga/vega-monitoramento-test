package dev.vegamonitoramento.vega_monitoramento_test.erro;

public record ErroResponse(int status, String error, String message) {

	public static ErroResponse de(int status, String message) {
		String error = switch (status) {
			case 400 -> "Bad Request";
			case 404 -> "Not Found";
			default -> "Error";
		};
		return new ErroResponse(status, error, message);
	}

}
