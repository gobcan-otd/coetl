import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CoetlSharedModule } from '../../shared';
import { EtlService } from './etl.service';
import { etlRoute, EtlResolvePagingParams } from './etl.route';
import { EtlComponent } from './etl.component';
import { EtlSearchComponent } from './etl-search';
import { EtlResolve } from './etl-resolve.service';
import { EtlFormComponent } from './etl-form.component';
import { EtlDeleteDialogComponent } from './etl-delete-dialog.component';
import { EtlRestoreDialogComponent } from './etl-restore-dialog.component';
import { EtlConfirmExecutionDialogComponent } from './etl-confirm-execution-dialog.component';
import { EtlExecutionListComponent } from './etl-execution-list/etl-execution-list.component';
import { EtlExpressionHelpDialogComponent } from './etl-expression-help-dialog/etl-expression-help-dialog.component';
import { EtlParameterListComponent } from './etl-parameter-list/etl-parameter-list.component';
import { EtlParameterDialogComponent } from './etl-parameter-list/etl-parameter-dialog.component';
import { EtlParameterDeleteDialogComponent } from './etl-parameter-list/etl-parameter-delete-dialog.component';
import { EtlParameterHelpDialogComponent } from './etl-parameter-help-dialog';
import { OrganismService } from '../../admin/organism/organism.service';

const ENTITY_STATES = [...etlRoute];

@NgModule({
    imports: [CoetlSharedModule, RouterModule.forRoot(ENTITY_STATES, { useHash: true })],
    declarations: [
        EtlComponent,
        EtlSearchComponent,
        EtlFormComponent,
        EtlDeleteDialogComponent,
        EtlRestoreDialogComponent,
        EtlConfirmExecutionDialogComponent,
        EtlExecutionListComponent,
        EtlExpressionHelpDialogComponent,
        EtlParameterListComponent,
        EtlParameterDialogComponent,
        EtlParameterDeleteDialogComponent,
        EtlParameterHelpDialogComponent
    ],
    entryComponents: [
        EtlDeleteDialogComponent,
        EtlRestoreDialogComponent,
        EtlConfirmExecutionDialogComponent,
        EtlExpressionHelpDialogComponent,
        EtlParameterDialogComponent,
        EtlParameterDeleteDialogComponent,
        EtlParameterHelpDialogComponent
    ],
    providers: [EtlService, EtlResolve, EtlResolvePagingParams, OrganismService]
})
export class CoetlEtlModule {}
