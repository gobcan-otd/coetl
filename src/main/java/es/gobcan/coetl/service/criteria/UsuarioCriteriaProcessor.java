package es.gobcan.coetl.service.criteria;

import java.util.ArrayList;
import java.util.Arrays;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.arte.libs.grammar.domain.QueryPropertyRestriction;
import com.arte.libs.grammar.orm.jpa.criteria.AbstractCriteriaProcessor;
import com.arte.libs.grammar.orm.jpa.criteria.CriteriaProcessorContext;
import com.arte.libs.grammar.orm.jpa.criteria.RestrictionProcessorBuilder;
import com.arte.libs.grammar.orm.jpa.criteria.converter.CriterionConverter;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.errors.CustomParameterizedException;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.service.criteria.util.CriteriaUtil;

public class UsuarioCriteriaProcessor extends AbstractCriteriaProcessor {

    private static final String TABLE_FIELD_LOGIN = "login";
    private static final String TABLE_FIELD_NOMBRE = "nombre";
    private static final String TABLE_FIELD_APELLIDO1 = "apellido1";
    private static final String TABLE_FIELD_APELLIDO2 = "apellido2";

    private static final String ENTITY_FIELD_LOGIN = "login";
    private static final String ENTITY_FIELD_NOMBRE = "nombre";
    private static final String ENTITY_FIELD_APELLIDO1 = "apellido1";
    private static final String ENTITY_FIELD_APELLIDO2 = "apellido2";
    private static final String ENTITY_FIELD_EMAIL = "email";

    public UsuarioCriteriaProcessor() {
        super(Usuario.class);
    }

    public enum QueryProperty {
        LOGIN, NOMBRE, APELLIDO1, APELLIDO2, ROLES, EMAIL, USUARIO, ORGANISMO, ROLES_ORGANISMO
    }

    @Override
    public void registerProcessors() {
        //@formatter:off
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.enumRestrictionProcessor(Rol.class)
                .withQueryProperty(QueryProperty.ROLES)
                .withCriterionConverter(new RolCriterionBuilder())
                .build());
    	
    	registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.ORGANISMO)
                .withCriterionConverter(new OrganismoCriterionBuilder())
                .build());
    	
    	registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.ROLES_ORGANISMO)
                .withCriterionConverter(new RolOrganismoCriterionBuilder())
                .build());
        
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.LOGIN).sortable()
                .withEntityProperty(ENTITY_FIELD_LOGIN).build());
        
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.NOMBRE).sortable()
                .withEntityProperty(ENTITY_FIELD_NOMBRE).build());
        
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.APELLIDO1).sortable()
                .withEntityProperty(ENTITY_FIELD_APELLIDO1).build());
        
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
                .withQueryProperty(QueryProperty.APELLIDO2).sortable()
                .withEntityProperty(ENTITY_FIELD_APELLIDO2).build());
    	
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
    			.withQueryProperty(QueryProperty.USUARIO)
    			.withCriterionConverter(new UsuarioCriterionBuilder())
    			.build());
    	
        registerProcessorsWithLogicalDeletionPolicy(RestrictionProcessorBuilder.stringRestrictionProcessor()
    	        .withQueryProperty(QueryProperty.EMAIL)
    	        .withEntityProperty(ENTITY_FIELD_EMAIL)
    	        .sortable()
	            .build());

        //@formatter:on
    }

    private static class UsuarioCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if (QueryProperty.USUARIO.name().equalsIgnoreCase(property.getLeftExpression())) {
                ArrayList<String> fields = new ArrayList<>(Arrays.asList(TABLE_FIELD_LOGIN, TABLE_FIELD_NOMBRE, TABLE_FIELD_APELLIDO1, TABLE_FIELD_APELLIDO2));
                return CriteriaUtil.buildAccentAndCaseInsensitiveCriterion(property, fields);
            }
            throw queryNoSoportadaException(property);
        }
    }

    private static class RolCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildUsersByRole(property);
            }
            throw queryNoSoportadaException(property);
        }

        private Criterion buildUsersByRole(QueryPropertyRestriction property) {
        	if (Rol.ADMIN.name().equals(property.getRightExpression().substring(1, property.getRightExpression().length() - 1))) {
        		return Restrictions.sqlRestriction("{alias}.is_admin = true");
        	} else {
        		String query = "{alias}.id in (select urg.id_usuario from tb_usuario_rol_organismo urg join tb_roles r on urg.id_rol = r.id where r.name = %s)";
        		return Restrictions.sqlRestriction( String.format(query, property.getRightExpression()) );
        	}
        }
    }
    
    private static class OrganismoCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name())) {
                return buildUsersByOrganism(property);
            }
            throw queryNoSoportadaException(property);
        }

        private Criterion buildUsersByOrganism(QueryPropertyRestriction property) {
            String query = "{alias}.id in (select urg.id_usuario from tb_usuario_rol_organismo urg join tb_roles r on urg.id_rol = r.id where r.name != 'ADMIN' and urg.id_organismo = %s)";
            String sql = String.format(query, property.getRightExpression());
            return Restrictions.sqlRestriction(sql);
        }
    }
    
    private static class RolOrganismoCriterionBuilder implements CriterionConverter {

        @Override
        public Criterion convertToCriterion(QueryPropertyRestriction property, CriteriaProcessorContext context) {
            if ("EQ".equals(property.getOperationType().name()) && property.getRightExpression().split("@@").length == 2 ) {
                return buildUsersByRole(property);
            }
            throw queryNoSoportadaException(property);
        }

        private Criterion buildUsersByRole(QueryPropertyRestriction property) {
        	String[] roleOrganism = property.getRightExpression().substring(1, property.getRightExpression().length() - 1).split("@@");
        	String role = roleOrganism[0];
        	String organism = roleOrganism[1];
            String query = "{alias}.id in (select urg.id_usuario from tb_usuario_rol_organismo urg join tb_roles r on urg.id_rol = r.id where r.name = '%s' and urg.id_organismo = '%s')";
            String sql = String.format(query, role, organism);
            return Restrictions.sqlRestriction(sql);
        }
    }
    
    private static CustomParameterizedException queryNoSoportadaException(QueryPropertyRestriction property) {
    	return new CustomParameterizedExceptionBuilder().message(String.format("Parámetro de búsqueda no soportado: '%s'", property))
                .code(ErrorConstants.QUERY_NO_SOPORTADA, property.getLeftExpression(), property.getOperationType().name()).build();
    }
}
