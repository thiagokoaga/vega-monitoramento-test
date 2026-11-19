package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import dev.vegamonitoramento.vega_monitoramento_test.compartimento.Compartimento;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ingrediente")
public class Ingrediente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 160)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoIngrediente tipo;

	@Column(nullable = false, precision = 19, scale = 3)
	private BigDecimal quantidade;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "compartimento_id", nullable = false)
	private Compartimento compartimento;

	public Ingrediente(String nome, TipoIngrediente tipo, BigDecimal quantidade, Compartimento compartimento) {
		this.nome = nome;
		this.tipo = tipo;
		this.quantidade = quantidade;
		this.compartimento = compartimento;
	}

	public void adicionar(BigDecimal valor) {
		this.quantidade = this.quantidade.add(valor);
	}

	public void retirar(BigDecimal valor) {
		this.quantidade = this.quantidade.subtract(valor);
	}

}
