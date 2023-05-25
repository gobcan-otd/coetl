package es.gobcan.coetl.service;

import java.util.List;

import es.gobcan.coetl.domain.Organismo;

public interface OrganismoService {

    public List<Organismo> findAll();
    public List<Organismo> findByIdUsuario(Long idUsuario);
    public List<Organismo> findByIdUsuarioManage(Long idUsuario);
    public Organismo create(Organismo organismo);
    public Organismo update(Organismo organismo);
    public void delete(Organismo organismo);
    public Organismo findOneByOrganizationInCharge(Long id);
    public void validaciones(Organismo organismo, Organismo repetido);
    public void validationDelete(Long idOrganismo);

}
