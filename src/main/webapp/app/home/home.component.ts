import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserRouteAccessService, Principal, Account } from '../shared';
import { ONLY_ADMIN } from '../shared';

export const DEFAULT_PATH = 'etl';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
    public account: Account;

    constructor(
        private principal: Principal,
        private userRouteAccessService: UserRouteAccessService,
        private router: Router
    ) {}

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.account = account;

            if (!account.id && account.usuarioRolOrganismo.length === 0) {
                this.router.navigate(['non-existent-user']);
            }

            if (account.deletionDate) {
                this.router.navigate(['blocked']);
            }
            this.userRouteAccessService.checkLogin(ONLY_ADMIN).then((canActivate) => {
                if (canActivate) {
                    this.router.navigate([DEFAULT_PATH]);
                }
            });
        });
    }

    getNombreCompleto(account: Account): string {
        return [account.nombre, account.apellido1, account.apellido2].join(' ');
    }
}
