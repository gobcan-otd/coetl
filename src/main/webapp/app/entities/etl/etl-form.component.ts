import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { JhiEventManager } from 'ng-jhipster';
import { Autosize } from 'ng-autosize';
import { Subscription, Observable } from 'rxjs';

import {
    GenericModalService,
    PermissionService,
    HasTitlesContainer,
    Principal,
    Rol,
    Visibility
} from '../../shared';
import { Etl, Type } from './etl.model';
import { EtlService } from './etl.service';
import { EtlDeleteDialogComponent } from './etl-delete-dialog.component';
import { EtlRestoreDialogComponent } from './etl-restore-dialog.component';
import { EtlConfirmExecutionDialogComponent } from './etl-confirm-execution-dialog.component';
import { EtlExpressionHelpDialogComponent } from './etl-expression-help-dialog/etl-expression-help-dialog.component';
import { Organism } from '../../admin/organism/organism.model';
import { OrganismService } from '../../admin/organism/organism.service';
import { Roles } from '../../shared/service/roles/roles.model';
import { UsuarioRolOrganismo } from '../../shared/service/user-rol-organismos/user-rol-organismos.model';
import { isatty } from 'tty';

@Component({
    selector: 'ac-etl-form',
    templateUrl: 'etl-form.component.html'
})
export class EtlFormComponent implements OnInit, AfterViewInit, OnDestroy, HasTitlesContainer {
    public static EVENT_NAME = 'etlListModification';

    instance: EtlFormComponent;

    public organismSelected: Organism;

    public etl: Etl;
    public typeEnum = Type;
    public isSaving: boolean;

    updatesSubscription: Subscription;

    public organisms: Organism[];
    visibleButtons: boolean;
    private idOrganismoActualElegido: number;
    private accionCrear: boolean;
    public isAdmin: boolean;
    public visibilityEnum = Visibility;
    public visibilitySelected: Visibility;

    @ViewChild(Autosize) purposeContainer: Autosize;

    @ViewChild(Autosize) functionalInChargeContainer: Autosize;

    @ViewChild(Autosize) technicalInChargeContainer: Autosize;

    @ViewChild(Autosize) commentsContainer: Autosize;

    @ViewChild(Autosize) executionDescriptionContainer: Autosize;

