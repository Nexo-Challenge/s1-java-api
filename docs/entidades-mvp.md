# Entidades do MVP — contrato de domínio Java / banco (v1)

## Status, finalidade e referência

Este documento orienta o início da modelagem do **entregável FIAP — Sprint 1**. Não altera a arquitetura do produto real Nexo.

- Direção acordada na conversa com o solicitante: `AreaAplicacao`, `AplicacaoNuclear`, `ImpactoSocial` e relação **N:N entre aplicação e impacto**.
- A associação `AplicacaoImpacto`, seus campos próprios, os atributos abaixo e as regras físicas são **propostas para revisão Java/banco**, não decisões técnicas já aprovadas pelo Kenji.
- Kenji foi indicado pelo solicitante para conduzir o trabalho de banco. Seu usuário GitHub e o revisor nominal ainda precisam ser confirmados; nenhuma atribuição automática foi realizada.
- Origem principal deste contrato de dados: `Nexo-Challenge/s1-database`, `docs/entidades-mvp.md`. A cópia em `s1-java-api` é o espelho v1 para leitura do backend. Mudanças exigem revisão conjunta e atualização explícita das referências/cópias, não sincronização automática.
- Consultar também `docs/requisitos-sprint-1.md` em cada repositório. O PDF original citado nos checklists não foi relido nesta atualização; esta proposta não certifica cumprimento integral nem altera os requisitos acadêmicos.

