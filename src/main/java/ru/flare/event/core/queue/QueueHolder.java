package ru.flare.event.core.queue;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.flare.event.core.dao.TasksDao;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.processing.Worker;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Slf4j
public class QueueHolder {

    private final Worker worker;
    private TreeSet<AbstractTask> tasksQ = new TreeSet<>(AbstractTask::compareTo);
    //fair for save ordering id dates are same
    private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();
    private TasksDao taskDao;

    @Autowired
    public QueueHolder(TasksDao taskDao, EventReaderAdapter eventReaderAdapter) {
        this.taskDao = taskDao;
        this.worker = new Worker(eventReaderAdapter);
        //avoid cross inject
        worker.setTaskQ(tasksQ);
        worker.setLock(readLock);
    }

    public void onTask(@NotNull AbstractTask abstractTask) {
        writeLock.lock();
        try {

            if(abstractTask.getTaskTime() == null) {
                logger.error("task has no time {}", abstractTask);
                return;
            }

            tasksQ.add(abstractTask);
            taskDao.save(abstractTask);
            synchronized (worker) {
                worker.notify();
            }

        }
        finally {
            writeLock.unlock();
        }
    }
}
