import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Response } from '@angular/http';
import { TranslateService } from '@ngx-translate/core';
import { JhiEventManager } from 'ng-jhipster';
import {
    User,
    UserService,
    Rol,
    ResponseWrapper,
    Principal,
    PermissionService
} from '../../shared';
import { Observable, Subscription } from 'rxjs';
import { UsuarioRolOrganismo } from '../../shared/service/user-rol-organismos/user-rol-organismos.model';
import { Roles } from '../../shared/service/roles/roles.model';
import { Organism, OrganismService } from '../organism';
import { RolesService } from '../../shared/service/roles';
import { AcAlertService } from '../../shared/component/alert/alert.service';
import { Etl, EtlBase } from '../../entities/etl/etl.model';
import { EtlService } from '../../entities/etl/etl.service';
import { EtlListItem } from './etl-list-item.model';

@Component({
    selector: 'jhi-user-mgmt-form',
    templateUrl: './user-management-form.component.html'
})
export class UserMgmtFormComponent implements OnInit, OnDestroy {
    public static EVENT_NAME = 'cserMgmtFormComponent';
    user: User;
    isSaving: Boolean;
    roleEnum = Rol;
    private subscription: Subscription;
    paramLogin: string;
    eventSubscriber: Subscription;
    public usuarioRolOrganismo: UsuarioRolOrganismo[];
    public rolesSelect: Roles[];
    public organismosSelect: Organism[];
    public organismos: Organism[];
    public mapSelect = new Map();
    public allRoles: Roles[];
    public allOrganismos: Organism[];

    public organismosSelectedTecnico: Organism[];
    public organismosSelectedLector: Organism[];

    public isEditItSelf: boolean;
    alerts: any[];

    public selectedEtls: EtlListItem[];
    public etlList: Etl[];
    public allEtlAccess: boolean;
    public selectorList: EtlListItem[];

    constructor(
        private userService: UserService,
        private etlService: EtlService,
        public permissionService: PermissionService,
        private eventManager: JhiEventManager,
        private route: ActivatedRoute,
        private router: Router,
        private organismService: OrganismService,
        private rolesService: RolesService,
        private principal: Principal,
        public acAlertService: AcAlertService,
        private translateService: TranslateService
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.isEditItSelf = false;
        this.organismosSelectedTecnico = [];
        this.organismosSelectedLector = [];

        this.selectedEtls = [];
        this.etlList = [];
        this.selectorList = [];

        this.subscription = this.route.params.subscribe((params) => {
            this.paramLogin = params['login'];
            this.load(this.paramLogin);
        });
        this.eventSubscriber = this.eventManager.subscribe('UserModified', (response) => {
            if (!response.content || response.content.action !== 'deleted') {
                this.load(response.content);
            }
        });
    }

    isEditMode(): Boolean {
        const lastPath = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
        return lastPath === 'edit' || lastPath === 'user-management-new';
    }

    private initSelectedEtls() {
        this.selectedEtls = [];
        this.user.etls.forEach((element, index) => {
            this.selectedEtls.push(this.selectorList.find((etl) => etl.id === element.id));
        });
    }

    load(login) {
        if (login) {
            this.userService.find(login).subscribe((user) => {
                this.getAllRoles();
                this.user = user;
                this.isEditItSelf = this.principal.getUserId() === this.user.id;
            });
        } else {
            this.user = new User();
            this.allEtlAccess = false;
            this.getAllRoles();
            this.getAllOrganismosByIdListOrganismNotIn();
        }
    }

    private loadAll() {
        this.etlService
            .findAllByOrganism(this.getOrganismos())
            .subscribe((res: ResponseWrapper) => this.onListSuccess(res.json));
    }

    private onListSuccess(data: EtlBase[]) {
        this.etlList = data;
        let tmp = [];
        data.forEach((etl: EtlBase) => {
            tmp.push({ id: etl.id, name: etl.name + ' - ' + etl.organizationInCharge.name });
        });
        this.selectorList = tmp;
        this.initSelectedEtls();
    }

    public getOrganismos(): number[] {
        const organismos: number[] = [];
        this.organismosSelectedTecnico.map((o) => {
            organismos.push(o.id);
        });
        this.organismosSelectedLector.map((o) => {
            organismos.push(o.id);
        });
        return organismos;
    }

    public selectEtl() {
        this.loadAll();
        this.getAllOrganismosByIdListOrganismNotIn();
    }

