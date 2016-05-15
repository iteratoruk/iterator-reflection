
package iterator;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;

public final class Reflection {

    @FunctionalInterface
    static interface Callback<T> {

        T execute() throws Exception;

    }

    private static final Logger LOG = getLogger(Reflection.class);

    public static <A extends Annotation, T> T findAnnotationMemberDefault(Class<A> annotationClass, String memberName) {
        return find(getAnnotationMemberDefaultCallback(annotationClass, memberName));
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return find(getFieldCallback(clazz, fieldName));
    }

    public static <A extends Annotation> A findFieldAnnotation(Class<?> clazz, String fieldName, Class<A> annotationClass) {
        Field f = findField(clazz, fieldName);
        return f != null ? f.getAnnotation(annotationClass) : null;
    }

    public static <A extends Annotation, T> T getAnnotationMemberDefault(Class<A> annotationClass, String memberName) {
        return get(getAnnotationMemberDefaultCallback(annotationClass, memberName));
    }

    public static <A extends Annotation> Class<?> getAnnotationMemberType(Class<A> annotationClass, String memberName) {
        return get(getAnnotationMemberTypeCallback(annotationClass, memberName));
    }

    public static <A extends Annotation, T> T getAnnotationMemberValue(A annotation, String memberName) {
        return get(getAnnotationMemberValueCallback(annotation, memberName));
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return get(getFieldCallback(clazz, fieldName));
    }

    public static <A extends Annotation> A getFieldAnnotation(Class<?> clazz, String fieldName, Class<A> annotationClass) {
        return getField(clazz, fieldName).getAnnotation(annotationClass);
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) {
        try {
            return args != null && args.length > 0 ? ConstructorUtils.invokeConstructor(clazz, args) : clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static <T> T find(Callback<T> callback) {
        try {
            return callback.execute();
        } catch (Exception e) {
            LOG.debug("Find callback failed. Were you expecting a value here?", e);
            return null;
        }
    }

    private static <T> T get(Callback<T> callback) {
        try {
            return callback.execute();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, A extends Annotation> Callback<T> getAnnotationMemberDefaultCallback(Class<A> annotationClass, String memberName) {
        return () -> (T) annotationClass.getDeclaredMethod(memberName).getDefaultValue();
    }

    private static <A extends Annotation> Callback<Class<?>> getAnnotationMemberTypeCallback(Class<A> annotationClass, String memberName) {
        return () -> annotationClass.getDeclaredMethod(memberName).getReturnType();
    }

    @SuppressWarnings("unchecked")
    private static <T, A extends Annotation> Callback<T> getAnnotationMemberValueCallback(A annotation, String memberName) {
        return () -> (T) annotation.annotationType().getDeclaredMethod(memberName).invoke(annotation);
    }

    private static Callback<Field> getFieldCallback(Class<?> clazz, String fieldName) {
        return () -> clazz.getDeclaredField(fieldName);
    }

    private Reflection() {
        throw new IllegalStateException();
    }

}
