import { Injector } from '@angular/core';
import { Http, XHRBackend, RequestOptions } from '@angular/http';
import { JhiEventManager, JhiInterceptableHttp } from 'ng-jhipster';

import { AuthInterceptor } from './auth.interceptor';
import { LocalStorageService, SessionStorageService } from 'ng2-webstorage';
import { AuthExpiredInterceptor } from './auth-expired.interceptor';
import { ErrorHandlerInterceptor } from './errorhandler.interceptor';
import { CookieService } from 'ngx-cookie';

export function interceptableFactory(
    backend: XHRBackend,
    defaultOptions: RequestOptions,
    localStorage: LocalStorageService,
    sessionStorage: SessionStorageService,
    cookieService: CookieService,
    injector: Injector,
    eventManager: JhiEventManager
) {
    return new JhiInterceptableHttp(backend, defaultOptions, [
        new AuthInterceptor(localStorage, sessionStorage, cookieService),
        new AuthExpiredInterceptor(injector),
        // Other interceptors can be added here
        new ErrorHandlerInterceptor(eventManager)
    ]);
}

export function customHttpProvider() {
    return {
        provide: Http,
        useFactory: interceptableFactory,
        deps: [
            XHRBackend,
            RequestOptions,
            LocalStorageService,
            SessionStorageService,
            CookieService,
            Injector,
            JhiEventManager
        ]
    };
}
