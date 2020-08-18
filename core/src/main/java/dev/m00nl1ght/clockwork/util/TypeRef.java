package dev.m00nl1ght.clockwork.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A minimalistic implemetation of the 'Super Type Token' concept from
 * <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html">Neal Gafter's blog</a>.
 *
 * @param <T> the generic type represented by this type reference
 */
public abstract class TypeRef<T> {

    private final Type type;

    public static <T> TypeRef<T> of(Class<T> theClass) {
        return new TypeRef<>(theClass) {};
    }

    private TypeRef(Class<T> theClass) {
        this.type = theClass;
    }

    protected TypeRef() {
        final var superclassType = getClass().getGenericSuperclass();
        if (superclassType instanceof ParameterizedType) {
            final var parameterizedType = (ParameterizedType) superclassType;
            if (parameterizedType.getRawType() == TypeRef.class) {
                this.type = parameterizedType.getActualTypeArguments()[0];
            } else {
                throw new RuntimeException("TypeRef is invalid");
            }
        } else {
            throw new RuntimeException("TypeRef is missing type parameters");
        }
    }

    public final Type getType() {
        return type;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeRef)) return false;
        TypeRef<?> typeRef = (TypeRef<?>) o;
        return type.equals(typeRef.type);
    }

    @Override
    public final int hashCode() {
        return type.hashCode();
    }

    @Override
    public final String toString() {
        return type.toString();
    }

    @Override
    protected final Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected final void finalize() throws Throwable {
        super.finalize();
    }

}