    @ViewChild('titlesContainer') titlesContaner: ElementRef;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private etlService: EtlService,
        private genericModalService: GenericModalService,
        private eventManager: JhiEventManager,
        private permissionService: PermissionService,
        private translateService: TranslateService,
        private organismService: OrganismService,
        private principal: Principal
    ) {
        this.instance = this;
    }

    ngOnInit() {
        this.isSaving = false;
        this.etl = !!this.route.snapshot.data['etl'] ? this.route.snapshot.data['etl'] : new Etl();
        this.accionCrear =
            this.route.snapshot.url[this.route.snapshot.url.length - 1].path === 'etl-new';
        this.getOrganismosSelect();
        this.organismSelected = this.etl.organizationInCharge;
        this.registerChangesOnEtl();
        if (this.accionCrear) {
            this.idOrganismoActualElegido = null;
        } else {
            this.idOrganismoActualElegido = this.etl.organizationInCharge.id;
        }
        this.visibleButtons = this.canEdit();
        this.isAdmin = this.getIsAdmin();
        this.getVisibilitySelected();
    }

    ngAfterViewInit() {
        setTimeout(() => this.adjustAllContainers(), null);
    }

    ngOnDestroy() {}

    clear() {
        if (this.etl.id) {
            this.router.navigate(['/etl', this.etl.id]);
        } else {
            this.router.navigate(['/etl']);
        }
    }

    organismItemTemplate(item: Organism) {
        return `${item.name}`;
    }

    public setOrganismInCharge() {
        this.etl.organizationInCharge = this.organismSelected;
    }

    public setVisibility() {
        this.etl.visibility = this.visibilitySelected === Visibility.PRIVADA ? true : false;
    }

    save() {
        this.etl.visibility = this.etl.visibility === undefined ? false : this.etl.visibility;
        this.isSaving = true;
        const etlEditObservable = !!this.etl.id
            ? this.etlService.update(this.etl)
            : this.etlService.create(this.etl);
        this.subscribeToSaveResponse(etlEditObservable);
    }

    delete() {
        const copy = Object.assign(new Etl(), this.etl);
        this.genericModalService.open(<any>EtlDeleteDialogComponent, { etl: copy });
    }

    restore() {
        const copy = Object.assign(new Etl(), this.etl);
        this.genericModalService.open(<any>EtlRestoreDialogComponent, { etl: copy });
    }

    execute() {
        const copy = Object.assign(new Etl(), this.etl);
        this.genericModalService.open(<any>EtlConfirmExecutionDialogComponent, { etl: copy });
    }

    help() {
        this.genericModalService.open(<any>EtlExpressionHelpDialogComponent, {});
    }

    isEditMode(): Boolean {
        const lastPath = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
        return lastPath === 'edit' || lastPath === 'etl-new';
    }

    public canEdit(): boolean {
        if (this.accionCrear) {
            return true;
        } else {
            return this.validateCanEditNotCreate();
        }
    }

    private validateCanEditNotCreate(): boolean {
        const organismosUsuario: UsuarioRolOrganismo[] = this.instance.principal.getUsuarioRolOrganismo();
        const organismoEtl: number = this.idOrganismoActualElegido;
        const rolesUsuario: Roles[] = this.instance.principal.getRoles();

        if (this.permissionService.isAdmin()) {
            return true;
        }
        if (
            rolesUsuario &&
            rolesUsuario.find((rol, index) => {
                if (
                    this.permissionService.isTecnico(rol.name) &&
                    organismosUsuario[index].rol.id === rol.id &&
                    organismosUsuario[index].organismo.id === organismoEtl
                ) {
                    return true;
                }
            })
        ) {
            return true;
        }
        return false;
    }

    getDeletedMessage(etl: Etl): string {
        const codeMessage = etl.isDeleted()
            ? 'coetlApp.etl.state.isDeleted'
            : 'coetlApp.etl.state.isNotDeleted';
        return this.translateService.instant(codeMessage);
    }

    getTitlesContainer(): ElementRef {
        return this.titlesContaner;
    }

    canShowNextExecution(): boolean {
        return this.etl.isPlanning() && !!this.etl.id;
    }

    canSave(): boolean {
        return !this.isSaving && !!this.etl.uriRepository;
    }

    private subscribeToSaveResponse(result: Observable<Etl>) {
        result.subscribe((res: Etl) => this.onSaveSuccess(res), () => this.onSaveError());
    }

    private onSaveSuccess(result: Etl) {
        this.isSaving = false;
        this.eventManager.broadcast({ name: EtlFormComponent.EVENT_NAME, content: 'saved' });
        this.router.navigate(['etl', result.id]);
        this.visibleButtons = this.canEdit();
        this.idOrganismoActualElegido = this.etl.organizationInCharge.id;
    }

    private onSaveError() {
        this.isSaving = false;
    }

    private registerChangesOnEtl() {
        this.updatesSubscription = this.eventManager.subscribe(
            EtlFormComponent.EVENT_NAME,
            (result) => {
                if (result.content !== 'saved') {
                    this.load(result.content);
                }
            }
        );
    }

    private load(entity: any) {
        this.etl = Object.assign(new Etl(), entity);
        this.idOrganismoActualElegido = this.etl.organizationInCharge.id;
    }

    private adjustAllContainers() {
        this.purposeContainer.adjust();
        this.functionalInChargeContainer.adjust();
        this.technicalInChargeContainer.adjust();
        this.commentsContainer.adjust();
        this.executionDescriptionContainer.adjust();
    }

    private getOrganismosSelect() {
        if (this.accionCrear) {
            this.organismService
                .findByIdUsuarioRolOrganismoManageEtl(this.principal.getUserId())
                .subscribe((organism) => {
                    this.organisms = organism;
                });
        } else {
            this.organismService
                .findByIdUsuarioRolOrganismo(this.principal.getUserId())
                .subscribe((organism) => {
                    this.organisms = organism;
                });
        }
    }

    public getIsAdmin() {
        return this.permissionService.isAdmin();
    }

    public getVisibilityMessage() {
        const codeMessage = this.isAdmin
            ? 'coetlApp.etl.visibility.state.private'
            : 'coetlApp.etl.visibility.state.public';
        return this.translateService.instant(codeMessage);
    }

    public getVisibilitySelected() {
        if (this.etl && this.etl.id) {
            this.visibilitySelected =
                this.etl.visibility === true ? Visibility.PRIVADA : Visibility.PUBLICA;
        } else {
            this.visibilitySelected = Visibility.PUBLICA;
        }
    }
}
