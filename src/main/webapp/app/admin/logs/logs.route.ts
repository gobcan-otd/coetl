import { Route } from '@angular/router';

import { LogsComponent } from './logs.component';
import { UserRouteAccessService } from '../../shared';
import { ONLY_ADMIN } from '../../shared';

export const logsRoute: Route = {
    path: 'logs',
    component: LogsComponent,
    data: {
        pageTitle: 'logs.title',
        roles: ONLY_ADMIN
    },
    canActivate: [UserRouteAccessService]
};
