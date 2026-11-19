package dev.vegamonitoramento.vega_monitoramento_test.erro;

import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.MovimentacaoService;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.OperacaoMovimentacao;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.RegistroMovimentacao;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.StatusMovimentacao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

	private final MovimentacaoService movimentacaoService;

	public ApiExceptionHandler(MovimentacaoService movimentacaoService) {
		this.movimentacaoService = movimentacaoService;
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException ex) {
		registrarFalha(ex.getRegistro());
		return ResponseEntity.badRequest().body(ErroResponse.de(400, ex.getMessage()));
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
		registrarFalha(ex.getRegistro());
		return ResponseEntity.status(404).body(ErroResponse.de(404, ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
		String mensagem = ex.getBindingResult().getFieldErrors().stream()
			.map(erro -> erro.getDefaultMessage())
			.distinct()
			.collect(Collectors.joining(" "));
		if (mensagem.isBlank()) {
			mensagem = "Requisição inválida.";
		}
		return ResponseEntity.badRequest().body(ErroResponse.de(400, mensagem));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
		String causa = ex.getMostSpecificCause().getMessage();
		if (causa != null && causa.contains("TipoIngrediente")) {
			return ResponseEntity.badRequest().body(ErroResponse.de(400,
				"Tipo de ingrediente inválido. Valores aceitos: SECO, LIQUIDO, REFRIGERADO."));
		}
		return ResponseEntity.badRequest().body(ErroResponse.de(400, "Corpo da requisição inválido."));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex) {
		return ResponseEntity.badRequest().body(ErroResponse.de(400, "Parâmetro obrigatório: " + ex.getParameterName() + "."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
		if (ex.getRequiredType() == TipoIngrediente.class) {
			return ResponseEntity.badRequest().body(ErroResponse.de(400,
				"Tipo de ingrediente inválido. Valores aceitos: SECO, LIQUIDO, REFRIGERADO."));
		}
		if (ex.getRequiredType() == OperacaoMovimentacao.class) {
			return ResponseEntity.badRequest().body(ErroResponse.de(400, "Operação inválida. Valores aceitos: ENTRADA, SAIDA."));
		}
		if (ex.getRequiredType() == StatusMovimentacao.class) {
			return ResponseEntity.badRequest().body(ErroResponse.de(400, "Status inválido. Valores aceitos: SUCESSO, FALHA."));
		}
		return ResponseEntity.badRequest().body(ErroResponse.de(400, "Parâmetro inválido: " + ex.getName() + "."));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
		return ResponseEntity.badRequest().body(ErroResponse.de(400,
			"Conflito ao salvar o ingrediente. Verifique se o nome já existe e tente novamente."));
	}

	private void registrarFalha(RegistroMovimentacao registro) {
		if (registro != null && registro.status() == StatusMovimentacao.FALHA) {
			movimentacaoService.registrarFalha(registro);
		}
	}

}
