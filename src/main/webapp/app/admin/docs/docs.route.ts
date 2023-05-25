import { Route } from '@angular/router';

import { JhiDocsComponent } from './docs.component';
import { UserRouteAccessService } from '../../shared';
import { ONLY_ADMIN } from '../../shared';

export const docsRoute: Route = {
    path: 'docs',
    component: JhiDocsComponent,
    data: {
        pageTitle: 'global.menu.admin.apidocs',
        roles: ONLY_ADMIN
    },
    canActivate: [UserRouteAccessService]
};
