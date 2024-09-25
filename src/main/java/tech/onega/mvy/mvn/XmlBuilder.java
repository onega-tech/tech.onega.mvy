package tech.onega.mvy.mvn;

import tech.onega.mvy.utils.StringUtils;

class XmlBuilder {

  private static final String IDENT_CHAR = " ";

  private final StringBuilder xmlBuilder;

  public XmlBuilder() {
    this.xmlBuilder = new StringBuilder();
  }

  public XmlBuilder append(final String value) {
    this.xmlBuilder.append(value);
    return this;
  }

  public XmlBuilder appendIdent(final int indent) {
    this.xmlBuilder.append(IDENT_CHAR.repeat(indent));
    return this;
  }

  public XmlBuilder appendLine() {
    return this.appendLine("");
  }

  public XmlBuilder appendLine(final String line) {
    this.xmlBuilder.append(line).append("\n");
    return this;
  }

  public XmlBuilder appendTag(final int indent, final String tag, final String value) {
    this.appendIdent(indent);
    this.xmlBuilder.append("<").append(tag).append(">");
    if (value != null) {
      this.xmlBuilder.append(value);
    }
    this.xmlBuilder.append("</").append(tag).append(">");
    this.xmlBuilder.append("\n");
    return this;
  }

  public XmlBuilder appendTagClose(final int indent, final String tag) {
    this.appendIdent(indent);
    this.xmlBuilder.append("</").append(tag).append(">");
    this.xmlBuilder.append("\n");
    return this;
  }

  public XmlBuilder appendTagIfNotBlank(final int indent, final String tag, final String value) {
    return StringUtils.isNotBlank(value) ? this.appendTag(indent, tag, value) : this;
  }

  public XmlBuilder appendTagOpen(final int indent, final String tag) {
    this.appendIdent(indent);
    this.xmlBuilder.append("<").append(tag).append(">");
    this.xmlBuilder.append("\n");
    return this;
  }

  public void clear() {
    this.xmlBuilder.setLength(0);
  }

  @Override
  public String toString() {
    return this.toXml();
  }

  public String toXml() {
    return this.xmlBuilder.toString();
  }

}
