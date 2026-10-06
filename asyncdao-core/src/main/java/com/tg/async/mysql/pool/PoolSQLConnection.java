package com.tg.async.mysql.pool;

import com.tg.async.mysql.SQLConnection;
import com.tg.async.mysql.TransactionIsolation;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.List;

/**
 * Runs every statement on the pool: a connection is borrowed for the statement and returned as soon as it completes.
 * Transactions need a dedicated connection, see {@link ConnectionPool#getConnection}.
 */
public class PoolSQLConnection implements SQLConnection {
    private final Pool pool;

    public PoolSQLConnection(Pool pool) {
        this.pool = pool;
    }

    @Override
    public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
        handler.handle(autoCommit ? Future.succeededFuture() : notSupported());
        return this;
    }

    @Override
    public SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler) {
        run(sql, null).<Void>mapEmpty().onComplete(handler);
        return this;
    }

    @Override
    public SQLConnection executeWithParams(String sql, List params, Handler<AsyncResult<Void>> handler) {
        run(sql, params).<Void>mapEmpty().onComplete(handler);
        return this;
    }

    @Override
    public SQLConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
        run(sql, null).onComplete(handler);
        return this;
    }

    @Override
    public SQLConnection queryWithParams(String sql, List params, Handler<AsyncResult<RowSet<Row>>> handler) {
        run(sql, params).onComplete(handler);
        return this;
    }

    @Override
    public SQLConnection update(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
        run(sql, null).onComplete(handler);
        return this;
    }

    @Override
    public SQLConnection updateWithParams(String sql, List params, Handler<AsyncResult<RowSet<Row>>> handler) {
        run(sql, params).onComplete(handler);
        return this;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void close() {
    }

    @Override
    public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
        handler.handle(notSupported());
        return this;
    }

    @Override
    public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
        handler.handle(notSupported());
        return this;
    }

    @Override
    public SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler) {
        handler.handle(notSupported());
        return this;
    }

    @Override
    public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private static <T> Future<T> notSupported() {
        return Future.failedFuture(new IllegalStateException("transactions need a dedicated connection, use ConnectionPool.getConnection"));
    }

    @SuppressWarnings("unchecked")
    private Future<RowSet<Row>> run(String sql, List params) {
        return params == null
                ? pool.query(sql).execute()
                : pool.preparedQuery(sql).execute(Tuple.wrap(params));
    }
}
