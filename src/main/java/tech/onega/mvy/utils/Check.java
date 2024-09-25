package tech.onega.mvy.utils;

import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

final public class Check {

  private static Validator VALIDATOR = Validation
    .buildDefaultValidatorFactory()
    .getValidator();

  public static void equals(final Object v1, final Object v2) {
    if (Equals.no(v1, v2)) {
      throw new StdError("Values:[\n  %s\n  !=  \n  %s\n]".formatted(v1, v2));
    }
  }

  public static void equals(
    final Object v1,
    final Object v2,
    final Lambda.Supplier<String, RuntimeException> errorMessageFactory) {
    //
    if (Equals.no(v1, v2)) {
      throw new StdError(errorMessageFactory.invoke());
    }
  }

  public static void equals(
    final Object v1,
    final Object v2,
    final String errorMessage,
    final Object... errorMessageArgs) {
    //
    if (Equals.no(v1, v2)) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void fail(final String errorMessage, final Object... errorMessageArgs) {
    throw new StdError(errorMessage.formatted(errorMessageArgs));
  }

  @Nullable
  private static String getValidErrors(final Object value) {
    if (value == null) {
      return "Value can't be null. ";
    }
    final var errors = new HashSet<ConstraintViolation<Object>>();
    if (value instanceof Iterable) {
      final var iterator = ((Iterable<?>) value).iterator();
      while (iterator.hasNext()) {
        errors.addAll(VALIDATOR.validate(iterator.next()));
        if (!errors.isEmpty()) {
          break;
        }
      }
    }
    else {
      errors.addAll(VALIDATOR.validate(value));
    }
    if (!errors.isEmpty()) {
      final var validateErrors = errors.stream()
        .map(v -> "%s | %s | %s".formatted(v.getPropertyPath(), v.getMessage(), v.toString()))
        .collect(Collectors.joining("\n"));
      return validateErrors;
    }
    else {
      return null;
    }
  }

  private static boolean isBlank(final String value) {
    return (value == null) || value.isBlank();
  }

  public static void isFalse(final boolean value) {
    if (value) {
      throw new StdError("Value != false");
    }
  }

  public static void isFalse(final boolean value, final String errorMessage, final Object... errorMessageArgs) {
    if (value) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void isNull(final Object value) {
    if (value != null) {
      throw new StdError("%s != null".formatted(value));
    }
  }

  public static void isNull(final Object value, final String errorMessage, final Object... errorMessageArgs) {
    if (value != null) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void isNull(final Object value, final Supplier<String> errorMessageFactory) {
    if (value != null) {
      throw new StdError(errorMessageFactory.get());
    }
  }

  public static void isTrue(final boolean value) {
    if (!value) {
      throw new StdError("Value != true");
    }
  }

  public static void isTrue(final boolean value, final String errorMessage, final Object... errorMessageArgs) {
    if (!value) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void isTrue(final boolean value, final Supplier<String> errorMessageFactory) {
    if (!value) {
      throw new StdError(errorMessageFactory.get());
    }
  }

  public static void notBlank(@Nullable final String value) {
    if (isBlank(value)) {
      throw new StdError("Value is blank");
    }
  }

  public static void notBlank(@Nullable final String value, final String errorMessage, final Object... errorMessageArgs) {
    if (isBlank(value)) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void notEquals(final Object v1, final Object v2) {
    if (Equals.yes(v1, v2)) {
      throw new StdError("Values are equals:\n  %s\n  %s".formatted(v1, v2));
    }
  }

  public static void notEquals(final Object v1, final Object v2, final String errorMessage, final Object... errorMessageArgs) {
    if (Equals.yes(v1, v2)) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void notNull(final Object value) {
    if (value == null) {
      throw new StdError("Value == null");
    }
  }

  public static void notNull(final Object value, final String errorMessage, final Object... errorMessageArgs) {
    if (value == null) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void valid(final Object value) {
    final var validErrors = getValidErrors(value);
    if (validErrors != null) {
      throw new StdError("Bean is not valid. " + validErrors);
    }
  }

  public static void valid(final Object value, final String errorMessage, final Object... errorMessageArgs) {
    final var validErrors = getValidErrors(value);
    if (validErrors != null) {
      throw new StdError("%s \n %s".formatted(errorMessage.formatted(errorMessageArgs), validErrors));
    }
  }

  public static <E extends Throwable> void withThrow(final Lambda.Void<E> lambda) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null) {
      throw new StdError("No throw");
    }
  }

  public static <E extends Throwable> void withThrow(final Lambda.Void<E> lambda, final String errorMessage, final Object... errorMessageArgs) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static void withThrowAndMatch(final Lambda.Void<Throwable> lambda, final Lambda.Function<@NotNull Throwable, Boolean, RuntimeException> matcher) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null || !matcher.invoke(error)) {
      throw new StdError("No throw, or wrong throw are not equals");
    }
  }

  public static void withThrowAndMatch(final Lambda.Void<Throwable> lambda, final Lambda.Function<Throwable, Boolean, RuntimeException> matcher, final String errorMessage, final Object... errorMessageArgs) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null || !matcher.invoke(error)) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

  public static <E extends Throwable> void withThrowType(final Class<E> throwType, final Lambda.Void<E> lambda) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null || !throwType.isInstance(error)) {
      throw new StdError("No throw, or wrong throw type");
    }
  }

  public static <E extends Throwable> void withThrowType(final Class<E> throwType, final Lambda.Void<E> lambda, final String errorMessage, final Object... errorMessageArgs) {
    Throwable error = null;
    try {
      lambda.invoke();
    }
    catch (final Throwable e) {
      error = e;
    }
    if (error == null || !throwType.isInstance(error)) {
      throw new StdError(errorMessage.formatted(errorMessageArgs));
    }
  }

}
