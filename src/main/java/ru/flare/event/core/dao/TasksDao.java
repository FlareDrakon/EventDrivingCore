package ru.flare.event.core.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flare.event.core.model.AbstractTask;

/**
 * this dao can be replaced to rabbitmq/kafka if need
 * but we does not need rabbit only for load/save so I use JPA storage
 */
@Repository
public interface TasksDao extends JpaRepository<AbstractTask, Long>{
}
