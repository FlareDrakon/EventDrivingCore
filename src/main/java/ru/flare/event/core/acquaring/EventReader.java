package ru.flare.event.core.acquaring;

/**
 * this is marker
 * @param <T> is extension for type save
 */
public interface EventReader<T extends Event> {
    void onEvent(Event event);

    Class<T> getSupportedEventType();
}
