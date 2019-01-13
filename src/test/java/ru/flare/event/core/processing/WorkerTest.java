package ru.flare.event.core.processing;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.api.IntraceprtorBase;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.mockito.Matchers.any;

public class WorkerTest {

    @Mock
    private EventReaderAdapter eventReaderAdapter;

    @Mock
    private Dao<AbstractTask> abstractTaskDao;
    private Object dummyMonitor;

    @Mock
    private ReentrantReadWriteLock.ReadLock readLock;
    private PriorityQueue<AbstractTask> testQ = new PriorityQueue<>();


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void EmptyQExitWorkerTest() throws Exception {
        dummyMonitor = new Object();
        Worker worker = new Worker(eventReaderAdapter, dummyMonitor, abstractTaskDao);
        worker.setLock(readLock);
        worker.setTaskQ(testQ);
        worker.run();
        Mockito.verify(eventReaderAdapter, Mockito.never()).onEvent(any());
    }

    @Test
    public void oneTaskInQWorkerTest() throws Exception {
        dummyMonitor = new Object();
        IntraceprtorBase.SimpleTask simpleTask = new IntraceprtorBase.SimpleTask(LocalDateTime.now(), () -> null);
        testQ.add(simpleTask);
        Worker worker = new Worker(eventReaderAdapter, dummyMonitor, abstractTaskDao);
        worker.setLock(readLock);
        worker.setTaskQ(testQ);
        worker.run();
        Mockito.verify(eventReaderAdapter).onEvent(any());
        Mockito.verify(abstractTaskDao).delete(any());
    }

    @After
    public void tearDown() throws Exception {
        testQ.clear();
    }
}
