import { Route } from '@angular/router';

import { HealthComponent } from './health.component';
import { UserRouteAccessService } from '../../shared';
import { ONLY_ADMIN } from '../../shared';

export const healthRoute: Route = {
    path: 'jhi-health',
    component: HealthComponent,
    data: {
        pageTitle: 'health.title',
        roles: ONLY_ADMIN
    },
    canActivate: [UserRouteAccessService]
};
