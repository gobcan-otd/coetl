import { Route } from '@angular/router';

import { JhiConfigurationComponent } from './configuration.component';
import { UserRouteAccessService } from '../../shared';
import { ONLY_ADMIN } from '../../shared';

export const configurationRoute: Route = {
    path: 'jhi-configuration',
    component: JhiConfigurationComponent,
    data: {
        pageTitle: 'configuration.title',
        roles: [ONLY_ADMIN]
    },
    canActivate: [UserRouteAccessService]
};
