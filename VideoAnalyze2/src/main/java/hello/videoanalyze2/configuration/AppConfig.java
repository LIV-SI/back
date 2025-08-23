package hello.videoanalyze2.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
//        RestTemplate restTemplate = new RestTemplate();
//
//        // RestTemplate이 사용하는 메시지 컨버터 중에서 StringHttpMessageConverter를 찾아
//        // 기본 인코딩을 UTF-8로 강제 설정합니다.
//        restTemplate.getMessageConverters()
//                .stream()
//                .filter(converter -> converter instanceof StringHttpMessageConverter)
//                .forEach(converter -> ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8));
//
//        return restTemplate;
        return new RestTemplate();
    }
}
