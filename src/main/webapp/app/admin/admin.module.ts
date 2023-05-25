import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CoetlSharedModule } from '../shared';
/* jhipster-needle-add-admin-module-import - JHipster will add admin modules imports here */

import {
    adminState,
    AuditsComponent,
    UserMgmtComponent,
    UserDeleteDialogComponent,
    UserMgmtFormComponent,
    UserMgmtDeleteDialogComponent,
    LogsComponent,
    JhiMetricsMonitoringModalComponent,
    JhiMetricsMonitoringComponent,
    HealthDialogComponent,
    HealthEditDialogComponent,
    HealthDeleteDialogComponent,
    HealthComponent,
    JhiConfigurationComponent,
    JhiDocsComponent,
    AuditsService,
    JhiConfigurationService,
    HealthService,
    JhiMetricsService,
    LogsService,
    UserResolvePagingParams,
    UserModalService,
    AuditsResolvePagingParams,
    GlobalParameterComponent,
    OrganismComponent
} from '.';
import { UserSearchComponent } from './user-management/user-search';
import { GloablParameterService } from './global-parameters/global-parameter.service';
import { ParameterResolvePagingParams } from './global-parameters/global-parameter.route';
import { GlobalParameterDialogComponent } from './global-parameters/gloabl-parameter-dialog/global-parameter-dialog.component';
import { GlobalParameterDeleteDialogComponent } from './global-parameters/gloabl-parameter-dialog/global-parameter-delete-dialog.component';
import { OrganismService } from './organism/organism.service';
import { OrganismResolvePagingParams } from './organism/organism.route';
import { OrganismDialogComponent } from './organism/organism-dialog/organism-dialog.component';
import { OrganismDialogDeleteComponent } from './organism/organism-dialog/organism-dialog-delete.component';
import { UsuarioRolOrganismoService } from '../shared/service/user-rol-organismos/user-rol-organismos.service';
import { RolesService } from '../shared/service/roles/roles.service';
import { MultiSelectModule } from 'primeng/primeng';

@NgModule({
    imports: [
        CoetlSharedModule,
        RouterModule.forRoot(adminState, { useHash: true }),
        MultiSelectModule
        /* jhipster-needle-add-admin-module - JHipster will add admin modules here */
    ],
    declarations: [
        AuditsComponent,
        UserSearchComponent,
        UserMgmtComponent,
        UserDeleteDialogComponent,
        UserMgmtFormComponent,
        UserMgmtDeleteDialogComponent,
        LogsComponent,
        JhiConfigurationComponent,
        HealthComponent,
        HealthDialogComponent,
        HealthEditDialogComponent,
        HealthDeleteDialogComponent,
        JhiDocsComponent,
        JhiMetricsMonitoringComponent,
        JhiMetricsMonitoringModalComponent,
        GlobalParameterComponent,
        GlobalParameterDialogComponent,
        GlobalParameterDeleteDialogComponent,
        OrganismComponent,
        OrganismDialogComponent,
        OrganismDialogDeleteComponent
    ],
    entryComponents: [
        UserMgmtFormComponent,
        UserMgmtDeleteDialogComponent,
        HealthDialogComponent,
        HealthEditDialogComponent,
        HealthDeleteDialogComponent,
        JhiMetricsMonitoringModalComponent,
        GlobalParameterDialogComponent,
        GlobalParameterDeleteDialogComponent,
        OrganismDialogComponent,
        OrganismComponent,
        OrganismDialogDeleteComponent
    ],
    providers: [
        AuditsService,
        JhiConfigurationService,
        HealthService,
        JhiMetricsService,
        LogsService,
        AuditsResolvePagingParams,
        UserResolvePagingParams,
        UserModalService,
        GloablParameterService,
        ParameterResolvePagingParams,
        OrganismService,
        OrganismResolvePagingParams,
        UsuarioRolOrganismoService,
        RolesService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class CoetlAdminModule {}
