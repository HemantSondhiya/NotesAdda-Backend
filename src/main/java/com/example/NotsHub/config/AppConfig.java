package com.example.NotsHub.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {

    @Value("${app.upload.async.core-pool-size:2}")
    private int uploadAsyncCorePoolSize;

    @Value("${app.upload.async.max-pool-size:4}")
    private int uploadAsyncMaxPoolSize;

    @Value("${app.upload.async.queue-capacity:25}")
    private int uploadAsyncQueueCapacity;

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean(name = "uploadTaskExecutor")
    public TaskExecutor uploadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(uploadAsyncCorePoolSize);
        executor.setMaxPoolSize(uploadAsyncMaxPoolSize);
        executor.setQueueCapacity(uploadAsyncQueueCapacity);
        executor.setThreadNamePrefix("upload-worker-");
        executor.initialize();
        return executor;
    }
}
