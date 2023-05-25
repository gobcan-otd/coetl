import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { GenericModalService, ResponseWrapper, PermissionService } from '../../shared';
import { Parameter, Type, Typology } from '../../entities/parameter';
import { GloablParameterService } from './global-parameter.service';
import { GlobalParameterDialogComponent } from './gloabl-parameter-dialog/global-parameter-dialog.component';
import { GlobalParameterDeleteDialogComponent } from './gloabl-parameter-dialog/global-parameter-delete-dialog.component';
import { Subscription } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { EtlParameterHelpDialogComponent } from '../../entities/etl/etl-parameter-help-dialog';

@Component({
    templateUrl: 'global-parameter.component.html',
    styleUrls: ['global-parameter.component.scss']
})
export class GlobalParameterComponent implements OnInit, OnDestroy {
    public static EVENT_NAME = 'etlParameterListModification';
    private page: number;
    private totalItems: number;
    private itemsPerPage: number;
    public visibleAction: boolean;

    public isPassword: boolean;
    public parameters: Parameter[];

    private routeDataSubscription: any;
    private predicate: any;
    private reverse: any;
    public styleCase = false;

    private eventSubscriber: Subscription;

    constructor(
        private globalParameterService: GloablParameterService,
        private activatedRoute: ActivatedRoute,
        private genericModalService: GenericModalService,
        private translateService: TranslateService,
        private router: Router,
        private eventManager: JhiEventManager,
        public permissionService: PermissionService
    ) {
        this.routeDataSubscription = this.activatedRoute.data.subscribe((data) => {
            this.page = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
            this.itemsPerPage = data['pagingParams'].itemsPerPage;
        });
        this.visibleAction = false;
    }

    ngOnInit() {
        this.loadAll();
        this.registerChanges();
        this.isVisibleAction();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    private loadAll(req?: { page; size }) {
        const requestOption = req
            ? req
            : {
                  page: this.page,
                  size: this.itemsPerPage
              };

        this.globalParameterService
            .findAllParameters({
                page: requestOption.page - 1,
                size: requestOption.size,
                sort: this.sort()
            })
            .subscribe((response: ResponseWrapper) =>
                this.onSuccess(response.json, response.headers)
            );
    }

    private registerChanges() {
        this.eventSubscriber = this.eventManager.subscribe(
            GlobalParameterComponent.EVENT_NAME,
            (response) => this.loadAll()
        );
    }

    public transition() {
        this.router.navigate(['/global-parameters'], {
            queryParams: Object.assign({}, this.activatedRoute.snapshot.queryParams, {
                page: this.page,
                size: this.itemsPerPage,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            })
        });
    }

    private sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    public existParameters(): boolean {
        return !!this.parameters && !!this.parameters.length;
    }

    public editParameter(parameter?: Parameter) {
        let copy = new Parameter();
        if (!!parameter) {
            copy = Object.assign(copy, parameter);
            if (Typology.PASSWORD === parameter.typology) {
                this.globalParameterService.decodeParameter(parameter.id).subscribe((response) => {
                    this.openEditParameterDialog(response);
                });
            } else {
                this.openEditParameterDialog(copy);
            }
        } else {
            copy.type = Type.GLOBAL;
            this.openEditParameterDialog(copy);
        }
    }

    private openEditParameterDialog(parameter: Parameter) {
        this.genericModalService.open(GlobalParameterDialogComponent as Component, { parameter });
    }

    public deleteParameter(parameter: Parameter) {
        const copy = Object.assign(new Parameter(), parameter);

        this.genericModalService.open(GlobalParameterDeleteDialogComponent as Component, {
            parameter: copy
        });
    }

    private onSuccess(data: Parameter[], headers) {
        this.totalItems = headers.get('X-Total-Count');
        this.parameters = data;
    }

    public isPasswordTypology(parameter: Parameter): boolean {
        this.isOverflow(parameter);
        return parameter.typology === Typology.PASSWORD ? true : false;
    }

    public getTypeName(parameter: Parameter): string {
        return this.translateService.instant(`coetlApp.parameter.type.${parameter.type}`);
    }

    public help() {
        this.genericModalService.open(<any>EtlParameterHelpDialogComponent, {});
    }

    public isOverflow(parameter: Parameter) {
        this.styleCase = parameter.value.length > 255;
    }

    public isVisibleAction() {
        this.visibleAction = this.permissionService.canEditParametrosGlobales();
    }
}
