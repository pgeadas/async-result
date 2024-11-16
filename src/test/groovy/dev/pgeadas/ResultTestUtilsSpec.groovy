package dev.pgeadas

import spock.lang.Specification
import spock.lang.Unroll

class ResultTestUtilsSpec extends Specification {

    @Unroll
    def "should return the value when result is a Success"() {
        given: "a Success result with a value"
        def result = Result.success(VALUE)

        when: "unwrapSuccess is called"
        def unwrappedValue = ResultTestUtils.unwrapSuccess(result)

        then: "the value should be correctly returned"
        unwrappedValue == VALUE

        where:
        VALUE << [
                "testValue",
                1,
                1L,
                [1, 2, 3],
                List.of('a', 'b', 'c').toList(),
                ["key": "value"]
        ]
    }

    @Unroll
    def "should return expected type, when result is a Success and providing a concrete value type"() {
        given: "a Success result with a value"
        def result = Result.success(VALUE)

        when: "unwrapSuccess is called"
        def unwrappedValue = ResultTestUtils.unwrapSuccess(result, TYPE)

        then: "the value should be correctly returned"
        TYPE.isAssignableFrom(unwrappedValue.getClass())
        unwrappedValue == VALUE

        where:
        VALUE                  | TYPE
        "testValue"            | String
        1                      | Integer
        1L                     | Long
        [1, 2, 3]              | List
        List.of('a', 'b', 'c') | List
        ["key": "value"]       | Map
    }

    @Unroll
    def "should throw AssertionError, when result is a Success and providing a wrong concrete value type"() {
        given: "a Success result with a value"
        def result = Result.success(VALUE)

        when: "unwrapSuccess is called with the wrong type"
        ResultTestUtils.unwrapSuccess(result, TYPE)

        then: "the value cannot be casted to the provided type"
        def ex = thrown(AssertionError)
        ex.message == "Expected success of type ${TYPE} but got ${VALUE.getClass()}"

        where:
        VALUE                  | TYPE
        "testValue"            | Long
        1                      | String
        1L                     | Double
        [1, 2, 3]              | Map
        List.of('a', 'b', 'c') | Set
        ["key": "value"]       | List
    }

    @Unroll
    def "should throw AssertionError when result is a Failure and Success is expected"() {
        given: "a Failure result with an error detail"
        def result = Result.failure(ERROR)

        when: "unwrapSuccess is called"
        ResultTestUtils.unwrapSuccess(result)

        then: "an AssertionError is thrown with the appropriate message"
        def exception = thrown(AssertionError)
        exception.message == "Expected success but got failure: ${result}"

        where:
        ERROR << [new Exception("error message"), "error message"]
    }

    @Unroll
    def "should throw AssertionError when result is a Failure and success type is provided"() {
        given: "a Failure result with an error detail"
        def result = Result.failure(ERROR)

        when: "unwrapSuccess is called"
        ResultTestUtils.unwrapSuccess(result, Object)

        then: "an AssertionError should be thrown with the appropriate message"
        def exception = thrown(AssertionError)
        exception.message == "Expected success but got failure: ${result}"

        where:
        ERROR << [new Exception("error message"), "error message"]
    }

    @Unroll
    def "should throw AssertionError when result is a Success and Failure is expected"() {
        given: "a Success result with an error detail"
        def result = Result.success(VALUE)

        when: "unwrapFailure is called"
        ResultTestUtils.unwrapFailure(result)

        then: "an AssertionError is thrown with the appropriate message"
        def exception = thrown(AssertionError)
        exception.message == "Expected failure but got success: ${result}"

        where:
        VALUE << [
                "testValue",
                1,
                1L,
                [1, 2, 3],
                List.of('a', 'b', 'c').toList(),
                ["key": "value"]
        ]
    }

}
