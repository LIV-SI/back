package hello.livsi_0820.config; // 실제 프로젝트의 패키지 경로에 맞게 수정하세요.

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 스프링의 설정 파일임을 명시합니다.
public class CORSConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // "/**"는 모든 경로에 대해 CORS 설정을 적용한다는 의미입니다.
                .allowedOrigins("http://localhost:5173/", "https://livsi.vercel.app/") //  허용할 프론트엔드 Origin(주소)을 명시합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메소드를 지정합니다.
                .allowedHeaders("*") // 허용할 요청 헤더를 지정합니다. "*"는 모든 헤더를 허용합니다.
                .allowCredentials(true) // 쿠키 등 자격 증명 정보를 포함한 요청을 허용합니다.
                .maxAge(3600); // Preflight 요청의 결과를 캐시할 시간을 초(second) 단위로 지정합니다.
    }
}