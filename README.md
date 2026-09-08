# Checkpoint 4 - Bug Hunt StreamFIAP

## Identificação

**Grupo:** Arthur da Silva Alencar, Felipe Paula Burba Molonhoni, Lucas de Freitas Barbosa e Pedro Del Neri Correia

| Integrante | RM | Turma |
|---|---|---|
| Arthur da Silva Alencar | 563684 | 2CCPX |
| Felipe Paula Burba Molonhoni | 564395 | 2CCPX |
| Lucas de Freitas Barbosa | 564685 | 2CCPX |
| Pedro Del Neri Correia | 562168 | 2CCPX |

| Campo | Total confirmado |
|---|---|
| Total de bugs corrigidos | 12 / 12 |
| Total de ajustes de Clean Code | 6 / 6 |

## Parte 1 - Bugs encontrados

| # | Sintoma do código original | Causa raiz (arquivo e localização) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | A promoção de um filme de estreia retornava 17,88, embora o aluguel custasse 14,90. | `Filme.java`: `aplicarPromocao` multiplicava o preço por 1.2 e aumentava o valor em 20%. | Alteramos a regra para aplicar 20% de desconto, mantendo 80% do preço. | Interface `Promocionavel` e regra de negócio (Aula 9). |
| bug02 | Buscar um conteúdo inexistente retornava HTTP 200 com corpo vazio. | `ConteudoController.java`: `buscarPorId` capturava a exceção e não a propagava. | Removemos o `catch` vazio para o `GlobalExceptionHandler` retornar HTTP 404 com mensagem. | Propagação e tratamento específico de exceções (Aula 11). |
| bug03 | A consulta por categoria não encontrava conteúdos cadastrados com o mesmo texto. | `ConteudoController.java`: a comparação de `String` era feita com `==`. | Passamos a usar `findByCategoria` do repository. | Igualdade de Strings e consultas derivadas do Spring Data (Aula 13). |
| bug04 | Conteúdos com duração zero ou negativa eram aceitos e salvos. | `Conteudo.java` aceitava qualquer duração e o erro ainda não era traduzido corretamente para HTTP. | Validamos a duração no setter/construtor e tratamos o erro como HTTP 400. | Construtores, setters, fail fast e exceções (Aulas 3, 4, 11 e 13). |
| bug05 | A série perdia título, categoria, duração, classificação e disponibilidade no cadastro. | `Serie.java`: o construtor não inicializava corretamente os atributos herdados de `Conteudo`. | Chamamos `super(...)` com os dados recebidos e preservamos `disponivel`. | Herança e encadeamento de construtores (Aulas 4 e 6). |
| bug06 | Uma série de 5 temporadas usava o preço herdado de 9,90 e gerava promoção de 7,92. | `Serie.java`: existia uma sobrecarga `calcularPrecoAluguel(double)` em vez de sobrescrever o método sem parâmetros. | Removemos o parâmetro, adicionamos `@Override` e calculamos 4,90 por temporada. | Sobrescrita, sobrecarga e polimorfismo (Aula 7). |
| bug07 | O documentário herdava cobrança de 9,90. | `Documentario.java` não sobrescrevia `calcularPrecoAluguel`. | Sobrescrevemos o método para retornar 0,00, mantendo a classe fora de `Promocionavel`. | Polimorfismo, abstração e interface (Aulas 7, 8 e 9). |
| bug08 | O cadastro de usuário sem ID falhava na persistência. | `Usuario.java`: havia `@Id`, mas não geração automática de chave. | Adicionamos `@GeneratedValue(strategy = GenerationType.IDENTITY)`. | JPA, chave primária e IDENTITY (Aulas 12 e 13). |
| bug09 | Depois da geração do ID, o nome do usuário era salvo como nulo. | `Usuario.java`: o construtor fazia `nome = nome`, atribuindo o parâmetro a ele mesmo. | Corrigimos para `this.nome = nome`. | Construtores, escopo e palavra-chave `this` (Aula 4). |
| bug10 | A regra de créditos estava invertida: usuários com saldo suficiente podiam ser recusados e usuários sem saldo podiam prosseguir. | `Usuario.java`: `temCreditosSuficientes` comparava preço >= saldo. | Corrigimos para saldo >= preço e mantivemos a validação antes do débito. | Condições, regra de negócio e proteção de estado (Aulas 3 e 11). |
| bug11 | Um conteúdo indisponível ainda podia ser alugado. | `Usuario.java`: `alugar` não validava `isDisponivel()` antes da operação. | Adicionamos validação fail fast e lançamos `ConteudoIndisponivelException`, mapeada para HTTP 409. | Fail fast e exceções customizadas (Aula 11). |
| bug12 | Um usuário abaixo da classificação indicativa recebia erro genérico HTTP 500. | `GlobalExceptionHandler.java`: não existia tratamento específico para `ClassificacaoIndicativaException`. | Adicionamos `@ExceptionHandler` retornando HTTP 422 e a mensagem da exceção. | Checked/unchecked e tradução de exceções para HTTP (Aulas 11 e 13). |

