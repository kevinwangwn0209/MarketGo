package com.easy.marketgo.core.repository.wecom.taskcenter;

import com.easy.marketgo.core.entity.taskcenter.WeComTaskCenterMemberStatisticEntity;
import com.easy.marketgo.core.model.bo.QueryMassTaskMemberMetricsBuildSqlParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 7/8/22 4:24 PM
 * Describe:
 */
public interface WeComTaskCenterMemberStatisticCustomizedRepository {

    List<WeComTaskCenterMemberStatisticEntity> listByBuildSqlParam(QueryMassTaskMemberMetricsBuildSqlParam param);

    Integer countByBuildSqlParam(QueryMassTaskMemberMetricsBuildSqlParam param);

    @Slf4j
    class WeComTaskCenterMemberStatisticCustomizedRepositoryImpl implements WeComTaskCenterMemberStatisticCustomizedRepository {
        @Resource
        private DataSource dataSource;

        @Override
        public List<WeComTaskCenterMemberStatisticEntity> listByBuildSqlParam(QueryMassTaskMemberMetricsBuildSqlParam param) {
            BeanPropertySqlParameterSource paramSource = new BeanPropertySqlParameterSource(param);
            return new NamedParameterJdbcTemplate(this.dataSource).query(getSelectByCndSql(param, false),
                    paramSource, new BeanPropertyRowMapper(WeComTaskCenterMemberStatisticEntity.class));
        }

        @Override
        public Integer countByBuildSqlParam(QueryMassTaskMemberMetricsBuildSqlParam param) {
            BeanPropertySqlParameterSource paramSource = new BeanPropertySqlParameterSource(param);
            return new NamedParameterJdbcTemplate(this.dataSource).queryForObject(getSelectByCndSql(param,
                    true), paramSource, Integer.class);
        }

        private String getSelectByCndSql(QueryMassTaskMemberMetricsBuildSqlParam param, boolean isCount) {
            StringBuilder sql = new StringBuilder(
                    String.format("SELECT %s FROM wecom_task_center_statistic_member WHERE project_uuid = :projectUuid"
                            , isCount ? "COUNT(*)" : "*"));

            if (StringUtils.isNotEmpty(param.getKeyword())) {
                sql.append(" AND (member_name LIKE CONCAT('%', :keyword, '%')");
                if (StringUtils.isNumeric(param.getKeyword())) {
                    sql.append(" OR id = ").append(param.getKeyword());
                }
                sql.append(")");
            }
            if (StringUtils.isNotEmpty(param.getTaskUuid())) {
                sql.append(" AND task_uuid = :taskUuid");
            }

            if (StringUtils.isNotEmpty(param.getStatus())) {
                sql.append(" AND status = :status");
            }

            if (!isCount) {
                sql.append(" LIMIT :startIndex,:pageSize");
            }
            String sqlStr = sql.toString();
            log.info("select by build sql param: sql={}", sqlStr);
            return sqlStr;
        }
    }
}
