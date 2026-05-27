package Licenta.Licenta.Configuration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurare Jackson pentru a permite string-uri mari în JSON.
 * Necesar pentru endpoint-ul /annotate care primește base64 encoded images (5-10MB).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Suport pentru Java 8 date/time (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Permite string-uri de până la 50MB (base64 PNG adnotat)
        JsonFactory factory = mapper.getFactory();
        factory.setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(50_000_000) // 50MB
                        .build()
        );

        return mapper;
    }
}
