package ru.flare.event.core.queue;

import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.processing.Worker;

import javax.annotation.PreDestroy;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class QueueHolder {

    private Worker worker;
    private final Object monitor = new Object();
    private Logger logger = LoggerFactory.getLogger(QueueHolder.class);
    private TreeSet<AbstractTask> tasksQ = new TreeSet<>(AbstractTask::compareTo);
    //fair for save ordering id dates are same
    private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();
    private Dao<AbstractTask> taskDao;
    private EventReaderAdapter eventReaderAdapter;

    @Autowired
    public QueueHolder(Dao<AbstractTask> taskDao, EventReaderAdapter eventReaderAdapter) {
        this.taskDao = taskDao;
        this.eventReaderAdapter = eventReaderAdapter;
    }

    public void onTask(@NotNull AbstractTask abstractTask) {
        writeLock.lock();
        try {

            if(abstractTask.getTaskTime() == null) {
                logger.error("task has no time {}", abstractTask);
                return;
            }
            if(this.worker == null || !this.worker.isAlive()) {
                constructWorker();
            }

            tasksQ.add(abstractTask);
            taskDao.save(abstractTask);
            worker.run();
        }
        finally {
            writeLock.unlock();
        }

        synchronized (monitor) {
            worker.notify();
        }

    }

    private void constructWorker() {
        this.worker = new Worker(eventReaderAdapter, monitor, taskDao);
        worker.setTaskQ(tasksQ);
        worker.setLock(readLock);
    }

    @PreDestroy
    public void onShutdown() {
        if(worker != null) {
            worker.shutdown();
            synchronized (monitor) {
                worker.notify();
            }
        }
    }
}
