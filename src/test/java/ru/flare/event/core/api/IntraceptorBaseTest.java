package ru.flare.event.core.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import ru.flare.event.core.acquaring.EventReaderAdapter;
import ru.flare.event.core.dao.Dao;
import ru.flare.event.core.model.AbstractTask;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class IntraceptorBaseTest {

    private IntraceprtorBase intraceprtorBase;

    @Mock
    private Dao<AbstractTask> taskDao;

    @Mock
    private EventReaderAdapter eventReaderAdapter;

    private DummyQHandler queueHolder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        queueHolder = new DummyQHandler(taskDao, eventReaderAdapter);
        intraceprtorBase = new IntraceprtorBase(queueHolder);
    }

    private Map<Callable<?>, LocalDateTime>  testData = new HashMap<>();


    @Test
    public void testSortUsingCompare() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Callable<Object> pastCallable = () -> null;
        Callable<Object> nowCallable = () -> null;
        Callable<Object> futureCallable = () -> null;
        testData.put(futureCallable, now.plusHours(1));
        testData.put(nowCallable, now);
        testData.put(pastCallable, now.minusHours(1));
        testData.forEach((callable, localDateTime) ->
            intraceprtorBase.accept(callable, localDateTime)
        );
        Assert.assertEquals(queueHolder.getTasksQ().size(), 3);
        Assert.assertTrue(queueHolder.getTasksQ().first().getTaskTime().isEqual(now.minusHours(1)));
        Assert.assertTrue(queueHolder.getTasksQ().last().getTaskTime().isEqual(now.plusHours(1)));
    }

    @Test
    public void testEqualsDatesHasInsertOrdering() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime now2 = LocalDateTime.now();
        Callable<Boolean> nowCallable = () -> false;
        Callable<Boolean> nowCallable2 = () -> true;
        testData.put(nowCallable2, now);
        testData.put(nowCallable, now2);
        testData.forEach((callable, localDateTime) ->
            intraceprtorBase.accept(callable, localDateTime)
        );
        Assert.assertEquals(queueHolder.getTasksQ().size(), 2);
        Assert.assertTrue(queueHolder.getTasksQ().first().getTaskTime() == now);
        Assert.assertTrue(queueHolder.getTasksQ().last().getTaskTime() == now2);
    }
}
