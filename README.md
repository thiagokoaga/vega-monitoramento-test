# Armazém de ingredientes

API REST para controlar o estoque de uma fábrica de alimentos. Cinco compartimentos (C1 a C5) recebem ingredientes secos, líquidos ou refrigerados, respeitando capacidade, tipo e o dia da última entrada. Toda entrada e toda saída ficam no histórico, inclusive quando a operação é recusada.

## Como executar

Requisito: Java 21.

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco é H2 em memória: ao encerrar o processo, os dados somem e os compartimentos são recriados na próxima subida. O console do H2 fica em `http://localhost:8080/h2-console` (JDBC URL impressa no log, usuário `sa`, senha vazia).

Testes:

```bash
./mvnw test
```

O dashboard fica em `frontend/`. Com a API no ar, em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

A tela abre em `http://localhost:5173`. O menu lateral separa dashboard, nova entrada, saída e histórico. O tema claro ou escuro fica no rodapé do menu. O Vite encaminha `/ingredientes`, `/compartimentos` e `/historico` para `http://localhost:8080`.

## Exemplo rápido

```bash
curl -s -X POST http://localhost:8080/ingredientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Farinha de Trigo","tipo":"SECO","quantidade":400,"compartimentoId":1,"responsavel":"pedro"}'

curl -s http://localhost:8080/ingredientes/volume

curl -s "http://localhost:8080/compartimentos/disponiveis?quantidade=100&tipo=SECO"

curl -s "http://localhost:8080/compartimentos/disponiveis-para-venda?tipo=SECO"

curl -s -X POST http://localhost:8080/ingredientes/1/saida \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":50,"responsavel":"maria"}'

curl -s "http://localhost:8080/historico?sortBy=data&order=desc"
```

Os compartimentos nascem com id fixo: 1 = C1, 2 = C2, 3 = C3, 4 = C4, 5 = C5.

## Endpoints

| Método | Caminho | Função |
| --- | --- | --- |
| `POST` | `/ingredientes` | Entrada. Cria o ingrediente ou soma no existente |
| `GET` | `/ingredientes` | Lista ingredientes e o saldo atual |
| `GET` | `/ingredientes/{id}` | Consulta um ingrediente |
| `GET` | `/ingredientes/volume` | Total armazenado por tipo |
| `POST` | `/ingredientes/{id}/saida` | Saída de uma quantidade |
| `GET` | `/compartimentos/disponiveis?quantidade=&tipo=` | Compartimentos que cabem a quantidade e aceitam o tipo hoje |
| `GET` | `/compartimentos/disponiveis-para-venda?tipo=` | Compartimentos com saldo maior que zero daquele tipo |
| `GET` | `/historico` | Movimentações, com filtros e ordenação |

`GET /ingredientes/volume` responde no formato pedido no desafio:

```json
[
  { "type": "SECO", "totalQuantity": 400.000 },
  { "type": "LIQUIDO", "totalQuantity": 200.000 },
  { "type": "REFRIGERADO", "totalQuantity": 150.000 }
]
```

Os três tipos sempre aparecem. Tipo sem estoque volta com total zero. A ordem é `SECO`, `LIQUIDO`, `REFRIGERADO`.

Filtros de `GET /historico`: `sortBy` (`data`, `date` ou `compartimento`), `order` (`asc` ou `desc`), `tipo`, `operacao` (`ENTRADA` ou `SAIDA`), `status` (`SUCESSO` ou `FALHA`), `compartimentoId`, `responsavel`, `dataInicio` e `dataFim` (`yyyy-MM-dd`). O padrão é data decrescente e, no empate, código do compartimento crescente.

Erros de regra respondem `400` com `{ "status", "error", "message" }`. Recurso inexistente responde `404` no mesmo formato.

## Regras de negócio

- Tipos aceitos: `SECO` (600 kg), `LIQUIDO` (500 L) e `REFRIGERADO` (400 kg). A capacidade é do tipo, não uma coluna fixa do compartimento.
- Um compartimento guarda um tipo por vez. A troca só ocorre no dia civil seguinte e com saldo zero. Esvaziar no mesmo dia não libera outro tipo. O mesmo tipo pode entrar de novo no mesmo dia, se couber.
- "Dia seguinte" usa o calendário de `America/Sao_Paulo`, não um intervalo de 24 horas. A data considerada é a da entrada. A saída não renova essa trava.
- Compartimento nunca usado aceita qualquer tipo.
- O nome do ingrediente é comparado com igualdade, depois de remover espaços nas pontas. `Farinha` e `Farinha Integral` são ingredientes diferentes. Maiúsculas e minúsculas também diferenciam.
- Se o nome já existe, a nova chamada soma a quantidade no mesmo registro e grava outra entrada no histórico, com o responsável desta chamada.
- O mesmo nome com outro tipo é recusado. O ingrediente permanece no compartimento da primeira entrada; outro `compartimentoId` também é recusado.
- A saída não pode ser maior que o saldo. O ingrediente zerado continua cadastrado, vinculado ao compartimento original.
- Quantidades são normalizadas para três casas decimais, com arredondamento half-up. Valor que vira zero é recusado.
- Toda entrada e toda saída geram histórico. Falha de regra ou de recurso inexistente também gera histórico, com `status = FALHA`. O registro da falha é gravado numa transação separada, depois do rollback, para não ser desfeito junto com a operação. Pedido malformado (JSON inválido ou campo obrigatório ausente) não vira movimentação.

## Modelo

- `compartimento`: id, código (`C1`…`C5`), último tipo armazenado e data da última entrada.
- `ingrediente`: nome único, tipo, saldo atual e compartimento.
- `movimentacao`: data/hora, operação, status, quantidade, tipo, responsável, compartimento, ingrediente e metadata. É o log do armazém. Os campos usados em filtro e ordenação são colunas, não um JSON genérico. O responsável é o nome enviado na requisição (`"pedro"`), porque o desafio não pede cadastro de usuário.

A entrada e a saída travam o compartimento com lock pessimista antes de alterar o saldo. Duas entradas simultâneas no mesmo compartimento são serializadas e não ultrapassam a capacidade. O nome do ingrediente tem restrição única no banco para o caso de duas requisições criarem o mesmo nome ao mesmo tempo em compartimentos diferentes.

## Organização

```
compartimento   cadastro dos cinco compartimentos e consulta de espaço
ingrediente     saldo, entrada, saída e volume
movimentacao    histórico
erro            400 e 404 com mensagem explícita
tempo           relógio em America/Sao_Paulo, substituível nos testes
```

Controllers devolvem DTOs. A transação fica nos services (`@Transactional`). `open-in-view` está desligado.

## Decisões

- O `POST /ingredientes` é a entrada. A saída ficou em `POST /ingredientes/{id}/saida`, porque o enunciado exige operação de saída e não descrevia o endpoint.
- O histórico de falha não é gravado dentro da transação que dá rollback. Se fosse, o lock do compartimento e o insert do log se bloqueariam, e a falha desapareceria no rollback.
- Ingrediente com saldo zero permanece no banco. Assim o histórico continua apontando para ele e uma nova entrada do mesmo nome ainda sabe quem acrescentou o quê.
