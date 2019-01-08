package ru.flare.event.core.model;

import ru.flare.event.core.processing.tasks.TaskResult;


import java.time.LocalDateTime;
import java.util.concurrent.Callable;


public interface AbstractTask extends Comparable<AbstractTask> {
    LocalDateTime getTaskTime();

    Callable<TaskResult> getTask();

    void setTaskTime(LocalDateTime localDateTime);

    void setTask(Callable<TaskResult> task);
}
