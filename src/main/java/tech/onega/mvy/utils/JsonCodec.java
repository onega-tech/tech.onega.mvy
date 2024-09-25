package tech.onega.mvy.utils;

import java.util.function.Supplier;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonCodec {

  private static class PrettyPrinter extends DefaultPrettyPrinter {

    private static final long serialVersionUID = 1L;

    public PrettyPrinter() {
      super(Separators.createDefaultInstance());
      this._objectIndenter = new DefaultIndenter("  ", "\n");
    }

  }

  private static ObjectMapper mapper = createMapper(ObjectMapper::new);

  private static ObjectWriter writer = mapper.writer();

  private static ObjectWriter writerPretty = mapper.writer().with(new PrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

  private static ObjectMapper createMapper(final Supplier<ObjectMapper> mapperFactory) {
    final var prettyPrinter = new PrettyPrinter()
      .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    final var mapper = mapperFactory.get();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, true);
    mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    mapper.setSerializationInclusion(Include.ALWAYS);
    mapper.setDefaultPropertyInclusion(Include.ALWAYS);
    /*/
    mapper.activateDefaultTypingAsProperty(BasicPolymorphicTypeValidator.builder()
      .allowIfBaseType(Temporal.class)
      .denyForExactBaseType(Duration.class)
      .build(),
      ObjectMapper.DefaultTyping.EVERYTHING,
      "__class");
      /*/
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
      .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
      .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));
    mapper.setDefaultPrettyPrinter(prettyPrinter);
    return mapper;
  }

  public static <T> T fromBytes(final byte[] jsonBytes, final Class<T> targetType) {
    try {
      return mapper.readValue(jsonBytes, targetType);
    }
    catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String toString(final Object value, final boolean pretty) {
    try {
      if (pretty) {
        return writerPretty.writeValueAsString(value);
      }
      else {
        return writer.writeValueAsString(value);
      }
    }
    catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

}
