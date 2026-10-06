package com.tg.async.mysql.pool;

import io.vertx.core.net.PemTrustOptions;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.PoolOptions;

import java.util.concurrent.TimeUnit;

/**
 * Created by twogoods on 2018/4/8.
 */
public class PoolConfiguration {
    private String username;
    private String host;
    private int port;
    private String password;
    private String database;

    private String charset = "utf8mb4";
    /**
     * MySQL 8.0+ 默认的 caching_sha2_password；账号使用其他认证插件时服务端会要求切换，客户端会自动跟随
     */
    private MySQLAuthenticationPlugin authenticationPlugin = MySQLAuthenticationPlugin.CACHING_SHA2_PASSWORD;
    private SslMode sslMode = SslMode.DISABLED;
    /**
     * 校验服务端证书用的 CA 证书（PEM），sslMode 为 REQUIRED / VERIFY_CA / VERIFY_IDENTITY 时使用，不配置则使用 JVM 默认信任库
     */
    private String sslRootCertPath;
    /**
     * 非 TLS 连接做 caching_sha2_password 完整认证时用来加密密码的服务端 RSA 公钥，不配置则自动向服务端获取
     */
    private String serverRsaPublicKeyPath;
    private long connectTimeout = 10000L;

    private int maxTotal = 12;
    private int maxWaitQueueSize = -1;
    private long borrowMaxWaitMillis = 10000L;
    private long idleTimeoutMillis = 0L;

    public PoolConfiguration(String username, String host, int port, String password, String database) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
        this.database = database;
    }

    public MySQLConnectOptions getConnectOptions() {
        MySQLConnectOptions options = new MySQLConnectOptions()
                .setUser(username)
                .setHost(host)
                .setPort(port)
                .setPassword(password)
                .setDatabase(database)
                .setCharset(charset)
                .setAuthenticationPlugin(authenticationPlugin)
                .setSslMode(sslMode)
                // rowCount 返回实际被修改的行数，与之前的 mysql-async 驱动保持一致
                .setUseAffectedRows(true);
        options.setConnectTimeout((int) connectTimeout);
        if (sslRootCertPath != null && !sslRootCertPath.isEmpty()) {
            options.setPemTrustOptions(new PemTrustOptions().addCertPath(sslRootCertPath));
        }
        if (serverRsaPublicKeyPath != null && !serverRsaPublicKeyPath.isEmpty()) {
            options.setServerRsaPublicKeyPath(serverRsaPublicKeyPath);
        }
        return options;
    }

    public PoolOptions getPoolOptions() {
        return new PoolOptions()
                .setMaxSize(maxTotal)
                .setMaxWaitQueueSize(maxWaitQueueSize)
                .setConnectionTimeout((int) borrowMaxWaitMillis)
                .setConnectionTimeoutUnit(TimeUnit.MILLISECONDS)
                .setIdleTimeout((int) idleTimeoutMillis)
                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public MySQLAuthenticationPlugin getAuthenticationPlugin() {
        return authenticationPlugin;
    }

    public void setAuthenticationPlugin(MySQLAuthenticationPlugin authenticationPlugin) {
        this.authenticationPlugin = authenticationPlugin;
    }

    public SslMode getSslMode() {
        return sslMode;
    }

    public void setSslMode(SslMode sslMode) {
        this.sslMode = sslMode;
    }

    public String getSslRootCertPath() {
        return sslRootCertPath;
    }

    public void setSslRootCertPath(String sslRootCertPath) {
        this.sslRootCertPath = sslRootCertPath;
    }

    public String getServerRsaPublicKeyPath() {
        return serverRsaPublicKeyPath;
    }

    public void setServerRsaPublicKeyPath(String serverRsaPublicKeyPath) {
        this.serverRsaPublicKeyPath = serverRsaPublicKeyPath;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxWaitQueueSize() {
        return maxWaitQueueSize;
    }

    public void setMaxWaitQueueSize(int maxWaitQueueSize) {
        this.maxWaitQueueSize = maxWaitQueueSize;
    }

    public long getBorrowMaxWaitMillis() {
        return borrowMaxWaitMillis;
    }

    public void setBorrowMaxWaitMillis(long borrowMaxWaitMillis) {
        this.borrowMaxWaitMillis = borrowMaxWaitMillis;
    }

    public long getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public void setIdleTimeoutMillis(long idleTimeoutMillis) {
        this.idleTimeoutMillis = idleTimeoutMillis;
    }
}
