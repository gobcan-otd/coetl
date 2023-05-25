import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs';
import { Roles } from './roles.model';
import { createRequestOption, ResponseWrapper } from '../../../shared/model';

@Injectable()
export class RolesService {
    private resourceUrl = 'api/roles';

    constructor(private http: Http) {}

    public findAllRoles(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http
            .get(`${this.resourceUrl}`, options)
            .map((response: Response) => this.convertResponseToRolesReponseWrapper(response));
    }

    private convertResponseToRolesReponseWrapper(response: Response): ResponseWrapper {
        const jsonResponse = response
            .json()
            .map((element: any) => this.convertItemToRoles(element));
        return new ResponseWrapper(response.headers, jsonResponse, response.status);
    }

    private convertItemToRoles(entity: any): Roles {
        return Object.assign(new Roles(), entity);
    }
}
