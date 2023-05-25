package es.gobcan.coetl.service.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;

@Component
public class OrganismValidator extends AbstractValidator<Organismo> {

    private static final String FIELD_BLANK_ERROR_MESSAGE = "Field \"%s\" of Organism (id=%s) can not be blank";
    private static final String DESCRIPTION_LIMIT_EXCEED_ERROR = "Description field can not exceed 255 characters";
    private static final int FIELD_DESCRIPTION_SIZE = 255;

    @Override
    public void validate(Organismo organismo) {
        checkNameIsNotBlank(organismo);
        checkDescriptionLimit(organismo);
    }

    private void checkNameIsNotBlank(Organismo organismo) {
        if (StringUtils.isBlank(organismo.getName())) {
            throw new CustomParameterizedExceptionBuilder().message(String.format(FIELD_BLANK_ERROR_MESSAGE, "name", organismo.getId())).code(ErrorConstants.ORGANISM_KEY_IS_BLANK).build();
        }
    }

    private void checkDescriptionLimit(Organismo organismo) {
        if (organismo.getDescription() != null && Integer.compare(organismo.getDescription().length(), FIELD_DESCRIPTION_SIZE) == 1) {
            throw new CustomParameterizedExceptionBuilder().message(String.format(DESCRIPTION_LIMIT_EXCEED_ERROR)).code(ErrorConstants.ORGANISM_FIELD_DESCRIPTION_LIMIT_EXCEED_ERROR).build();
        }
    }

}
