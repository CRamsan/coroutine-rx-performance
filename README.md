# Coroutine RxJava2 Performance Comparison

For some projects I have been working with, I have been wanting to migrate them from RxJava2 to Coroutines. I personally 
like coroutines, and I would lie if I said that I was not a little bias. But I wanted to understand how RxJava and 
Coroutines performed and get some real numbers that would hopefully show the benefits of coroutines.

The Kotlin's page has an [example of how coroutines are lightweight](https://kotlinlang.org/docs/coroutines-basics.html#coroutines-are-light-weight)
and they are able to launch 100 thousand concurrent jobs. This seems impressive but it does not show how it compared
to other solutions nor how their concurrency model scales.

To get this data I wanted to simulate some tasks that were similar to something you would find in a real world scenario.
I decided to test a network call followed by two DB operations. And then to scale this problem, I wanted to launch as
many of this tasks as I could concurrently to see how they would behave when executed using suspending functions
and when run through RxJava2.

To do this I created this small application. You can set the number of concurrent tasks to launch, if you want to run 
them serially or concurrently and if you would like to run them using RxJava or Coroutines.

<img alt="application screen" src="/assets/app.png"/>

The app will measure them memory consumption before and after running the tasks, so you can see the change in memory 
usage. It also provides a button to kill the app as a way to flush the memory by killing the process.

# Methodology

A single use-case would be as follow:

1. Launch app.
2. Update the number of tasks to execute
3. Set the mode to Rx or Coroutines
4. Press `Run`.
5. Wait for the tasks to complete running.
6. Save the duration and memory information.
7. Kill the app.

I will then run this same operations but I will change the # of concurrent tasks. For each # of concurrent tasks, I will
run the operations once in Rx mode and then in Coroutine mode.

## Data collected

