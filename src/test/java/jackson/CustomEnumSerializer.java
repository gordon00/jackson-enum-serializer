package jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

public class CustomEnumSerializer extends JsonSerializer<Enum> {

    private final Map<Class, AnnotatedMember> cache = new HashMap<>();

    @Override
    public void serialize(Enum value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException {

        final Object jsonValue;
        if (value == null) {
            jsonValue = null;
        } else {
            AnnotatedMember annotatedMember;
            if (!cache.containsKey(value.getClass())) {
                annotatedMember = serializers.getConfig().introspectClassAnnotations(value.getClass()).findJsonValueAccessor();
                cache.put(value.getClass(), annotatedMember);
            } else {
                annotatedMember = cache.get(value.getClass());
            }

            if (annotatedMember != null) {
                try {
                    jsonValue = annotatedMember.getValue(value);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else {
                jsonValue = value.name();
            }
        }

        gen.writeStartObject();
        gen.writeStringField("id", jsonValue == null ? null : jsonValue.toString());
        gen.writeStringField("libelle", value == null ? null : value.toString());
        gen.writeEndObject();
    }
}

