package im.zego.callsdk.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * if enum class construct with int value ,and need to
 * serialized to int,use this.
 */
public class CustomTypeAdapterFactory implements TypeAdapterFactory {

    private static final String TAG = "gson";
    public static final String INT = "int";
    public static final String STRING = "java.lang.String";
    public static final String LONG = "long";
    public static final String BOOLEAN = "boolean";

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (rawType.isEnum()) {
            Map<T, TypeAndValue> map = new HashMap<>();
            for (T enumConstant : rawType.getEnumConstants()) {
                if (enumConstant == null) {
                    continue;
                }
                for (Field field : enumConstant.getClass().getDeclaredFields()) {
                    if (INT.equals(field.getType().getName())) {
                        field.setAccessible(true);
                        try {
                            int intValue = field.getInt(enumConstant);
                            TypeAndValue data = new TypeAndValue(INT, intValue);
                            map.put(enumConstant, data);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        TypeAndValue typeAndValue = map.get(value);
                        if (INT.equals(typeAndValue.type)) {
                            out.value((int) typeAndValue.value);
                        }
                    }
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                    } else {
                        T enumConstant = null;
                        String string = reader.nextString();
                        for (Entry<T, TypeAndValue> entry : map.entrySet()) {
                            T key = entry.getKey();
                            TypeAndValue typeAndValue = entry.getValue();
                            if (typeAndValue.value.toString().equals(string)) {
                                enumConstant = key;
                                break;
                            }
                        }
                        return enumConstant;
                    }
                    return null;
                }
            };
        } else {
            return null;
        }
    }

    static class TypeAndValue {

        // INT,STRING,LONG,etc.
        public String type;
        // enum's field's value
        public Object value;

        public TypeAndValue(String type, Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TypeAndValue{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
        }
    }
}
