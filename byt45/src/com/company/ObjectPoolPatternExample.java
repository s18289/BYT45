package com.company;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

abstract class ObjectPool<T> {
    private ConcurrentLinkedQueue<T> pool;
    private ScheduledExecutorService executorService;

    public ObjectPool(final int minObjects)
    {
        initialize(minObjects);
    }

    public ObjectPool(final int minObjects, final int maxObjects, final long validationInterval) {
        initialize(minObjects);

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run() {
                int size = pool.size();

                if (size < minObjects) {
                    int sizeToBeAdded = minObjects + size;
                    for (int i = 0; i < sizeToBeAdded; i++) {
                        pool.add(createObject());
                    }
                } else if (size > maxObjects) {
                    int sizeToBeRemoved = size - maxObjects;
                    for (int i = 0; i < sizeToBeRemoved; i++) {
                        pool.poll();
                    }
                }
            }
        }, validationInterval, validationInterval, TimeUnit.SECONDS);
    }

    public T borrowObject() {
        T object;
        if ((object = pool.poll()) == null)
        {
            object = createObject();
        }
        return object;
    }

    public void returnObject(T object) {
        if (object == null) {
            return;
        }
        this.pool.offer(object);
    }

    public void shutdown(){
        if (executorService != null){
            executorService.shutdown();
        }
    }

    protected abstract T createObject();

    private void initialize(final int minObjects)  {
        pool = new ConcurrentLinkedQueue<T>();
        for (int i = 0; i < minObjects; i++) {
            pool.add(createObject());
        }
    }
}

class ExportingProcess {
    private long processNo;

    public ExportingProcess(long processNo)  {
        this.processNo = processNo;
        System.out.println("Object with process # " + processNo + " was created");
    }

    public long getProcessNo() {
        return processNo;
    }
}

class ExportingTask implements Runnable {
    private ObjectPool<ExportingProcess> pool;
    private int threadNo;
    public ExportingTask(ObjectPool<ExportingProcess> pool, int threadNo){
        this.pool = pool;
        this.threadNo = threadNo;
    }

    public void run() {
        ExportingProcess exportingProcess = pool.borrowObject();
        System.out.println("Thread " + threadNo + ": Object with process # "
                + exportingProcess.getProcessNo() + " was borrowed");

        pool.returnObject(exportingProcess);

        System.out.println("Thread " + threadNo +": Object with process # "
                + exportingProcess.getProcessNo() + " was returned");
    }
}

class ObjectPoolPatternExample
{
    private ObjectPool<ExportingProcess> pool;
    private AtomicLong processNo=new AtomicLong(0);
    public void setUp() {

        pool = new ObjectPool<ExportingProcess>(4, 10, 5)
        {
            protected ExportingProcess createObject()
            {
                return new ExportingProcess( processNo.incrementAndGet());
            }
        };
    }
    public void tearDown() {
        pool.shutdown();
    }
    public void testObjectPool() {
        ExecutorService executor = Executors.newFixedThreadPool(8);

        executor.execute(new ExportingTask(pool, 1));
        executor.execute(new ExportingTask(pool, 2));
        executor.execute(new ExportingTask(pool, 3));
        executor.execute(new ExportingTask(pool, 4));
        executor.execute(new ExportingTask(pool, 5));
        executor.execute(new ExportingTask(pool, 6));
        executor.execute(new ExportingTask(pool, 7));
        executor.execute(new ExportingTask(pool, 8));

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e)

        {
            e.printStackTrace();
        }
    }
    public static void main(String args[])  {
        ObjectPoolPatternExample op = new ObjectPoolPatternExample();
        op.setUp();
        op.tearDown();
        op.testObjectPool();
    }
}