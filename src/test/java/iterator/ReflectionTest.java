package iterator;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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

    public String getFoo() {
      return foo;
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }
  }

  public static class ExtendedTestBean extends TestBean {

    private String bar;
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
  void shouldReturnDefaultWhenGetAnnotationMemberDefault() {
    assertThat(Reflection.getAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
  }

  @Test
  void shouldThrowGivenNonExistentMemberWhenGetAnnotationMemberDefault() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.getAnnotationMemberDefault(TestAnnotation.class, "does not exist");
        });
  }

  @Test
  void shouldReturnDefaultWhenFindAnnotationMemberDefault() {
    assertThat(Reflection.findAnnotationMemberDefault(TestAnnotation.class, "someMember"), is(FOO));
  }

  @Test
  void shouldReturnNullGivenNonExistentMemberWhenFindAnnotationMemberDefault() {
    assertThat(
        Reflection.findAnnotationMemberDefault(TestAnnotation.class, "does not exist"),
        nullValue());
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
  void shouldThrowGivenNonExistentFieldWhenGetField() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
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
  void shouldReturnNullGivenNonExistentFieldWhenFindField() {
    assertThat(Reflection.findField(TestBean.class, "does not exist"), nullValue());
  }

  @Test
  void shouldReturnAnnotationWhenGetFieldAnnotation() throws Exception {
    // given
    TestAnnotation expected =
        TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
    // when
    TestAnnotation actual =
        Reflection.getFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
    // then
    assertThat(actual, is(expected));
  }

  @Test
  void shouldReturnAnnotationWhenFindFieldAnnotation() throws Exception {
    // given
    TestAnnotation expected =
        TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
    // when
    TestAnnotation actual =
        Reflection.findFieldAnnotation(TestBean.class, "foo", TestAnnotation.class);
    // then
    assertThat(actual, is(expected));
  }

  @Test
  void shouldReturnAnnotationMemberValue() throws Exception {
    // given
    TestAnnotation anno =
        TestBean.class.getDeclaredField("foo").getAnnotation(TestAnnotation.class);
    // when
    String actual = Reflection.getAnnotationMemberValue(anno, "someMember");
    // then
    assertThat(actual, is(BAR));
  }

  @Test
  void shouldReturnNullGivenNonExistentFieldWhenFindFieldAnnotation() {
    assertThat(
        Reflection.findFieldAnnotation(TestBean.class, "does not exist", TestAnnotation.class),
        nullValue());
  }

  @Test
  void shouldInstantiateClassWithNoArgConstructor() {
    // given
    TestBean expected = new TestBean();
    // when
    TestBean actual = Reflection.newInstance(TestBean.class);
    // then
    assertThat(actual, is(expected));
  }

  @Test
  void shouldInstantiateImmutableBeanWhenNewInstance() {
    // given
    ImmutableBean expected = new ImmutableBean(FOO);
    // when
    ImmutableBean actual = Reflection.newInstance(ImmutableBean.class, FOO);
    // then
    assertThat(actual, is(expected));
  }

  @Test
  void shouldThrowGivenWhenNoNoArgConstructorPresentGivenNoArgsWhenNewInstance() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.newInstance(ImmutableBean.class);
        });
  }

  @Test
  void shouldThrowGivenWhenOnlyNoArgConstructorGivenArgsWhenNewInstance() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.newInstance(TestBean.class, FOO);
        });
  }

  @Test
  void shouldReturnAnnotationMemberTypeGivenMemberNameWhenGetAnnotationMemberType()
      throws Exception {
    assertThat(
        Reflection.getAnnotationMemberType(TestAnnotation.class, "someMember").getName(),
        is(String.class.getName()));
  }

  @Test
  void shouldThrowGivenNonExistentMemberNameWhenGetAnnotationMemberType() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.getAnnotationMemberType(TestAnnotation.class, "does not exist as a member");
        });
  }

  @Test
  void shouldReturnAnnotationWhenFindTypeAnnotation() {
    // given
    TestAnnotation expected = AnnotationUtils.findAnnotation(TestBean.class, TestAnnotation.class);
    // when
    TestAnnotation actual = Reflection.findTypeAnnotation(TestBean.class, TestAnnotation.class);
    // then
    assertThat(actual, is(expected));
  }

  @Test
  void shouldSetFieldToValueGivenExistingFieldNameOnNonNullInstanceWhenSet() {
    // given
    ImmutableBean bean = new ImmutableBean("foo");
    // when
    Reflection.setField(bean, "foo", "bar");
    // then
    assertThat(bean.getFoo(), is("bar"));
  }

  @Test
  void shouldSetFieldToNullGivenExistingFieldNameOnNonNullInstanceWhenSet() {
    // given
    ImmutableBean bean = new ImmutableBean("foo");
    // when
    Reflection.setField(bean, "foo", null);
    // then
    assertThat(bean.getFoo(), nullValue());
  }

  @Test
  void shouldThrowGivenNonExistentFieldNameWhenSet() {
    // given
    ImmutableBean bean = new ImmutableBean("foo");
    // then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.setField(bean, "notAFieldOnThisClass", "foo");
        });
  }

  @Test
  void shouldThrowGivenNullInstanceWhenSet() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.setField(null, "foo", "bar");
        });
  }

  @Test
  void shouldThrowGivenNullFieldNameWhenSet() {
    // given
    ImmutableBean bean = new ImmutableBean("foo");
    // then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Reflection.setField(bean, null, "bar");
        });
  }

  @Test
  void shouldSetSuperclassFieldWhenSet() {
    // given
    ExtendedTestBean bean = new ExtendedTestBean();
    // when
    Reflection.setField(bean, "foo", "baz");
    // then
    assertThat(bean.getFoo(), is("baz"));
  }

  @Test
  void shouldReturnNullGivenNoAnnotationPresentWhenFindTypeAnnotation() {
    assertThat(
        Reflection.findTypeAnnotation(ImmutableBean.class, TestAnnotation.class), nullValue());
  }

  @Test
  void shouldThrowGivenNullInstanceWhenGetFieldValue() {
    assertThrows(IllegalArgumentException.class, () -> Reflection.getFieldValue(null, "foo"));
  }

  @Test
  void shouldThrowGivenNonExistentFieldNameWhenGetFieldValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Reflection.getFieldValue(new ImmutableBean("bar"), "bar"));
  }

  @Test
  void shouldReturnFieldValueOnInstanceWhenGetFieldValue() {
    // given
    ImmutableBean bean = new ImmutableBean("bar");
    // when
    String actual = Reflection.getFieldValue(bean, "foo");
    // then
    assertThat(actual, is("bar"));
  }

  @Test
  void shouldReturnFieldValueFromSuperclassOfInstanceWhenGetFieldValue() {
    // given
    ExtendedTestBean bean = new ExtendedTestBean();
    bean.setFoo("bar");
    // when
    String actual = Reflection.getFieldValue(bean, "foo");
    // then
    assertThat(actual, is("bar"));
  }
}
