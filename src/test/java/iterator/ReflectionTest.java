
package iterator;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;

public class ReflectionTest {

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

    public static final String BAR = "bar";

    public static final String FOO = "foo";

    public static final String BAZ = "baz";

    @Test
    public void shouldNotBeAbleToInstantiateViaReflection() throws Exception {
        Constructor<Reflection> constructor = Reflection.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance(new Object[0]);
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void shouldReturnDefaultWhenGetAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.getAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowGivenNonExistentMemberWhenGetAnnotationMemberDefault() throws Exception {
        Reflection.getAnnotationMemberDefault(TestAnnotation.class, "does not exist");
    }

    @Test
    public void shouldReturnDefaultWhenFindAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.findAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
    }

    @Test
    public void shouldReturnNullGivenNonExistentMemberWhenFindAnnotationMemberDefault() throws Exception {
        assertThat(Reflection.findAnnotationMemberDefault(TestAnnotation.class, "does not exist"), nullValue());
    }

    @Test
    public void shouldReturnFieldWhenGetField() throws Exception {
        // given
        Field expected = TestBean.class.getDeclaredField("foo");
        // when
        Field actual = Reflection.getField(TestBean.class, "foo");
        // then
        assertThat(actual, is(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowGivenNonExistentFieldWhenGetField() throws Exception {
        Reflection.getField(TestBean.class, "does not exist");
    }

    @Test
    public void shouldReturnFieldWhenFindField() throws Exception {
        // given
        Field expected = TestBean.class.getDeclaredField("foo");
        // when
        Field actual = Reflection.findField(TestBean.class, "foo");
        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldReturnNullGivenNonExistentFieldWhenFindField() throws Exception {
        assertThat(Reflection.findField(TestBean.class, "does not exist"), nullValue());
    }

    @Test
    public void shouldReturnAnnotationWhenGetFieldAnnotation() throws Exception {
        // given
        TestAnnotation expected = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.getFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldReturnAnnotationWhenFindFieldAnnotation() throws Exception {
        // given
        TestAnnotation expected = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.findFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldReturnAnnotationMemberValue() throws Exception {
        // given
        TestAnnotation anno = TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
        // when
        String actual = Reflection.getAnnotationMemberValue(anno, "someMember");
        // then
        assertThat(actual, is(BAR));
    }

    @Test
    public void shouldReturnNullGivenNonExistentFieldWhenFindFieldAnnotation() throws Exception {
        assertThat(Reflection.findFieldAnnotation(TestBean.class, "does not exist", TestAnnotation.class), nullValue());
    }

    @Test
    public void shouldInstantiateClassWithNoArgConstructor() throws Exception {
        // given
        TestBean expected = new TestBean();
        // when
        TestBean actual = Reflection.newInstance(TestBean.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldInstantiateImmutableBeanWhenNewInstance() throws Exception {
        // given
        ImmutableBean expected = new ImmutableBean(FOO);
        // when
        ImmutableBean actual = Reflection.newInstance(ImmutableBean.class, FOO);
        // then
        assertThat(actual, is(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowGivenWhenNoNoArgConstructorPresentGivenNoArgsWhenNewInstance() throws Exception {
        Reflection.newInstance(ImmutableBean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowGivenWhenOnlyNoArgConstructorGivenArgsWhenNewInstance() throws Exception {
        Reflection.newInstance(TestBean.class, FOO);
    }

    @Test
    public void shouldReturnAnnotationMemberTypeGivenMemberNameWhenGetAnnotationMemberType() throws Exception {
        assertThat(Reflection.getAnnotationMemberType(TestAnnotation.class, "someMember").getName(), is(String.class.getName()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowGivenNonExistentMemberNameWhenGetAnnotationMemberType() throws Exception {
        Reflection.getAnnotationMemberType(TestAnnotation.class, "does not exist as a member");
    }

    @Test
    public void shouldReturnAnnotationWhenFindTypeAnnotation() throws Exception {
        // given
        TestAnnotation expected = AnnotationUtils.findAnnotation(TestBean.class, TestAnnotation.class);
        // when
        TestAnnotation actual = Reflection.findTypeAnnotation(TestBean.class, TestAnnotation.class);
        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldReturnNullGivenNoAnnotationPresentWhenFindTypeAnnotation() throws Exception {
        assertThat(Reflection.findTypeAnnotation(ImmutableBean.class, TestAnnotation.class), nullValue());
    }

}
