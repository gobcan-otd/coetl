import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs';
import { Organism } from '.';
import { createRequestOption, ResponseWrapper } from '../../shared';

@Injectable()
export class OrganismService {
    private resourceUrl = 'api/organism';

    constructor(private http: Http) {}

    public findAllOrganism(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http
            .get(`${this.resourceUrl}`, options)
            .map((response: Response) => this.convertResponseToOrganismReponseWrapper(response));
    }

    public findByIdUsuarioRolOrganismo(idUsuario: number): Observable<Organism[]> {
        return this.http.get(`${this.resourceUrl}/${idUsuario}`).map((res: Response) => res.json());
    }

    public findByIdUsuarioRolOrganismoManageEtl(idUsuario: number): Observable<Organism[]> {
        return this.http
            .get(`${this.resourceUrl}/${idUsuario}/manage`)
            .map((res: Response) => res.json());
    }

    public createOrganism(organismo: Organism): Observable<Organism> {
        return this.http
            .post(`${this.resourceUrl}`, organismo)
            .map((response: Response) => this.convertItemToOrganism(response.json()));
    }

    public updateOrganism(organismo: Organism): Observable<Organism> {
        return this.http
            .put(`${this.resourceUrl}`, organismo)
            .map((response: Response) => this.convertItemToOrganism(response.json()));
    }

    public deleteOrganism(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    private convertResponseToOrganismReponseWrapper(response: Response): ResponseWrapper {
        const jsonResponse = response
            .json()
            .map((element: any) => this.convertItemToOrganism(element));
        return new ResponseWrapper(response.headers, jsonResponse, response.status);
    }

    private convertItemToOrganism(entity: any): Organism {
        return Object.assign(new Organism(), entity);
    }

    findAll(): Observable<Organism[]> {
        return this.http.get(this.resourceUrl).map((res: Response) => res.json());
    }
}
