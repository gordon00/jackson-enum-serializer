package jackson;

import java.io.IOException;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomEnumSerialDeserialTest {

    private ObjectMapper objectMapper;

    @Before
    public void initObjectMapper() {
        this.objectMapper = new ObjectMapper();

        final SimpleModule customEnumMappingModule = new SimpleModule("customEnumMappingModule");
        customEnumMappingModule.addSerializer(Enum.class, new CustomEnumSerializer());
        customEnumMappingModule.addDeserializer(Enum.class, new CustomEnumDeserializer(null, null));
        objectMapper.registerModule(customEnumMappingModule);
    }

    @Test
    public void testEnumSerialWithJsonValue() throws JsonProcessingException {
        // GIVEN
        final EnumWithCreator input = EnumWithCreator.BBB;

        // WHEN
        final String jsonOutput = objectMapper.writeValueAsString(input);

        // THEN
        assertThat(jsonOutput).isEqualTo("{\"id\":\"2b\",\"libelle\":\"BBB\"}");
    }

    @Test
    public void testEnumDeserialWithJsonCreator() throws IOException {
        // GIVEN
        final String jsonInput = "{\"id\":\"2b\",\"libelle\":\"BBB\"}";

        // WHEN
        final EnumWithCreator enumOutput = objectMapper.readValue(jsonInput, EnumWithCreator.class);

        // THEN
        assertThat(enumOutput).isEqualTo(EnumWithCreator.BBB);
    }

    enum EnumWithCreator {
        AAA("1a"),
        BBB("2b"),
        CCC("3c");

        private final String code;

        EnumWithCreator(String code) {
            this.code = code;
        }

        @JsonValue
        public String getCode() {
            return code;
        }

        @JsonCreator
        public static EnumWithCreator deserialize(String code) {
            for (EnumWithCreator value : EnumWithCreator.values()) {
                if(Objects.equals(value.code, code)) {
                    return value;
                }
            }

            return null;
        }
    }
}
