package es.gobcan.coetl.service.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.arte.libs.grammar.domain.QueryPropertyRestriction;
import com.arte.libs.grammar.orm.jpa.criteria.AbstractCriteriaProcessor;
import com.arte.libs.grammar.orm.jpa.criteria.CriteriaProcessorContext;
import com.arte.libs.grammar.orm.jpa.criteria.OrderProcessorBuilder;
import com.arte.libs.grammar.orm.jpa.criteria.RestrictionProcessorBuilder;
import com.arte.libs.grammar.orm.jpa.criteria.converter.CriterionConverter;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Etl.Type;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.service.criteria.util.CriteriaUtil;
import es.gobcan.coetl.util.StringUtils;

public class EtlCriteriaProcessor extends AbstractCriteriaProcessor {

    private static final String TABLE_FIELD_NAME = "name";

    private static final String ENTITY_FIELD_CODE = "code";
    private static final String ENTITY_FIELD_NAME = "name";
    private static final String ENTITY_FIELD_TYPE = "type";
    private static final String ENTITY_FIELD_CREATED_DATE = "createdDate";
    private static final String ENTITY_FIELD_ORGANIZATION_IN_CHARGE = "organismo";

    public EtlCriteriaProcessor() {
        super(Etl.class);
    }

    public enum QueryProperty {
        CODE, NAME, TYPE, CREATED_DATE, ORGANISMO, IS_PLANNED, LAST_EXECUTION, NEXT_EXECUTION, ORGANIZATION_IN_CHARGE, LAST_EXECUTION_BY_RESULT,
        LAST_EXECUTION_CUSTOM, VISIBILITY
    }

