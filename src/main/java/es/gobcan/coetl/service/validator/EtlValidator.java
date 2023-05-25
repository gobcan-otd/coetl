package es.gobcan.coetl.service.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.errors.util.CustomExceptionUtil;
import es.gobcan.coetl.repository.EtlRepository;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class EtlValidator extends AbstractValidator<Etl> {

    @Autowired
    private EtlRepository etlRepository;

    @Override
    public void validate(Etl entity) {
        checkCodeIsUnique(entity);
        checkUriRepository(entity);
    }

    private void checkCodeIsUnique(Etl entity) {
        if (entity.getId() != null) {
            return;
        }

        Etl foundEtl = etlRepository.findOneByCode(entity.getCode());
        if (foundEtl != null) {
            CustomExceptionUtil.throwCustomParameterizedException(String.format("Etl code %s exists", entity.getCode()), ErrorConstants.ETL_CODE_EXISTS);
        }
    }

    private void checkUriRepository(Etl entity) {
        if (entity.getUriRepository() == null) {
            CustomExceptionUtil.throwCustomParameterizedException(String.format("URL repository not found in Etl %s", entity.getCode()), ErrorConstants.ETL_URL_NOT_EXIST);
        }

        try {
            new URL(entity.getUriRepository());
        } catch (MalformedURLException e) {
            CustomExceptionUtil.throwCustomParameterizedException(String.format("URL repository '%s' is malformed", entity.getCode()), ErrorConstants.ETL_MALFORMED_URL);
        }
    }
}
