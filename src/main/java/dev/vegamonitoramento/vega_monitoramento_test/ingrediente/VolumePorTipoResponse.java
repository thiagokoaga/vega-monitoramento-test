package dev.vegamonitoramento.vega_monitoramento_test.ingrediente;

import java.math.BigDecimal;

public record VolumePorTipoResponse(TipoIngrediente type, BigDecimal totalQuantity) {
}
