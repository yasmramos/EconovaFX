module econovafx {
    requires io.ebean;
    requires io.avaje.applog;
    requires io.avaje.applog.slf4j;
    requires io.avaje.inject;
    requires io.ebean.annotation;
    requires io.ebean.types;
    requires io.ebean.datasource.api;
    requires io.ebean.migration.auto;
    requires org.joda.time;
    requires io.ebean.jackson.jsonnode;
    requires io.ebean.jackson.mapper;
    requires io.ebean.datasource;
    requires io.ebean.migration;
    requires io.ebean.querybean;
    requires io.ebean.platform.all;
    requires io.ebean.platform.h2;
    requires io.ebean.platform.clickhouse;
    requires io.ebean.platform.db2;
    requires io.ebean.platform.hana;
    requires io.ebean.platform.hsqldb;
    requires io.ebean.platform.mysql;
    requires io.ebean.platform.mariadb;
    requires io.ebean.platform.nuodb;
    requires io.ebean.platform.oracle;
    requires io.ebean.platform.postgres;
    requires io.ebean.platform.sqlanywhere;
    requires io.ebean.platform.sqlite;
    requires io.ebean.platform.sqlserver;
    requires io.ebean.ddl.generator;
    requires io.ebean.ddl.runner;
    requires com.h2database;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    
    // JavaFX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    
    // Java Preferences
    requires java.prefs;

    // Ikonli (available for future use)
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    // Open packages for JavaFX reflection and Ebean test access
    opens com.econovafx to javafx.fxml, javafx.graphics;
    opens com.econovafx.ui.controller to javafx.fxml;
    opens com.econovafx.ui.view to javafx.fxml;
    opens com.econovafx.domain to javafx.fxml, io.ebean, io.ebean.core, io.ebean.api;
    opens com.econovafx.config to javafx.fxml;

    // Export domain package for Ebean
    exports com.econovafx.domain to io.ebean, io.ebean.core, io.ebean.api;
    exports com.econovafx.repository;
}
