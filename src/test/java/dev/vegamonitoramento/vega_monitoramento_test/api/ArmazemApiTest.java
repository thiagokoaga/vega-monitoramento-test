package dev.vegamonitoramento.vega_monitoramento_test.api;

import dev.vegamonitoramento.vega_monitoramento_test.support.ApiTest;
import dev.vegamonitoramento.vega_monitoramento_test.support.BaseApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
class ArmazemApiTest extends BaseApiTest {

	@Test
	void cadastraIngredienteERegistraEntrada() throws Exception {
		long id = entrar("Farinha de Trigo", "SECO", "400", 1, "pedro");

		mockMvc.perform(get("/ingredientes/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Farinha de Trigo"))
			.andExpect(jsonPath("$.tipo").value("SECO"))
			.andExpect(jsonPath("$.quantidade").value(400))
			.andExpect(jsonPath("$.compartimentoId").value(1))
			.andExpect(jsonPath("$.compartimentoCodigo").value("C1"));

		mockMvc.perform(get("/historico").param("status", "SUCESSO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].operacao").value("ENTRADA"))
			.andExpect(jsonPath("$[0].responsavel").value("pedro"))
			.andExpect(jsonPath("$[0].compartimentoCodigo").value("C1"))
			.andExpect(jsonPath("$[0].metadata").value("Entrada inicial do ingrediente"));
	}

	@Test
	void nomeIgualAcumulaNoMesmoIngredienteEGuardaOResponsavel() throws Exception {
		long id = entrar("Farinha de Trigo", "SECO", "100", 1, "pedro");
		entrar("Farinha de Trigo", "SECO", "50", 1, "maria");

		mockMvc.perform(get("/ingredientes/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.quantidade").value(150));

		mockMvc.perform(get("/ingredientes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/historico").param("operacao", "ENTRADA").param("order", "asc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].responsavel").value("pedro"))
			.andExpect(jsonPath("$[0].metadata").value("Entrada inicial do ingrediente"))
			.andExpect(jsonPath("$[1].responsavel").value("maria"))
			.andExpect(jsonPath("$[1].metadata").value("Entrada acumulada no ingrediente existente"));
	}

