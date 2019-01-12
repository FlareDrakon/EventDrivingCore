# Event Driving Core

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://github.com/FlareDrakon/EventDrivingCore)

This is a simple project example with implementation of Event driving paradigm
use that [link] for more information

#### Building for source

```sh
gradlew build
```
# Getting Started
use spring component scan on ru.flare.event.core.*
Implement your configuration for Dao<AbstractTask> Bean
And then use like that:

```java
import ru.flare.event.core.api.IntraceprtorBase;
@Component
public class example {
    @Autowired
    private IntraceprtorBase introceptorBase;

    public void reactor() {
        introceptorBase.accept(() -> {
            //some logic as you need
            return true;
            //for for make it works immediately
        }, LocalDateTime.now())
    }
}
```


### Todos

 - Write MORE Tests

   [link]: https://github.com/iluwatar/java-design-patterns/tree/master/event-driven-architecture