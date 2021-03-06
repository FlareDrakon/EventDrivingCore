package ru.flare.event.core.api;

import com.sun.istack.internal.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.comparator.Comparators;
import ru.flare.event.core.model.AbstractTask;
import ru.flare.event.core.processing.tasks.TaskResult;
import ru.flare.event.core.queue.QueueHolder;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * this class are basic implementation for integration wth core
 * also you can write your implementation or override it
 */
@Component
public class IntraceprtorBase implements Intraceptor {


    public static class SimpleTask implements AbstractTask {
        private LocalDateTime localDateTime;
        private Callable<?> subject;

        public SimpleTask(LocalDateTime localDateTime, Callable<?> subject) {
            this.localDateTime = localDateTime;
            this.subject = subject;
        }



        @Override
        public LocalDateTime getTaskTime() {
            return localDateTime;
        }


        @Override
        public Callable<TaskResult> getTask() {
            return () -> {
                try {
                    subject.call();
                    return TaskResult.success("");
                }
                catch (Exception e) {
                    return TaskResult.error(e.getMessage());
                }
            };
        }

        @Override
        public void setTaskTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        @Override
        public void setTask(Callable<TaskResult> task) {
            this.subject = task;
        }

        @Override
        public int compareTo(@NotNull AbstractTask o) {
            int compareResult = getTaskTime().compareTo(o.getTaskTime());
            if(compareResult == 0) {
                compareResult = 1;
            }
            return compareResult;
        }
    }

    private QueueHolder queueHolder;

    @Autowired
    public IntraceprtorBase(QueueHolder queueHolder) {
        this.queueHolder = queueHolder;
    }

    @Override
    public void accept(Callable<?> callable, LocalDateTime localDateTime) {
        //Proxy.newProxyInstance(getClass().getClassLoader(), Class[] {AbstractTask.class}, new InvocationHandler() { });
        //here we can use AOP or create implementation or other solution as we want - that class can be re implemented by integrator
        AbstractTask abstractTask = new SimpleTask(localDateTime, callable);
        queueHolder.onTask(abstractTask);
    }
}
