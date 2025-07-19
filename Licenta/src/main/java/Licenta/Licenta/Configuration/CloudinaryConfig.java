package Licenta.Licenta.Configuration;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
 @Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dhmwppzhn");
        config.put("api_key", "296442131218422");
        config.put("api_secret", "yag06U2kymAKc-RqeyJSwmvg7nU");
        return new Cloudinary(config);
    }
}
