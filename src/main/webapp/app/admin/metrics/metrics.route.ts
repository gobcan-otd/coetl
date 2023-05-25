import { Route } from '@angular/router';

import { JhiMetricsMonitoringComponent } from './metrics.component';
import { UserRouteAccessService } from '../../shared';
import { ONLY_ADMIN } from '../../shared';

export const metricsRoute: Route = {
    path: 'jhi-metrics',
    component: JhiMetricsMonitoringComponent,
    data: {
        pageTitle: 'metrics.title',
        roles: ONLY_ADMIN
    },
    canActivate: [UserRouteAccessService]
};
