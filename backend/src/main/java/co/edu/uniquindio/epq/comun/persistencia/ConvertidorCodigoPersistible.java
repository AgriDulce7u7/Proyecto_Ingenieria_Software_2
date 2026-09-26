package co.edu.uniquindio.epq.comun.persistencia;

import java.util.Arrays;

import jakarta.persistence.AttributeConverter;

/**
 * Conversor JPA genérico (reutilizable por cualquier enum {@link CodigoPersistible}).
 * Evita duplicar la lógica de conversión en cada enum (DRY) y permite agregar
 * nuevos enums sin modificar esta clase (OCP).
 */
public abstract class ConvertidorCodigoPersistible<E extends Enum<E> & CodigoPersistible>
        implements AttributeConverter<E, String> {

    private final Class<E> tipo;

    protected ConvertidorCodigoPersistible(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(E valor) {
        return valor == null ? null : valor.codigo();
    }

    @Override
    public E convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }
        return Arrays.stream(tipo.getEnumConstants())
                .filter(valor -> valor.codigo().equals(codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Valor '%s' no reconocido para %s".formatted(codigo, tipo.getSimpleName())));
    }
}
