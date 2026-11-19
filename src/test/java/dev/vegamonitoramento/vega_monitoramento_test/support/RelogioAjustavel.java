package dev.vegamonitoramento.vega_monitoramento_test.support;

import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class RelogioAjustavel implements Relogio {

	private final ZoneId zona = ZoneId.of("America/Sao_Paulo");
	private Clock clock = relogioPadrao();

	public void fixar(LocalDateTime dataHora) {
		clock = Clock.fixed(dataHora.atZone(zona).toInstant(), zona);
	}

	public void reiniciar() {
		clock = relogioPadrao();
	}

	@Override
	public LocalDateTime agora() {
		return LocalDateTime.now(clock);
	}

	@Override
	public LocalDate hoje() {
		return LocalDate.now(clock);
	}

	private Clock relogioPadrao() {
		return Clock.fixed(LocalDateTime.of(2026, 11, 18, 10, 0).atZone(zona).toInstant(), zona);
	}

}
