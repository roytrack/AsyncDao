package com.tg.async.mysql;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import scala.Function1;
import scala.concurrent.ExecutionContext;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

public final class ScalaUtils {
    private ScalaUtils() {
    }

    public static <T> Future<T> scalaToVertx(scala.concurrent.Future<T> future, ExecutionContext ec) {
        Promise<T> promise = Promise.promise();
        future.onComplete(new AbstractFunction1<Try<T>, Void>() {
            @Override
            public Void apply(Try<T> v1) {
                if (v1.isSuccess()) {
                    promise.complete(v1.get());
                } else {
                    promise.fail(v1.failed().get());
                }
                return null;
            }
        }, ec);
        return promise.future();
    }

    public static <T> Future<Void> scalaToVertxVoid(scala.concurrent.Future<T> future, ExecutionContext ec) {
        Promise<Void> promise = Promise.promise();
        future.onComplete(new AbstractFunction1<Try<T>, Void>() {
            @Override
            public Void apply(Try<T> v1) {
                if (v1.isSuccess()) {
                    promise.complete();
                } else {
                    promise.fail(v1.failed().get());
                }
                return null;
            }
        }, ec);
        return promise.future();
    }


    public static <T> java.util.List<T> toJavaList(scala.collection.immutable.List<T> list) {
        return scala.collection.JavaConversions.bufferAsJavaList(list.toBuffer());
    }

    public static <T> scala.collection.immutable.List<T> toScalaList(java.util.List<T> list) {
        return scala.collection.JavaConversions.asScalaBuffer(list).toList();
    }

    public static <V> Function1<Try<V>, Void> toFunction1(Handler<AsyncResult<V>> code) {
        return new AbstractFunction1<Try<V>, Void>() {
            @Override
            public Void apply(Try<V> v1) {
                if (v1.isSuccess()) {
                    code.handle(Future.succeededFuture(v1.get()));
                } else {
                    code.handle(Future.failedFuture(v1.failed().get()));
                }
                return null;
            }
        };
    }
}
