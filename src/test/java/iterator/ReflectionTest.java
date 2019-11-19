
package iterator;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;

class ReflectionTest {

    @TestAnnotation(someMember = BAZ)
    public static class TestBean {

        @TestAnnotation(someMember = BAR)
        private String foo;

        @Override
        public boolean equals(Object obj) {
            return reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

    }

    public static class ImmutableBean {

        private final String foo;

        public ImmutableBean(String foo) {
            this.foo = foo;
        }

        @Override
        public boolean equals(Object obj) {
            return reflectionEquals(this, obj);
        }

        public String getFoo() {
            return foo;
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

    }

    private static final String BAR = "bar";

    static final String FOO = "foo";

    static final String BAZ = "baz";

    @Test
    void shouldNotBeAbleToInstantiateViaReflection() throws Exception {
        Constructor<Reflection> constructor = Reflection.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance(new Object[0]);
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
        }
    }

    @Test
    void shouldReturnDefaultWhenGetAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.getAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
    }

    @Test
    void shouldThrowGivenNonExistentMemberWhenGetAnnotationMemberDefault() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflection.getAnnotationMemberDefault(TestAnnotation.class, "does not exist");
        });
    }

    @Test
    void shouldReturnDefaultWhenFindAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.findAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
    }

    @Test
    void shouldReturnNullGivenNonExistentMemberWhenFindAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.findAnnotationMemberDefault(TestAnnotation.class, "does not exist"), nullValue());
    }

    @Test
    void shouldReturnFieldWhenGetField() throws Exception {
        // given
        Field expected = TestBean.class.getDeclaredField("foo");
        // when
        Field actual = Reflection.getField(TestBean.class, "foo");
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldThrowGivenNonExistentFieldWhenGetField() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflection.getField(TestBean.class, "does not exist");
        });
    }

    @Test
    void shouldReturnFieldWhenFindField() throws Exception {
        // given
        Field expected = TestBean.class.getDeclaredField("foo");
        // when
        Field actual = Reflection.findField(TestBean.class, "foo");
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldReturnNullGivenNonExistentFieldWhenFindField() throws Exception {
        assertThat(Reflection.findField(TestBean.class, "does not exist"), nullValue());
    }

    @Test
    void shouldReturnAnnotationWhenGetFieldAnnotation() throws Exception {
        // given
        TestAnnotation expected = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.getFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldReturnAnnotationWhenFindFieldAnnotation() throws Exception {
        // given
        TestAnnotation expected = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.findFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldReturnAnnotationMemberValue() throws Exception {
        // given
        TestAnnotation anno = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        String actual = Reflection.getAnnotationMemberValue(anno, "someMember");
        // then
        assertThat(actual, is(BAR));
    }

    @Test
    void shouldReturnNullGivenNonExistentFieldWhenFindFieldAnnotation() throws Exception {
        assertThat(Reflection.findFieldAnnotation(TestBean.class, "does not exist", TestAnnotation.class), nullValue());
    }

    @Test
    void shouldInstantiateClassWithNoArgConstructor() throws Exception {
        // given
        TestBean expected = new TestBean();
        // when
        TestBean actual = Reflection.newInstance(TestBean.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldInstantiateImmutableBeanWhenNewInstance() throws Exception {
        // given
        ImmutableBean expected = new ImmutableBean(FOO);
        // when
        ImmutableBean actual = Reflection.newInstance(ImmutableBean.class, FOO);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldThrowGivenWhenNoNoArgConstructorPresentGivenNoArgsWhenNewInstance() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflection.newInstance(ImmutableBean.class);
        });
    }

    @Test
    void shouldThrowGivenWhenOnlyNoArgConstructorGivenArgsWhenNewInstance() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflection.newInstance(TestBean.class, FOO);
        });
    }

    @Test
    void shouldReturnAnnotationMemberTypeGivenMemberNameWhenGetAnnotationMemberType() throws Exception {
        assertThat(Reflection.getAnnotationMemberType(TestAnnotation.class, "someMember").getName(), is(String.class.getName()));
    }

    @Test
    void shouldThrowGivenNonExistentMemberNameWhenGetAnnotationMemberType() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            Reflection.getAnnotationMemberType(TestAnnotation.class, "does not exist as a member");
        });
    }

    @Test
    void shouldReturnAnnotationWhenFindTypeAnnotation() throws Exception {
        // given
        TestAnnotation expected = AnnotationUtils.findAnnotation(TestBean.class, TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.findTypeAnnotation(TestBean.class, TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    void shouldReturnNullGivenNoAnnotationPresentWhenFindTypeAnnotation() throws Exception {
        assertThat(Reflection.findTypeAnnotation(ImmutableBean.class, TestAnnotation.class), nullValue());
    }

}
