package dev.vegamonitoramento.vega_monitoramento_test.tempo;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class RelogioSistema implements Relogio {

	public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

	@Override
	public LocalDateTime agora() {
		return LocalDateTime.now(ZONA);
	}

	@Override
	public LocalDate hoje() {
		return LocalDate.now(ZONA);
	}

}