	@Test
	void nomeParecidoNaoEAgrupado() throws Exception {
		entrar("Farinha", "SECO", "10", 1, "pedro");
		entrar("Farinha Integral", "SECO", "15", 1, "pedro");

		mockMvc.perform(get("/ingredientes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void rejeitaMesmoNomeComOutroTipoERegistraFalha() throws Exception {
		long id = entrar("Farinha de Trigo", "SECO", "10", 1, "pedro");

		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Farinha de Trigo", "REFRIGERADO", "5", 1, "ana")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(
				"Já existe o ingrediente 'Farinha de Trigo' com o tipo SECO. O tipo informado (REFRIGERADO) não confere."));

		mockMvc.perform(get("/ingredientes/{id}", id))
			.andExpect(jsonPath("$.tipo").value("SECO"))
			.andExpect(jsonPath("$.quantidade").value(10));

		mockMvc.perform(get("/historico").param("status", "FALHA"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].operacao").value("ENTRADA"))
			.andExpect(jsonPath("$[0].responsavel").value("ana"));
	}

	@Test
	void rejeitaMesmoNomeEmOutroCompartimento() throws Exception {
		entrar("Farinha de Trigo", "SECO", "10", 1, "pedro");

		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Farinha de Trigo", "SECO", "5", 2, "ana")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(
				"O ingrediente 'Farinha de Trigo' já está vinculado ao compartimento C1. Nova entrada deve usar o compartimentoId 1."));
	}

	@Test
	void rejeitaCapacidadeInsuficienteSemGravarSaldo() throws Exception {
		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Acucar", "SECO", "601", 1, "pedro")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Capacidade insuficiente")));

		mockMvc.perform(get("/ingredientes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(get("/historico").param("status", "FALHA"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].quantidade").value(601));
	}

	@Test
	void trocaDeTipoSoNoDiaSeguinteEComCompartimentoVazio() throws Exception {
		long leite = entrar("Leite", "LIQUIDO", "100", 2, "pedro");

		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Acucar", "SECO", "10", 2, "pedro")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("compartimento vazio")));

		mockMvc.perform(post("/ingredientes/{id}/saida", leite)
				.contentType(MediaType.APPLICATION_JSON)
				.content(saidaJson("100", "pedro")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.quantidade").value(0));

		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Acucar", "SECO", "10", 2, "pedro")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("dia seguinte")));

		fixarRelogio(LocalDateTime.of(2026, 11, 19, 9, 0));

		entrar("Acucar", "SECO", "10", 2, "pedro");

		mockMvc.perform(get("/compartimentos/disponiveis-para-venda").param("tipo", "SECO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].codigo").value("C2"))
			.andExpect(jsonPath("$[0].saldo").value(10));
	}

	@Test
	void trocaNoDiaSeguinteContinuaBloqueadaEnquantoHouverSaldo() throws Exception {
		entrar("Leite", "LIQUIDO", "80", 3, "pedro");
		fixarRelogio(LocalDateTime.of(2026, 11, 19, 9, 0));

		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Queijo", "REFRIGERADO", "10", 3, "pedro")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("compartimento vazio")));
	}

	@Test
	void saidaNaoPodeUltrapassarOSaldo() throws Exception {
		long id = entrar("Oleo", "LIQUIDO", "20", 4, "pedro");

		mockMvc.perform(post("/ingredientes/{id}/saida", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(saidaJson("25", "maria")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Saldo insuficiente")));

		mockMvc.perform(get("/ingredientes/{id}", id))
			.andExpect(jsonPath("$.quantidade").value(20));

		mockMvc.perform(get("/historico").param("operacao", "SAIDA").param("status", "FALHA"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].responsavel").value("maria"));
	}

	@Test
	void consultaVolumePorTipo() throws Exception {
		entrar("Farinha", "SECO", "400", 1, "pedro");
		entrar("Oleo", "LIQUIDO", "200", 2, "pedro");
		entrar("Queijo", "REFRIGERADO", "150", 3, "pedro");

		mockMvc.perform(get("/ingredientes/volume"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].type").value("SECO"))
			.andExpect(jsonPath("$[0].totalQuantity").value(400))
			.andExpect(jsonPath("$[1].type").value("LIQUIDO"))
			.andExpect(jsonPath("$[1].totalQuantity").value(200))
			.andExpect(jsonPath("$[2].type").value("REFRIGERADO"))
			.andExpect(jsonPath("$[2].totalQuantity").value(150));
	}

	@Test
	void listaCompartimentosComEspacoEComSaldoParaVenda() throws Exception {
		entrar("Farinha", "SECO", "500", 1, "pedro");

		mockMvc.perform(get("/compartimentos/disponiveis").param("quantidade", "100").param("tipo", "SECO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.codigo == 'C1')].espacoLivre").value(org.hamcrest.Matchers.hasItem(100.0)))
			.andExpect(jsonPath("$[*].codigo", org.hamcrest.Matchers.hasItem("C2")));

		mockMvc.perform(get("/compartimentos/disponiveis").param("quantidade", "200").param("tipo", "SECO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].codigo", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("C1"))));

		mockMvc.perform(get("/compartimentos/disponiveis").param("quantidade", "10").param("tipo", "LIQUIDO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].codigo", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("C1"))))
			.andExpect(jsonPath("$[*].codigo", org.hamcrest.Matchers.hasItem("C2")));

		mockMvc.perform(get("/compartimentos/disponiveis-para-venda").param("tipo", "SECO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].codigo").value("C1"))
			.andExpect(jsonPath("$[0].saldo").value(500));

		mockMvc.perform(get("/compartimentos/disponiveis-para-venda").param("tipo", "LIQUIDO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void historicoOrdenaEFiltra() throws Exception {
		fixarRelogio(LocalDateTime.of(2026, 11, 18, 8, 0));
		entrar("Farinha", "SECO", "10", 1, "pedro");
		fixarRelogio(LocalDateTime.of(2026, 11, 18, 12, 0));
		entrar("Oleo", "LIQUIDO", "10", 2, "maria");

		mockMvc.perform(get("/historico").param("status", "SUCESSO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].compartimentoCodigo").value("C2"))
			.andExpect(jsonPath("$[1].compartimentoCodigo").value("C1"));

		mockMvc.perform(get("/historico").param("status", "SUCESSO").param("sortBy", "date").param("order", "asc"))
			.andExpect(jsonPath("$[0].compartimentoCodigo").value("C1"))
			.andExpect(jsonPath("$[1].compartimentoCodigo").value("C2"));

		mockMvc.perform(get("/historico").param("status", "SUCESSO").param("sortBy", "compartimento").param("order", "desc"))
			.andExpect(jsonPath("$[0].compartimentoCodigo").value("C2"))
			.andExpect(jsonPath("$[1].compartimentoCodigo").value("C1"));

		mockMvc.perform(get("/historico").param("responsavel", "maria").param("tipo", "LIQUIDO").param("compartimentoId", "2"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].ingredienteNome").value("Oleo"));

		mockMvc.perform(get("/historico").param("dataInicio", "2026-11-19"))
			.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(get("/historico").param("sortBy", "nome"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void compartimentoInexistenteRetorna404() throws Exception {
		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Sal", "SECO", "1", 99, "pedro")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Compartimento 99 não encontrado."));

		mockMvc.perform(get("/historico").param("status", "FALHA"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].metadata").value("Compartimento 99 não encontrado."));
	}

	@Test
	void tipoInvalidoRetorna400() throws Exception {
		mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson("Sal", "SECOS", "1", 1, "pedro")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("SECO, LIQUIDO, REFRIGERADO")));
	}

	@Test
	void entradasSimultaneasNoMesmoCompartimentoNaoEstouramCapacidade() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch inicio = new CountDownLatch(1);
		try {
			Future<Integer> primeira = executor.submit(() -> postarEntrada(inicio, "Farinha A"));
			Future<Integer> segunda = executor.submit(() -> postarEntrada(inicio, "Farinha B"));
			inicio.countDown();
			int statusPrimeira = primeira.get(20, TimeUnit.SECONDS);
			int statusSegunda = segunda.get(20, TimeUnit.SECONDS);
			assertTrue((statusPrimeira == 201 && statusSegunda == 400) || (statusPrimeira == 400 && statusSegunda == 201),
				"Status recebidos: " + statusPrimeira + " e " + statusSegunda);
		}
		finally {
			executor.shutdownNow();
		}

		mockMvc.perform(get("/ingredientes/volume"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].type").value("SECO"))
			.andExpect(jsonPath("$[0].totalQuantity").value(400));

		mockMvc.perform(get("/historico"))
			.andExpect(jsonPath("$.length()").value(2));
	}

	private int postarEntrada(CountDownLatch inicio, String nome) throws Exception {
		inicio.await(5, TimeUnit.SECONDS);
		return mockMvc.perform(post("/ingredientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(entradaJson(nome, "SECO", "400", 1, "pedro")))
			.andReturn()
			.getResponse()
			.getStatus();
	}

}
