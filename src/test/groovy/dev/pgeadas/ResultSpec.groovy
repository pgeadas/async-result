package dev.pgeadas

import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class ResultSpec extends Specification {

    def "Success result contains correct string data"() {
        given:
        final value = "Test Data"
        final successResult = Result.success(value)

        expect:
        successResult.value() == value
    }

    def "Success result contains correct list data"() {
        given:
        final value = List.of(1, 2, 3)
        final successResult = Result.success(value)

        expect:
        successResult.value() == value
    }

    def "Failure result contains expected message and cause"() {
        given:
        final error = "Some error class"
        final failureResult = Result.failure(error)

        expect:
        failureResult.error() == error
    }

    def "flatMapAsync should chain successful results"() {
        given: 'a successful result'
        final success = Result.success("hello")
        final mapper = str -> CompletableFuture.completedFuture(Result.success(str + " world"))

        when: 'applying flatMapAsync'
        final result = success.flatMapAsync(mapper).get()

        then: 'the transformation is applied'
        ResultTestUtils.unwrapSuccess(result) == "hello world"
    }

    def "flatMapAsync should short-circuit on failure"() {
        given: 'a failure result'
        final error = "Some error class"
        final failure = Result.failure(error)
        final mapper = str -> CompletableFuture.completedFuture(Result.success("never called"))

        when: 'applying flatMapAsync'
        final result = failure.flatMapAsync(mapper).get()

        then: 'the original error is preserved'
        ResultTestUtils.unwrapFailure(result) == error
    }

    def "should handle exceptions in async operations"() {
        given: 'a transformation that throws an exception'
        final mapper = num -> CompletableFuture.failedFuture(new RuntimeException("async error"))

        when: 'applying the transformation'
        Result.success(5).flatMapAsync(mapper).get()

        then: 'the exception is propagated'
        def ex = thrown(ExecutionException)
        ex.cause.message == "async error"
    }

}

