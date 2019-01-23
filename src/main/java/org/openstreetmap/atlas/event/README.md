#   EventService

The [EventService](EventService.java) is a class build on top of Guava's EventBus with
static keyword construction. It provides an non-blocking endpoint to
send events of various types to be processed asynchronously during program execution.


#   When to use
When you want to record activities of events during program execution
and have those be processed, recorded, or control other threads on the fly.

#   How To Use
1.   Get an [EventService](EventService.java) object with

```java
public static EventService get(final String key)
```
All threads on the same JVM that use the same keyword to get an EventService have the same EventService object.

2.  Extend [Event](Event.java) into a class that contains the message information that is needed.
3.  Implement the [Processor](Processor.java) interface
    *   Be sure to include a function that processes your desired event type
    *   Make sure all [Processor](Processor.java) implementations have the @Subscribe annotation above both their specific event process method and above the ```process(ShutdownEvent)``` method
4.  Construct and register all [Processor](Processor.java) objects with the EventService before sending events.
5.  Post [Event](Event.java) objects to the [EventService](EventService.java) to be processed by the appropriate registered [Processor](Processor.java)s.
6.  At the end of your job or JVM instance call EventService::complete to communicate to the processors that the program execution is complete.


There are multiple and varied flexible use cases for this usage pattern.