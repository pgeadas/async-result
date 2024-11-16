package dev.pgeadas;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * This class is a wrapper around a {@link CompletableFuture} that provides a fluent API for chaining async operations
 * returning a {@link Result}.
 * It is used to reduce the verbosity and simplify the code when dealing with async operations that rely on
 * {@link Result} as return type.
 *
 * @param <T> The type of the value inside the result
 * @param <E> The type of the error inside the result
 */
public class AsyncResult<T, E> {
    protected final CompletableFuture<Result<T, E>> future;

    private AsyncResult(CompletableFuture<Result<T, E>> future) {
        this.future = future;
    }

    public static <T, E> AsyncResult<T, E> of(CompletableFuture<Result<T, E>> future) {
        return new AsyncResult<>(future);
    }

    public <U> AsyncResult<U, E> thenCompose(Function<T, CompletableFuture<Result<U, E>>> mapper) {
        return new AsyncResult<>(future.thenCompose(result -> result.flatMapAsync(mapper)));
    }

    public CompletableFuture<Result<T, E>> toCompletableFuture() {
        return future;
    }

}
