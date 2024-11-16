package dev.pgeadas

import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class AsyncResultSpec extends Specification {

    def "should chain successful operations"() {
        given:
        def success = completedFuture(Result.success(5))

        when:
        def result = AsyncResult.of(success)
                .thenCompose(num -> completedFuture(Result.success(num * 2)))
                .thenCompose(num -> completedFuture(Result.success(num + 1)))
                .toCompletableFuture()
                .get()

        then:
        ResultTestUtils.unwrapSuccess(result) == 11
    }

    def "should short-circuit on failure"() {
        given:
        def success = completedFuture(Result.success(5))
        def error = "Some error class"

        when:
        def result = AsyncResult.of(success)
                .thenCompose(num -> completedFuture(Result.failure(error)))
                .thenCompose(num -> CompletableFuture.completedFuture(new Exception("should not be called")))
                .toCompletableFuture()
                .get()

        then:
        ResultTestUtils.unwrapFailure(result) == error
    }

    def "should throw the original exception when the chain fails"() {
        given:
        def success = completedFuture(Result.success(5))
        def exception = new RuntimeException("async error")

        when:
        AsyncResult.of(success)
                .thenCompose(num -> CompletableFuture.failedFuture(exception))
                .thenCompose(num -> CompletableFuture.failedFuture(new Exception("should not be called")))
                .toCompletableFuture()
                .get()

        then:
        def ex = thrown(ExecutionException)
        ex.cause == exception
    }

    def "should handle multiple transformations with different types"() {
        given:
        def success = completedFuture(Result.success(5))

        when:
        def result = AsyncResult.of(success)
                .thenCompose(num -> completedFuture(Result.success(num.toString())))
                .thenCompose(str -> completedFuture(Result.success(str + "!")))
                .toCompletableFuture()
                .get()

        then:
        ResultTestUtils.unwrapSuccess(result) == "5!"
    }

    def "should return the original error when the chain fails"() {
        given:
        def success = completedFuture(Result.success(5))
        def error = "Some error class"

        when:
        def result = AsyncResult.of(success)
                .thenCompose(num -> completedFuture(Result.success(num * 2)))
                .thenCompose(num -> completedFuture(Result.failure(error)))
                .toCompletableFuture()
                .get()

        then:
        ResultTestUtils.unwrapFailure(result) == error
    }

    private static <T, E> CompletableFuture<Result<T, E>> completedFuture(Result<T, E> result) {
        return CompletableFuture.completedFuture(result)
    }
}
