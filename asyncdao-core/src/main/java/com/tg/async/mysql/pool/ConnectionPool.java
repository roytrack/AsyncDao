package com.tg.async.mysql.pool;

import com.tg.async.mysql.AsyncSQLConnectionImpl;
import com.tg.async.mysql.SQLConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.Pool;

/**
 * Created by twogoods on 2018/4/8.
 */
public class ConnectionPool {

    private final Pool pool;
    private final SQLConnection pooledClient;

    public ConnectionPool(PoolConfiguration configuration, Vertx vertx) {
        this.pool = MySQLBuilder.pool()
                .with(configuration.getPoolOptions())
                .connectingTo(configuration.getConnectOptions())
                .using(vertx)
                .build();
        this.pooledClient = new PoolSQLConnection(pool);
    }

    public void close() {
        pool.close();
    }

    /**
     * borrow a dedicated connection (needed for transactions), call {@link SQLConnection#close()} to return it
     */
    public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
        pool.getConnection()
                .<SQLConnection>map(AsyncSQLConnectionImpl::new)
                .onComplete(handler);
    }

    /**
     * a client that borrows and returns a connection for each statement
     */
    public SQLConnection getPooledClient() {
        return pooledClient;
    }
}
