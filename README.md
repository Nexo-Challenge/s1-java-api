# s1-java-api

**FIAP — Sprint 1 — Java Advanced.** Challenge 2026, tecnologia nuclear / Marinha. Entregável acadêmico; reutilização no produto real exige acordo.

## Integrantes do Grupo (Nexo)

| Nome Completo | RM | Turma | Responsabilidade Principal |
| :--- | :--- | :--- | :--- |
| **Alisson Kawan Evangelista Silva** | **RM567598** | 2TDS | Responsável Técnico (Java e .NET) |
| **Kenji** | **RM568156** | 2TDS | IoT e Banco de Dados |
| **Erick Ramos** | **RM567837** | 2TDS | DevOps Tools & Cloud Computing |
| **Eduardo Boni** | **RM567236** | 2TDS | Mobile Application Development |
| **Marcos Vinicius de Jesus Almeida** | **RM567214** | 2TDS | Documentação e Qualidade de Software (QA) |

## Estado e responsáveis

CRUD completo de `AplicacaoNuclear` e consultas de `AreaAplicacao` implementados e testados com H2 isolado: criação com cabeçalho Location, listagem paginada, busca por ID, filtro por título via Query Method, atualização total idempotente e exclusão com proteção de integridade referencial. **Nenhuma conexão Oracle foi testada.**
Alisson responde por Java/.NET; Kenji por IoT e banco; Eduardo por mobile; Erick por DevOps; Marcos Vinicius por documentação e QA.

## Fonte e prazo

Fonte oficial: `2TDS Agosto - Marinha 2026 - 1º Semestre .pdf`, seção Java Advanced. O PDF não foi republicado.
O documento diverge: Entregas indica 01/11/2026; Cronograma, até 02/11/2026. Confirmar data/horário no portal ou com professor; não tratamos nenhuma como definitiva.

## Contrato e arquitetura

Controller → serviço transacional somente leitura → repository Spring Data JPA. DTOs não expõem entidades/proxies. Área carregada por EntityGraph N:1, sem coleção com paginação e sem open-in-view.

Ver [contrato esperado pelo Java](docs/contrato.md) para comparação campo a campo com o contrato **já publicado** em `Nexo-Challenge/s1-database/docs/entidades-mvp.md`, comprimentos, nulabilidade, PK/FK/UNIQUE, IDs gerados por IDENTITY, erros e pendências. A cópia local [entidades-mvp.md](docs/entidades-mvp.md) é anterior e não substitui o remoto. Nenhum documento de Kenji foi alterado.

`AreaAplicacao`, `AplicacaoNuclear` e `ImpactoSocial` usam `@GeneratedValue(strategy = GenerationType.IDENTITY)`, conforme acordo com Kenji relatado pelo solicitante (`GENERATED ... AS IDENTITY`). Não se presume `ALWAYS` ou `BY DEFAULT`, sequence, schema ou novos tipos SQL. A associação `AplicacaoImpacto` mantém `@EmbeddedId`/`@MapsId`, sem `@GeneratedValue`. **O DDL Oracle não foi verificado.**

A FK de área adota `area_aplicacao_id` com underline conforme o diagrama relacional e o DDL do banco Oracle. `aplicacao_id`, `impacto_id` e `fonte_url` seguem o padrão snake_case. Os campos `titulo`, `resumo` e `descricao` de `AplicacaoNuclear`, `titulo` de `ImpactoSocial` e `explicacao` de `AplicacaoImpacto` são configurados como `nullable = false` nas entidades e validados via Bean Validation (`@NotBlank`) na entrada da API para evitar nulos ou strings vazias no Oracle.

## Testes e compilação seguros

POM: Java release 21 e Spring Boot 4.1.2, sem atualização de versões. Execução verificada: JDK 24.0.2, Maven Wrapper 3.9.11, Windows. Única correção de dependência: escopo inválido `annotationProcessor` do Lombok → `provided`.

No terminal Windows, dentro do repositório (em Git Bash, prefixar com `cmd.exe /c`):

```bat
mvnw.cmd -o test
mvnw.cmd -o compile
```

O modo offline requer dependências previamente disponíveis. Testes de contexto, inclusive `NexoApiApplicationTests`, herdam `IsolatedDatabaseTest`: perfil `test`, URL exclusiva `jdbc:h2:mem:nexo_test_<UUID>`, driver/dialeto H2 e credenciais sintéticas definidos por propriedades dinâmicas, sem interpolar variáveis externas. `create-drop` aparece somente nesse código de teste. Não forneça overrides externos de pool, scripts ou migração. Os testes não abrem porta HTTP: usam MockMvc e dados sintéticos com rollback.

Resultado real da suíte após alinhamento de DDL e validações:

