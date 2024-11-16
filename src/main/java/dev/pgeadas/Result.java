package dev.pgeadas;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents the result of a computation that can be either a {@code Success} or a {@code Failure}.
 *
 * @param <T> The type of the value inside the Result
 * @param <E> The type of the error inside the Result
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    /**
     * The fold method handles the result of a computation, without having to check explicitly if it is a {@code
     * Success} or a {@code Failure}.
     * <p>
     * It takes two mapper functions as arguments, one for the success case and one for the failure case, and only
     * the function that corresponds to the actual result type is applied to the value inside the result.
     * This way, we can handle both cases in a single place.
     *
     * @param onSuccess The function to apply to the value inside the result if it is a {@code Success}
     * @param onFailure The function to apply to the error inside the result if it is a {@code Failure}
     * @param <R>       The type of the value inside the {@code Result}
     * @return The {@code Result} of applying the function to the value inside of a {@code Result}
     **/
    <R> R fold(Function<? super T, ? extends R> onSuccess,
               Function<? super E, ? extends R> onFailure);

    /**
     * The flatMapAsync method is similar to flatMap, but it takes a function that returns a
     * {@code CompletableFuture<Result<U>, E>>} instead of a {@code Result<U, E>}.
     * <p>
     * 1) If the result is a {@code Success}, the mapper function is applied to the {@code T} value, and wrapped into
     * a new {@code Result} (that can be of a different type).
     * <p>
     * 2) If the result is a {@code Failure}, the mapper function is not applied and the failure is returned.
     * <p>
     * When several flatMap calls are chained, the computation will short-circuit if any of the results is a failure,
     * so we don't need to check for failures in the middle of the chain.
     * <p>
     *
     * @param mapper the function to apply to the value inside the {@code Result}
     * @param <U>    The type of the value inside the {@code Result} to return
     * @return {@code CompletableFuture<Result<U, E>>}
     **/
    <U> CompletableFuture<Result<U, E>> flatMapAsync(Function<? super T, CompletableFuture<Result<U, E>>> mapper);

    record Success<T, E>(T value) implements Result<T, E> {

        @Override
        public <R> R fold(Function<? super T, ? extends R> onSuccess,
                          Function<? super E, ? extends R> onFailure) {
            return onSuccess.apply(value);
        }

        @Override
        public <U> CompletableFuture<Result<U, E>> flatMapAsync(Function<? super T, CompletableFuture<Result<U, E>>> mapper) {
            return mapper.apply(value);
        }
    }

    record Failure<T, E>(E error) implements Result<T, E> {

        @Override
        public <R> R fold(Function<? super T, ? extends R> onSuccess,
                          Function<? super E, ? extends R> onFailure) {
            return onFailure.apply(error);
        }

        @Override
        public <U> CompletableFuture<Result<U, E>> flatMapAsync(Function<? super T, CompletableFuture<Result<U, E>>> mapper) {
            return CompletableFuture.completedFuture(new Failure<>(error));
        }
    }

    /**
     * Creates a {@link Result.Success} with the given value.
     *
     * @param value The value to wrap in a {@link Result.Success}.
     * @param <T>   The type of the value.
     * @param <E>   The type of the error.
     * @return A {@link Result.Success} with the given value.
     */
    static <T, E> Success<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a {@link Result.Failure} with the given error.
     *
     * @param error The error to wrap in a {@link Result.Failure}.
     * @param <T>   The type of the value.
     * @param <E>   The type of the error.
     * @return A {@link Result.Failure} with the given error.
     */
    static <T, E> Failure<T, E> failure(E error) {
        return new Failure<>(error);
    }

}

