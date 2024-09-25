package tech.onega.mvy.mvn;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.onega.mvy.utils.Check;
import tech.onega.mvy.utils.Equals;
import tech.onega.mvy.utils.Nullable;

class YamlConfiguration {

  static class Key {

    final @NotBlank String name;

    final @Nullable List<String> tagChain;

    final @NotBlank String tag;

    final @Nullable String attr;

    final @Nullable String index;

    Key(final String strKey) {
      Check.notNull(strKey);
      Check.notBlank(strKey);
      this.name = strKey.trim();
      final var attrPos = this.name.lastIndexOf("@");
      if (attrPos > -1) {
        Check.isTrue(attrPos > 0 && attrPos < this.name.length() - 1, "Wrong key '%s', bad location for '@'", this.name);
        this.attr = this.name.substring(attrPos + 1);
        Check.isFalse(this.attr.contains("["), "Wrong key '%s', bad location for '@' and '['", this.name);
        Check.isFalse(this.attr.contains("]"), "Wrong key '%s', bad location for '@' and ']'", this.name);
        Check.isFalse(this.attr.contains("."), "Wrong key '%s', bad location for '@' and '.'", this.name);
      }
      else {
        this.attr = null;
      }
      final String key = attrPos < 0 ? this.name : this.name.substring(0, attrPos);
      Check.isFalse(key.contains("@"), "Wrong key '%s', bad location for '@'", this.name);
      this.tagChain = Arrays.asList(key.split("\\."));
      final var lastPart = this.tagChain.get(this.tagChain.size() - 1);
      final var indexOpenPos = lastPart.lastIndexOf("[");
      final var indexClosePos = lastPart.indexOf("]");
      if (indexOpenPos > -1 || indexClosePos > -1) {
        Check.isTrue(indexOpenPos > -1 && indexOpenPos < indexClosePos - 1, "Wrong key '%s', bad location for '[' and ']'", key);
      }
      if (indexOpenPos > -1 && indexClosePos > -1) {
        this.index = lastPart.substring(indexOpenPos + 1, indexClosePos);
        this.tag = lastPart.substring(0, indexOpenPos);
      }
      else {
        this.index = null;
        this.tag = lastPart;
      }
    }

    Key createChildKey(final Object child) {
      Check.notNull(child, "Can't create child key. Child key is null");
      return this.createChildKey(child.toString());
    }

    Key createChildKey(final String child) {
      Check.notBlank(child, "Can't create child key. Child key is null");
      Check.isNull(this.attr, "Can't create child key for key with attrs. %s | %s", this.name, child);
      return new Key(this.name + "." + child);
    }

    Key createIndexKey(final int index) {
      return this.createIndexKey(String.valueOf(index));
    }

    Key createIndexKey(final String index) {
      Check.isNull(this.attr, "Can't create index key for key with attrs. %s | %s", this.name, index);
      Check.isNull(this.index, "Can't create index key for key with index. %s | %s", this.name, index);
      return new Key(this.name + "[" + index + "]");
    }

    @Override
    public boolean equals(final Object obj) {
      return Equals.check(this, obj, f -> new Object[] { f.name });
    }

    @Override
    public int hashCode() {
      return this.name.hashCode();
    }

    @Override
    public String toString() {
      return this.name;
    }

  }

  private static class XmlNode {

    final @NotNull String tag;

    @Nullable
    String value = null;

    final @NotNull Map<String, String> attrs = new LinkedHashMap<>();

    final @NotNull Map<String, XmlNode> children = new LinkedHashMap<>();

    XmlNode(final String tag) {
      this.tag = tag;
    }

    XmlNode getOrCreateChild(final String path) {
      var child = this.children.get(path);
      if (child == null) {
        final var openPos = path.indexOf("[");
        child = new XmlNode(openPos > 0 ? path.substring(0, openPos) : path);
        this.children.put(path, child);
      }
      return child;
    }

    void setAttr(final String name, final Object value) {
      this.attrs.put(name, value == null ? null : value.toString());
    }

