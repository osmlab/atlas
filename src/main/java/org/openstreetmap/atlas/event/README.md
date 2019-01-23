#   EventService
    The EventService is a class build on top of Guava's EventBus with
    static keyword construction. It provides an non-blocking endpoint to
    send events of various types to be processed asynchronously during program execution.


#   When to use
    When you want to record activities of events during program execution
    and have those be processed, recorded, or control other threads on the fly.

#   How To Use
    1.   Get an EventService object with
    ```
    public static EventService get(final String key)
    ```

        All threads that use the same keyword to get an EventService have the same EventService object.


    2.  Extend Event into a class that contains the message information that is needed.
    3.  Implement the Processor interface
        *   Be sure to include a function that processes your desired event type
        *   Make sure all Processor implementations have the @Subscribe annotation above both their specific event process method and about the process(ShutdownEvent) method
    4.  At the end of your job call EventService::complete to communicate to the processors that program execution is complete


    There are multiple and varied flexible use cases for this usage pattern. 