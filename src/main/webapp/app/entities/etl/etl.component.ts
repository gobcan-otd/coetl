import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { JhiEventManager, JhiParseLinks } from 'ng-jhipster';
import { Subscription } from 'rxjs';

import { Account, PermissionService, Principal, ResponseWrapper } from '../../shared';
import { EtlBase } from './etl.model';
import { EtlFilter } from './etl-search';
import { EtlService } from './etl.service';
import { EtlFormComponent } from './etl-form.component';
import { DatePipe } from '@angular/common';
import { Organism } from '../../admin/organism/organism.model';
import { OrganismService } from '../../admin/organism/organism.service';
import { Execution, Result } from '../execution/execution.model';
import { Roles } from '../../shared/service/roles/roles.model';
import { UsuarioRolOrganismo } from '../../shared/service/user-rol-organismos';

@Component({
    selector: 'ac-etl',
    templateUrl: 'etl.component.html'
})
export class EtlComponent implements OnInit, OnDestroy {
    page: number;
    totalItems: number;
    itemsPerPage: number;

    currentAccount: Account;
    etls: EtlBase[];
    eventSubscriber: Subscription;
    searchSubsctiption: Subscription;
    routeDataSubscription: any;
    links: any;
    predicate: any;
    reverse: any;
    filters: EtlFilter;
    public showCreateButton: boolean;
    private instance: EtlComponent;

    constructor(
        private eventManager: JhiEventManager,
        private etlService: EtlService,
        private principal: Principal,
        private parseLinks: JhiParseLinks,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private translateService: TranslateService,
        private datePipe: DatePipe,
        private organismoService: OrganismService,
        private permissionService: PermissionService
    ) {
        this.routeDataSubscription = this.activatedRoute.data.subscribe((data) => {
            this.page = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
            this.itemsPerPage = data['pagingParams'].itemsPerPage;
        });
        this.instance = this;
        this.filters = new EtlFilter(this.datePipe);
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });

        this.activatedRoute.queryParams.subscribe((params) => {
            this.filters.fromQueryParams(params).subscribe(() => this.loadAll());
        });
        this.checkShowCreateButton();
        this.registerChangeInEtls();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
        this.eventManager.destroy(this.searchSubsctiption);
    }

    loadAll() {
        this.etlService
            .query({
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort(),
                query: this.getFiltersVisibility(),
                includeDeleted: this.filters ? this.filters.includeDeleted : false,
                idUsuario: this.principal.getUserId(),
                organismos: this.principal.getOrganismos(),
                lastExecution:
                    this.filters && this.filters.lastExecution ? this.filters.lastExecution : '',
                lastExecutionByResult:
                    this.filters && this.filters.lastExecutionByResult
                        ? this.filters.lastExecutionByResult
                        : ''
            })
            .subscribe((res: ResponseWrapper) => this.onSuccess(res.json, res.headers));
    }

    private checkShowCreateButton() {
        if (this.principal.userIsAdmin()) {
            this.showCreateButton = true;
        } else {
            if (
                this.principal
                    .getUsuarioRolOrganismo()
                    .find((uro) => this.permissionService.isTecnico(uro.rol.name))
            ) {
                this.showCreateButton = true;
            } else {
                this.showCreateButton = false;
            }
        }
    }

    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    transition() {
        this.router.navigate(['/etl'], {
            queryParams: Object.assign({}, this.activatedRoute.snapshot.queryParams, {
                page: this.page,
                size: this.itemsPerPage,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            })
        });
    }

    clear() {
        this.page = 0;
        this.router.navigate([
            '/etl',
            {
                page: this.page,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        ]);
    }

    trackId(index: number, item: EtlBase) {
        return item.id;
    }

    getTypeName(etl: EtlBase): string {
        return this.translateService.instant(`coetlApp.etl.type.${etl.type}`);
    }

    getPlanningMessage(etl: EtlBase): string {
        const messageCode = etl.isPlanning()
            ? 'coetlApp.etl.planning.isPlanning'
            : 'coetlApp.etl.planning.isNotPlanning';
        return this.translateService.instant(messageCode);
    }

    private registerChangeInEtls() {
        this.eventSubscriber = this.eventManager.subscribe(
            EtlFormComponent.EVENT_NAME,
            (response) => this.loadAll()
        );
        this.searchSubsctiption = this.eventManager.subscribe('etlSearch', () => {
            this.page = 1;
            const queryParams = Object.assign(
                {},
                this.filters.toUrl(this.activatedRoute.snapshot.queryParams),
                { page: this.page }
            );
            this.router.navigate(['etl'], { queryParams });
        });
    }

    private onSuccess(data: EtlBase[], headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.etls = data;
    }

    getResultBadgeClass(result: Result): any {
        if (result) {
            return {
                'bg-success': result === Result.SUCCESS,
                'bg-danger': result === Result.FAILED,
                'bg-warning': result === Result.WAITING,
                'bg-primary': result === Result.RUNNING,
                'bg-default': result === Result.DUPLICATED
            };
        }
        return '';
    }

    getResultName(execution: Execution): string {
        return execution
            ? this.translateService.instant(`coetlApp.execution.result.${execution}`)
            : '';
    }

    public getIsAdmin(): boolean {
        return this.permissionService.isAdmin();
    }

    private getFiltersVisibility(): string {
        if (!this.getIsAdmin()) {
            this.filters.visibility = true;
        }
        return this.filters ? this.filters.toQuery() : '';
    }
}
