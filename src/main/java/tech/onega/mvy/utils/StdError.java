package tech.onega.mvy.utils;

final public class StdError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public static StdError wrap(final Throwable error) {
    if (error instanceof StdError) {
      return (StdError) error;
    }
    else {
      return new StdError(error);
    }
  }

  public StdError(final String message) {
    super(message);
  }

  public StdError(final String message, final Throwable cause) {
    super(message, cause);
  }

  public StdError(final Throwable cause) {
    super(cause);
  }

}
