package com.practice.HA;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Executor {
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static void run(Runnable r, long init, long delay, TimeUnit u){
        service.scheduleWithFixedDelay(r, init, delay, u);
    }


}
