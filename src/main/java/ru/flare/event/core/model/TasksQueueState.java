package ru.flare.event.core.model;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.flare.event.core.dao.TasksDao;
import ru.flare.event.core.acquaring.Event;
import ru.flare.event.core.acquaring.EventReader;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Component
public class TasksQueueState implements EventReader<Event> {


    private LocalDateTime lastTaskTime = LocalDateTime.now();

    private TasksDao tasksDao;

    @Autowired
    public TasksQueueState(TasksDao tasksDao) {
        this.tasksDao = tasksDao;
        List<AbstractTask> lastQueue = tasksDao.findAll(Sort.by(Sort.Order.asc("taskTime")));
        if(!CollectionUtils.isEmpty(lastQueue)) {
            lastTaskTime = lastQueue.get(0).getTaskTime();
        }
    }

    @Override
    public void onEvent(Event event) {
        lastTaskTime = event.getEventTime();
    }

    @Override
    public Class<Event> getSupportedEventType() {
        return Event.class;
    }
}
