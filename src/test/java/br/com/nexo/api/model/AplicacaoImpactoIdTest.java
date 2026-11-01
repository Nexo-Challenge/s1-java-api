package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AplicacaoImpactoIdTest {

    @Test
    void compositeKeyMapsBothRequiredColumns() throws Exception {
        Class<?> type = keyType();
        assertNotNull(type.getAnnotation(Embeddable.class));
        assertTrue(Serializable.class.isAssignableFrom(type));
        Object empty = type.getConstructor().newInstance();
        for (String name : List.of("aplicacaoId", "impactoId")) {
            var field = type.getDeclaredField(name);
            assertEquals(Long.class, field.getType());
            var column = field.getAnnotation(Column.class);
            assertEquals(name.equals("aplicacaoId") ? "aplicacao_id" : "impacto_id", column.name());
            assertFalse(column.nullable());
            String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            assertNull(type.getMethod("get" + suffix).invoke(empty));
            type.getMethod("set" + suffix, Long.class).invoke(empty, 9L);
            assertEquals(9L, type.getMethod("get" + suffix).invoke(empty));
        }
    }

    @Test
    void equalityAndHashCodeUseBothPartsOfThePair() throws Exception {
        Object ax = key(1L, 2L);
        Object same = key(1L, 2L);
        Object third = key(1L, 2L);
        assertEquals(ax, ax);
        assertEquals(ax, same);
        assertEquals(same, ax);
        assertEquals(same, third);
        assertEquals(ax, third);
        assertEquals(ax.hashCode(), same.hashCode());
        assertNotEquals(ax, key(1L, 3L));
        assertNotEquals(ax, key(4L, 2L));
        assertNotEquals(ax, key(2L, 1L));
        assertNotEquals(ax, null);
        assertNotEquals(ax, "1/2");
        assertEquals(3, new HashSet<>(List.of(ax, same, key(1L, 3L), key(4L, 2L))).size());
    }

    @Test
    void emptyAndPartialKeysAreNullSafe() throws Exception {
        assertEquals(key(null, null), key(null, null));
        assertEquals(key(null, null).hashCode(), key(null, null).hashCode());
        assertEquals(key(1L, null), key(1L, null));
        assertNotEquals(key(1L, null), key(null, 1L));
        assertNotEquals(key(null, null), key(1L, 2L));
    }

    @Test
    void serializationPreservesIdentity() throws Exception {
        Object original = key(10L, 20L);
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            Object restored = in.readObject();
            assertEquals(original, restored);
            assertEquals(original.hashCode(), restored.hashCode());
        }
    }

    private static Class<?> keyType() {
        return assertDoesNotThrow(() -> Class.forName("br.com.nexo.api.model.AplicacaoImpactoId"));
    }

    private static Object key(Long application, Long impact) throws Exception {
        return keyType().getConstructor(Long.class, Long.class).newInstance(application, impact);
    }
}
