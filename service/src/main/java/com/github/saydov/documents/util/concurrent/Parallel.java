package com.github.saydov.documents.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public final class Parallel {

    private Parallel() {
    }

    public static <T> IoBatch<T> batch(Collection<T> items, int batchSize) {
        return new IoBatch<>(items, batchSize);
    }

    private static <T> List<List<T>> partition(Collection<T> items, int batchSize) {
        if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be > 0");

        var list = (items instanceof List<T> l) ? l : new ArrayList<>(items);
        var result = new ArrayList<List<T>>((list.size() + batchSize - 1) / batchSize);

        for (int i = 0; i < list.size(); i += batchSize) {
            result.add(List.copyOf(list.subList(i, Math.min(i + batchSize, list.size()))));
        }

        return result;
    }

    public static final class IoBatch<T> {
        private final List<List<T>> partitions;
        private int concurrency = Integer.MAX_VALUE;

        private IoBatch(Collection<T> items, int batchSize) {
            this.partitions = partition(items, batchSize);
        }

        public IoBatch<T> concurrency(int max) {
            if (max <= 0) throw new IllegalArgumentException("concurrency must be > 0");
            this.concurrency = max;
            return this;
        }

        public <R> List<R> map(Function<List<T>, R> mapper) {
            if (partitions.isEmpty()) return List.of();

            var semaphore = concurrency != Integer.MAX_VALUE ? new Semaphore(concurrency) : null;

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = new ArrayList<CompletableFuture<R>>(partitions.size());

                for (var partition : partitions) {
                    if (semaphore != null) semaphore.acquireUninterruptibly();

                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            return mapper.apply(partition);
                        } finally {
                            if (semaphore != null) semaphore.release();
                        }
                    }, executor));
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                return futures.stream().map(CompletableFuture::join).toList();
            }
        }
    }
}
