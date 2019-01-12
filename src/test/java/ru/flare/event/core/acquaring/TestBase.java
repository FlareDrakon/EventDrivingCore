package ru.flare.event.core.acquaring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.flare.event.core.SpringTestConfig;
import ru.flare.event.core.api.IntraceprtorBase;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringJUnitConfig(value = SpringTestConfig.class)
public class TestBase {

    @Autowired
    private IntraceprtorBase intraceprtorBase;

    @Test
    public void testInit() throws Exception {

    }
}
