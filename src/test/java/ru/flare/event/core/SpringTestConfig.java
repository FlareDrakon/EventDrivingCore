package ru.flare.event.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;

import java.util.List;

@Configuration
@SpringBootApplication
public class SpringTestConfig {

    @Bean
    public Dao<AbstractTask> abstractTaskDao() {
        return new Dao<AbstractTask>() {

            public void save(AbstractTask abstractTask) {

            }

            public List<AbstractTask> findAll(String sortProperty) {
                return null;
            }

            public void delete(AbstractTask abstractTask) {
            }
        };
    }
}
