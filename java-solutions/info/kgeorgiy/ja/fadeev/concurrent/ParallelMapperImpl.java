package info.kgeorgiy.ja.fadeev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads;
    private final Queue<Task> taskQueue;

    public ParallelMapperImpl(int threadsNumber) {
        taskQueue = new ArrayDeque<>();
        threads = Stream.generate(MapperThread::new).limit(threadsNumber).collect(Collectors.toList());
    }

    private class MapperThread extends Thread {
        public MapperThread() {
            this.start();
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Task task;
                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty()) {
                            taskQueue.wait();
                        }
                        task = taskQueue.poll();
                    }
                    task.run();
                    synchronized (task.counter) {
                        task.counter.assertTaskDone();
                        if (task.counter.isCompleted()) {
                            task.counter.notify();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class Task {
        private final Runnable runnable;
        private final Counter counter;

        Task(Runnable runnable, Counter counter) {
            this.runnable = runnable;
            this.counter = counter;
        }

        public void run() {
            this.runnable.run();
        }
    }

    private static class Counter {
        private int counter;

        Counter(int counter) {
            this.counter = counter;
        }

        public void assertTaskDone() {
            counter--;
        }

        public boolean isCompleted() {
            return counter <= 0;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        int n = args.size();
        List<R> answer = new ArrayList<>(Collections.nCopies(n, null));
        final Counter counter = new Counter(n);
        Task task = new Task(null, counter);
        for (int i = 0; i < n; i++) {
            int finalI = i;
            task = new Task(() -> answer.set(finalI, f.apply(args.get(finalI))), counter);
            synchronized (taskQueue) {
                taskQueue.add(task);
                taskQueue.notifyAll();
            }
        }

        synchronized (task.counter) {
            while (!counter.isCompleted()) {
                counter.wait();
            }
        }
        return answer;
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
    }

}
