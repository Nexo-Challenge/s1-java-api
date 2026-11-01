# Contrato esperado pelo Java — consultas e revisão do banco

## Origem, comparação e status

Origem principal: `Nexo-Challenge/s1-database`, `docs/entidades-mvp.md`, consultado via `gh api` somente para leitura em 01/11/2026; blob `b8c7a180c3939c21e3ab434d61edff3df44784bd`.
O remoto já registra o dicionário fechado, associação com dados próprios, PK composta e ausência de cascata. Este documento **não substitui nem altera o contrato publicado por Kenji**. Alisson responde por Java/.NET; Kenji pelo banco. O solicitante confirmou o acordo com Kenji para geração dos IDs simples por **IDENTITY (`GENERATED ... AS IDENTITY`)**. Esse acordo relatado orienta o mapeamento Java; não constitui verificação do DDL Oracle. Os novos detalhes locais de API e nomes ainda não especificados continuam propostos para revisão.
A cópia local `entidades-mvp.md` é anterior ao remoto e não deve prevalecer sobre ele; não foi sincronizada nesta tarefa.

### Comparação campo a campo antes de novos ajustes

`NULL` abaixo descreve o mapeamento Java atual. Ausência de obrigatoriedade explícita no remoto não prova aceite dessa nulabilidade. Tipos SQL são os esperados pelo documento de origem, não resultado de inspeção de banco real. Java usa `Long` para IDs e `String` para texto; nenhuma entidade define `schema` ou DDL via `columnDefinition`.

| Entidade / tabela publicada | Campo Java → coluna mapeada | Tipo / comprimento | Java aceita NULL? | PK, FK e UNIQUE | Comparação com remoto |
| --- | --- | --- | --- | --- | --- |
| AreaAplicacao / TB_AREA_APLICACAO | id → id | Long; NUMBER no remoto | Não | PK | IDENTITY conforme acordo relatado; precisão/escala e DDL Oracle a verificar |
| AreaAplicacao / TB_AREA_APLICACAO | nome → nome | String / VARCHAR2(100) | Não | UNIQUE | Alinhado: nome obrigatório e exclusivo |
| AreaAplicacao / TB_AREA_APLICACAO | descricao → descricao | String / VARCHAR2(1000) | Sim | Nenhuma | Tamanho alinhado; nulabilidade final confirmar |
| AplicacaoNuclear / TB_APLICACAO_NUCLEAR | id → id | Long; tipo físico não detalhado para esta linha no remoto | Não | PK | IDENTITY conforme acordo relatado; tipo/precisão e DDL Oracle a verificar |
| AplicacaoNuclear / TB_APLICACAO_NUCLEAR | areaAplicacao → areaAplicacaoId | Long da área | Não | FK → TB_AREA_APLICACAO.id; não UNIQUE | Remoto chama o atributo areaAplicacaoId, sem fixar a coluna física. **Por orientação explícita do solicitante, Java adota agora areaAplicacaoId literalmente**, sem alegar comprovação de DDL |
| AplicacaoNuclear / TB_APLICACAO_NUCLEAR | titulo → titulo | String / VARCHAR2(150) | Sim | Não UNIQUE | Nome/tamanho alinhados; remoto atual não exige NOT NULL |
| AplicacaoNuclear / TB_APLICACAO_NUCLEAR | resumo → resumo | String / VARCHAR2(500) | Sim | Nenhuma | Nome/tamanho alinhados; remoto atual não exige NOT NULL |
| AplicacaoNuclear / TB_APLICACAO_NUCLEAR | descricao → descricao | String / VARCHAR2(4000) | Sim | Nenhuma | Nome/tamanho alinhados; nulabilidade final confirmar |
| ImpactoSocial / TB_IMPACTO_SOCIAL | id → id | Long; tipo físico não detalhado para esta linha no remoto | Não | PK | IDENTITY conforme acordo relatado; tipo/precisão e DDL Oracle a verificar |
| ImpactoSocial / TB_IMPACTO_SOCIAL | titulo → titulo | String / VARCHAR2(150) | Sim | UNIQUE | Nome/tamanho/unicidade alinhados; NOT NULL não explicitado no remoto atual |
| ImpactoSocial / TB_IMPACTO_SOCIAL | descricao → descricao | String / VARCHAR2(1000) | Sim | Nenhuma | Nome/tamanho alinhados; nulabilidade final confirmar |
| AplicacaoImpacto / TB_APLICACAO_IMPACTO | id.aplicacaoId; aplicacao → aplicacao_id | Long; tipo SQL compatível com PK referenciada, a confirmar | Não | Parte da PK e FK → TB_APLICACAO_NUCLEAR.id | Nome físico explícito no remoto; alinhado |
| AplicacaoImpacto / TB_APLICACAO_IMPACTO | id.impactoId; impacto → impacto_id | Long; tipo SQL compatível com PK referenciada, a confirmar | Não | Parte da PK e FK → TB_IMPACTO_SOCIAL.id | Nome físico explícito no remoto; alinhado |
| AplicacaoImpacto / TB_APLICACAO_IMPACTO | explicacao → explicacao | String / VARCHAR2(2000) | Sim | Nenhuma | Nome/tamanho alinhados; nulabilidade final confirmar |
| AplicacaoImpacto / TB_APLICACAO_IMPACTO | fonteUrl → fonte_url | String / VARCHAR2(500) | Sim | Nenhuma | Remoto usa fonteUrl no dicionário e fonte_url na descrição da associação; mapeamento alinhado |

