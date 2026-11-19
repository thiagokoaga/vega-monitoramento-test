package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/historico")
public class HistoricoController {

	private final MovimentacaoService movimentacaoService;

	public HistoricoController(MovimentacaoService movimentacaoService) {
		this.movimentacaoService = movimentacaoService;
	}

	@GetMapping
	public List<MovimentacaoResponse> listar(
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String order,
			@RequestParam(required = false) TipoIngrediente tipo,
			@RequestParam(required = false) OperacaoMovimentacao operacao,
			@RequestParam(required = false) StatusMovimentacao status,
			@RequestParam(required = false) Long compartimentoId,
			@RequestParam(required = false) String responsavel,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
		return movimentacaoService.listar(new HistoricoConsulta(
			sortBy,
			order,
			tipo,
			operacao,
			status,
			compartimentoId,
			responsavel,
			dataInicio,
			dataFim));
	}

}
