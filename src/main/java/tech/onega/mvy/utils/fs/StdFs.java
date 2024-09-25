package tech.onega.mvy.utils.fs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;
import jakarta.validation.constraints.NotNull;
import tech.onega.mvy.utils.Check;
import tech.onega.mvy.utils.StdError;

final public class StdFs {

  public static Stream<StdFsPath> dirList(@NotNull final StdFsPath dirPath) {
    try {
      Check.notNull(dirPath, "dirPath can't be null");
      Check.isTrue(isDir(dirPath, true), "dirPath is not directory '%s'", dirPath);
      return Files
        .list(Path.of(dirPath.path()))
        .map(nioPath -> StdFsPath.of(nioPath));
    }
    catch (final Exception e) {
      throw StdError.wrap(e);
    }
  }

  public static boolean isDir(final StdFsPath path, final boolean followLinks) {
    final var nioPath = Path.of(path.path());
    return followLinks
      ? Files.isDirectory(nioPath)
      : Files.isDirectory(nioPath, LinkOption.NOFOLLOW_LINKS);
  }

  public static boolean isFile(final StdFsPath path, final boolean followLinks) {
    final var nioPath = Path.of(path.path());
    return followLinks
      ? Files.isRegularFile(nioPath)
      : Files.isRegularFile(nioPath, LinkOption.NOFOLLOW_LINKS);
  }

  public static boolean isSymLink(final StdFsPath path) {
    final var nioPath = Path.of(path.path());
    return Files.isSymbolicLink(nioPath);
  }

  public static String pathSeperator() {
    return File.separator;
  }

  public static char pathSeperatorChar() {
    return File.separatorChar;
  }

  public static StdFsPath workDir() {
    return new StdFsPath(Path.of(".").normalize().toAbsolutePath().toString());
  }

}