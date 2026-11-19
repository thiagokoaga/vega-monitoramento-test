package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ingredientes")
public class IngredienteController {

	private final IngredienteService ingredienteService;

	public IngredienteController(IngredienteService ingredienteService) {
		this.ingredienteService = ingredienteService;
	}

	@PostMapping
	public ResponseEntity<IngredienteResponse> criar(@Valid @RequestBody CriarIngredienteRequest request) {
		IngredienteResponse resposta = ingredienteService.cadastrar(request);
		return ResponseEntity.created(URI.create("/ingredientes/" + resposta.id())).body(resposta);
	}

	@GetMapping
	public List<IngredienteResponse> listar() {
		return ingredienteService.listar();
	}

	@GetMapping("/volume")
	public List<VolumePorTipoResponse> volume() {
		return ingredienteService.volumePorTipo();
	}

	@GetMapping("/{id}")
	public IngredienteResponse buscar(@PathVariable Long id) {
		return ingredienteService.buscar(id);
	}

	@PostMapping("/{id}/saida")
	public IngredienteResponse saida(@PathVariable Long id, @Valid @RequestBody SaidaIngredienteRequest request) {
		return ingredienteService.saida(id, request);
	}

}
