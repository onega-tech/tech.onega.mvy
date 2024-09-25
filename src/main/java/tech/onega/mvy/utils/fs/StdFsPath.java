package tech.onega.mvy.utils.fs;

import java.util.Arrays;
import java.util.LinkedList;
import tech.onega.mvy.utils.Check;

final public class StdFsPath {

  private static final char DOT_CHAR = '.';

  public static StdFsPath of(final Object... paths) {
    Check.notNull(paths, "Paths can't be null");
    Check.isTrue(paths.length > 0, "Paths can't be empty. '%s'", Arrays.asList(paths));
    final var buffer = new StringBuilder();
    final var pathSeperatorChar = StdFs.pathSeperator().charAt(0);
    final String fullPath;
    { //build fullPath
      boolean firstPath = true;
      for (var i = 0; i < paths.length; i++) {
        final var str = paths[i].toString().trim();
        if (str.length() == 0) {
          continue;
        }
        if (firstPath) {
          firstPath = false;
          if (str.length() == 1 && str.charAt(0) == DOT_CHAR
            || str.length() > 1 && str.charAt(0) == DOT_CHAR && str.charAt(1) == pathSeperatorChar) { //add work dir
            buffer.append(StdFs.workDir().path);
            buffer.append(pathSeperatorChar);
          }
        }
        buffer.append(str);
        if (i < paths.length - 1) {
          buffer.append(pathSeperatorChar);
        }
      }
      Check.isTrue(buffer.length() > 0, "Paths can't be blank '%s'", Arrays.asList(paths));
      fullPath = buffer.toString();
      buffer.setLength(0);
    }
    final LinkedList<String> pathItems;
    {//build pathItems
      pathItems = new LinkedList<String>();
      for (var i = 0; i < fullPath.length(); i++) {
        final var c = fullPath.charAt(i);
        if (c != pathSeperatorChar) {
          buffer.append(c);
        }
        if (c == pathSeperatorChar || i == fullPath.length() - 1) {
          if (buffer.length() == 1 && buffer.charAt(0) == DOT_CHAR) {
            //skip
          }
          else if (buffer.length() == 2 && buffer.charAt(0) == DOT_CHAR && buffer.charAt(1) == DOT_CHAR) {
            //remove last and skip
            if (!pathItems.isEmpty()) {
              pathItems.removeLast();
            }
          }
          else if (buffer.length() > 0) {
            pathItems.addLast(buffer.toString());
          }
          buffer.setLength(0);
        }
      }
    }
    final String finalPath;
    {//build finalPath
      buffer.setLength(0);
      buffer.append(pathSeperatorChar);
      final var iterator = pathItems.iterator();
      while (iterator.hasNext()) {
        buffer.append(iterator.next());
        if (iterator.hasNext()) {
          buffer.append(pathSeperatorChar);
        }
      }
      finalPath = buffer.toString();
    }
    Check.notBlank(finalPath, "Bad path %s", Arrays.asList(paths));
    return new StdFsPath(finalPath);
  }

  private static String resolveName(final String path) {
    final var pos = path.lastIndexOf(StdFs.pathSeperator());
    return path.substring(pos + 1);
  }

  private final String path;

  private final String name;

  StdFsPath(final String path) {
    this.path = path;
    this.name = resolveName(path);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    else if (obj == null || obj.getClass() != StdFsPath.class) {
      return false;
    }
    return this.path.equals(((StdFsPath) obj).path);
  }

  @Override
  public int hashCode() {
    return this.path.hashCode();
  }

  public String name() {
    return this.name;
  }

  public String path() {
    return this.path;
  }

  @Override
  public String toString() {
    return this.path;
  }

}
