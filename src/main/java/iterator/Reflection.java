/**
 * Copyright © 2016 Iterator Ltd. (iteratoruk@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package iterator;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

public final class Reflection {

  @FunctionalInterface
  interface Callback<T> {

    T execute() throws ReflectiveOperationException;
  }

  private static final Logger LOG = getLogger(Reflection.class);

  public static <A extends Annotation, T> T findAnnotationMemberDefault(
      Class<A> annotationClass, String memberName) {
    return find(getAnnotationMemberDefaultCallback(annotationClass, memberName));
  }

  public static Field findField(Class<?> clazz, String fieldName) {
    return find(getFieldCallback(clazz, fieldName));
  }

  public static <A extends Annotation> A findFieldAnnotation(
      Class<?> clazz, String fieldName, Class<A> annotationClass) {
    Field f = findField(clazz, fieldName);
    return f != null ? f.getAnnotation(annotationClass) : null;
  }

  public static <A extends Annotation> A findTypeAnnotation(
      Class<?> clazz, Class<A> annotationClass) {
    return AnnotationUtils.findAnnotation(clazz, annotationClass);
  }

  public static <A extends Annotation, T> T getAnnotationMemberDefault(
      Class<A> annotationClass, String memberName) {
    return get(getAnnotationMemberDefaultCallback(annotationClass, memberName));
  }

  public static <A extends Annotation> Class<?> getAnnotationMemberType(
      Class<A> annotationClass, String memberName) {
    return get(getAnnotationMemberTypeCallback(annotationClass, memberName));
  }

  public static <A extends Annotation, T> T getAnnotationMemberValue(
      A annotation, String memberName) {
    return get(getAnnotationMemberValueCallback(annotation, memberName));
  }

  public static Field getField(Class<?> clazz, String fieldName) {
    return get(getFieldCallback(clazz, fieldName));
  }

  public static <A extends Annotation> A getFieldAnnotation(
      Class<?> clazz, String fieldName, Class<A> annotationClass) {
    return getField(clazz, fieldName).getAnnotation(annotationClass);
  }

  @SuppressWarnings("unchecked")
  public static <O, T> T getFieldValue(O instance, String fieldName) {
    try {
      Field f = findField(instance.getClass(), fieldName);
      f.setAccessible(true);
      return (T) f.get(instance);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          String.format(
              "Unable to retrieve value of field %s from instance %s", fieldName, instance),
          e);
    }
  }

  public static <T> T newInstance(Class<T> clazz, Object... args) {
    try {
      return ConstructorUtils.invokeConstructor(clazz, args);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** @see #setFieldValue(Object, String, Object) */
  @Deprecated(since = "2.2.6", forRemoval = true)
  public static <T> void setField(T instance, String fieldName, Object value) {
    setFieldValue(instance, fieldName, value);
  }

  public static <T> void setFieldValue(T instance, String fieldName, Object value) {
    try {
      Field f = findField(instance.getClass(), fieldName);
      f.setAccessible(true);
      f.set(instance, value);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Cannot set '" + fieldName + "' to '" + value + "' on " + instance, e);
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
  private static <T, A extends Annotation> Callback<T> getAnnotationMemberDefaultCallback(
      Class<A> annotationClass, String memberName) {
    return () -> (T) annotationClass.getDeclaredMethod(memberName).getDefaultValue();
  }

  private static <A extends Annotation> Callback<Class<?>> getAnnotationMemberTypeCallback(
      Class<A> annotationClass, String memberName) {
    return () -> annotationClass.getDeclaredMethod(memberName).getReturnType();
  }

  @SuppressWarnings("unchecked")
  private static <T, A extends Annotation> Callback<T> getAnnotationMemberValueCallback(
      A annotation, String memberName) {
    return () -> (T) annotation.annotationType().getDeclaredMethod(memberName).invoke(annotation);
  }

  private static Callback<Field> getFieldCallback(Class<?> clazz, String fieldName) {
    return () -> {
      Field f = ReflectionUtils.findField(clazz, fieldName);
      if (f == null) {
        throw new NoSuchFieldException(
            String.format("Could not locate field %s in class %s", fieldName, clazz));
      }
      return f;
    };
  }

  private Reflection() {
    throw new IllegalStateException();
  }
}
