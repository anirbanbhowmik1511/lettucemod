package com.redislabs.mesclun;

import com.redislabs.mesclun.timeseries.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.lettuce.core.RedisURI;

@Testcontainers
public class TestTimeSeries {

    private RedisModulesClient client;
    protected StatefulRedisModulesConnection<String, String> connection;
    protected RedisModulesCommands<String, String> sync;
    protected RedisModulesAsyncCommands<String, String> async;
    protected RedisModulesReactiveCommands<String, String> reactive;

    protected String host;
    protected int port;

    @Container
    @SuppressWarnings("rawtypes")
    public static final GenericContainer REDISTIMESERIES = new GenericContainer(DockerImageName.parse("redislabs/redistimeseries")).withExposedPorts(6379);

    @BeforeEach
    public void setup() {
        host = REDISTIMESERIES.getHost();
        port = REDISTIMESERIES.getFirstMappedPort();
        client = RedisModulesClient.create(RedisURI.create(host, port));
        connection = client.connect();
        sync = connection.sync();
        async = connection.async();
        reactive = connection.reactive();
        sync.flushall();
    }

    @AfterEach
    public void teardown() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        // temperature:3:11 RETENTION 6000 LABELS sensor_id 2 area_id 32
        String status = sync.create("temperature:3:11", CreateOptions.builder().retentionTime(6000).build(), Label.of("sensor_id", "2"), Label.of("area_id", "32"));
        Assertions.assertEquals("OK", status);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdd() {
        // TS.CREATE temperature:3:11 RETENTION 6000 LABELS sensor_id 2 area_id 32
        sync.create("temperature:3:11", CreateOptions.builder().retentionTime(6000).build(), Label.of("sensor_id", "2"), Label.of("area_id", "32"));
        // TS.ADD temperature:3:11 1548149181 30
        Long add1 = sync.add("temperature:3:11", 1548149181, 30);
        Assertions.assertEquals(1548149181, add1);
        // TS.ADD temperature:3:11 1548149191 42
        Long add2 = sync.add("temperature:3:11", 1548149191, 42);
        Assertions.assertEquals(1548149191, add2);
    }

}