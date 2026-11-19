package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/compartimentos")
public class CompartimentoController {

	private final CompartimentoService compartimentoService;

	public CompartimentoController(CompartimentoService compartimentoService) {
		this.compartimentoService = compartimentoService;
	}

	@GetMapping("/disponiveis")
	public List<CompartimentoDisponivelResponse> disponiveis(@RequestParam BigDecimal quantidade,
			@RequestParam TipoIngrediente tipo) {
		return compartimentoService.disponiveis(quantidade, tipo);
	}

	@GetMapping("/disponiveis-para-venda")
	public List<CompartimentoDisponivelResponse> disponiveisParaVenda(@RequestParam TipoIngrediente tipo) {
		return compartimentoService.disponiveisParaVenda(tipo);
	}

}