O nome area_aplicacao_id da implementação Java anterior era inferido e não consta no contrato remoto. Por orientação explícita do solicitante, foi substituído por areaAplicacaoId, grafia literal do atributo publicado. Como o remoto não contém DDL, essa adoção não é confirmação de coluna física Oracle. A estratégia PhysicalNamingStrategyStandardImpl preserva nomes explícitos, sem quoted identifiers: no teste SQL H2 executado a coluna observada foi AREAAPLICACAOID, não AREA_APLICACAO_ID. aplicacao_id, impacto_id e fonte_url permanecem conforme nomes físicos expressos no texto remoto. Nomes físicos de constraints, schema/proprietário, precisão de NUMBER e semântica BYTE/CHAR de VARCHAR2 também não foram definidos. Não inventar essas decisões.

## Identidade, relacionamentos e integridade

- `AreaAplicacao`, `AplicacaoNuclear` e `ImpactoSocial` usam `@GeneratedValue(strategy = GenerationType.IDENTITY)`, conforme acordo com Kenji relatado pelo solicitante. Novas entidades são persistidas sem atribuir ID. Não foi definida aqui a variante `ALWAYS` ou `BY DEFAULT`, nem sequence, trigger, schema ou novos tipos SQL. O DDL Oracle e sua compatibilidade permanecem sem verificação.
- O teste `IdentidadePersistenciaTest` persiste duas entidades de cada tipo sem IDs, executa `flush`, verifica IDs não nulos e distintos dentro de cada tabela e persiste/lê uma associação pela chave composta após limpar o contexto. Essa evidência usa somente H2 isolado; não certifica Oracle.
- `AplicacaoImpactoId` é `@Embeddable`, serializável, com igualdade/hash pelo par. `AplicacaoImpacto.id` usa `@EmbeddedId`; `@MapsId("aplicacaoId")` e `@MapsId("impactoId")` ligam os pais à mesma chave. O par é único como PK; cada FK isolada pode repetir. Não existe ID substituto.
- Área 1 → 0..N aplicações; cada aplicação referencia uma área obrigatória. Aplicação e impacto 1 → 0..N vínculos; cada vínculo referencia exatamente um de cada. Isso resolve N:N, sem coleções bidirecionais no Java.
- Todas as relações são N:1 `LAZY`, `optional=false`, FK `nullable=false`, sem cascata JPA. Não há soft delete nem orphan removal. O contrato publicado exige impedir exclusões de pais referenciados; a anotação Java não comprova a constraint física existente. A expressão `ON DELETE RESTRICT` do documento descreve a política; não constitui DDL Oracle executável aqui.
- Não acrescentamos validações de escrita ou NOT NULL diferentes dos models existentes. UNIQUE e tratamento de NULL, caixa, acentos e comprimento precisam de validação Oracle autorizada; H2 não certifica essas semânticas.

## Novos termos locais da API — revisão pendente

Somente leitura de `AplicacaoNuclear`, com `AplicacaoNuclearRepository` expondo apenas consultas. Não foram adicionados repositórios sem consumidor para outras entidades.

