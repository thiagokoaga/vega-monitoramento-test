package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SaidaIngredienteRequest(
	@NotNull(message = "Quantidade é obrigatória.")
	@Positive(message = "Quantidade deve ser maior que zero.")
	BigDecimal quantidade,
	@NotBlank(message = "Responsável é obrigatório.")
	@Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres.")
	String responsavel
) {
}
