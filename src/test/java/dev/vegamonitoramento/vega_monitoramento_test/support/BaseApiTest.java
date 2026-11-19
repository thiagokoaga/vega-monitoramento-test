package dev.vegamonitoramento.vega_monitoramento_test.support;

import com.jayway.jsonpath.JsonPath;
import dev.vegamonitoramento.vega_monitoramento_test.compartimento.CompartimentoRepository;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.IngredienteRepository;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.MovimentacaoRepository;
import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseApiTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected Relogio relogio;

	@Autowired
	private MovimentacaoRepository movimentacaoRepository;

	@Autowired
	private IngredienteRepository ingredienteRepository;

	@Autowired
	private CompartimentoRepository compartimentoRepository;

	@BeforeEach
	void limparCenario() {
		movimentacaoRepository.deleteAll();
		ingredienteRepository.deleteAll();
		compartimentoRepository.findAll().forEach(compartimento -> {
			compartimento.limparUso();
			compartimentoRepository.save(compartimento);
		});
		((RelogioAjustavel) relogio).fixar(LocalDateTime.of(2026, 11, 18, 10, 0));
	}

	protected long entrar(String nome, String tipo, String quantidade, long compartimentoId, String responsavel) throws Exception {
		String corpo = mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson(nome, tipo, quantidade, compartimentoId, responsavel)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString(StandardCharsets.UTF_8);
		return ((Number) JsonPath.read(corpo, "$.id")).longValue();
	}

	protected String entradaJson(String nome, String tipo, String quantidade, long compartimentoId, String responsavel) {
		return """
			{"nome":"%s","tipo":"%s","quantidade":%s,"compartimentoId":%d,"responsavel":"%s"}
			""".formatted(nome, tipo, quantidade, compartimentoId, responsavel);
	}

	protected String saidaJson(String quantidade, String responsavel) {
		return """
			{"quantidade":%s,"responsavel":"%s"}
			""".formatted(quantidade, responsavel);
	}

	protected void fixarRelogio(LocalDateTime dataHora) {
		((RelogioAjustavel) relogio).fixar(dataHora);
	}

}
