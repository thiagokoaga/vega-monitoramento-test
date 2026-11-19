package dev.vegamonitoramento.vega_monitoramento_test.support;

import dev.vegamonitoramento.vega_monitoramento_test.tempo.Relogio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@org.springframework.boot.test.context.TestConfiguration
public class RelogioTestConfig {

	@Bean
	@Primary
	Relogio relogioAjustavel() {
		return new RelogioAjustavel();
	}

}