    public unSelectEtl(event: Organism, rol: any) {
        if (rol === Rol.TECNICO) {
            this.organismosSelectedTecnico = this.organismosSelectedTecnico.filter(
                (org) => org.id !== event.id
            );
        } else if (rol === Rol.LECTOR) {
            this.organismosSelectedLector = this.organismosSelectedLector.filter(
                (org) => org.id !== event.id
            );
        }
        this.loadAll();
        this.getAllOrganismosByIdListOrganismNotIn();
    }

    private loadDataSelect() {
        this.rolesSelect = [];
        let newList: Organism[] = [];
        this.user.usuarioRolOrganismo.map((u) => {
            if (this.rolesSelect && this.rolesSelect.length === 0) {
                this.organismosSelect = [];
                this.rolesSelect.push(u.rol);
                this.organismosSelect.push(u.organismo);
                this.mapSelect.set(u.rol.id, this.organismosSelect);
            } else if (!this.rolesSelect.find((r) => r.id === u.rol.id)) {
                newList = [];
                newList.push(u.organismo);
                this.mapSelect.set(u.rol.id, newList);
                this.rolesSelect.push(u.rol);
            } else if (this.rolesSelect.find((r) => r.id === u.rol.id)) {
                this.mapSelect.get(u.rol.id).push(u.organismo);
            }
        });
        this.setCurrentPermissionUserValues();
    }

    private getAllRoles() {
        this.rolesService
            .findAllRoles()
            .subscribe((response: ResponseWrapper) => this.onSuccess(response.json));
    }

    private getAllOrganismosByIdListOrganismNotIn() {
        const requestOption = { exclusions: this.getOrganismos() };
        this.organismService
            .findAllOrganism(requestOption)
            .subscribe((response: ResponseWrapper) =>
                this.convertResponseToRolesReponseWrapper(response.json)
            );
    }

    private onSuccess(data: Roles[]) {
        this.allRoles = data;
        if (this.user.id) {
            this.loadDataSelect();
            this.loadAll();
            this.allEtlAccess = this.user.allEtlAccess;
            this.getAllOrganismosByIdListOrganismNotIn();
        }
    }

    public setCurrentPermissionUserValues() {
        if (this.mapSelect && this.allRoles) {
            this.mapSelect.forEach((value: Organism[], key: number) => {
                if (this.allRoles.find((r) => r.id === key && r.name === Rol.TECNICO)) {
                    if (Array.isArray(value)) {
                        value.forEach((organismo: Organism) =>
                            this.organismosSelectedTecnico.push(organismo)
                        );
                    } else {
                        this.organismosSelectedTecnico.push(value['aux']);
                    }
                }
                if (this.allRoles.find((r) => r.id === key && r.name === Rol.LECTOR)) {
                    if (Array.isArray(value)) {
                        value.forEach((organismo: Organism) =>
                            this.organismosSelectedLector.push(organismo)
                        );
                    } else {
                        this.organismosSelectedLector.push(value['aux']);
                    }
                }
            });
        }
    }

    private convertResponseToRolesReponseWrapper(data: Organism[]) {
        this.allOrganismos = data;
    }

    organismosItemTemplate(item: Organism) {
        return `${item.name}`;
    }

    clear() {
        // const with arrays: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/const
        const returnPath = ['user-management'];
        if (this.paramLogin) {
            returnPath.push(this.paramLogin);
        }
        this.router.navigate(returnPath);
    }

    private setUsuarioRolOrganismo(
        organismoSeleted: Organism,
        currentOrganism: UsuarioRolOrganismo,
        rol: Rol
    ) {
        if (currentOrganism && currentOrganism.id !== null) {
            return currentOrganism;
        }

        const tmpThread: UsuarioRolOrganismo = new UsuarioRolOrganismo();
        tmpThread.organismo = organismoSeleted;
        tmpThread.rol = this.allRoles.find((r) => r.name === rol);
        return tmpThread;
    }

    private updateUsuarioRolOrganismoUser() {
        const tmpUsrRolOrganism = [];
        if (!this.user.isAdmin && this.allOrganismos.length !== 0) {
            this.organismosSelectedTecnico.forEach((value: Organism) => {
                tmpUsrRolOrganism.push(
                    this.setUsuarioRolOrganismo(
                        value,
                        this.user.usuarioRolOrganismo.find(
                            (current) =>
                                current.organismo.id == value.id && current.rol.name === Rol.TECNICO
                        ),
                        Rol.TECNICO
                    )
                );
            });
            this.organismosSelectedLector.forEach((value: Organism) => {
                tmpUsrRolOrganism.push(
                    this.setUsuarioRolOrganismo(
                        value,
                        this.user.usuarioRolOrganismo.find(
                            (current) =>
                                current.organismo.id == value.id && current.rol.name === Rol.LECTOR
                        ),
                        Rol.LECTOR
                    )
                );
            });
        }
        this.user.usuarioRolOrganismo = [];
        this.user.usuarioRolOrganismo = tmpUsrRolOrganism;
    }

