package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import dev.vegamonitoramento.vega_monitoramento_test.erro.RegraDeNegocioException;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "compartimento")
public class Compartimento {

	@Id
	private Long id;

	@Column(nullable = false, unique = true, length = 8)
	private String codigo;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TipoIngrediente tipoAtual;

	private LocalDate dataUltimaEntrada;

	public Compartimento(Long id, String codigo) {
		this.id = id;
		this.codigo = codigo;
	}

	public void registrarEntrada(TipoIngrediente tipo, LocalDate data) {
		this.tipoAtual = tipo;
		this.dataUltimaEntrada = data;
	}

	public void limparUso() {
		this.tipoAtual = null;
		this.dataUltimaEntrada = null;
	}

	public void validarEntrada(TipoIngrediente tipo, BigDecimal quantidade, BigDecimal saldo, LocalDate hoje) {
		BigDecimal saldoAtual = saldo == null ? BigDecimal.ZERO : saldo;
		if (tipoAtual != null && tipoAtual != tipo) {
			boolean vazio = saldoAtual.compareTo(BigDecimal.ZERO) <= 0;
			boolean diaSeguinte = dataUltimaEntrada == null || dataUltimaEntrada.isBefore(hoje);
			if (!vazio || !diaSeguinte) {
				throw new RegraDeNegocioException(
					"O compartimento %s está vinculado ao tipo %s. A troca para %s só pode ocorrer no dia seguinte e com o compartimento vazio."
						.formatted(codigo, tipoAtual, tipo));
			}
		}

		BigDecimal livre = tipo.getCapacidade().subtract(saldoAtual);
		if (livre.compareTo(quantidade) < 0) {
			throw new RegraDeNegocioException(
				"Capacidade insuficiente no compartimento %s para o tipo %s. Espaço livre: %s %s, quantidade solicitada: %s %s."
					.formatted(
						codigo,
						tipo,
						livre.setScale(3, RoundingMode.HALF_UP),
						tipo.getUnidade(),
						quantidade.setScale(3, RoundingMode.HALF_UP),
						tipo.getUnidade()));
		}
	}

	public boolean aceitaEntrada(TipoIngrediente tipo, BigDecimal quantidade, BigDecimal saldo, LocalDate hoje) {
		try {
			validarEntrada(tipo, quantidade, saldo, hoje);
			return true;
		}
		catch (RegraDeNegocioException ex) {
			return false;
		}
	}

}
