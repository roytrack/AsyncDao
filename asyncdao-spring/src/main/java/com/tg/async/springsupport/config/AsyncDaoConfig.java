package com.tg.async.springsupport.config;

import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.SslMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by twogoods on 2018/8/27.
 */
@Data
@ConfigurationProperties(prefix = AsyncDaoConfig.CONF_PREFIX)
public class AsyncDaoConfig {
    public static final String CONF_PREFIX = "async.dao";

    private String mapperLocations;

    private String basePackages;

    private String username;

    private String host;

    private int port;

    private String password;

    private String database;

    private String charset = "utf8mb4";

    /**
     * 认证插件，默认 MySQL 8.0+ 的 caching_sha2_password
     */
    private MySQLAuthenticationPlugin authenticationPlugin = MySQLAuthenticationPlugin.CACHING_SHA2_PASSWORD;

    private SslMode sslMode = SslMode.DISABLED;

    /**
     * 校验服务端证书用的 CA 证书（PEM）
     */
    private String sslRootCertPath;

    /**
     * 非 TLS 连接时加密密码用的服务端 RSA 公钥文件，不配置则自动向服务端获取
     */
    private String serverRsaPublicKeyPath;

    private int maxTotal=12;
    private long maxWaitMillis=10000L;
    private long idleTimeoutMillis=0L;

}
