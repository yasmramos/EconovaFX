module econovafx {
    requires java.base;
    requires java.net.http;
    requires javafx.controlsEmpty;
    requires javafx.controls;
    requires javafx.graphicsEmpty;
    requires javafx.graphics;
    requires javafx.fxmlEmpty;
    requires javafx.fxml;
    requires javafx.webEmpty;
    requires javafx.web;
    requires jdk.httpserver;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    requires io.ebean;
    requires io.ebean.api;
    requires io.avaje.applog.slf4j;
    requires jakarta.persistence.api;
    requires io.ebean.annotation;
    requires io.ebean.types;
    requires io.ebean.datasource.api;
    requires io.ebean.core;
    requires io.ebean.core.json;
    requires io.ebean.migration.auto;
    requires io.ebean.core.type;
    requires io.ebean.joda.time;
    requires org.joda.time;
    requires io.ebean.jackson.jsonnode;
    requires io.ebean.jackson.mapper;
    requires io.ebean.datasource;
    requires com.zaxxer.hikari;
    requires io.ebean.migration;
    requires io.ebean.querybean;
    requires io.ebean.platform.all;
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
    requires io.avaje.applog;
    requires io.ebean.platform.h2;
    requires com.h2database;
    requires org.postgresql.jdbc;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires io.avaje.inject;
    requires org.jspecify;
    requires jakarta.inject;
    requires io.avaje.inject.events;
    requires io.avaje.config;
    requires io.avaje.inject.aop;
    requires org.jsoup;
    requires jbcrypt;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires commons.logging;
    requires openhtmltopdf.core;
    requires openhtmltopdf.pdfbox;
    requires openhtmltopdf.slf4j;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.commons.codec;
    requires commons.math3;
    requires com.zaxxer.sparsebitset;
    requires org.apache.poi.ooxml.schemas;
    requires org.apache.xmlbeans;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;
    requires com.github.virtuald.curvesapi;
    requires org.apache.logging.log4j;
    requires org.apache.commons.collections4;
    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires javafx.baseEmpty;
    requires javafx.base;
    requires jdk.jsobject;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires java.desktop;
    
    provides io.avaje.inject.spi.InjectExtension with com.econovafx.EconovafxModule;
    
    // -----------------------------------------------------------------------
    // JavaFX reflective access (required, otherwise the app dies at startup)
    // -----------------------------------------------------------------------
    // The JavaFX launcher (com.sun.javafx.application.LauncherImpl, module
    // javafx.graphics) instantiates the Application subclass reflectively.
    // Without this export:
    //   IllegalAccessException: ... LauncherImpl (in module javafx.graphics)
    //   cannot access class com.econovafx.App (in module econovafx)
    exports com.econovafx to javafx.graphics;

    // FXMLLoader (module javafx.fxml) instantiates the controllers named by
    // fx:controller and injects their @FXML fields/methods reflectively, so
    // every controller package must be BOTH exported and opened to javafx.fxml.
    // Export alone fixes controller construction; opens is additionally needed
    // for the private @FXML field injection that happens in the same step.

    exports com.econovafx.modules.core.ui.controller to javafx.fxml;
    opens com.econovafx.modules.core.ui.controller to javafx.fxml;

    exports com.econovafx.modules.core.ui.view to javafx.fxml;
    opens com.econovafx.modules.core.ui.view to javafx.fxml;

    exports com.econovafx.modules.accounting.controller to javafx.fxml;
    opens com.econovafx.modules.accounting.controller to javafx.fxml;

    exports com.econovafx.modules.aft.controller to javafx.fxml;
    opens com.econovafx.modules.aft.controller to javafx.fxml;

    exports com.econovafx.modules.billing.controller to javafx.fxml;
    opens com.econovafx.modules.billing.controller to javafx.fxml;

    exports com.econovafx.modules.cash.controller to javafx.fxml;
    opens com.econovafx.modules.cash.controller to javafx.fxml;

    exports com.econovafx.modules.costing.controller to javafx.fxml;
    opens com.econovafx.modules.costing.controller to javafx.fxml;

    exports com.econovafx.modules.finance.controller to javafx.fxml;
    opens com.econovafx.modules.finance.controller to javafx.fxml;

    exports com.econovafx.modules.inventory.controller to javafx.fxml;
    opens com.econovafx.modules.inventory.controller to javafx.fxml;

    exports com.econovafx.modules.payroll.controller to javafx.fxml;
    opens com.econovafx.modules.payroll.controller to javafx.fxml;

    exports com.econovafx.modules.security.ui.controller to javafx.fxml;
    opens com.econovafx.modules.security.ui.controller to javafx.fxml;

    // -----------------------------------------------------------------------
    // Entity model packages
    // -----------------------------------------------------------------------
    // These must be EXPORTED, not only opened. Ebean reads the generated
    // `public static String[] _ebean_props` field through a symbolic reference
    // (MethodHandles public Lookup). Symbolic linkage requires a real `exports`;
    // `opens` alone only permits deep reflection (setAccessible) and leaves the
    // field unresolvable, which fails DB startup with:
    //   IllegalStateException: Error getting _ebean_props field on type class ...
    //   Caused by: IllegalAccessException: symbolic reference class is not
    //              accessible: class ..., from public Lookup
    exports com.econovafx.modules.core.model;
    exports com.econovafx.modules.accounting.model;
    exports com.econovafx.modules.billing.model;
    exports com.econovafx.modules.payroll.model;
    exports com.econovafx.modules.inventory.model;
    exports com.econovafx.modules.receivables.model;
    exports com.econovafx.modules.payables.model;
    exports com.econovafx.modules.bank.model;
    exports com.econovafx.modules.cash.model;
    exports com.econovafx.modules.assets.model;
    exports com.econovafx.modules.fixedassets.model;
    exports com.econovafx.modules.reporting.model;
    exports com.econovafx.modules.security.model;

    // Also open them to Ebean for reflective access (e.g. @DbEnumValue enum
    // introspection in io.ebean.core). Without these, io.ebean.core throws
    // IllegalAccessException when the model packages are not opened.
    opens com.econovafx.modules.core.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.accounting.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.billing.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.payroll.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.inventory.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.receivables.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.payables.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.bank.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.cash.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.assets.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.fixedassets.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.reporting.model to io.ebean.core, io.ebean;
    opens com.econovafx.modules.security.model to io.ebean.core, io.ebean;

}
