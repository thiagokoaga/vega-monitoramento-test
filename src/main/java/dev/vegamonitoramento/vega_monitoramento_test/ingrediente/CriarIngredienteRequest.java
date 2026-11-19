package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarIngredienteRequest(
	@NotBlank(message = "Nome do ingrediente é obrigatório.")
	@Size(max = 160, message = "Nome do ingrediente deve ter no máximo 160 caracteres.")
	String nome,
	@NotNull(message = "Tipo do ingrediente é obrigatório.")
	TipoIngrediente tipo,
	@NotNull(message = "Quantidade é obrigatória.")
	@Positive(message = "Quantidade deve ser maior que zero.")
	BigDecimal quantidade,
	@NotNull(message = "Compartimento é obrigatório.")
	Long compartimentoId,
	@NotBlank(message = "Responsável é obrigatório.")
	@Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres.")
	String responsavel
) {
}
