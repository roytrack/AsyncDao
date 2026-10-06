package com.tg.async.mysql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.List;


/**
 * Created by twogoods on 2018/4/8.
 */
public interface SQLConnection {

    SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler);

    SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler);

    SQLConnection executeWithParams(String sql, List params, Handler<AsyncResult<Void>> handler);

    SQLConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

    SQLConnection queryWithParams(String sql, List params, Handler<AsyncResult<RowSet<Row>>> handler);

    SQLConnection update(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

    SQLConnection updateWithParams(String sql, List params, Handler<AsyncResult<RowSet<Row>>> handler);

    void close(Handler<AsyncResult<Void>> handler);

    void close();

    SQLConnection commit(Handler<AsyncResult<Void>> handler);

    SQLConnection rollback(Handler<AsyncResult<Void>> handler);

    SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler);

    SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler);

}
