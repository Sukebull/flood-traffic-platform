package com.marvin.floodtrafficplatform.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@MappedTypes({Object.class})
public class PgJsonbTypeHandler extends JacksonTypeHandler {

    public PgJsonbTypeHandler(Class<?> type) {
        super(type);
    }

    // 核心逻辑：拦截即将发往数据库的数据，强制包装成 PostgreSQL 认识的 jsonb 类型
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        pgObject.setValue(toJson(parameter));
        ps.setObject(i, pgObject);
    }
}