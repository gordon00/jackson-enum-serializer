package jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class CustomEnumSerializer extends JsonSerializer<Enum> {

    private final Map<Class, AnnotatedMethod> cache = new HashMap<>();

    @Override
    public void serialize(Enum value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException {

        final String strValue;
        if (value == null) {
            strValue = null;
        } else {
            AnnotatedMethod annotatedMethod;
            if (!cache.containsKey(value.getClass())) {
                annotatedMethod = serializers.getConfig().introspectClassAnnotations(value.getClass()).findJsonValueMethod();
                cache.put(value.getClass(), annotatedMethod);
            } else {
                annotatedMethod = cache.get(value.getClass());
            }

            if (annotatedMethod != null) {
                try {
                    if (!annotatedMethod.getMember().isAccessible()) {
                        annotatedMethod.getMember().setAccessible(true);
                    }
                    strValue = (String) annotatedMethod.callOn(value);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else {
                strValue = value.name();
            }
        }

        gen.writeStartObject();
        gen.writeStringField("id", strValue);
        gen.writeStringField("libelle", value == null ? null : value.toString());
        gen.writeEndObject();
    }
}

