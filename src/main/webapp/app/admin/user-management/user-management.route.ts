import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil } from 'ng-jhipster';
import { UserMgmtComponent } from './user-management.component';
import { UserMgmtFormComponent } from './user-management-form.component';
import { UserDeleteDialogComponent } from './user-management-delete-dialog.component';
import { UserRouteAccessService, ITEMS_PER_PAGE } from '../../shared';
import { DEFAULT_PATH } from '../../home/home.component';
import { ONLY_ADMIN } from '../../shared';
import { BASE_DECIMAL } from '../../app.constants';

@Injectable()
export class UserResolvePagingParams implements Resolve<any> {
    constructor(private paginationUtil: JhiPaginationUtil) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        const itemsPerPage = route.queryParams['size'] ? route.queryParams['size'] : ITEMS_PER_PAGE;
        return {
            page: this.paginationUtil.parsePage(page),
            predicate: this.paginationUtil.parsePredicate(sort),
            ascending: this.paginationUtil.parseAscending(sort),
            itemsPerPage: parseInt(itemsPerPage, BASE_DECIMAL)
        };
    }
}

export const userMgmtRoute: Routes = [
    {
        path: 'user-management',
        canActivate: [UserRouteAccessService],
        component: UserMgmtComponent,
        resolve: {
            pagingParams: UserResolvePagingParams
        },
        data: {
            pageTitle: 'userManagement.home.title',
            roles: ONLY_ADMIN
        }
    },
    {
        path: 'user-management/:login',
        canActivate: [UserRouteAccessService],
        component: UserMgmtFormComponent,
        data: {
            pageTitle: 'userManagement.home.title',
            roles: ONLY_ADMIN
        }
    },
    {
        path: 'user-management-new',
        canActivate: [UserRouteAccessService],
        component: UserMgmtFormComponent,
        data: {
            pageTitle: 'userManagement.home.title',
            roles: ONLY_ADMIN
        }
    },
    {
        path: 'user-management/:login/edit',
        canActivate: [UserRouteAccessService],
        component: UserMgmtFormComponent,
        data: {
            roles: ONLY_ADMIN,
            pageTitle: 'userManagement.home.title'
        }
    }
];

export const userDialogRoute: Routes = [
    {
        path: 'user-management/:login/delete',
        canActivate: [UserRouteAccessService],
        component: UserDeleteDialogComponent,
        outlet: 'popup',
        data: {
            roles: ONLY_ADMIN
        }
    },
    {
        path: 'user-management/:login/restore',
        canActivate: [UserRouteAccessService],
        component: UserDeleteDialogComponent,
        outlet: 'popup',
        data: {
            roles: ONLY_ADMIN
        }
    }
];
