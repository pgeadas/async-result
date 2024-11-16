package dev.pgeadas;

public class ResultTestUtils {

    public static <T, E> T unwrapSuccess(Result<T, E> result) {
        return result.fold(
                value -> value,
                error -> {
                    throw new AssertionError("Expected success but got failure: " + result);
                }
        );
    }

    public static <T, E> T unwrapSuccess(Result<T, E> result, Class<T> type) {
        return result.fold(
                value -> tryCast(value, type),
                error -> {
                    throw new AssertionError("Expected success but got failure: " + result);
                }
        );
    }

    public static <T, E> E unwrapFailure(Result<T, E> result) {
        return result.fold(
                value -> {
                    throw new AssertionError("Expected failure but got success: " + result);
                },
                error -> error
        );
    }

    private static <T> T tryCast(T value, Class<T> successType) {
        try {
            return successType.cast(value);
        } catch (ClassCastException e) {
            throw new AssertionError("Expected success of type " + successType + " but got " + value.getClass());
        }
    }
}