### Evidências reais dos testes

Os IDs foram lidos dinamicamente das respostas de cadastro durante cada execução. Como o H2 é em memória, os IDs mudam após reiniciar a aplicação. No Oracle também foram usados os novos IDs devolvidos pela própria API.

| Bug | Requisição/dados usados | Resultado antes | Resultado depois | Banco e evidência |
|---|---|---|---|---|
| bug01 | Filme de estreia, `GET /api/conteudos/{id}/preco-promocional` | Aproximadamente 17,88 | 11,92 | H2: confirmado no teste individual e novamente nos testes finais. |
| bug02 | `GET /api/conteudos/999999999` | 200 com corpo vazio | 404 com mensagem de conteúdo não encontrado | H2: confirmado nos testes finais. |
| bug03 | Conteúdos `FICCAO` e `NATUREZA`, `GET /api/conteudos/categoria/FICCAO` | Lista vazia/incorreta | 200 com filmes e série `FICCAO` | H2: confirmado nos testes finais. |
| bug04 | Filme, série e documentário com duração `0` e `-5` | Cadastro inválido era aceito | Todos retornaram 400 e nenhum `CP4_INVALIDO_` foi salvo | H2: seção F completa aprovada. |
| bug05 | Série `CP4 Serie`, 5 temporadas, disponível | Dados herdados eram perdidos | 201 e GET com título, categoria, duração, classificação, disponibilidade e temporadas preservados | H2: confirmado; Oracle: cadastro da série também retornou 201. |
| bug06 | Série com 5 temporadas, preço promocional | 7,92 | 19,60 | H2: confirmado no teste individual e novamente nos testes finais. |
| bug07 | Documentário, consulta de preço | 9,90 | 0,0 | H2: confirmado no teste individual e novamente nos testes finais. |
| bug08 | `POST /api/usuarios` com `CP4 Adulto` | Erro de persistência por ID nulo | 201 com ID numérico gerado | H2: confirmado; Oracle: os cadastros de usuários da seção A retornaram 201. |
| bug09 | Cadastro de `CP4 Adulto` | Nome nulo | Nome `CP4 Adulto` salvo corretamente | H2: GET retornou nome correto; Oracle: cadastro retornou 201 com os dados enviados. |
| bug10 | Usuário com 0 créditos e adulto com 100 créditos | Comparação invertida | Sem saldo: 422 e saldo 0; aluguel válido: saldo 85,10 após filme de estreia | H2: seções C e D aprovadas. |
| bug11 | Adulto + documentário indisponível e repetição de aluguel | Conteúdo indisponível podia ser alugado | 409; saldo preservado; segundo aluguel do filme também retornou 409 | H2: seções C e D aprovadas. |
| bug12 | Usuário de 12 anos + filme classificação 14 | 500 genérico | 422 com mensagem de classificação; saldo 100 e filme ainda disponível | H2: seção C aprovada. |

### Conferência adicional do contrato em H2

Os testes finais das seções A até F foram executados em H2 e concluídos com os resultados esperados:

