package ru.flare.event.core.api;

import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.queue.QueueHolder;

import java.util.TreeSet;

public class DummyQHandler extends QueueHolder {

    private TreeSet<AbstractTask> tasksQ = new TreeSet<>(AbstractTask::compareTo);

    public DummyQHandler(Dao<AbstractTask> taskDao, EventReaderAdapter eventReaderAdapter) {
        super(taskDao, eventReaderAdapter);
    }

    @Override
    public void onTask(AbstractTask abstractTask) {
        tasksQ.add(abstractTask);
    }

    public TreeSet<AbstractTask> getTasksQ() {
        return tasksQ;
    }
}
