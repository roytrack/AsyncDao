package com.tg.async.mysql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

import java.util.List;

/**
 * A connection borrowed from the pool, used for transactions. With autoCommit=false a transaction is begun lazily
 * before the first statement and a new one is begun right after every commit/rollback; close() commits the
 * open transaction and returns the connection to the pool.
 */
public class AsyncSQLConnectionImpl implements SQLConnection {
    private final SqlConnection connection;
    /**
     * the open transaction, null when there is none
     */
    private Future<Transaction> transaction;
    private boolean inAutoCommit = true;

    public AsyncSQLConnectionImpl(SqlConnection connection) {
        this.connection = connection;
    }

    @Override
    public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
        Future<Void> fut;
        synchronized (this) {
            if (transaction != null && autoCommit) {
                fut = transaction.compose(Transaction::commit);
                transaction = null;
            } else {
                fut = Future.succeededFuture();
            }
            inAutoCommit = autoCommit;
        }
        fut.onComplete(handler);
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
        Future<Void> end;
        synchronized (this) {
            inAutoCommit = true;
            end = transaction == null ? Future.succeededFuture() : transaction.compose(Transaction::commit);
            transaction = null;
        }
        end.onComplete(ar -> connection.close().onComplete(closed -> handler.handle(ar)));
    }

    @Override
    public void close() {
        close((ar) -> {
        });
    }

    @Override
    public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
        return endAndStartTransaction(true, handler);
    }

    @Override
    public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
        return endAndStartTransaction(false, handler);
    }

    @Override
    public SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler) {
        String sql;
        switch (transactionIsolation) {
            case READ_UNCOMMITTED:
                sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                break;
            case REPEATABLE_READ:
                sql = "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                break;
            case READ_COMMITTED:
                sql = "SET TRANSACTION ISOLATION LEVEL READ COMMITTED";
                break;
            case SERIALIZABLE:
                sql = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                break;
            case NONE:
            default:
                sql = null;
                break;
        }
        if (sql == null) {
            handler.handle(Future.succeededFuture());
            return this;
        }
        return execute(sql, handler);
    }

    @Override
    public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private SQLConnection endAndStartTransaction(boolean commit, Handler<AsyncResult<Void>> handler) {
        Future<Transaction> current;
        synchronized (this) {
            current = transaction;
            transaction = null;
        }
        if (current == null) {
            handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
            return this;
        }
        current.compose(tx -> commit ? tx.commit() : tx.rollback())
                .compose(v -> beginTransactionIfNeeded())
                .onComplete(handler);
        return this;
    }

    private synchronized Future<Void> beginTransactionIfNeeded() {
        if (!inAutoCommit && transaction == null) {
            transaction = connection.begin();
        }
        return transaction == null ? Future.succeededFuture() : transaction.mapEmpty();
    }

    @SuppressWarnings("unchecked")
    private Future<RowSet<Row>> run(String sql, List params) {
        return beginTransactionIfNeeded().compose(v -> params == null
                ? connection.query(sql).execute()
                : connection.preparedQuery(sql).execute(Tuple.wrap(params)));
    }
}
