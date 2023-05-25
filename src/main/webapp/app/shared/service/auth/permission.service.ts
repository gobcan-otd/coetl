import { Injectable } from '@angular/core';
import { Principal } from './principal.service';
import { Rol } from './rol.model';

// El rol ADMIN no se gestiona como un rol al uso, pero la aplicación esta preparada para trabajar con roles a nivel de permisos.
// El ADMIN se establece a nivel de atributo del usuario porque los roles van en función de organismos
// Se mantiene rol ADMIN, que no debería tener nadie, para que el acceso solo se establezca al administrador
export const ONLY_ADMIN = [Rol.ADMIN];

export const READ_ETL_ROLES = [Rol.TECNICO, Rol.LECTOR];
export const MANAGE_ETL_ROLES = [Rol.TECNICO];

@Injectable()
export class PermissionService {
    constructor(private principal: Principal) {}

    puedeNavegarUserManagement(): boolean {
        return this.isAdmin();
    }

    puedeNavegarOpcionesHerramientas(): boolean {
        return this.isAdmin();
    }

    canEditParametrosGlobales(): boolean {
        return this.isAdmin();
    }

    canReadEtl(): boolean {
        return this.isAdmin() || this.principal.rolesRutaMatchesRolesUsuario(READ_ETL_ROLES);
    }

    canManageEtl(): boolean {
        return this.isAdmin() || this.principal.rolesRutaMatchesRolesUsuario(MANAGE_ETL_ROLES);
    }

    isAdmin(): boolean {
        return this.principal.userIsAdmin();
    }

    isTecnico(rolName): boolean {
        return rolName === Rol.TECNICO;
    }
}
