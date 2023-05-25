import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Response } from '@angular/http';
import { JhiEventManager } from 'ng-jhipster';
import {
    User,
    UserService,
    Rol,
    ResponseWrapper,
    Principal,
    PermissionService
} from '../../shared';
import { Subscription } from 'rxjs';
import { UsuarioRolOrganismo } from '../../shared/service/user-rol-organismos/user-rol-organismos.model';
import { UsuarioRolOrganismoService } from '../../shared/service/user-rol-organismos';
import { Roles } from '../../shared/service/roles/roles.model';
import { Organism, OrganismService } from '../organism';
import { RolesService } from '../../shared/service/roles';
import { AcAlertService } from '../../shared/component/alert/alert.service';

@Component({
    selector: 'jhi-user-mgmt-form',
    templateUrl: './user-management-form.component.html'
})
export class UserMgmtFormComponent implements OnInit, OnDestroy {
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

    public organismosSelectedADMIN: Organism[];
    public organismosSelectedTECNICO: Organism[];
    public organismosSelectedLECTOR: Organism[];

    public isEditItSelf: boolean;
    alerts: any[];

    constructor(
        private userService: UserService,
        public permissionService: PermissionService,
        private eventManager: JhiEventManager,
        private route: ActivatedRoute,
        private router: Router,
        private organismService: OrganismService,
        private rolesService: RolesService,
        private usuarioRolOrganismoService: UsuarioRolOrganismoService,
        private principal: Principal,
        public acAlertService: AcAlertService
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.isEditItSelf = false;
        this.organismosSelectedADMIN = [];
        this.organismosSelectedTECNICO = [];
        this.organismosSelectedLECTOR = [];

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

    load(login) {
        this.getAllRoles();
        this.getAllOrganismos();
        if (login) {
            this.userService.find(login).subscribe((user) => {
                this.user = user;
                this.loadDataSelect();
                this.isEditItSelf = this.principal.getUserId() === this.user.id;
            });
        } else {
            this.user = new User();
        }
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

    private getAllOrganismos() {
        this.organismService
            .findAllOrganism()
            .subscribe((response: ResponseWrapper) =>
                this.convertResponseToRolesReponseWrapper(response.json)
            );
    }

    private onSuccess(data: Roles[]) {
        this.allRoles = data;
    }

    public setCurrentPermissionUserValues() {
        if (this.mapSelect && this.allRoles) {
            this.mapSelect.forEach((value: Organism[], key: number) => {
                if (this.allRoles.find((r) => r.id === key && r.name === Rol.TECNICO)) {
                    if (Array.isArray(value)) {
                        value.forEach((organismo: Organism) =>
                            this.organismosSelectedTECNICO.push(organismo)
                        );
                    } else {
                        this.organismosSelectedTECNICO.push(value['aux']);
                    }
                }
                if (this.allRoles.find((r) => r.id === key && r.name === Rol.LECTOR)) {
                    if (Array.isArray(value)) {
                        value.forEach((organismo: Organism) =>
                            this.organismosSelectedLECTOR.push(organismo)
                        );
                    } else {
                        this.organismosSelectedLECTOR.push(value['aux']);
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

    private updateUsuarioRolOrganismoUser() {
        this.user.usuarioRolOrganismo = [];
        let o: UsuarioRolOrganismo = new UsuarioRolOrganismo();

        if (!this.user.isAdmin && this.allOrganismos.length !== 0) {
            this.organismosSelectedTECNICO.forEach((value: Organism) => {
                o = new UsuarioRolOrganismo();
                o.organismo = value;
                o.rol = this.getRolByName(Rol.TECNICO);
                this.user.usuarioRolOrganismo.push(o);
            });

            this.organismosSelectedLECTOR.forEach((value: Organism) => {
                o = new UsuarioRolOrganismo();
                o.organismo = value;
                o.rol = this.getRolByName(Rol.LECTOR);
                this.user.usuarioRolOrganismo.push(o);
            });
        }
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

    save() {
        this.isSaving = true;
        this.updateUsuarioRolOrganismoUser();
        if (!this.user.isAdmin && this.user.usuarioRolOrganismo.length === 0) {
            this.acAlertService.error(
                'Un usuario debe de tener al menos configurado un organismo para un rol'
            );
            this.isSaving = false;
        } else {
            if (this.user.id !== null) {
                this.usuarioRolOrganismoService
                    .delete({ idUsuario: this.user.id })
                    .subscribe((soloLector) => {
                        this.userService
                            .update(this.user)
                            .subscribe(
                                (response) => this.onSaveSuccess(response),
                                () => this.onSaveError()
                            );
                    });
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
