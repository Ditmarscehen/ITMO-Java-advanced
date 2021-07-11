package info.kgeorgiy.ja.fadeev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {
    ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {

    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelism(threads,
                values,
                findMax(comparator), findMax(comparator));
    }

    private static <T> Function<Collection<? extends T>, T> findMax(Comparator<? super T> comparator) {
        return collection -> collection.stream().max(comparator).orElseThrow();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelism(threads, values,
                matchAll(predicate),
                matchAll(it -> it.equals(true)));
    }

    private static <T> Function<Collection<? extends T>, Boolean> matchAll(Predicate<? super T> predicate) {
        return collection -> collection.stream().allMatch(predicate);
    }


    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T, R> R parallelism(int threads,
                                 List<? extends T> values,
                                 Function<Collection<? extends T>, R> method,
                                 Function<Collection<? extends R>, R> answerMethod) throws InterruptedException {
        threads = Math.min(threads, values.size());
        int quotient = values.size() / threads;
        int remainder = values.size() % threads;
        List<List<? extends T>> lists = new ArrayList<>();
        List<R> threadsResults = new ArrayList<>(Collections.nCopies(threads, null));
        List<Thread> threadsList = new ArrayList<>();
        int l, r = 0;
        for (int i = 0; i < threads; i++) {
            int numberOfElements = quotient;
            if (i < remainder) {
                numberOfElements++;
            }
            l = r;
            r = l + numberOfElements;
            if (mapper == null) {
                int finalI = i;
                int finalL = l;
                int finalR = r;
                Thread thread = new Thread(() -> threadsResults.set(finalI, method.apply(values.subList(finalL, finalR))));
                thread.start();
                threadsList.add(thread);
            } else {
                lists.add(values.subList(l, r));
            }
        }
        if (mapper == null) {
            for (Thread thread : threadsList) {
                thread.join();
            }
            return answerMethod.apply(threadsResults);
        } else {
            return answerMethod.apply(mapper.map(method, lists));
        }
    }

}
