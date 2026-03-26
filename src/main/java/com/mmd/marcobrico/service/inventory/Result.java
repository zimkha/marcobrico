package com.mmd.marcobrico.service.inventory;

import com.mmd.marcobrico.exception.BusinessException;

import java.util.function.Function;

public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default T getOrThrow() {
        return switch (this) {
            case Success<T, E>  s -> s.value();
            case Failure<T, E> f -> throw new BusinessException(f.error().toString());
        };
    }

    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
          case Success<T, E> s -> new Success<>(mapper.apply(s.value()));
          case Failure<T, E> f -> new Failure<>(f.error());
        };
    }
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        return switch(this) {
            case Success<T, E> s -> mapper.apply(s.value());
            case Failure<T, E> f -> new Failure<>(f.error());
        };
    }
}