- `GET /api/aplicacoes?page=0&size=20&titulo=nuclear`: lista paginada, ordenação fixa `id ASC`. `page` inteiro ≥ 0; `size` inteiro entre 1 e 100. Valores padrão 0 e 20. Não há ordenação arbitrária pelo cliente.
- `titulo` opcional: ausente, vazio ou somente espaços lista tudo; caso contrário `trim()` e Query Method `findByTituloContainingIgnoreCase`. Busca por trecho ignorando caixa, com `%`/`_` tratados como literais pelo Spring Data. Não há promessa de remover acentos ou equivalência de collation Oracle/H2.
- Página além do fim ou filtro sem resultados: HTTP 200, `content: []`, mantendo os metadados. Ordenação por PK estabiliza resultados de um conjunto fixo, não fornece snapshot entre requisições concorrentes.
- `GET /api/aplicacoes/{id}`: ID Long existente retorna HTTP 200; inexistente, 404. ID não numérico/fora de Long ou paginação inválida: 400. IDs negativos não recebem nova regra de domínio: se inexistentes, 404.
- DTO entrada de escrita (`AplicacaoRequest`): `areaAplicacaoId` (obrigatório, positivo), `titulo` (obrigatório, não branco, max 150), `resumo` (opcional, max 500), `descricao` (opcional, max 4000). Não expõe IDs internos, não altera área e não permite manipular impactos.
- DTO aplicação (`AplicacaoResponse`): `id`, `titulo`, `resumo`, `descricao`, `area: {id, nome, descricao}`. Textos opcionais podem ser nulos. **Impactos e vínculos ficam fora desta resposta inicial.** Entidades/proxies JPA nunca são retornados pelo controller.
- DTO área (`AreaResponse`): `id`, `nome`, `descricao`.
- DTO página (`PaginaResponse`): `content`, `page`, `size`, `totalElements`, `totalPages`. Não depende do formato de serialização de `PageImpl`.
- Erros MVC/ausência/validação usam `application/problem+json` (RFC 9457 / ProblemDetail): `type`, `title`, `status`, `detail`, `instance` e lista ordenada `errors: [{campo, mensagem}]`. 400 para dados inválidos, tipos incompatíveis ou erros de paginação; 404 para aplicação ou área ausente; 409 para violação de integridade ou tentativa de exclusão de aplicação vinculada a impactos sociais.
- Serviços separados: `AplicacaoConsultaService` e `AreaConsultaService` com `@Transactional(readOnly=true)`; `AplicacaoEscritaService` com `@Transactional(readOnly=false)` e `saveAndFlush`/`flush` explícitos. Entity graph carrega apenas a relação para uma área: nenhuma coleção é carregada com paginação.
- Endpoints implementados:
  - `POST /api/aplicacoes` (201 Created + header Location + AplicacaoResponse)
  - `GET /api/aplicacoes` (200 OK + PaginaResponse<AplicacaoResponse>, ordenação id ASC, filtro titulo)
  - `GET /api/aplicacoes/{id}` (200 OK + AplicacaoResponse ou 404)
  - `PUT /api/aplicacoes/{id}` (200 OK + AplicacaoResponse ou 404)
  - `DELETE /api/aplicacoes/{id}` (204 No Content, 404 se inexistente, 409 se houver vínculos de impacto)
  - `GET /api/areas` (200 OK + PaginaResponse<AreaResponse>, ordenação id ASC)
- Autenticação/autorização, documentação Swagger/OpenAPI (a adicionar na sequência), chamadas de procedures e functions TABLE do banco permanecem para etapas seguintes.

## Configuração e limites de execução

Base: `ddl-auto=none`, `spring.sql.init.mode=never`, `open-in-view=false`. Perfil `oracle`: URL/usuário/senha exclusivamente em `ORACLE_JDBC_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`, sem defaults; driver Oracle e `ddl-auto=validate`. Nenhuma criação/alteração/seed é executada pela configuração versionada. Overrides externos devem ser revisados antes de executar; `validate` não certifica todas as constraints.

**Configurar não significa testar conexão. Nenhuma conexão Oracle foi iniciada.** Nenhum DDL, migration, seed, credencial ou dado real foi usado. Testes de contexto usam destino `jdbc:h2:mem:nexo_test_<UUID>` fixado por `@DynamicPropertySource`, usuário/senha de teste explícitos, perfil `test`, dialeto H2 e `create-drop` exclusivamente no código de teste. Não executar testes com overrides externos de pool, inicialização ou migração.

## Pendências acadêmicas e de revisão

Fonte oficial: `2TDS Agosto - Marinha 2026 - 1º Semestre .pdf`. Conforme leitura do PDF registrada pelo solicitante, banco exige procedures INSERT/UPDATE/DELETE por tabela, functions TABLE e integração das chamadas via API, com **10 registros por tabela**. O remoto ainda menciona cinco; sinalizamos a divergência sem editar o documento de Kenji. As amostras sintéticas de teste não cumprem essa carga acadêmica.
Java ainda precisa completar CRUD JPA, validações de escrita, exceções correspondentes e Swagger. A escolha remota de `AplicacaoNuclear` como principal da PoC .NET coincide com o recorte Java; alinhar entregáveis com Alisson/Kenji sem reescrever o domínio ou implementar .NET aqui.

Revisão pendente: comprovar no DDL a coluna de área adotada literalmente como areaAplicacaoId; schema e tipos numéricos; semântica de tamanhos e nulabilidade; implementação Oracle da estratégia IDENTITY acordada segundo o solicitante; permissões somente leitura; assinaturas/retornos/erros/transações das procedures/functions; compatibilidade Oracle real; aceite dos novos DTOs e endpoints. Esta preparação não conclui a entrega da Sprint.

## Referências técnicas consultadas

- Spring Data JPA, Query Methods e EntityGraph: `https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html`
- Spring MVC, validação: `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html`
- Spring MVC, respostas de erro: `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html`
- Spring Boot, inicialização de banco: `https://docs.spring.io/spring-boot/how-to/data-initialization.html`
- Spring Test, precedência das propriedades dinâmicas: `https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/dynamic-property-sources.html`

APIs verificadas adicionalmente por compilação e testes com Spring Boot 4.1.2 do POM, sem atualizar versões.
