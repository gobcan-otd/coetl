import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { OrganismService } from './organism.service';
import { Organism } from './organism.model';
import { GenericModalService, ResponseWrapper, PermissionService } from '../../shared';
import { Subscription } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { OrganismDialogComponent } from './organism-dialog/organism-dialog.component';
import { OrganismDialogDeleteComponent } from './organism-dialog/organism-dialog-delete.component';

@Component({
    templateUrl: 'organism.component.html',
    styleUrls: ['organism.component.scss']
})
export class OrganismComponent implements OnInit, OnDestroy {
    public static EVENT_NAME = 'etlOrganismListModification';
    public organismos: Organism[];
    private page: number;
    private itemsPerPage: number;
    private predicate: any;
    private reverse: any;
    private eventSubscriber: Subscription;

    constructor(
        private globalOrganismService: OrganismService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private eventManager: JhiEventManager,
        private genericModalService: GenericModalService,
        public permissionService: PermissionService
    ) {
        this.activatedRoute.data.subscribe((data) => {
            this.page = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
            this.itemsPerPage = data['pagingParams'].itemsPerPage;
        });
    }

    ngOnInit() {
        this.loadAll();
        this.registerChanges();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    private registerChanges() {
        this.eventSubscriber = this.eventManager.subscribe(
            OrganismComponent.EVENT_NAME,
            (response) => this.loadAll()
        );
    }

    public existOrganismos(): boolean {
        return !!this.organismos && !!this.organismos.length;
    }

    private loadAll(req?: { page; size }) {
        const requestOption = req
            ? req
            : {
                  page: this.page,
                  size: this.itemsPerPage
              };

        this.globalOrganismService
            .findAllOrganism({
                page: requestOption.page - 1,
                size: requestOption.size,
                sort: this.sort()
            })
            .subscribe((response: ResponseWrapper) => this.onSuccess(response.json));
    }

    private sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    private onSuccess(data: Organism[]) {
        this.organismos = data;
    }

    public transition() {
        this.router.navigate(['/organism'], {
            queryParams: Object.assign({}, this.activatedRoute.snapshot.queryParams, {
                page: this.page,
                size: this.itemsPerPage,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            })
        });
    }

    public isOverflow(organism: Organism) {
        return organism.description == null ? false : organism.description.length > 255;
    }

    public editOrganismo(organismo?: Organism) {
        if (organismo) {
            this.openEditParameterDialog(organismo);
        } else {
            this.openEditParameterDialog(new Organism());
        }
    }

    public deleteOrganismo(organismo?: Organism) {
        if (organismo && organismo.id) {
            this.genericModalService.open(OrganismDialogDeleteComponent as Component, {
                organismo
            });
        }
    }

    private openEditParameterDialog(organismo: Organism) {
        this.genericModalService.open(OrganismDialogComponent as Component, { organismo });
    }
}
