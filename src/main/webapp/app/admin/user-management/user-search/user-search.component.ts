import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { JhiEventManager } from 'ng-jhipster';
import { Organism, OrganismService } from '../../organism';
import { Subject, Subscription } from 'rxjs';
import { UserFilter } from '.';
import { Rol, ResponseWrapper } from '../../../shared';

@Component({
    selector: 'ac-user-search',
    templateUrl: 'user-search.component.html'
})
export class UserSearchComponent implements OnInit, OnDestroy {
    private filterChangesSubject: Subject<any> = new Subject<any>();
    subscription: Subscription;
    roleEnum = Rol;
    public allOrganismos: Organism[];

    @Input() filters: UserFilter;

    constructor(private eventManager: JhiEventManager, private organismService: OrganismService) {
        this.filters = new UserFilter(organismService);
        this.getAllOrganismos();
    }

    ngOnInit() {
        this.subscription = this.filterChangesSubject.debounceTime(300).subscribe(() =>
            this.eventManager.broadcast({
                name: 'userSearch',
                content: this.filtersToUrl()
            })
        );
    }

    private getAllOrganismos() {
        this.organismService
            .findAllOrganism()
            .subscribe((response: ResponseWrapper) => (this.allOrganismos = response.json));
    }

    organismosItemTemplate(item: Organism) {
        return `${item.name}`;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    filter() {
        if (this.filters.role == Rol.ADMIN) {
            this.filters.organism = null;
        }
        this.filterChangesSubject.next();
    }

    resetFilters() {
        this.filters.reset();
        this.filter();
    }

    private filtersToUrl() {
        return this.filters.toUrl();
    }
}
