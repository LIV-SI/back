package hello.livsi_0820.config;

// config/AsyncConfig.java

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // @Async 기능을 활성화합니다.
public class AsyncConfig {

    @Bean(name = "videoProcessingExecutor") // 빈의 이름을 지정합니다.
    public Executor videoProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // 기본 스레드 수를 1로 설정
        executor.setMaxPoolSize(1);  // 최대 스레드 수도 1로 설정
        executor.setQueueCapacity(50); // 작업 대기 큐의 크기를 설정 (50개까지 쌓일 수 있음)
        executor.setThreadNamePrefix("VideoWorker-");
        executor.initialize();
        return executor;
    }
}