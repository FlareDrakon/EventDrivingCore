package ru.flare.event.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.processing.tasks.TaskResult;
import ru.flare.event.core.queue.QueueHolder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * this class are for avoid using threadpool as a timer
 */
public class Worker extends Thread
{
    private boolean isShoutDown = false;
    private Queue<AbstractTask> taskQ;
    private Dao<AbstractTask> tasksDao;
    private ReentrantReadWriteLock.ReadLock lock;
    private EventReaderAdapter eventReaderAdapter;
    private Logger logger = LoggerFactory.getLogger(QueueHolder.class);
    private final Object monitor;

    @Override
    public void run() {
        startListen();
    }

    private void startListen() {

        while (!isShoutDown) {
            try {
                lock.lock();
                Duration duration = Duration.of(1, ChronoUnit.SECONDS);
                try
                {
                    if(taskQ == null || taskQ.isEmpty()) {
                        return;
                    }
                    LocalDateTime taskTime = getPollTime();

                    duration = Duration.between(LocalDateTime.now(), taskTime);
                    if(duration.isNegative() || duration.isZero()) {
                        Optional<AbstractTask> task = Optional.ofNullable(taskQ.peek());
                        if(task.isPresent()) {
                            AbstractTask abstractTask = task.get();
                            TaskResult call = abstractTask.getTask().call();
                            if(!call.isComplete()) {
                                logger.error(call.getMessage());

                            }
                            else {
                                eventReaderAdapter.onEvent(abstractTask::getTaskTime);
                            }

                            taskQ.remove();
                            tasksDao.delete(abstractTask);
                            if(taskQ.isEmpty()) {
                                return;
                            }
                            taskTime = getPollTime();
                            duration = Duration.between(LocalDateTime.now(), taskTime);
                        }
                    }
                }
                finally {
                    lock.unlock();
                }

                synchronized (monitor) {
                    monitor.wait(duration.toMillis());
                }

            } catch (Exception e) {
                logger.error("Error in general common thread caused by:", e);
                Throwable t = e.getCause();
                while(t != null)
                {
                    logger.error("Global error caused by:", t);
                    t = t.getCause();
                }
                //avoid circle of Interrupt exeptions
                if(!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }

            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        //for make sure we stop listen if no more references on that class
        isShoutDown = true;
        super.finalize();
    }

    public Worker(EventReaderAdapter eventReaderAdapter, String name, Object monitor, Dao<AbstractTask> abstractTaskDao) {
        super(name + ": " + UUID.randomUUID());
        this.eventReaderAdapter = eventReaderAdapter;
        this.monitor = monitor;
        this.tasksDao = abstractTaskDao;
    }

    public Worker(EventReaderAdapter eventReaderAdapter, Object monitor, Dao<AbstractTask> abstractTaskDao) {
        super("Worker: " + UUID.randomUUID());
        this.eventReaderAdapter = eventReaderAdapter;
        this.monitor = monitor;
        this.tasksDao = abstractTaskDao;
    }

    public void setTaskQ(Queue<AbstractTask> taskQ) {
        this.taskQ = taskQ;
    }

    public void setLock(ReentrantReadWriteLock.ReadLock lock) {
        this.lock = lock;
    }

    public LocalDateTime getPollTime() {
        AbstractTask firstTask = taskQ.peek();
        if(firstTask != null) {
            return firstTask.getTaskTime();
        }
        return null;
    }

    public void shutdown() {
        isShoutDown = true;
    }
}