You can find the spreadsheet alongside with some graphs [here](https://docs.google.com/spreadsheets/d/1RXvJlm5MNJVDg6iuqC57l2obt3-YDVmVFajD1ng9Z5k/edit?usp=sharing).

The experiment was run on three targets. A Pixel 4 XL Emulator, a real Pixel 2 and a Galaxy S21. Each target has a table that shows the numbers for duration to execute all tasks in milliseconds
and a table for the change in total memory usage at the end of the operation. The delta columns show the change in value from the Rx case to the coroutine case.

The Pixel4 XL target represents an ideal situation, as it uses the desktop's CPU as well as having access to lower
latency network due to the wired internet connected. Both other physical devices run on a fully charged device with a 
wifi network connection.

### Pixel 4 XL Emulator - AMD Ryzen 9 3900X 12-Core Processor 3.79 GHz 64GB RAM

<img alt="pixel 4 xl emulator metrics" src="/assets/pixel4sim.png"/>

|                        | Duration(ms) |           |       |          |
|------------------------|--------------|-----------|-------|----------|
| \# of concurrent tasks | Rx           | Coroutine | Delta | Delta(%) |
| 10                     | 528          | 502       | \-26  | 4.92%    |
| 20                     | 559          | 513       | \-46  | 8.23%    |
| 50                     | 806          | 551       | \-255 | 31.64%   |
| 100                    | 1286         | 707       | \-579 | 45.02%   |
| 150                    | 1423         | 813       | \-610 | 42.87%   |
| 200                    | 1709         | 976       | \-733 | 42.89%   |
| 250                    | 2007         | 1133      | \-874 | 43.55%   |
| 500                    | 2434         | 1685      | \-749 | 30.77%   |
| 750                    | 2763         | 2474      | \-289 | 10.46%   |
| 1000                   | 3003         | 2806      | \-197 | 6.56%    |
| 1250                   | 3213         | 3710      | 497   | \-15.47% |
| 1500                   | 3998         | 4269      | 271   | \-6.78%  |
| 2000                   | 4390         | 5076      | 686   | \-15.63% |
| 2500                   | 5215         | 7204      | 1989  | \-38.14% |
| 3000                   | 4791         | 7348      | 2557  | \-53.37% |
| 4000                   | 8632         | 9512      | 880   | \-10.19% |
| 5000                   | 9498         | 12453     | 2955  | \-31.11% |
| 7500                   | 12208        | 17727     | 5519  | \-45.21% |
| 10000                  | app crashed  | 23764     | \-    | \-%      |
| 15000                  | app crashed  | 36215     | \-    | \-%      |
| 20000                  | app crashed  | 47595     | \-    | \-%      |

|                        | Memory(mb)  |           |       |          |
|------------------------|-------------|-----------|-------|----------|
| \# of concurrent tasks | Rx          | Coroutine | Delta | Delta(%) |
| 10                     | 15          | 11        | \-4   | 26.67%   |
| 20                     | 19          | 12        | \-7   | 36.84%   |
| 50                     | 19          | 14        | \-5   | 26.32%   |
| 100                    | 60          | 17        | \-43  | 71.67%   |
| 150                    | 51          | 19        | \-32  | 62.75%   |
| 200                    | 76          | 20        | \-56  | 73.68%   |
| 250                    | 68          | 21        | \-47  | 69.12%   |
| 500                    | 98          | 28        | \-70  | 71.43%   |
| 750                    | 119         | 16        | \-103 | 86.55%   |
| 1000                   | 131         | 23        | \-108 | 82.44%   |
| 1250                   | 125         | 16        | \-109 | 87.20%   |
| 1500                   | 136         | 23        | \-113 | 83.09%   |
| 2000                   | 174         | 22        | \-152 | 87.36%   |
| 2500                   | 204         | 22        | \-182 | 89.22%   |
| 3000                   | 240         | 23        | \-217 | 90.42%   |
| 4000                   | 320         | 27        | \-293 | 91.56%   |
| 5000                   | 384         | 25        | \-359 | 93.49%   |
| 7500                   | 543         | 41        | \-502 | 92.45%   |
| 10000                  | app crashed | 23        | \-    | \-%      |
| 15000                  | app crashed | 31        | \-    | \-%      |
| 20000                  | app crashed | 21        | \-    | \-%      |

### Pixel 2 - Physical device

<img alt="pixel 2 metrics" src="/assets/pixel2.png"/>

|                        | Duration(ms) |           |       |          |
|------------------------|--------------|-----------|-------|----------|
| \# of concurrent tasks | Rx           | Coroutine | Delta | Delta(%) |
| 10                     | 597          | 775       | 178   | \-29.82% |
| 15                     | 642          | 657       | 15    | \-2.34%  |
| 20                     | 722          | 658       | \-64  | 8.86%    |
| 30                     | 730          | 785       | 55    | \-7.53%  |
| 50                     | 825          | 1009      | 184   | \-22.30% |
| 75                     | 987          | 1045      | 58    | \-5.88%  |
| 100                    | 1317         | 1111      | \-206 | 15.64%   |
| 250                    | 2031         | 2338      | 307   | \-15.12% |
| 500                    | 2378         | 3584      | 1206  | \-50.71% |

|                        | Memory(mb) |           |       |          |
|------------------------|------------|-----------|-------|----------|
| \# of concurrent tasks | Rx         | Coroutine | Delta | Delta(%) |
| 10                     | 12         | 11        | \-1   | 8.33%    |
| 15                     | 13         | 10        | \-3   | 23.08%   |
| 20                     | 17         | 11        | \-6   | 35.29%   |
| 30                     | 22         | 12        | \-10  | 45.45%   |
| 50                     | 31         | 14        | \-17  | 54.84%   |
| 75                     | 26         | 15        | \-11  | 42.31%   |
| 100                    | 27         | 17        | \-10  | 37.04%   |
| 250                    | 48         | 20        | \-28  | 58.33%   |
| 500                    | 60         | 28        | \-32  | 53.33%   |

### Galaxy S21 - Physical device

<img alt="galaxy s21 metrics" src="/assets/galaxys21.png"/>

|                        | Duration(ms) |           |       |           |
|------------------------|--------------|-----------|-------|-----------|
| \# of concurrent tasks | Rx           | Coroutine | Delta | Delta(%)  |
| 10                     | 289          | 361       | 72    | \-24.91%  |
| 15                     | 306          | 391       | 85    | \-27.78%  |
| 20                     | 374          | 433       | 59    | \-15.78%  |
| 30                     | 357          | 542       | 185   | \-51.82%  |
| 50                     | 534          | 694       | 160   | \-29.96%  |
| 75                     | 492          | 1012      | 520   | \-105.69% |
| 100                    | 654          | 1413      | 759   | \-116.06% |
| 250                    | 1041         | 2702      | 1661  | \-159.56% |
| 500                    | 2041         | 4227      | 2186  | \-107.10% |

|                        | Memory(mb) |           |       |          |
|------------------------|------------|-----------|-------|----------|
| \# of concurrent tasks | Rx         | Coroutine | Delta | Delta(%) |
| 10                     | 15         | 11        | \-4   | 26.67%   |
| 15                     | 17         | 13        | \-4   | 23.53%   |
| 20                     | 18         | 13        | \-5   | 27.78%   |
| 30                     | 23         | 13        | \-10  | 43.48%   |
| 50                     | 33         | 15        | \-18  | 54.55%   |
| 75                     | 25         | 17        | \-8   | 32.00%   |
| 100                    | 33         | 4         | \-29  | 87.88%   |
| 250                    | 72         | 22        | \-50  | 69.44%   |
| 500                    | 116        | 28        | \-88  | 75.86%   |

You can find the spreadsheet alongside with some graphs [here](https://docs.google.com/spreadsheets/d/1RXvJlm5MNJVDg6iuqC57l2obt3-YDVmVFajD1ng9Z5k/edit?usp=sharing).

## Analysis

This data shows that overall, the tasks took similar or less to complete in RxJava. The execution time seems to scale 
linearly as more tasks are created. At low task counts, the difference is not mayor, but as the concurrent tasks
increases, the RxJava implementation continues to execute the tasks faster than coroutines.

This is true up to a point. In the Pixel 4 XL Emulator, when running between 7500 and 10000 concurrent tasks, the 
application will crash due to not been able to allocate any more threads. This is not the case with coroutines, as the 
data shows we have been able to run up to 20000 tasks without signs of a problem.

If we look at the memory consumption we can clearly see what is going on. During every single test, coroutines consumed
less memory compared to RxJava, not only that but as the number of tasks increased, the memory consumption remained 
mostly constant. This was not the case for RxJava, for which the memory consumption increased linearly with the number 
of concurrent tasks. If we look at the logs we can clearly see this:

```
     Caused by: java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Try again
        at java.lang.Thread.nativeCreate(Native Method)
        at java.lang.Thread.start(Thread.java:730)
        at java.util.concurrent.ThreadPoolExecutor.addWorker(ThreadPoolExecutor.java:941)
        at java.util.concurrent.ThreadPoolExecutor.ensurePrestart(ThreadPoolExecutor.java:1582)
        at java.util.concurrent.ScheduledThreadPoolExecutor.delayedExecute(ScheduledThreadPoolExecutor.java:313)
        at java.util.concurrent.ScheduledThreadPoolExecutor.schedule(ScheduledThreadPoolExecutor.java:550)
        at java.util.concurrent.ScheduledThreadPoolExecutor.submit(ScheduledThreadPoolExecutor.java:651)
        at io.reactivex.internal.schedulers.NewThreadWorker.scheduleActual(NewThreadWorker.java:145)
        at io.reactivex.internal.schedulers.IoScheduler$EventLoopWorker.schedule(IoScheduler.java:253)
        at io.reactivex.Scheduler.scheduleDirect(Scheduler.java:232)
        ...
        ...
```

Here we can see the follow. We see that the exception was `OutOfMemoryError`, which is very self-explanatory. This 
happened in the `pthread_create` function, which is the native call to start a new thread. This function was called from 
`ThreadPoolExecutor.addWorker()`, so we know we are trying to create a new thread in a thread pool. And this function 
was called by RxJava's `NewThreadWorker.scheduleActual()`, as it needed more workers to be able to execute the high 
volume of concurrent tasks.

On the RxJava implementation, we are using the IO Scheduler, which is backed up by a ThreadPool. The problem is that as 
more tasks are created, the thread-pool needs to create more workers to be able to handle the new tasks. Each thread is 
heavy memory-wise as each thread needs to allocate their own stack in memory. Additionally, even though there are
multiple threads concurrently running, a lot of them will be blocked waiting for IO. The RxJava documentation mentions 
the risk of OOM errors [here](https://javadoc.io/static/io.reactivex.rxjava2/rxjava/2.1.0/io/reactivex/schedulers/Schedulers.html#io()): `Note that this scheduler may create an unbounded number of worker threads that can result in system slowdowns or OutOfMemoryError.`

In comparison, Kotlin coroutines do not see much of an increase in memory consumption. We see that there is an initial
increase and then there is an almost plateau. This is due to the different approach to concurrency used by Coroutines.
Coroutines are based on suspending function, which allows the process to stop a function(for example when needing to 
wait for IO) and continue the execution of another function. In our app we are running all the tasks in the IO Dispatcher.
This dispatcher is also based on a thread pool, but these thread never have to be blocked, as when needing to wait for IO
the function can yield to the next function. This allows the threads to be a lot more efficient, and this is what JetBrains
was talking about when showing [how coroutines are lightweight compared to threads](https://kotlinlang.org/docs/coroutines-basics.html#coroutines-are-light-weight).
Another difference is that by default, [the IO Dispatcher is bounded to a maximum number of threads](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html).
```The number of threads used by tasks in this dispatcher is limited by the value of "kotlinx.coroutines.io.parallelism" (IO_PARALLELISM_PROPERTY_NAME) system property. It defaults to the limit of 64 threads or the number of cores (whichever is larger).```

It is clear that the lightweight nature of coroutines allow them to scale much better than RxJava, but with a performance
hit as the tasks hit the limit on number of concurrent threads. But this performance hit is, in my opinion, a trade-off 
I am willing to take to be able to reduce the memory footprint of the application I work on. At the end of the day, 
different tools will work for different tasks, so it will be up to you to decide which tool fits your needs.

### Final words

This work was inspired on the need to understand the trade-off between RxJava and Coroutines. I am way more familiar with 
Coroutines than with RxJava, but I tried to do a good job at finding a good implementation that was roughly equivalent
between the two libraries. If you feel that there is a mistake on my methodology, or I there is a mistake, please let me 
know so it can be addressed. You can contact@cramsan.com or at twitter at @cramsan_dev.