- todos os cadastros válidos retornaram HTTP 201;
- consultas de usuário e conteúdo retornaram HTTP 200;
- categoria `FICCAO` retornou somente os conteúdos esperados e categoria inexistente retornou lista vazia;
- conteúdo inexistente retornou HTTP 404;
- preços promocionais confirmados: filme estreia 11,92; filme comum 7,92; série 19,60; documentário 0,0;
- recusas confirmadas: créditos insuficientes 422, classificação indicativa 422 e conteúdo indisponível 409;
- nenhuma recusa alterou saldo ou disponibilidade indevidamente;
- aluguéis válidos debitaram o preço normal, resultando no saldo 50,70 após filme estreia, filme comum e série;
- usuário com zero créditos alugou documentário gratuito mantendo saldo 0;
- saldo exatamente igual ao preço e idade igual à classificação foram aceitos, deixando saldo 0;
- todos os cadastros com duração zero ou negativa retornaram 400 e não foram persistidos.

### Validação no Oracle FIAP

A aplicação foi iniciada com o perfil `StreamFIAP - Oracle FIAP`, utilizando as credenciais apenas como variáveis de ambiente da execução. A conexão iniciou sem erros de Oracle, a API respondeu HTTP 200 em `GET /api/conteudos` e todos os cadastros da seção A foram repetidos no Oracle com HTTP 201. As seções B até G não foram repetidas no Oracle por limitação de tempo; portanto, não registramos como executadas evidências que não foram verificadas.

## Parte 2 - Ajustes de Clean Code

| # | Onde estava | Princípio/boa prática | O que mudou |
|---|---|---|---|
| clean01 | `Usuario.java`: comentário dizia que o método adicionava créditos, embora ele debitasse. | Comentários coerentes e código autoexplicativo. | Removemos o comentário incorreto. |
| clean02 | `Usuario.java`: parâmetros e variáveis como `c` e `p` não deixavam clara a intenção do método. | Nomes significativos. | Renomeamos para `conteudo` e `precoAluguel`, sem alterar a regra. |
| clean03 | `Conteudo.java` e `ConteudoController.java`: duração era pública e acessada diretamente. | Encapsulamento (Aula 3). | Tornamos `duracaoMinutos` privado e passamos a usar `getDuracaoMinutos()`. |
| clean04 | `Usuario.java`: `alugar` misturava validação, débito e impressão do recibo. | Métodos pequenos e separação de responsabilidades. | Extraímos a impressão para `imprimirRecibo`, preservando o comportamento. |
| clean05 | `Filme.java`, `Serie.java`, `Promocionavel.java` e `Conteudo.java`: preços e desconto apareciam como números mágicos. | Constantes, legibilidade e redução de duplicação. | Criamos constantes nomeadas e compartilhamos `PERCENTUAL_DESCONTO`. |
| clean06 | `ConteudoController.java`: havia método antigo sem uso e bloco de cupons comentado. | Remoção de código morto e uso do controle de versão. | Removemos o código antigo; o histórico permanece no Git. |

## Parte 3 - Perguntas de reflexão

### 1. Injeção de dependência (Aula 13)

No `ConteudoController`, o `ConteudoRepository` é recebido pelo Spring por injeção de dependência.  
Como ele é uma interface, não faria sentido tentar criar diretamente um `new ConteudoRepository()`.  
O Spring Data gera a implementação necessária e registra esse objeto no contexto da aplicação.  
Com isso, o controller consegue usar métodos como `save`, `findById` e `findByCategoria` sem implementar acesso ao banco manualmente.  
Essa abordagem também evita que o controller fique responsável por criar e configurar as dependências de persistência.  
No projeto mantivemos esse padrão e usamos o próprio repository na correção do `bug03`.

### 2. JDBC vs Spring Data JPA (Aulas 12 e 13)

Com JDBC, normalmente é necessário abrir a conexão, escrever SQL, preencher parâmetros e transformar o `ResultSet` em objetos.  
Também é necessário ter mais cuidado com o fechamento de recursos e o tratamento dos erros de acesso ao banco.  
No StreamFIAP, o Spring Data JPA reduz bastante esse código porque os repositories estendem `JpaRepository`.  
O Hibernate faz o mapeamento entre as entidades Java e as tabelas do banco, enquanto o Spring Data fornece operações comuns de CRUD.  
Um exemplo prático foi `findByCategoria`, cuja consulta é derivada pelo próprio nome do método.  
JDBC ainda pode ser útil quando é necessário controlar SQL muito específico, mas para este projeto o JPA deixou a persistência mais simples.

