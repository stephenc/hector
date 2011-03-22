package me.prettyprint.hector.api.beans;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.prettyprint.cassandra.serializers.AsciiSerializer;
import me.prettyprint.cassandra.serializers.BigIntegerSerializer;
import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.cassandra.serializers.ShortSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.utils.ByteBufferOutputStream;
import me.prettyprint.hector.api.Serializer;

import org.apache.cassandra.utils.ByteBufferUtil;

@SuppressWarnings("rawtypes")
public class DynamicComposite extends AbstractList<Object> implements
    Comparable<DynamicComposite> {

  static final Logger logger = Logger.getLogger(DynamicComposite.class
      .getName());

  public static final Map<Class<? extends Serializer>, String> DEFAULT_SERIALIZER_TO_COMPARER_MAPPING;

  static {
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING = new HashMap<Class<? extends Serializer>, String>();
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(AsciiSerializer.class,
        "AsciiType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(BigIntegerSerializer.class,
        "IntegerType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(BooleanSerializer.class,
        "BytesType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(ByteBufferSerializer.class,
        "BytesType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(BytesArraySerializer.class,
        "BytesType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING
        .put(LongSerializer.class, "LongType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(ShortSerializer.class,
        "BytesType");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING.put(StringSerializer.class,
        "UTF8Type");
    DEFAULT_SERIALIZER_TO_COMPARER_MAPPING
        .put(UUIDSerializer.class, "UUIDType");
  }

  public static final Map<String, Serializer> DEFAULT_COMPARER_TO_SERIALIZER_MAPPING = new HashMap<String, Serializer>();

  static {
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("AsciiType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("BytesType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("IntegerType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("LexicalUUIDType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("LongType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("TimeUUIDType",
        ByteBufferSerializer.get());
    DEFAULT_COMPARER_TO_SERIALIZER_MAPPING.put("UTF8Type",
        ByteBufferSerializer.get());

  }

  public static final Map<Byte, String> DEFAULT_ALIAS_TO_COMPARER_MAPPING = new HashMap<Byte, String>();

  static {
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 'a', "AsciiType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 'b', "BytesType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 'i', "IntegerType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 'x', "LexicalUUIDType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 'l', "LongType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 't', "TimeUUIDType");
    DEFAULT_ALIAS_TO_COMPARER_MAPPING.put((byte) 's', "UTF8Type");

  }

  public static final Map<String, Byte> DEFAULT_COMPARER_TO_ALIAS_MAPPING = new HashMap<String, Byte>();

  static {
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("AsciiType", (byte) 'a');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("BytesType", (byte) 'b');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("IntegerType", (byte) 'i');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("LexicalUUIDType", (byte) 'x');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("LongType", (byte) 'l');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("TimeUUIDType", (byte) 't');
    DEFAULT_COMPARER_TO_ALIAS_MAPPING.put("UTF8Type", (byte) 's');

  }

  Map<Class<? extends Serializer>, String> serializerToComparerMapping = DEFAULT_SERIALIZER_TO_COMPARER_MAPPING;

  Map<String, Serializer> comparerToSerializerMapping = DEFAULT_COMPARER_TO_SERIALIZER_MAPPING;

  Map<Byte, String> aliasesToComparerMapping = DEFAULT_ALIAS_TO_COMPARER_MAPPING;

  Map<String, Byte> comparerToAliasMapping = DEFAULT_COMPARER_TO_ALIAS_MAPPING;

  public class Component<T> {
    final Serializer<T> serializer;
    final T value;
    final String comparer;
    final boolean inclusive;

    public Component(T value, Serializer<T> serializer, String comparer,
        boolean inclusive) {
      this.serializer = serializer;
      this.value = value;
      this.comparer = comparer;
      this.inclusive = inclusive;
    }

    public Serializer<T> getSerializer() {
      return serializer;
    }

    public T getValue() {
      return value;
    }

    public String getComparer() {
      return comparer;
    }

    public boolean isInclusive() {
      return inclusive;
    }
  }

  List<Component> components = new ArrayList<Component>();

  ByteBuffer serialized = null;

  public DynamicComposite() {

  }

  public List<Component> getComponents() {
    return components;
  }

  public void setComponents(List<Component> components) {
    serialized = null;
    this.components = components;
  }

  public Map<Class<? extends Serializer>, String> getSerializerToComparerMapping() {
    return serializerToComparerMapping;
  }

  public void setSerializerToComparerMapping(
      Map<Class<? extends Serializer>, String> serializerToComparerMapping) {
    serialized = null;
    this.serializerToComparerMapping = serializerToComparerMapping;
  }

  public Map<String, Serializer> getComparerToSerializerMapping() {
    return comparerToSerializerMapping;
  }

  public void setComparerToSerializerMapping(
      Map<String, Serializer> comparerToSerializerMapping) {
    serialized = null;
    this.comparerToSerializerMapping = comparerToSerializerMapping;
  }

  public Map<Byte, String> getAliasesToComparerMapping() {
    return aliasesToComparerMapping;
  }

  public void setAliasesToComparerMapping(
      Map<Byte, String> aliasesToComparerMapping) {
    serialized = null;
    this.aliasesToComparerMapping = aliasesToComparerMapping;
  }

  public Map<String, Byte> getComparerToAliasMapping() {
    return comparerToAliasMapping;
  }

  public void setComparerToAliasMapping(Map<String, Byte> comparerToAliasMapping) {
    serialized = null;
    this.comparerToAliasMapping = comparerToAliasMapping;
  }

  @Override
  public int compareTo(DynamicComposite arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  private String comparerForSerializer(Serializer<?> s) {
    String comparer = serializerToComparerMapping.get(s);
    if (comparer != null) {
      return comparer;
    }
    return "BytesType";
  }

  public <T> DynamicComposite add(T value, Serializer<T> s) {
    serialized = null;

    add(value, s, comparerForSerializer(s));

    return this;

  }

  public <T> DynamicComposite add(T value, Serializer<T> s, String comparer) {
    serialized = null;

    add(value, s, comparer, false);

    return this;

  }

  @SuppressWarnings("unchecked")
  public <T> DynamicComposite add(T value, Serializer<T> s, String comparer,
      boolean inclusive) {
    serialized = null;

    components.add(new Component(value, s, comparer, inclusive));

    return this;

  }

  @Override
  public void clear() {
    serialized = null;
    components = new ArrayList<Component>();
  }

  @Override
  public int size() {
    return components.size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void add(int index, Object element) {
    serialized = null;
    Serializer s = SerializerTypeInferer.getSerializer(element);
    components.add(index, new Component(element, s, comparerForSerializer(s),
        false));
  }

  @Override
  public Object remove(int index) {
    serialized = null;
    Component prev = components.remove(index);
    if (prev != null) {
      return prev.getValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object set(int index, Object element) {
    serialized = null;
    Serializer s = SerializerTypeInferer.getSerializer(element);
    Component prev = components.set(index, new Component(element, s,
        comparerForSerializer(s), false));
    if (prev != null) {
      return prev.getValue();
    }
    return null;
  }

  @Override
  public Object get(int i) {
    Component c = components.get(i);
    if (c != null) {
      return c.getValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public ByteBuffer serialize() {
    if (serialized != null) {
      return serialized;
    }

    ByteBufferOutputStream out = new ByteBufferOutputStream();

    for (Component c : components) {
      ByteBuffer cb = c.getSerializer().toByteBuffer(c.getValue());

      if (comparerToAliasMapping.containsKey(c.getComparer())) {
        out.writeShort((short) (0x8000 | comparerToAliasMapping.get(c
            .getComparer())));
      } else {
        out.writeShort((short) c.getComparer().length());
        out.write(ByteBufferUtil.bytes(c.getComparer()));
      }
      out.write(cb.remaining());
      out.write(cb.slice());
      out.write(c.isInclusive() ? 1 : 0);
    }

    serialized = out.getByteBuffer();
    return serialized;
  }

  public void deserialize(ByteBuffer b) {
    serialized = b;
    components = new ArrayList<Component>();

    // TODO deserialize
  }

}
