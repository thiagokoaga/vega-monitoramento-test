package dev.vegamonitoramento.vega_monitoramento_test.compartimento;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompartimentoSeeder implements ApplicationRunner {

	static final List<String> CODIGOS = List.of("C1", "C2", "C3", "C4", "C5");

	private final CompartimentoRepository compartimentoRepository;

	public CompartimentoSeeder(CompartimentoRepository compartimentoRepository) {
		this.compartimentoRepository = compartimentoRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		for (int indice = 0; indice < CODIGOS.size(); indice++) {
			long id = indice + 1L;
			if (!compartimentoRepository.existsById(id)) {
				compartimentoRepository.save(new Compartimento(id, CODIGOS.get(indice)));
			}
		}
	}

}
