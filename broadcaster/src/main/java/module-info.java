module broadcaster.server {
    requires slf4j.api;

    requires simpleclient;
    requires simpleclient.common;
    requires micrometer.core;
    requires micrometer.registry.prometheus;
    requires micrometer.jvm.extras;

    requires typesafe.config;
    requires io.netty.all;

    requires java.sql;
    requires java.management;
    requires kafka.streams;
    requires kafka.clients;

    requires jdk.unsupported;
    requires lorem;
    requires jdk.jfr;
}