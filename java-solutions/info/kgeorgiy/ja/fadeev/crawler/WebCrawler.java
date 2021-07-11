package info.kgeorgiy.ja.fadeev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;

    private static final int DEFAULT_DOWNLOADERS = 1;
    private static final int DEFAULT_EXTRACTORS = 1;
    private static final int DEFAULT_PER_HOST = 1;
    private static final int DEFAULT_DEPTH = 1;

    @SuppressWarnings("unused")
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        System.out.println("downloaders " + downloaders);
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(final String url, final int depth) {
        final ConcurrentMap<String, IOException> exceptions = new ConcurrentHashMap<>();
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        walk(url, exceptions, visited, depth);
        visited.removeAll(exceptions.keySet());
        return new Result(new ArrayList<>(visited), exceptions);
    }

    @Override
    public void close() {
        // :NOTE: Не дожидается окончания
        shutdownAndAwaitTermination(downloadService);
        shutdownAndAwaitTermination(extractService);

    }

    private void walk(final String startUrl, final ConcurrentMap<String, IOException> exceptions, final Set<String> visited, final int depth) {
        final Queue<String> queue = new ConcurrentLinkedQueue<>();
        queue.add(startUrl);
        visited.add(startUrl);

        IntStream.range(0, depth).forEach(layer -> {
            // :NOTE: Новый?
            Phaser phaser = new Phaser(1);
            final List<String> thisLayer = new ArrayList<>(queue);
            queue.clear();
            thisLayer.forEach(url -> {
                phaser.register();
                downloadService.execute(() -> {
                    try {
                        final Document document = downloader.download(url);
                        if (layer < depth - 1) {
                            phaser.register();
                            extractService.execute(() -> {
                                try {
                                    document.extractLinks().stream()
                                            .filter(visited::add)
                                            .forEach(queue::add);
                                } catch (final IOException ignored) {
                                } finally {
                                    phaser.arrive();
                                }
                            });
                        }
                    } catch (final IOException e) {
                        exceptions.put(url, e);
                    } finally {
                        phaser.arrive();
                    }
                });
            });

            phaser.arriveAndAwaitAdvance();
        });
    }

    public static void main(final String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("Invalid number of arguments");
            return;
        }

        final int downloaders = getValue(args, 1, DEFAULT_DOWNLOADERS);
        final int extractors = getValue(args, 2, DEFAULT_EXTRACTORS);
        final int perHost = getValue(args, 3, DEFAULT_PER_HOST);
        final int depth = getValue(args, 4, DEFAULT_DEPTH);
        final String url = args[0];
        final Downloader downloader;
        try {
            downloader = new CachingDownloader();
            final WebCrawler webCrawler = new WebCrawler(downloader, downloaders, extractors, perHost);
            webCrawler.download(url, depth);
            webCrawler.close();
        } catch (final IOException e) {
            System.err.println("Failed to create downloader " + e.getMessage());
        }

    }

    private static int getValue(final String[] args, final int i, final int defaultValue) {
        return i < args.length ? Integer.parseInt(args[i]) : defaultValue;
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
