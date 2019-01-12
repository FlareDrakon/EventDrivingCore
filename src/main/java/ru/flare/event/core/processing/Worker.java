package ru.flare.event.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.processing.tasks.TaskResult;
import ru.flare.event.core.queue.QueueHolder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * this class are for avoid using threadpool as a timer
 */
public class Worker extends Thread
{
    private boolean isShoutDown = false;
    private Collection<AbstractTask> taskQ;
    private ReentrantReadWriteLock.ReadLock lock;
    private EventReaderAdapter eventReaderAdapter;
    private Logger logger = LoggerFactory.getLogger(QueueHolder.class);

    @Override
    public void run() {
        startListen();
    }

    private synchronized void startListen() {
        while (!isShoutDown) {
            try {
                lock.lock();
                Duration duration = Duration.of(1, ChronoUnit.SECONDS);
                try
                {
                    LocalDateTime taskTime = getPollTime();
                    duration = Duration.between(LocalDateTime.now(), taskTime);
                    if(duration.isNegative() || duration.isZero()) {
                        Optional<AbstractTask> task = taskQ.stream().findFirst();
                        if(task.isPresent()) {
                            AbstractTask abstractTask = task.get();
                            TaskResult call = abstractTask.getTask().call();
                            if(!call.isComplete()) {
                                logger.error(call.getMessage());

                            }
                            else {
                                eventReaderAdapter.onEvent(abstractTask::getTaskTime);
                            }

                            taskQ.remove(abstractTask);
                            taskTime = getPollTime();
                            duration = Duration.between(LocalDateTime.now(), taskTime);
                        }
                    }
                }
                finally {
                    lock.unlock();
                }

                Thread.currentThread().wait(duration.toMillis());
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

    public Worker(EventReaderAdapter eventReaderAdapter, String name) {
        super(name + ": " + UUID.randomUUID());
        this.eventReaderAdapter = eventReaderAdapter;
    }

    public Worker(EventReaderAdapter eventReaderAdapter) {
        super("Worker: " + UUID.randomUUID());
        this.eventReaderAdapter = eventReaderAdapter;
    }

    public void setTaskQ(TreeSet<AbstractTask> taskQ) {
        this.taskQ = taskQ;
    }

    public void setLock(ReentrantReadWriteLock.ReadLock lock) {
        this.lock = lock;
    }

    public LocalDateTime getPollTime() {
        LocalDateTime now = LocalDateTime.now();
        if(taskQ == null) {
            return now.plusSeconds(1);
        }
        if(taskQ.isEmpty()) {
            return now.plusSeconds(1);
        }
        Optional<AbstractTask> firstTask = taskQ.stream().findFirst();
        if(firstTask.isPresent()) {
            return firstTask.get().getTaskTime();
        }
        return now.plusSeconds(1);
    }

    public void shutdown() {
        isShoutDown = true;
    }
}
