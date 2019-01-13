package ru.flare.event.core.acquaring;

import java.time.LocalDateTime;

@FunctionalInterface
public interface Event {

    LocalDateTime getEventTime();
}
