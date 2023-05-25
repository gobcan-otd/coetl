import { Rol } from '../auth/rol.model';
import { UsuarioRolOrganismo } from '../user-rol-organismos/user-rol-organismos.model';

export class Account {
    constructor(
        public id: number,
        public activated: boolean,
        public roles: Rol[],
        public email: string,
        public nombre: string,
        public idioma: string,
        public apellido1: string,
        public apellido2: string,
        public login: string,
        public isAdmin: boolean,
        public usuarioRolOrganismo?: UsuarioRolOrganismo[]
    ) {}
}