    private getRolByName(name: string): Roles {
        return this.allRoles.find((r) => r.name === name);
    }

    public showRolName(name: string): boolean {
        return (
            (this.user.isAdmin && name === Rol.ADMIN) ||
            ((name === Rol.ADMIN || name === Rol.TECNICO || name === Rol.LECTOR) &&
                !this.user.isAdmin)
        );
    }

    private check(etl: EtlListItem) {
        const etltmp = this.etlList.filter((val) => etl && etl.id === val.id);
        return etltmp.length > 0;
    }

    private getTranslationName(jsonToTranslate: string): string {
        return this.translateService.instant(jsonToTranslate);
    }

    private checkEtlSelected() {
        this.user.allEtlAccess = this.user.isAdmin ? true : this.allEtlAccess;
        if (!this.user.isAdmin && !this.allEtlAccess && this.selectedEtls.length === 0) {
            this.acAlertService.error(
                this.getTranslationName(`error.user.userRestrictedAccessEtlNoEtlSelected`)
            );
            return false;
        }
        return true;
    }

    private checkUserHasRolOrganism() {
        if (!this.user.isAdmin && this.user.usuarioRolOrganismo.length === 0) {
            this.acAlertService.error(this.getTranslationName(`error.userRolOrganismoNotEmpty`));
            return false;
        }
        return true;
    }

    private setEtl(data: EtlBase) {
        const etl: Etl = new Etl();
        etl.id = data.id;
        etl.code = data.code;
        etl.name = data.name;
        etl.organizationInCharge = data.organizationInCharge;
        etl.type = data.type;
        etl.executionPlanning = data.executionPlanning;
        etl.nextExecution = data.nextExecution;
        etl.lastExecution = data.lastExecution;
        return etl;
    }

    private setDataUserEtl(selectedEtl: Etl, currentUserEtl: Etl) {
        if (!currentUserEtl) {
            let tmpUserEtl: Etl = new Etl();
            tmpUserEtl = this.setEtl(selectedEtl);
            return tmpUserEtl;
        }
        return currentUserEtl;
    }

    private setUserEtls() {
        const tmpUserEtls = [];
        if (!this.user.allEtlAccess) {
            this.selectedEtls.forEach((selectedEtl, index) => {
                tmpUserEtls.push(
                    this.setDataUserEtl(
                        this.etlList.find((etl) => etl.id == selectedEtl.id),
                        this.user.etls.find((etlThread) => etlThread.id == selectedEtl.id)
                    )
                );
            });
        }
        this.user.etls = [];
        this.user.etls = tmpUserEtls;
    }

    save() {
        this.selectedEtls = this.selectedEtls.filter((etl) => this.check(etl) == true);
        this.isSaving = true;
        this.updateUsuarioRolOrganismoUser();
        if (!this.checkUserHasRolOrganism() || !this.checkEtlSelected()) {
            this.isSaving = false;
        } else {
            this.setUserEtls();
            if (this.user.id !== null) {
                this.userService
                    .update(this.user)
                    .subscribe(
                        (response) => this.onSaveSuccess(response),
                        () => this.onSaveError()
                    );
            } else {
                this.userService
                    .create(this.user)
                    .subscribe(
                        (response) => this.onSaveSuccess(response),
                        () => this.onSaveError()
                    );
            }
        }
    }

    restore(login: string) {
        this.userService.restore(login).subscribe((res: Response) => {
            this.eventManager.broadcast({
                name: 'UserModified',
                content: login
            });
        });
    }

    private onSaveSuccess(result) {
        this.eventManager.broadcast({ name: 'userListModification', content: 'OK' });
        this.isSaving = false;
        this.router.navigate(['user-management', this.user.login]);
    }

    private onSaveError() {
        this.isSaving = false;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    public toLowerCase() {
        if (this.user && this.user.login) {
            this.user.login = this.user.login.toLowerCase();
        }
    }

    public getRolAdminName() {
        return Rol.ADMIN;
    }

    public getRolTecnicoName() {
        return Rol.TECNICO;
    }

    public getRolTecnicoLector() {
        return Rol.LECTOR;
    }
}
