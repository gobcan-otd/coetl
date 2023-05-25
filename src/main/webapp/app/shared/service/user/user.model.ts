import { BaseVersionedAndAuditingWithDeletionEntity } from '../../model/base-versioned-auditing-with-deletion-entity';
import { UsuarioRolOrganismo } from '../user-rol-organismos/user-rol-organismos.model';

export class User extends BaseVersionedAndAuditingWithDeletionEntity {
    public id?: any;
    public login?: string;
    public nombre?: string;
    public apellido1?: string;
    public apellido2?: string;
    public email?: string;
    public isAdmin?: boolean;
    public usuarioRolOrganismo?: UsuarioRolOrganismo[];

    constructor(
        id?: any,
        login?: string,
        nombre?: string,
        apellido1?: string,
        apellido2?: string,
        email?: string,
        isAdmin?: boolean,
        usuarioRolOrganismo?: UsuarioRolOrganismo[]
    ) {
        super();
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.nombre = nombre ? nombre : null;
        this.apellido1 = apellido1 ? apellido1 : null;
        this.apellido2 = apellido2 ? apellido2 : null;
        this.email = email ? email : null;
        this.isAdmin = isAdmin ? isAdmin : null;
        this.usuarioRolOrganismo = usuarioRolOrganismo ? usuarioRolOrganismo : null;
    }
}
