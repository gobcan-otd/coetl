package es.gobcan.coetl.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.repository.RolesRepository;
import es.gobcan.coetl.service.RolesService;

@Service
public class RolesServiceImpl implements RolesService {

    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public List<Roles> findAll() {
        return rolesRepository.findAll();
    }

}