### 3. Exceções checked vs unchecked (Aula 11)

`ClassificacaoIndicativaException` estende `Exception`, por isso é uma exceção checked.  
Isso obriga o código a declarar ou tratar a exceção, como acontece no método `Usuario.alugar`.  
Já exceções que estendem `RuntimeException` são unchecked e não exigem essa declaração do compilador.  
No nosso caso, o principal problema não era ela ser checked, mas a API não ter um tratamento específico para essa exceção.  
No `bug12`, mantivemos a exceção como estava e adicionamos um `@ExceptionHandler` no `GlobalExceptionHandler`.  
Assim a regra continua no model e o cliente recebe HTTP 422 com uma mensagem útil, em vez de um erro 500 genérico.

### 4. Sobrescrita vs sobrecarga (Aula 7)

O método usado pelo contrato é `calcularPrecoAluguel()` sem parâmetros, definido em `Conteudo`.  
Na versão original da `Serie`, existia `calcularPrecoAluguel(double desconto)`, que possui outra assinatura.  
Por isso aquilo era uma sobrecarga e não substituía o comportamento herdado de `Conteudo`.  
A aplicação continuava chamando o método sem argumentos e calculava o preço errado para a série.  
No `bug06`, alteramos a assinatura para a mesma do método da superclasse e adicionamos `@Override`.  
Depois disso, o polimorfismo passou a chamar corretamente o cálculo de 4,90 por temporada.

### 5. Onde blindar o objeto? (Aulas 3, 4 e 13)

Uma regra importante deve ficar o mais próximo possível do estado que ela protege.  
Por isso a duração passou a ser validada dentro de `Conteudo`, e não apenas no controller.  
O construtor reutiliza essa validação e o atributo ficou privado no `clean03`, evitando alteração direta externa.  
A mesma ideia aparece no aluguel: `Usuario.alugar` verifica disponibilidade, idade e créditos antes de alterar o saldo.  
Também corrigimos problemas de construção de objetos usando `this.nome` e `super(...)`, garantindo que o estado já nasça correto.  
O controller recebe a requisição e o handler traduz os erros para HTTP, mas as regras principais continuam protegidas no model.

### 6. Abstração e interface (Aulas 8 e 9)

`Conteudo` é abstrata porque concentra os dados e comportamentos comuns a filme, série e documentário.  
As subclasses aproveitam título, categoria, duração, classificação e disponibilidade, mas podem sobrescrever regras específicas.  
A interface `Promocionavel` representa uma capacidade separada: participar de uma promoção.  
Filme e série implementam essa interface, enquanto documentário não implementa e apenas sobrescreve o preço para zero.  
Isso evita obrigar todos os tipos de conteúdo a terem uma regra de promoção que não faz sentido para eles.  
Se futuramente outro tipo puder receber promoção, ele pode implementar `Promocionavel` sem precisar alterar toda a hierarquia ou os controllers.

## Parte 4 - Dificuldade encontrada

Uma dificuldade prática foi preparar o ambiente no VS Code, configurando JDK, Maven e as opções de execução do projeto. Também foi necessário alternar entre H2 e Oracle sem colocar as credenciais reais no repositório. O H2 ajudou bastante porque permitiu repetir os testes com um banco limpo após cada reinicialização. No Oracle, a conexão e os cadastros foram validados usando variáveis de ambiente definidas pelo `launch.json`.

## Execução

Requer JDK 17 ou superior e Maven. Nesta execução foi utilizado JDK 21. A classe principal é `br.com.fiap.streamfiap.StreamFiapApplication`.

O `application.properties` versionado permanece com `SEU_RM` e `SUA_SENHA`. Para executar com Oracle, as credenciais devem ser fornecidas por variáveis de ambiente (`SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`) na configuração de execução da IDE, sem commitá-las no Git.

Para validar rapidamente a API, inicie a aplicação e acesse:

```text
http://localhost:8080/api/conteudos
```

**Validação realizada em 07/09/2026:** contrato completo testado em H2 nas seções A-F com os resultados esperados; Oracle conectado com sucesso, `GET /api/conteudos` retornando HTTP 200 e seção A repetida com todos os cadastros retornando HTTP 201. As seções B-G do Oracle não foram repetidas por limitação de tempo.
