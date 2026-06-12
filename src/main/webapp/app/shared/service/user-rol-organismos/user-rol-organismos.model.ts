import { Organism } from '../../../admin/organism';
import { Roles } from '../roles/roles.model';

export class UsuarioRolOrganismo {
    constructor(public id?: number, public rol?: Roles, public organismo?: Organism) {}
}
