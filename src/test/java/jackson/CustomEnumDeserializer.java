package jackson;

import java.io.IOException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class CustomEnumDeserializer extends JsonDeserializer<Enum> implements ContextualDeserializer {

    private final Class<Enum> clazz;

    private final Method creatorMethod;

    public CustomEnumDeserializer(Class<Enum> clazz, DeserializationContext ctxt) {
        this.clazz = clazz;

        Method creatorMethod = null;
        if(this.clazz != null && ctxt != null) {
            for (AnnotatedMethod factoryMethod : ctxt.getConfig().introspectClassAnnotations(clazz).getFactoryMethods()) {
                if(factoryMethod.hasAnnotation(JsonCreator.class)) {
                    creatorMethod = factoryMethod.getMember();
                    break;
                }
            }
        }
        this.creatorMethod = creatorMethod;
    }

    @Override
    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if(JsonToken.VALUE_STRING.equals(p.currentToken())) {
            return fromValue(p.getValueAsString());
        } else if(!p.isExpectedStartObjectToken()) {
            throw InvalidFormatException.from(p, "Impossible de parser " + clazz.getName());
        }

        String enumName = null;
        while(!JsonToken.END_OBJECT.equals(p.nextToken())) {
            if(JsonToken.FIELD_NAME.equals(p.currentToken()) && "id".equals(p.getCurrentName())) {
                enumName = p.nextTextValue();
            }
        }

        if(enumName == null) {
            return null;
        }

        return fromValue(enumName);
    }


    private Enum fromValue(String value) throws IOException {
        if(this.creatorMethod != null) {
            try {
                return (Enum)creatorMethod.invoke(null, value);
            } catch (ReflectiveOperationException e) {
                throw new IOException(e);
            }
        }
        return Enum.valueOf(clazz, value);
    }


    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

        Class<?> clazz = null;

        if(property != null && property.getType().getRawClass().isEnum()) {
            clazz = property.getType().getRawClass();
        }

        if(clazz == null) {
            // Cas d'un objet générique List<Enum> ...
            clazz = ctxt.getContextualType().getRawClass();
        }
        return new CustomEnumDeserializer((Class<Enum>)clazz, ctxt);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return super.deserializeWithType(p, ctxt, typeDeserializer);
    }
}
