import { Organism } from '../../../admin/organism';
import { BaseVersionedAndAuditingWithDeletionEntity } from '../../model/base-versioned-auditing-with-deletion-entity';
import { Roles } from '../roles/roles.model';

export class UsuarioRolOrganismo {
    public id?: number;
    public rol: Roles;
    public organismo: Organism;
}