Repositórios: [Banco](https://github.com/Nexo-Challenge/s1-database) e [Java](https://github.com/Nexo-Challenge/s1-java-api).

## 1. O que cada entidade representa

| Entidade | Responsabilidade | Exemplo apenas ilustrativo |
| --- | --- | --- |
| `AreaAplicacao` | Categoria temática que organiza as aplicações. | Saúde; agricultura e alimentos. |
| `AplicacaoNuclear` | Uso concreto da tecnologia descrito em conteúdo educativo. Não se limita a projetos com nome próprio. | Irradiação de alimentos; esterilização de materiais médicos. |
| `ImpactoSocial` | Benefício geral reutilizável entre aplicações, não um resultado quantitativo garantido. | Proteção da saúde; redução de perdas de alimentos. |
| `AplicacaoImpacto` | Associação entre uma aplicação e um impacto; pode explicar a contribuição específica e apontar sua fonte. | Explicação de como a aplicação A se relaciona com o impacto X. |

Os exemplos servem para entender o modelo. Conteúdo real, números e alegações institucionais precisam de fontes verificadas antes de publicação; não reutilizar como fato as estimativas mencionadas informalmente na conversa.

## 2. Relacionamentos e cardinalidades propostos

```text
AreaAplicacao     1 ---- 0..N AplicacaoNuclear
AplicacaoNuclear  1 ---- 0..N AplicacaoImpacto
ImpactoSocial     1 ---- 0..N AplicacaoImpacto

AplicacaoNuclear N:N ImpactoSocial (resolvido por AplicacaoImpacto)
```

- Cada aplicação tem **uma área principal**, como simplificação proposta para o MVP; confirmar com o grupo. Uma área pode existir sem aplicações.
- Uma aplicação pode ter vários impactos; um mesmo impacto pode estar associado a várias aplicações, sem duplicar seu cadastro.
- Cada associação aponta para exatamente uma aplicação e um impacto existentes.
- Mínimo zero de vínculos permite cadastro em preparação. Exigir ao menos um impacto antes da publicação é uma regra possível, ainda não aprovada; não criar status de publicação por suposição.
- Se o grupo precisar de várias áreas por aplicação, revisar o contrato antes de acrescentar outra associação.

Exemplo sintético de vínculos, sem representar dados científicos:

| Aplicação | Impacto |
| --- | --- |
| A | X |
| A | Y |
| B | X |

X tem um único cadastro e dois vínculos. A possui dois impactos. O par A/X não deve se repetir.

## 3. Dicionário inicial — proposta a validar

Os nomes de tabelas/colunas são sugestões. Tipos Oracle, tamanhos, nulabilidade final e geração de IDs devem ser fechados por Kenji com revisão Java antes de implementação. Não são DDL executável.

| Entidade / tabela sugerida | Atributos iniciais | Regras propostas |
| --- | --- | --- |
| `AreaAplicacao` / `TB_AREA_APLICACAO` | `id` (identificador), `nome` (texto curto), `descricao` (texto explicativo) | PK em `id`; nome obrigatório. Definir se nomes são únicos e como tratar maiúsculas/acentos. |
| `AplicacaoNuclear` / `TB_APLICACAO_NUCLEAR` | `id`, `areaAplicacaoId` (referência), `titulo`, `resumo`, `descricao` | PK em `id`; FK de área obrigatória; título e resumo obrigatórios. Mesmo título não implica mesma aplicação; não impor UNIQUE sem decisão. |
| `ImpactoSocial` / `TB_IMPACTO_SOCIAL` | `id`, `titulo`, `descricao` | PK em `id`; título obrigatório. Validar identidade/duplicidade sem depender apenas de igualdade de texto. |
| `AplicacaoImpacto` / `TB_APLICACAO_IMPACTO` | `aplicacaoId`, `impactoId`; como extensão proposta: `explicacao`, `fonteUrl` | FKs obrigatórias; par aplicação/impacto único. Explicação e fonte dependem da aprovação de associação com dados próprios. |

### Identidade e integridade da associação

Escolher **uma** estratégia em revisão:

1. PK composta `(aplicacao_id, impacto_id)`: impede repetição do vínculo diretamente; Java precisará mapear a chave composta.
2. PK própria `id` mais UNIQUE `(aplicacao_id, impacto_id)`: simplifica referenciar o vínculo por ID, mas exige a constraint de unicidade adicional.

Não manter as duas estratégias como alternativas simultâneas no esquema final. Em ambos os casos, testar a impossibilidade de vínculo duplicado e de referências inexistentes.

Não usar exclusão em cascata por padrão. Proposta conservadora: impedir excluir área, aplicação ou impacto enquanto houver referências, com erro compreensível na API; confirmar a regra antes do DDL. Não criar soft delete, `ativo` ou cascatas sem acordo.

### O que fica fora do recorte inicial

Usuário/login, quiz, progresso, gamificação, indicadores numéricos, múltiplas fontes por vínculo, instituições como entidade, estágio de desenvolvimento e auditoria não entram automaticamente. Acrescentar somente mediante necessidade e aprovação. A descrição do impacto é genérica; explicação específica pertence ao vínculo, se esse desenho for aceito.

## 4. Orientação para Java

- CRUD principal proposto: `AplicacaoNuclear`; busca por texto no título. DTOs, endpoints, códigos HTTP, paginação e contrato de erros ainda precisam de acordo; não estão implementados por este documento.
- A referência da aplicação para a área é N:1.
- Se `AplicacaoImpacto` tiver explicação/fonte, tratá-la como entidade associativa explícita com duas referências N:1, em vez de um N:N direto sem atributos.
- Não obrigar coleções bidirecionais nas entidades nem serializar entidades JPA diretamente como contrato da API. Definir DTOs separadamente.
- Confirmar como cadastrar/selecionar áreas e impactos para testar o CRUD. Não presumir CRUD completo para todas as entidades.
- Não gerar ou alterar tabelas automaticamente pelo Hibernate no Oracle compartilhado. Migrations, seeds e execução DDL/DML exigem autorização específica.

## 5. Roteiro de trabalho para Kenji — banco

Estas são tarefas de orientação, não issues criadas nem confirmação de trabalho em andamento. Kenji é o dono proposto da implementação de banco conforme indicação do solicitante; o revisor Java permanece a confirmar. Não há prazo definitivo confirmado.

| Tarefa | Área / dono principal | Dependência | Aceite observável |
| --- | --- | --- | --- |
| Validar o dicionário e as regras do vínculo | Banco / Kenji | Revisão Java; confirmar campos próprios, áreas e chave | Decisões registradas, sem alternativa pendente para PK, obrigatoriedade e exclusão. |
| Desenhar o modelo lógico/físico e o DER/MER | Banco / Kenji | Dicionário validado | Modelo em 3FN, notação Barker conforme checklist, PK/FK e mínimos/máximos explícitos; N:N resolvido sem repetição do par. |
| Elaborar DDL Oracle versionado | Banco / Kenji | Modelo revisado | Script coerente com modelo; tipos/tamanhos/constraints e estratégia de IDs explícitos. Execução só em ambiente autorizado. |
| Elaborar dados e consultas de demonstração | Banco / Kenji | Modelo e DDL aprovados | Conforme checklist atual: pelo menos cinco registros coerentes por tabela e duas consultas JOIN; incluir A/X, A/Y e B/X para demonstrar N:N. |
| Registrar evidências de execução e empacotar | Banco / Kenji | Autorização de execução; testes reais | Registrar ambiente sem segredos e resultados reais; conferir PDF e SQL no ZIP exigido pelo checklist. |

Limites autorizados para a futura tarefa de banco, após acordo: `models/`, `sql/` e documentação do entregável em `s1-database`. Alteração Java, produto real, permissões e execução em banco compartilhado estão fora desse escopo. Não substituir arquivos/trabalho já em andamento sem combinar com o responsável.

Testes propostos para ambiente autorizado: aceitar vários impactos na mesma aplicação; compartilhar um impacto entre aplicações; rejeitar par repetido e FK inexistente; verificar campos obrigatórios e regra de exclusão aprovada. Consultas sugeridas: listar aplicações de uma área com seus impactos; listar aplicações associadas a um impacto.

## 6. Pendências antes de fechar o contrato

- [ ] Kenji e o revisor Java confirmam o desenho e seus usuários GitHub, sem inventar atribuições.
- [ ] Confirmar uma área principal por aplicação e cardinalidades mínimas.
- [ ] Aprovar associação com explicação/fonte ou somente ligação; escolher a chave.
- [ ] Fechar nomes físicos, tipos Oracle, tamanhos, obrigatoriedade, unicidade e exclusão.
- [ ] Definir DTOs, endpoints e validações Java em contrato próprio, sem implementar por antecipação.
- [ ] Definir com o responsável .NET quais entidades a PoC utilizará: o checklist de banco pede essa indicação, mas não existe decisão nesta conversa. Não atribuir entidades à disciplina por suposição.
- [ ] Conferir o PDF/portal oficial e o prazo definitivo. Os checklists atuais não substituem instruções atualizadas.

**Esta publicação é documental:** não cria entidades Java, modelos gráficos, tabelas, sequences, constraints, dados de teste ou evidências de execução; não conclui a entrega acadêmica e não autoriza mudanças no produto real.
