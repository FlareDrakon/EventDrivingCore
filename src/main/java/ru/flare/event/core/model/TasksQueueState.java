package ru.flare.event.core.model;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.acquaring.Event;
import ru.flare.event.core.acquaring.EventReader;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TasksQueueState implements EventReader<Event> {

    private LocalDateTime lastTaskTime = LocalDateTime.now();

    private Dao<AbstractTask> tasksDao;

    @Autowired
    public TasksQueueState(Dao<AbstractTask> tasksDao) {
        this.tasksDao = tasksDao;
        List<AbstractTask> lastQueue = tasksDao.findAll("taskTime");
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


    public LocalDateTime getLastTaskTime() {
        return lastTaskTime;
    }

    public void setLastTaskTime(LocalDateTime lastTaskTime) {
        this.lastTaskTime = lastTaskTime;
    }

    public Dao<AbstractTask> getTasksDao() {
        return tasksDao;
    }

    public void setTasksDao(Dao<AbstractTask> tasksDao) {
        this.tasksDao = tasksDao;
    }
}