    @Override
    public void registerProcessors() {
        //@formatter:off
        // Orders
        registerOrderProcessor(OrderProcessorBuilder.orderProcessor()
                .withQueryProperty(QueryProperty.CODE)
                .withEntityProperty(ENTITY_FIELD_CODE)
                .build());
        registerOrderProcessor(OrderProcessorBuilder.orderProcessor()
                .withQueryProperty(QueryProperty.NAME)
                .withEntityProperty(ENTITY_FIELD_NAME)
                .build());
        registerOrderProcessor(OrderProcessorBuilder.orderProcessor()
                .withQueryProperty(QueryProperty.TYPE)
                .withEntityProperty(ENTITY_FIELD_TYPE)
                .build());
        registerOrderProcessor(OrderProcessorBuilder.orderProcessor()
                .withQueryProperty(QueryProperty.CREATED_DATE)
                .withEntityProperty(ENTITY_FIELD_CREATED_DATE)
                .build());
        registerOrderProcessor(OrderProcessorBuilder.orderProcessor()
                .withQueryProperty(QueryProperty.ORGANISMO)
                .withEntityProperty(ENTITY_FIELD_ORGANIZATION_IN_CHARGE)
                .build());

        // Restrictions
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.CODE)
                .withEntityProperty(ENTITY_FIELD_CODE)
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.NAME)
                .withCriterionConverter(new NameCriterionBuilder())
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.enumRestrictionProcessor(Type.class)
                .withQueryProperty(QueryProperty.TYPE)
                .withEntityProperty(ENTITY_FIELD_TYPE)
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.enumRestrictionProcessor(Type.class)
                .withQueryProperty(QueryProperty.CREATED_DATE)
                .withEntityProperty(ENTITY_FIELD_CREATED_DATE)
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.ORGANISMO)
                .withCriterionConverter(new OrganizationInChargeCriterionBuilder())
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.IS_PLANNED)
                .withCriterionConverter(new IsPlannedCriterionBuilder())
                .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
            .withQueryProperty(QueryProperty.NEXT_EXECUTION)
            .withCriterionConverter(new NextExecutionCriterionBuilder())
            .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
            .withQueryProperty(QueryProperty.LAST_EXECUTION)
            .withCriterionConverter(new LastExecutionCriterionBuilder())
            .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
            .withQueryProperty(QueryProperty.ORGANIZATION_IN_CHARGE)
            .withCriterionConverter(new EtlOrganizationInChargeCriterionBuilder())
            .build());
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.LAST_EXECUTION_BY_RESULT)
                .withCriterionConverter(new LastExecutionByResultCriterionBuilder())
                .build());
        registerProcessorsWithLogicalDeletionPolicy(
                RestrictionProcessorBuilder.restrictionProcessor()
                    .withQueryProperty(QueryProperty.LAST_EXECUTION_CUSTOM)
                    .withCriterionConverter(new LastExecutionCustomCriterionBuilder())
                .build());
        registerProcessorsWithLogicalDeletionPolicy(
                RestrictionProcessorBuilder.restrictionProcessor()
                .withQueryProperty(QueryProperty.VISIBILITY)
                .withCriterionConverter(new IsVisibleCriterionBuilder())
                .build());
        //@formatter:on
    }

    private static class NameCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("ILIKE".equals(property.getOperationType().name())) {
                ArrayList<String> fields = new ArrayList<>(Arrays.asList(TABLE_FIELD_NAME));
                return CriteriaUtil.buildAccentAndCaseInsensitiveCriterion(property, fields);
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }
    }

    private static class OrganizationInChargeCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("ILIKE".equals(property.getOperationType().name())) {
                return buildEtlByOrganizationInCharge(property.getRightExpression());
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildEtlByOrganizationInCharge(String value) {
            String sql = String.format("{alias}.organization_in_charge IN (SELECT org.id FROM tb_organismo org WHERE org.name ILIKE %s)",value);
            return Restrictions.sqlRestriction(sql);
        }

    }

    private static class IsPlannedCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                boolean value = Boolean.valueOf(property.getRightExpression());
                return value ? buildEtlByIsPlanned() : buildEtlByIsNotPlanned();
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildEtlByIsPlanned() {
            String sql = "{alias}.execution_planning IS NOT NULL";
            return Restrictions.sqlRestriction(sql);
        }

        private Criterion buildEtlByIsNotPlanned() {
            String sql = "{alias}.execution_planning IS NULL";
            return Restrictions.sqlRestriction(sql);
        }
    }

    private static class NextExecutionCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildEtlByNextExecution(property.getRightExpression());
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildEtlByNextExecution(String value) {
            String dateValue = StringUtils.changeFormatStringDate(value);
            String sql = String.format("date({alias}.next_execution) = '%s'", dateValue);
            return Restrictions.sqlRestriction(sql);
        }
    }

    private static class LastExecutionCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildEtlByLastExecution(property.getRightExpression());
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildEtlByLastExecution(String value) {
            String dateValue = StringUtils.changeFormatStringDate(value);
            String sql = String.format("{alias}.id IN (SELECT execution.etl_fk FROM tb_executions execution WHERE date(execution.start_date) = '%s')",dateValue);
            return Restrictions.sqlRestriction(sql);
        }
    }

    private static class EtlOrganizationInChargeCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("IN".equals(property.getOperationType().name())) {
                return buildEtlByOrganizationInCharge(property.getRightValues());
            }
            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildEtlByOrganizationInCharge(List<String> ids) {
            StringBuilder query = new StringBuilder();
            for (int i = 0; i < ids.size(); i++) {
                if (i < ids.size() - 1) {
                    query.append("'" + ids.get(i) + "',");
                } else {
                    query.append("'" + ids.get(i) + "'");
                }
            }
            String sql = String.format("{alias}.organization_in_charge IN (%s)", query.toString());
            return Restrictions.sqlRestriction(sql);
        }

    }

    private static class LastExecutionByResultCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildQueryLastExecutionEtlByResult(property.getRightExpression());
            }

            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildQueryLastExecutionEtlByResult(String result) {
            String sql = String.format(" {alias}.id IN (select etl_fk from tb_executions te where \"result\" = %s) ", result);
            return Restrictions.sqlRestriction(sql);
        }

    }

    private static class LastExecutionCustomCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("IN".equals(property.getOperationType().name())) {
                return buildQueryLastExecutionEtlByResult(property.getRightExpressions());
            }

            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildQueryLastExecutionEtlByResult(List<String> result) {
            String dateValue = StringUtils.changeFormatStringDate(result.get(1));
            String sql = String.format(" {alias}.id IN (select etl_fk from tb_executions te where \"result\" = %s and date(planning_date) = '%s') "
                    , result.get(0), dateValue);
            return Restrictions.sqlRestriction(sql);
        }
    }

    private static class IsVisibleCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildIsVisible(Boolean.valueOf(property.getRightExpression()));
            }

            throw new CustomParameterizedExceptionBuilder().message(String.format("Search Parameter not supported: '%s'", property))
                    .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
        }

        private Criterion buildIsVisible(boolean visible) {
            String sql = String.format("{alias}.visibility = %s", visible);
            return Restrictions.sqlRestriction(sql);
        }

    }

}
