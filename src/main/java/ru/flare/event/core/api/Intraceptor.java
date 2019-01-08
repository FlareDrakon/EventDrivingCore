package ru.flare.event.core.api;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public interface Intraceptor {

    void accept(Callable<?> callable, LocalDateTime localDateTime);
}
