import { query } from '@angular/core/src/animation/dsl';
import { QueryValueType } from '@angular/core/src/view';
import { URLSearchParams, BaseRequestOptions } from '@angular/http';

export const createRequestOption = (req?: any): BaseRequestOptions => {
    const options: BaseRequestOptions = new BaseRequestOptions();
    if (req) {
        const params: URLSearchParams = new URLSearchParams();
        params.set('page', req.page);
        params.set('size', req.size);
        if (req.sort) {
            params.paramsMap.set('sort', req.sort);
        }

        params.set('query', req.query);
        if (!!req.includeDeleted) {
            params.set('includeDeleted', 'true');
        }

        if (req.idUsuario) {
            params.set('organismos', req.organismos);
        }

        if (req.permisos) {
            params.set('permisos', req.permisos);
        }

        options.params = params;
    }
    return options;
};

export const orderParamsToQuery = (param) => {
    // Ejemplo: ['sujeto,asc', 'accion,desc', 'id'] => 'SUJETO ASC, ACCION DESC'
    return param
        .map((p) => p.split(','))
        .filter((a) => a.length === 2)
        .map((a) => a.map((s) => s.toUpperCase()).join(' '))
        .join(',');
};
