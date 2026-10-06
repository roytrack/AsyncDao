package com.tg.async.test;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by twogoods on 2018/4/12.
 */
public class BaseSqlTest {
    @Test
    public void origin() throws Exception {
        Vertx vertx = Vertx.vertx();
        MySQLConnectOptions options = new MySQLConnectOptions()
                .setUser("root")
                .setHost("localhost")
                .setPort(3306)
                .setPassword("admin")
                .setDatabase("test")
                .setAuthenticationPlugin(MySQLAuthenticationPlugin.CACHING_SHA2_PASSWORD);

        try {
            RowSet<Row> rows = MySQLConnection.connect(vertx, options)
                    .compose(connection -> connection.query("insert into T_User(username) values('twogoods')").execute()
                            .onComplete(ar -> connection.close()))
                    .toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

            System.out.println("rowsAffected: " + rows.rowCount());
            System.out.println("insert id: " + rows.property(MySQLClient.LAST_INSERTED_ID));
            assertEquals(1, rows.rowCount());
            assertTrue(rows.property(MySQLClient.LAST_INSERTED_ID) > 0);
        } finally {
            vertx.close();
        }
    }
}