```text
Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Cobertura: ciclo completo CRUD (POST, GET paginado/id, PUT, DELETE), cabeçalho Location, documentação Swagger/OpenAPI interativa e especificação JSON validadas, models/chave composta preservados, geração de IDs por IDENTITY, filtro Query Method real, paginação/ordenação estável por id ASC, validações Bean Validation (400 com campos detalhados), tratamento de integridade referencial sem cascata (409 Conflict), recursos inexistentes (404 Not Found), DTOs sem expor entidades e isolamento completo sem tocar em variáveis Oracle reais.

`mvnw.cmd -o package` também foi tentado: compilou e passou os testes, mas falhou no `spring-boot:repackage` por dependências do plugin não resolvíveis no cache offline (por exemplo `spring-boot-buildpack-platform:4.1.2`). Um JAR simples foi gerado, mas **não há JAR executável verificado**. Não foram alterados cache, versões ou repositórios para contornar o bloqueio. Para distribuir, resolver dependências em ambiente autorizado e repetir `package`. Há avisos do JDK 24 sobre Unsafe/Mockito/Byte Buddy e aviso de dialeto H2 explícito; não foram ocultados.

## Configuração Oracle — não executar automaticamente

Configuração preparada em `application-oracle.yaml`:

```text
SPRING_PROFILES_ACTIVE=oracle
ORACLE_JDBC_URL=jdbc:oracle:thin:@//HOST_AUTORIZADO:1521/SERVICO_AUTORIZADO
ORACLE_USERNAME=USUARIO_SOMENTE_LEITURA
ORACLE_PASSWORD=SENHA_FORNECIDA_FORA_DO_REPOSITORIO
```

São placeholders, não credenciais funcionais. Configure no ambiente/gerenciador de segredos, nunca em arquivos versionados, chat ou histórico compartilhado. **Configurar não significa testar conexão.** Nenhum comando desta preparação iniciou a aplicação contra Oracle.

Base: `ddl-auto=none`; Oracle: `ddl-auto=validate`; ambos com `spring.sql.init.mode=never` e `open-in-view=false`. Não há migrations/seeds. Antes de iniciar: obter autorização para o alvo, confirmar schema/contrato, permissões de leitura e variáveis efetivas, sem overrides que habilitem DDL. Após resolver o empacotamento, o comando abaixo é apenas orientação futura, não foi executado:

```bat
java -jar target/nexo-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=oracle
```

Sem perfil/configuração/dados aprovados, não esperar demonstração funcional contra banco real; a base não cria tabelas nem popula H2 automaticamente.

## Endpoints HTTP

Exemplos de requisições suportadas pela API:

```http
POST /api/aplicacoes
Content-Type: application/json

{
  "areaAplicacaoId": 1,
  "titulo": "Medicina Nuclear e Diagnóstico",
  "resumo": "Uso de radiofármacos para diagnósticos precisos e precoces.",
  "descricao": "Detalhamento das técnicas de cintilografia e PET scan aplicadas ao diagnóstico oncológico e cardiológico."
}

GET /api/aplicacoes?page=0&size=20
GET /api/aplicacoes?titulo=nuclear&page=0&size=10
GET /api/aplicacoes/1

PUT /api/aplicacoes/1
Content-Type: application/json

{
  "areaAplicacaoId": 1,
  "titulo": "Medicina Nuclear Avançada",
  "resumo": "Aplicações terapêuticas e diagnósticas atualizadas.",
  "descricao": "Descrição expandida de terapias com radioisótopos."
}

DELETE /api/aplicacoes/1

GET /api/areas?page=0&size=20
```

- `POST /api/aplicacoes`: Retorna HTTP 201 Created com cabeçalho `Location` apontando para o recurso criado e corpo `AplicacaoResponse`.
- `GET /api/aplicacoes`: Listagem paginada (`page >= 0`, `size 1..100`), ordenada por `id ASC`, com filtro opcional por `titulo` (case-insensitive).
- `GET /api/aplicacoes/{id}`: Retorna HTTP 200 com DTO ou 404 Not Found se inexistente.
- `PUT /api/aplicacoes/{id}`: Atualiza os dados da aplicação existente, retornando HTTP 200 ou 404.
- `DELETE /api/aplicacoes/{id}`: Exclui a aplicação se não possuir vínculos de impacto (retorna 204 No Content). Se houver vínculos com impactos sociais, retorna HTTP 409 Conflict protegendo a integridade referencial.
- `GET /api/areas`: Listagem paginada de áreas temáticas disponíveis.
- Respostas de erro seguem RFC 7807 (`ProblemDetail`): 400 Bad Request com lista estruturada de campos (`errors: [{campo, mensagem}]`), 404 Not Found e 409 Conflict.

## Documentação Interativa (Swagger / OpenAPI)

Com a aplicação em execução, a documentação interativa e os testes dos endpoints estão disponíveis em:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (ou `/swagger-ui/index.html`)
- **Especificação OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

## Pendências de entrega

- Validar documentação Swagger em conjunto com o time e exportar JSON/YAML para os entregáveis FIAP.
- Procedures INSERT/UPDATE/DELETE por tabela, functions TABLE e chamadas via API, com **10 registros por tabela**, conforme PDF informado.
- Validar Oracle em ambiente autorizado, inclusive o DDL para a estratégia IDENTITY acordada segundo o solicitante; não há sequence presumida.
- Alinhar a PoC .NET, cujo principal remoto também é `AplicacaoNuclear`, sem mudar domínio por suposição.
- Revisão de autenticação/autorização antes de exposição pública; esta etapa não implementa segurança de produção.

O [checklist preexistente](docs/requisitos-sprint-1.md) não substitui o PDF oficial. Evidências acadêmicas, integração e revisão continuam pendentes. Ver [CONTRIBUTING.md](CONTRIBUTING.md); nenhuma branch, commit, push, sincronização, permissão ou configuração GitHub foi realizada.