    void setValue(final Object value) {
      this.value = value == null ? null : value.toString();
    }

  }

  public static YamlConfiguration createFromMap(final Map<String, Object> map) {
    final var yamlConfig = new YamlConfiguration();
    if (map != null) {
      for (final var kv : map.entrySet()) {
        yamlConfig.set(kv.getKey(), kv.getValue());
      }
    }
    return yamlConfig;
  }

  private final Map<Key, Object> data = new LinkedHashMap<>();

  public void set(final String key, final Object value) {
    this.data.put(new Key(key), value);
  }

  private Map<Key, Object> toFlatMap() {
    final var result = new LinkedHashMap<Key, Object>();
    for (final var kv : this.data.entrySet()) {
      this.toFlatMapRec(result, kv.getKey(), kv.getValue());
    }
    return result;
  }

  private void toFlatMapRec(final Map<Key, Object> result, final Key key, final Object value) {
    Check.notNull(key);
    if (value == null) {
      result.put(key, value);
    }
    else if (value instanceof Map) {
      final var map = (Map<?, ?>) value;
      for (final var kv : map.entrySet()) {
        this.toFlatMapRec(result, key.createChildKey(kv.getKey()), kv.getValue());
      }
    }
    else if (value instanceof Collection) {
      final var col = (Collection<?>) value;
      int i = 0;
      for (final var v : col) {
        this.toFlatMapRec(result, key.createIndexKey(i), v);
        i++;
      }
    }
    else if (value.getClass().isArray()) {
      final var length = Array.getLength(value);
      for (int i = 0; i < length; i++) {
        this.toFlatMapRec(result, key.createIndexKey(i), Array.get(value, i));
      }
    }
    else {
      result.put(key, value);
    }
  }

  @Override
  public String toString() {
    return this.toXml(0);
  }

  public String toXml(final int indent) {
    final var root = this.toXmlTree();
    final var result = new StringBuilder();
    for (final var child : root.children.values()) {
      this.toXmlRec(result, indent, child);
    }
    final var v = result.toString();
    return v.isEmpty() ? "" : v.substring(0, v.length() - 1);
  }

  private void toXmlRec(final StringBuilder result, final int indent, final XmlNode xmlNode) {
    final var indentStr = " ".repeat(indent);
    final var noValue = xmlNode.value == null || xmlNode.value.isEmpty();
    final var noChildren = xmlNode.children.isEmpty();
    result.append(indentStr).append("<").append(xmlNode.tag);
    { //attrs
      for (final var kv : xmlNode.attrs.entrySet()) {
        result.append(" ").append(kv.getKey()).append("=\"").append(kv.getValue()).append("\"");
      }
    }
    {//content
      if (noValue && noChildren) { //signle
        result.append("/>\n");
      }
      else if (!noValue && noChildren) {//value without children
        result
          .append(">")
          .append(xmlNode.value)
          .append("</")
          .append(xmlNode.tag)
          .append(">")
          .append("\n");
      }
      else {
        result.append(">\n");
        if (!noValue) {
          result
            .append(" ".repeat(indent + 2))
            .append(xmlNode.value)
            .append("\n");
        }
        for (final var children : xmlNode.children.values()) {
          this.toXmlRec(result, indent + 2, children);
        }
        result.append(indentStr).append("</").append(xmlNode.tag).append(">\n");
      }
    }
  }

  private XmlNode toXmlTree() {
    final var root = new XmlNode("root");
    final var flatMap = this.toFlatMap();
    for (final var kv : flatMap.entrySet()) {
      var curNode = root;
      final var tagChain = kv.getKey().tagChain;
      for (int i = 0; i < tagChain.size(); i++) {
        final var curTag = tagChain.get(i);
        curNode = curNode.getOrCreateChild(curTag);
      }
      if (kv.getKey().attr != null) {
        curNode.setAttr(kv.getKey().attr, kv.getValue());
      }
      else {
        curNode.setValue(kv.getValue());
      }
    }
    return root;
  }

}
