package Licenta.Licenta.Configuration;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;
 @Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        Dotenv dotenv = Dotenv.load();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", dotenv.get("CN"));
        config.put("api_key", dotenv.get("AK"));
        config.put("api_secret", dotenv.get("AS"));
        return new Cloudinary(config);
    }
}
