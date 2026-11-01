package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de metadados: sem contexto Spring, conexão ou criação de esquema. */
class ModelContractTest {

    @Test
    void areaTemIdentityEColunasDoContrato() throws Exception {
        Class<?> type = entity("AreaAplicacao", "TB_AREA_APLICACAO");
        idGerado(type);
        column(type, "nome", "nome", 100, false, true);
        column(type, "descricao", "descricao", 1000, true, false);
        beanProperties(type);
    }

    @Test
    void aplicacaoTemAreaObrigatoriaSemCascataECamposObrigatorios() throws Exception {
        Class<?> type = entity("AplicacaoNuclear", "TB_APLICACAO_NUCLEAR");
        idGerado(type);
        column(type, "titulo", "titulo", 150, false, false);
        column(type, "resumo", "resumo", 500, false, false);
        column(type, "descricao", "descricao", 4000, false, false);
        relationship(type, "areaAplicacao", AreaAplicacao.class, "area_aplicacao_id");
        beanProperties(type);
    }

    @Test
    void impactoTemTituloObrigatorioEUnico() throws Exception {
        Class<?> type = entity("ImpactoSocial", "TB_IMPACTO_SOCIAL");
        idGerado(type);
        column(type, "titulo", "titulo", 150, false, true);
        column(type, "descricao", "descricao", 1000, true, false);
        beanProperties(type);
    }

    @Test
    void associacaoUsaIdentidadeCompostaEDoisPaisSemCascata() throws Exception {
        Class<?> type = entity("AplicacaoImpacto", "TB_APLICACAO_IMPACTO");
        Field id = type.getDeclaredField("id");
        assertEquals(AplicacaoImpactoId.class, id.getType());
        assertNotNull(id.getAnnotation(jakarta.persistence.EmbeddedId.class));
        assertNull(id.getAnnotation(GeneratedValue.class));
        relationship(type, "aplicacao", AplicacaoNuclear.class, "aplicacao_id");
        relationship(type, "impacto", ImpactoSocial.class, "impacto_id");
        assertEquals("aplicacaoId", type.getDeclaredField("aplicacao")
                .getAnnotation(jakarta.persistence.MapsId.class).value());
        assertEquals("impactoId", type.getDeclaredField("impacto")
                .getAnnotation(jakarta.persistence.MapsId.class).value());
        column(type, "explicacao", "explicacao", 2000, false, false);
        column(type, "fonteUrl", "fonte_url", 500, true, false);
        beanProperties(type);
    }

    private static void relationship(Class<?> type, String name, Class<?> target, String column) throws Exception {
        Field field = type.getDeclaredField(name);
        assertEquals(target, field.getType());
        var relation = field.getAnnotation(jakarta.persistence.ManyToOne.class);
        assertNotNull(relation);
        assertFalse(relation.optional());
        assertEquals(0, relation.cascade().length, "Sem escritas ou exclusões em cascata");
        var join = field.getAnnotation(jakarta.persistence.JoinColumn.class);
        assertEquals(column, join.name());
        assertFalse(join.nullable());
        assertFalse(join.unique(), "Várias aplicações podem compartilhar a mesma área ou impacto");
    }

    private static Class<?> entity(String name, String table) throws Exception {
        Class<?> type = Class.forName("br.com.nexo.api.model." + name);
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals(table, type.getAnnotation(Table.class).name());
        assertEquals("", type.getAnnotation(Table.class).schema());
        assertFalse(Modifier.isFinal(type.getModifiers()));
        assertNotNull(type.getConstructor().newInstance());
        return type;
    }

    private static void idGerado(Class<?> type) throws Exception {
        Field id = type.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        var geracao = id.getAnnotation(GeneratedValue.class);
        assertNotNull(geracao);
        assertEquals(GenerationType.IDENTITY, geracao.strategy());
        assertEquals("", geracao.generator(), "Não presumir sequence ou gerador nomeado");
        assertEquals("id", id.getAnnotation(Column.class).name());
        Object entity = type.getConstructor().newInstance();
        assertNull(type.getMethod("getId").invoke(entity));
        type.getMethod("setId", Long.class).invoke(entity, 42L);
        assertEquals(42L, type.getMethod("getId").invoke(entity));
    }

    private static void column(Class<?> type, String field, String name, int length,
                               boolean nullable, boolean unique) throws Exception {
        Column column = type.getDeclaredField(field).getAnnotation(Column.class);
        assertNotNull(column, field);
        assertEquals(name, column.name(), field);
        assertEquals(length, column.length(), field);
        assertEquals(nullable, column.nullable(), field);
        assertEquals(unique, column.unique(), field);
        assertEquals("", column.columnDefinition(), "Não inventar DDL Oracle");
    }

    private static void beanProperties(Class<?> type) throws Exception {
        Object bean = type.getConstructor().newInstance();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            Object value = field.getType() == String.class ? "conteudo sintetico"
                    : field.getType() == Long.class ? 7L : field.getType().getConstructor().newInstance();
            type.getMethod("set" + suffix, field.getType()).invoke(bean, value);
            assertEquals(value, type.getMethod("get" + suffix).invoke(bean), field.getName());
        }
    }
}
