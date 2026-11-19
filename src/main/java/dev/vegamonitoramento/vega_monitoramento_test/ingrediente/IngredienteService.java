package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import dev.vegamonitoramento.vega_monitoramento_test.comum.Quantidades;
import dev.vegamonitoramento.vega_monitoramento_test.compartimento.Compartimento;
import dev.vegamonitoramento.vega_monitoramento_test.compartimento.CompartimentoRepository;
import dev.vegamonitoramento.vega_monitoramento_test.erro.RecursoNaoEncontradoException;
import dev.vegamonitoramento.vega_monitoramento_test.erro.RegraDeNegocioException;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.MovimentacaoService;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.OperacaoMovimentacao;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.RegistroMovimentacao;
import dev.vegamonitoramento.vega_monitoramento_test.movimentacao.StatusMovimentacao;
import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngredienteService {

	private final IngredienteRepository ingredienteRepository;
	private final CompartimentoRepository compartimentoRepository;
	private final MovimentacaoService movimentacaoService;
	private final Relogio relogio;

	@Transactional
	public IngredienteResponse cadastrar(CriarIngredienteRequest request) {
		String nome = request.nome().trim();
		String responsavel = request.responsavel().trim();
		TipoIngrediente tipo = request.tipo();
		BigDecimal quantidade = Quantidades.normalizar(request.quantidade());

		Compartimento compartimento = compartimentoRepository.findByIdComBloqueio(request.compartimentoId())
			.orElseThrow(() -> naoEncontrado(
				"Compartimento %d não encontrado.".formatted(request.compartimentoId()),
				falha(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, null, null,
					"Compartimento %d não encontrado.".formatted(request.compartimentoId()))));

		rejeitarQuantidadeInvalida(quantidade, OperacaoMovimentacao.ENTRADA, tipo, responsavel, compartimento.getId(), null);

		Ingrediente existente = ingredienteRepository.findByNomeComBloqueio(nome).orElse(null);
		if (existente != null) {
			if (existente.getTipo() != tipo) {
				String mensagem = "Já existe o ingrediente '%s' com o tipo %s. O tipo informado (%s) não confere."
					.formatted(nome, existente.getTipo(), tipo);
				throw new RegraDeNegocioException(mensagem,
					falha(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, compartimento.getId(), existente.getId(), mensagem));
			}
			if (!existente.getCompartimento().getId().equals(compartimento.getId())) {
				String mensagem = "O ingrediente '%s' já está vinculado ao compartimento %s. Nova entrada deve usar o compartimentoId %d."
					.formatted(nome, existente.getCompartimento().getCodigo(), existente.getCompartimento().getId());
				throw new RegraDeNegocioException(mensagem,
					falha(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, compartimento.getId(), existente.getId(), mensagem));
			}
			validarCapacidade(compartimento, tipo, quantidade, responsavel, existente.getId());
			existente.adicionar(quantidade);
			compartimento.registrarEntrada(tipo, relogio.hoje());
			movimentacaoService.registrarSucesso(
				sucesso(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, "Entrada acumulada no ingrediente existente"),
				compartimento,
				existente);
			return IngredienteResponse.de(existente);
		}

		validarCapacidade(compartimento, tipo, quantidade, responsavel, null);
		Ingrediente ingrediente = ingredienteRepository.save(new Ingrediente(nome, tipo, quantidade, compartimento));
		compartimento.registrarEntrada(tipo, relogio.hoje());
		movimentacaoService.registrarSucesso(
			sucesso(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, "Entrada inicial do ingrediente"),
			compartimento,
			ingrediente);
		return IngredienteResponse.de(ingrediente);
	}

	@Transactional
	public IngredienteResponse saida(Long id, SaidaIngredienteRequest request) {
		String responsavel = request.responsavel().trim();
		BigDecimal quantidade = Quantidades.normalizar(request.quantidade());

		Long compartimentoId = ingredienteRepository.findCompartimentoIdByIngredienteId(id)
			.orElseThrow(() -> naoEncontrado(
				"Ingrediente %d não encontrado.".formatted(id),
				falha(OperacaoMovimentacao.SAIDA, quantidade, null, responsavel, null, null,
					"Ingrediente %d não encontrado.".formatted(id))));

		Compartimento compartimento = compartimentoRepository.findByIdComBloqueio(compartimentoId)
			.orElseThrow(() -> naoEncontrado("Compartimento %d não encontrado.".formatted(compartimentoId)));

		rejeitarQuantidadeInvalida(quantidade, OperacaoMovimentacao.SAIDA, null, responsavel, compartimento.getId(), id);

		Ingrediente ingrediente = ingredienteRepository.findByIdComBloqueio(id)
			.orElseThrow(() -> naoEncontrado(
				"Ingrediente %d não encontrado.".formatted(id),
				falha(OperacaoMovimentacao.SAIDA, quantidade, null, responsavel, compartimento.getId(), null,
					"Ingrediente %d não encontrado.".formatted(id))));

		if (ingrediente.getQuantidade().compareTo(quantidade) < 0) {
			String mensagem = "Saldo insuficiente do ingrediente '%s'. Saldo atual: %s, quantidade solicitada: %s."
				.formatted(ingrediente.getNome(), Quantidades.normalizar(ingrediente.getQuantidade()), quantidade);
			throw new RegraDeNegocioException(mensagem,
				falha(OperacaoMovimentacao.SAIDA, quantidade, ingrediente.getTipo(), responsavel, compartimento.getId(), ingrediente.getId(), mensagem));
		}

		ingrediente.retirar(quantidade);
		movimentacaoService.registrarSucesso(
			sucesso(OperacaoMovimentacao.SAIDA, quantidade, ingrediente.getTipo(), responsavel, "Saída de ingrediente"),
			compartimento,
			ingrediente);
		return IngredienteResponse.de(ingrediente);
	}

	@Transactional(readOnly = true)
	public List<IngredienteResponse> listar() {
		return ingredienteRepository.findAllComCompartimento().stream()
			.map(IngredienteResponse::de)
			.toList();
	}

	@Transactional(readOnly = true)
	public IngredienteResponse buscar(Long id) {
		Ingrediente ingrediente = ingredienteRepository.findById(id)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ingrediente %d não encontrado.".formatted(id)));
		return IngredienteResponse.de(ingrediente);
	}

	@Transactional(readOnly = true)
	public List<VolumePorTipoResponse> volumePorTipo() {
		Map<TipoIngrediente, BigDecimal> totais = new EnumMap<>(TipoIngrediente.class);
		for (TipoIngrediente tipo : TipoIngrediente.values()) {
			totais.put(tipo, Quantidades.zeroSeNulo(BigDecimal.ZERO));
		}
		for (Object[] linha : ingredienteRepository.somarPorTipo()) {
			TipoIngrediente tipo = (TipoIngrediente) linha[0];
			totais.put(tipo, Quantidades.zeroSeNulo(new BigDecimal(linha[1].toString())));
		}
		return totais.entrySet().stream()
			.map(entrada -> new VolumePorTipoResponse(entrada.getKey(), entrada.getValue()))
			.toList();
	}

	private void validarCapacidade(Compartimento compartimento, TipoIngrediente tipo, BigDecimal quantidade,
			String responsavel, Long ingredienteId) {
		BigDecimal saldo = Quantidades.zeroSeNulo(ingredienteRepository.somarSaldoPorCompartimento(compartimento.getId()));
		try {
			compartimento.validarEntrada(tipo, quantidade, saldo, relogio.hoje());
		}
		catch (RegraDeNegocioException ex) {
			throw new RegraDeNegocioException(ex.getMessage(),
				falha(OperacaoMovimentacao.ENTRADA, quantidade, tipo, responsavel, compartimento.getId(), ingredienteId, ex.getMessage()));
		}
	}

	private void rejeitarQuantidadeInvalida(BigDecimal quantidade, OperacaoMovimentacao operacao, TipoIngrediente tipo,
			String responsavel, Long compartimentoId, Long ingredienteId) {
		if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			String mensagem = "Quantidade deve ser maior que zero.";
			throw new RegraDeNegocioException(mensagem,
				falha(operacao, quantidade, tipo, responsavel, compartimentoId, ingredienteId, mensagem));
		}
	}

	private RegistroMovimentacao sucesso(OperacaoMovimentacao operacao, BigDecimal quantidade, TipoIngrediente tipo,
			String responsavel, String metadata) {
		return new RegistroMovimentacao(operacao, StatusMovimentacao.SUCESSO, quantidade, tipo, responsavel, null, null, metadata);
	}

	private RegistroMovimentacao falha(OperacaoMovimentacao operacao, BigDecimal quantidade, TipoIngrediente tipo,
			String responsavel, Long compartimentoId, Long ingredienteId, String metadata) {
		return new RegistroMovimentacao(operacao, StatusMovimentacao.FALHA, quantidade, tipo, responsavel, compartimentoId, ingredienteId, metadata);
	}

	private RecursoNaoEncontradoException naoEncontrado(String mensagem, RegistroMovimentacao registro) {
		return new RecursoNaoEncontradoException(mensagem, registro);
	}

	private RecursoNaoEncontradoException naoEncontrado(String mensagem) {
		return new RecursoNaoEncontradoException(mensagem);
	}

}
