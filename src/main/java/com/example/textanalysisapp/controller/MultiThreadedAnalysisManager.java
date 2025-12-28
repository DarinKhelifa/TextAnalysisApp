package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.AnalysisResult;
import com.example.textanalysisapp.model.TextAnalyzer;
import com.example.textanalysisapp.view.FileInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedAnalysisManager {
    private ThreadPoolExecutor executor;
    private final TextAnalyzer textAnalyzer;
    private final Map<String, Future<Map<String, Object>>> futures;
    private final AtomicInteger completedCount;
    private final AtomicInteger totalFiles;
    private volatile boolean isCancelled;

    // إعدادات ThreadPool
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;

    public MultiThreadedAnalysisManager() {
        this.textAnalyzer = new TextAnalyzer();
        this.futures = new ConcurrentHashMap<>();
        this.completedCount = new AtomicInteger(0);
        this.totalFiles = new AtomicInteger(0);
        this.isCancelled = false;

        // إنشاء ThreadPoolExecutor
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                workQueue,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * بدء تحليل عدة ملفات في وقت واحد
     */
    public void analyzeFiles(List<FileInfo> fileInfos, AnalysisProgressListener listener) {
        if (fileInfos == null || fileInfos.isEmpty()) {
            if (listener != null) {
                Platform.runLater(() -> listener.onAnalysisComplete(Collections.emptyMap()));
            }
            return;
        }

        resetState();
        totalFiles.set(fileInfos.size());

        // تحديث حالة جميع الملفات إلى "Analyzing"
        Platform.runLater(() -> {
            for (FileInfo fileInfo : fileInfos) {
                fileInfo.setStatus("Analyzing");
            }
        });

        // إنشاء مهمة لكل ملف
        for (FileInfo fileInfo : fileInfos) {
            if (isCancelled) break;

            // استخدام Callable مباشرة بدلاً من Task داخل executor.submit()
            Callable<Map<String, Object>> analysisTask = createAnalysisCallable(fileInfo, listener);

            Future<Map<String, Object>> future = executor.submit(analysisTask);
            futures.put(fileInfo.getPath(), future);
        }

        // بدء مراقبة الانتهاء
        monitorCompletion(listener);
    }

    /**
     * إنشاء Callable لتحليل ملف واحد
     */
    private Callable<Map<String, Object>> createAnalysisCallable(FileInfo fileInfo, AnalysisProgressListener listener) {
        return () -> {
            try {
                if (isCancelled) {
                    Platform.runLater(() -> {
                        if (fileInfo != null) {
                            fileInfo.setStatus("Cancelled");
                        }
                    });
                    return null;
                }

                File file = new File(fileInfo.getPath());

                // التحقق من وجود الملف
                if (!file.exists() || !file.canRead()) {
                    throw new Exception("Cannot read file: " + fileInfo.getName());
                }

                // قراءة المحتوى
                String content = FileController.readFileContent(file);

                if (content == null || content.trim().isEmpty()) {
                    throw new Exception("File is empty: " + fileInfo.getName());
                }

                // التحليل
                Map<String, Object> results = textAnalyzer.analyzeText(content);
                results.put("fileName", fileInfo.getName());
                results.put("filePath", fileInfo.getPath());
                results.put("fileContent", content);
                results.put("fileSize", file.length());

                // تحديث التقدم
                int completed = completedCount.incrementAndGet();
                double progress = totalFiles.get() > 0 ? (double) completed / totalFiles.get() : 0;

                Platform.runLater(() -> {
                    if (fileInfo != null) {
                        fileInfo.setStatus("Completed");
                    }
                    if (listener != null) {
                        listener.onProgressUpdate(progress, completed, totalFiles.get());
                        listener.onFileComplete(fileInfo.getName(), results);
                    }
                });

                return results;

            } catch (Exception e) {
                // معالجة الخطأ
                Platform.runLater(() -> {
                    if (fileInfo != null) {
                        fileInfo.setStatus("Error");
                    }
                    if (listener != null) {
                        listener.onFileError(fileInfo != null ? fileInfo.getName() : "Unknown file",
                                e.getMessage());
                    }
                });
                throw e;
            }
        };
    }

    /**
     * مراقبة اكتمال جميع المهام
     */
    private void monitorCompletion(AnalysisProgressListener listener) {
        // استخدام ScheduledExecutorService للتحقق من الانتهاء
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            boolean allDone = true;
            int completed = 0;
            int errors = 0;

            for (Future<?> future : futures.values()) {
                if (future.isDone() || future.isCancelled()) {
                    completed++;
                    if (future.isCancelled()) {
                        errors++;
                    } else if (future.isDone()) {
                        try {
                            future.get(); // إذا كان هناك استثناء، سيرميه هنا
                        } catch (Exception e) {
                            errors++;
                        }
                    }
                } else {
                    allDone = false;
                }
            }

            // تحديث التقدم
            if (listener != null) {
                double progress = totalFiles.get() > 0 ? (double) completed / totalFiles.get() : 0;
                final int finalCompleted = completed;
                final int finalErrors = errors;

                boolean finalAllDone = allDone;
                Platform.runLater(() -> {
                    listener.onProgressUpdate(progress, finalCompleted, totalFiles.get());

                    // إذا انتهت جميع المهام
                    if (finalAllDone || isCancelled) {
                        // تجميع جميع النتائج
                        Map<String, AnalysisResult> allResults = new HashMap<>();
                        for (Map.Entry<String, Future<Map<String, Object>>> entry : futures.entrySet()) {
                            try {
                                if (entry.getValue().isDone() && !entry.getValue().isCancelled()) {
                                    Map<String, Object> result = entry.getValue().get();
                                    if (result != null) {
                                        AnalysisResult analysisResult = convertToAnalysisResult(result);
                                        allResults.put(entry.getKey(), analysisResult);
                                    }
                                }
                            } catch (Exception e) {
                                // تجاهل الاستثناءات
                            }
                        }

                        listener.onAnalysisComplete(allResults);
                    }
                });
            }

            // إيقاف المراقبة إذا انتهى كل شيء
            if (allDone || isCancelled) {
                scheduler.shutdown();
            }

        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * تحويل Map النتائج إلى AnalysisResult
     */
    private AnalysisResult convertToAnalysisResult(Map<String, Object> resultMap) {
        AnalysisResult result = new AnalysisResult((String) resultMap.get("fileName"));

        if (resultMap.containsKey("totalWords")) {
            Object totalWords = resultMap.get("totalWords");
            if (totalWords instanceof Integer) {
                result.setTotalWords((Integer) totalWords);
            }
        }
        if (resultMap.containsKey("uniqueWords")) {
            Object uniqueWords = resultMap.get("uniqueWords");
            if (uniqueWords instanceof Integer) {
                result.setUniqueWords((Integer) uniqueWords);
            }
        }
        if (resultMap.containsKey("mostFrequent")) {
            Object mostFrequent = resultMap.get("mostFrequent");
            if (mostFrequent instanceof String) {
                result.setMostFrequent((String) mostFrequent);
            }
        }
        if (resultMap.containsKey("charsWithSpaces")) {
            Object chars = resultMap.get("charsWithSpaces");
            if (chars instanceof Integer) {
                result.setCharacterCount((Integer) chars);
            }
        }

        return result;
    }

    /**
     * إلغاء جميع عمليات التحليل الجارية
     */
    public void cancelAll() {
        isCancelled = true;

        for (Future<?> future : futures.values()) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }

        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * إعادة تعيين الحالة
     */
    private void resetState() {
        futures.clear();
        completedCount.set(0);
        totalFiles.set(0);
        isCancelled = false;

        if (executor.isShutdown()) {
            // إعادة إنشاء الـ executor إذا كان مغلقاً
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            this.executor = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS,
                    workQueue,
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
    }

    /**
     * إغلاق الموارد
     */
    public void shutdown() {
        cancelAll();
    }

    /**
     * الواجهة لتحديث التقدم
     */
    public interface AnalysisProgressListener {
        void onProgressUpdate(double overallProgress, int completedFiles, int totalFiles);
        void onFileComplete(String fileName, Map<String, Object> results);
        void onFileError(String fileName, String errorMessage);
        void onAnalysisComplete(Map<String, AnalysisResult> allResults);
    }

    // Getters
    public int getActiveThreads() {
        return executor.getActiveCount();
    }

    public long getCompletedTasks() {
        return executor.getCompletedTaskCount();
    }

    public int getQueueSize() {
        return executor.getQueue().size();
    }

    public int getPoolSize() {
        return executor.getPoolSize();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }
}