package tech.onega.mvy.utils;

import java.util.Iterator;

final public class IteratorUtils {

  public static boolean equals(final Iterator<?> iteratorA, final Iterator<?> iteratorB) {
    if (iteratorA == iteratorB) {
      return true;
    }
    else if (iteratorA == null || iteratorB == null) {
      return false;
    }
    while (iteratorA.hasNext()) {
      if (!iteratorB.hasNext()) {
        return false;
      }
      if (Equals.no(iteratorA.next(), iteratorB.next())) {
        return false;
      }
    }
    return !iteratorA.hasNext() && !iteratorB.hasNext();
  }

  public static <V> int firstIndexOf(final Iterator<V> iterator, final Lambda.Function<V, Boolean, RuntimeException> filter) {
    int index = 0;
    while (iterator.hasNext()) {
      if (filter.invoke(iterator.next())) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public static <V> int firstIndexOf(final Iterator<V> iterator, final V value) {
    int i = 0;
    while (iterator.hasNext()) {
      if (Equals.yes(iterator.next(), value)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public static <V> Iterator<V> readOnly(final Iterator<V> original) {
    return new Iterator<>() {

      @Override
      public boolean hasNext() {
        return original.hasNext();
      }

      @Override
      public V next() {
        return original.next();
      }

    };
  }

}
