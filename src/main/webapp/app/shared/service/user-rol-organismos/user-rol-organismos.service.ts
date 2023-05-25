import { Injectable } from '@angular/core';
import { BaseRequestOptions, Http, Response } from '@angular/http';
import { Observable } from 'rxjs';
import { createRequestOption } from '../../model/request-util';
import { ResponseWrapper } from '../../model/response-wrapper.model';
import { UsuarioRolOrganismoDto } from './user-rol-organismo.model';
import { UsuarioRolOrganismo } from './user-rol-organismos.model';

@Injectable()
export class UsuarioRolOrganismoService {
    private resourceUrl = 'api/usuarioRolOrganismo';

    constructor(private http: Http) {}

    public hasOrganismosOnlyLector(): Observable<boolean> {
        return this.http.get(`${this.resourceUrl}/canCreate`).map((response) => response.json());
    }

    updateUser(req?: any): Observable<UsuarioRolOrganismoDto[]> {
        return this.http
            .put(`${this.resourceUrl}/${req.idUsuario}`, req.permisos)
            .map((res: Response) => res.json());
    }

    delete(req?: any): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${req.idUsuario}`);
    }
}
