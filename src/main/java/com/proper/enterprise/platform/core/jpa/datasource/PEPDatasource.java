package com.proper.enterprise.platform.core.jpa.datasource;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.SQLException;

public class PEPDatasource extends DruidDataSource {

    private enum DataBaseType {
        /**
         * MYSQL 数据库
         */
        MYSQL,
        /**
         * H2 数据库
         */
        H2
    }

    private String databaseType;

    private String dialect;

    @Override
    public void init() throws SQLException {
        DataBaseType type;
        try {
            type = DataBaseType.valueOf(databaseType);
        } catch (IllegalArgumentException e) {
            throw new SQLException("databaseType is not support");
        }
        switch (type) {
            case MYSQL:
                super.setDriverClassName("org.mariadb.jdbc.Driver");
                setDialect("org.hibernate.dialect.MySQL57InnoDBDialect");
                break;
            case H2:
                super.setDriverClassName("org.h2.Driver");
                setDialect("org.hibernate.dialect.H2Dialect");
                break;
            default:
                throw new SQLException("databaseType is not support");
        }
        super.init();
    }

    @Override
    public void close() {
        super.close();
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
