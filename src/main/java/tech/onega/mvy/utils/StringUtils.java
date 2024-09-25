package tech.onega.mvy.utils;

public class StringUtils {

  public static boolean isAnyValueIsNotBlank(final Object... vals) {
    for (final var val : vals) {
      if (val == null) {
        continue;
      }
      if (isNotBlank(val.toString())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isNotBlank(final String value) {
    return value != null && !value.isBlank();
  }

}
