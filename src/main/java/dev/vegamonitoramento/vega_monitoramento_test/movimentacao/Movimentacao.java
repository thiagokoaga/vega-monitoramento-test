package dev.vegamonitoramento.vega_monitoramento_test.movimentacao;

import dev.vegamonitoramento.vega_monitoramento_test.compartimento.Compartimento;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.Ingrediente;
import dev.vegamonitoramento.vega_monitoramento_test.ingrediente.TipoIngrediente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "movimentacao")
public class Movimentacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime dataHora;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OperacaoMovimentacao operacao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusMovimentacao status;

	@Column(nullable = false, precision = 19, scale = 3)
	private BigDecimal quantidade;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TipoIngrediente tipo;

	@Column(nullable = false, length = 120)
	private String responsavel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "compartimento_id")
	private Compartimento compartimento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ingrediente_id")
	private Ingrediente ingrediente;

	@Column(length = 500)
	private String metadata;

	public Movimentacao(LocalDateTime dataHora, OperacaoMovimentacao operacao, StatusMovimentacao status,
			BigDecimal quantidade, TipoIngrediente tipo, String responsavel, Compartimento compartimento,
			Ingrediente ingrediente, String metadata) {
		this.dataHora = dataHora;
		this.operacao = operacao;
		this.status = status;
		this.quantidade = quantidade;
		this.tipo = tipo;
		this.responsavel = responsavel;
		this.compartimento = compartimento;
		this.ingrediente = ingrediente;
		this.metadata = metadata;
	}

}
