import { EntityFilter, Rol, ResponseWrapper } from '../../../shared';
import { Organism, OrganismService } from '../../organism';

export class UserFilter implements EntityFilter {
    allRoles: string[];
    allOrganismos: Organism[];

    public name?: string;
    public email?: string;
    public role?: string;
    public organism?: Organism;
    public includeDeleted = false;

    constructor(private organismService: OrganismService) {
        this.allRoles = Object.keys(Rol);
        this.getAllOrganismos();
    }

    fromQueryParams(params: any) {
        if (params['name']) {
            this.name = params['name'];
        }
        if (params['email']) {
            this.email = params['email'];
        }
        if (params['role']) {
            this.role = this.convertParamToRole(params['role']);
        }
        if (params['organism']) {
            this.organism = this.getOrganismoFromId(params['organism']);
        }
        if (params['roleOrganism']) {
            const auxRoleOrganism = params['roleOrganism'].split('@@');
            this.role = this.convertParamToRole(auxRoleOrganism[0]);
            this.organism = this.getOrganismoFromId(auxRoleOrganism[1]);
        }
        if (params['includeDeleted']) {
            this.includeDeleted = params['includeDeleted'] === 'true';
        }
    }

    reset() {
        this.name = null;
        this.email = null;
        this.role = null;
        this.organism = null;
        this.includeDeleted = false;
    }

    toQuery() {
        return this.getCriterias().join(' AND ');
    }

    toUrl() {
        return {
            name: this.name,
            email: this.email,
            role: !this.organism && this.role ? this.role : null,
            organism: this.organism && !this.role ? this.organism.id : null,
            roleOrganism: this.organism && this.role ? this.role + '@@' + this.organism.id : null,
            includeDeleted: this.includeDeleted
        };
    }

    private getAllOrganismos() {
        this.organismService
            .findAllOrganism()
            .subscribe((response: ResponseWrapper) => (this.allOrganismos = response.json));
    }

    private convertParamToRole(param: any): Rol {
        const currentKey = this.allRoles.find((key) => key === param);
        return currentKey ? Rol[currentKey] : undefined;
    }

    private getOrganismoFromId(id: number) {
        return this.allOrganismos.find((obj) => obj.id == id);
    }

    private getCriterias() {
        const criterias: string[] = [];
        if (this.name) {
            const subcriterias: string[] = [];
            this.name.split(' ').forEach((word) => subcriterias.push(`USUARIO ILIKE '%${word}%'`));
            criterias.push('(' + subcriterias.join(' AND ') + ')');
        }
        if (this.email) {
            criterias.push(`EMAIL ILIKE '%${this.email}%'`);
        }
        if (this.role && !this.organism) {
            criterias.push(`ROLES EQ '${this.role}'`);
        }
        if (this.organism && !this.role) {
            criterias.push(`ORGANISMO EQ '${this.organism.id}'`);
        }
        if (this.organism && this.role) {
            criterias.push(`ROLES_ORGANISMO EQ '${this.role}@@${this.organism.id}'`);
        }
        return criterias;
    }
}
