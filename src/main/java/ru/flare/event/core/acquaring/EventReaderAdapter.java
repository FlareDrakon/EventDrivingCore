package ru.flare.event.core.acquaring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * this class mask type difference of Reaction holders from Event producers
 */
@Component
@Slf4j
public class EventReaderAdapter {

    List<EventReader<?>> eventReaders;

    private Map<? extends Class<?>, List<EventReader<? extends Event>>> classEventReaderMap = new HashMap<>();

    @Autowired
    public EventReaderAdapter(List<EventReader<? extends Event>> eventReaders) {
        this.eventReaders = eventReaders;
    }

    @PostConstruct
    public void init() {
        classEventReaderMap = eventReaders.stream().collect(Collectors.groupingBy(EventReader::getSupportedEventType, Collectors.toList()));
    }

    public <T extends Event> void onEvent(T event) {
        List<EventReader<? extends Event>> typeReaders = classEventReaderMap.get(event.getClass());
        List<EventReader<? extends Event>> globalReaders = classEventReaderMap.get(Event.class);

        if(typeReaders != null) {
            typeReaders.forEach(eventReader -> eventReader.onEvent(event));
        }

        if(globalReaders != null) {
            globalReaders.forEach(eventReader -> eventReader.onEvent(event));
        }

        if(globalReaders == null && typeReaders == null) {
            logger.warn("no handlers for event: {}", event);
        }
    }
}
