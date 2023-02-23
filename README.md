## Experiments with virtual threads introduced in JDK 19 as part of Project Loom

### 1. Platform vs Virtual Thread

Platform Threads:
- Managed & scheduled by OS
- Wrappers for OS threads
- System call needed to create a new kernel thread

Virtual Threads:
- Managed & scheduled by JVM
- Free of OS's context switches
- Consume OS thread only when they need CPU
- Fully compatible with the existing Thread API

Number of virtual threads can be significantly larger than the number of OS threads!

### 2. Virtual Threads

#### What **are** their goals?
- I/O
- Better scale (higher throughput) 
- Making asynchronous code more readable (sometimes even looking as synchronous?)

#### What **are not** their goals?
- Speed (lower latency)
- Improving results of heavy computational tasks
- Pooling them

#### Concepts
Carrier thread:
- Platform thread on which the JVM scheduler allocates virtual threads
- Single virtual thread can be handled by different carriers during its lifetime
- Thread.currentThread() always return the virtual thread itself, not the carrier

Scheduler:
- Scheduler assigns the virtual thread for execution on a platform thread by *mounting* the virtual thread on a platform thread
- Mounting a virtual thread means temporarily copying the needed stack frames from the heap to the stack of the carrier thread, and borrowing the carriers stack while it is mounted
- Virtual threads do not require or expect application code to explicitly hand back control to the scheduler
- Implemented using a work-stealing ForkJoinPool that operates in FIFO mode
- Every time that a virtual thread waits for e.g. I/O, it *yields* to free its carrier thread. Once the IO operation gets completed, the virtual thread is put back into the FIFO queue of the ForkJoinPool and will wait until a carrier thread is available
- Max number of platform threads available to the scheduler can be configured with system property *jdk.virtualThreadScheduler.maxPoolSize* (default = 256)

Continuation:
- Low-level API that **is not meant** to be used by developers
- Execution unit that can be started, then parked (yielded), rescheduled back, and resumed
- Once a blocking operation happens, the continuation will yield, leaving the carrier thread unblocked

That being said, for the following code:
````
var scope = new ContinuationScope("CS");
var continuation = new Continuation(scope, () -> {
    System.out.println("Start CS");
    Continuation.yield(scope);
    System.out.println("End CS");
});

while (!continuation.isDone()) {
    System.out.println("Start run()");
    continuation.run();
    System.out.println("End run()");
}
````
The result will be:
````
Start run()
Start CS
End run()
Start run()
End CS
End run()
````

### Practice time!
1. Clone and run: https://github.com/stanislaw-tokarski/dummy-http-responder
2. ``mvn clean install spring-boot:run``
