import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { User, UserService } from '../../shared';
import { UserModalService } from './user-modal.service';

@Component({
    selector: 'jhi-user-mgmt-delete-dialog',
    templateUrl: './user-management-delete-dialog.component.html'
})
export class UserMgmtDeleteDialogComponent {
    user: User;

    constructor(
        private userService: UserService,
        public activeModal: NgbActiveModal,
        private eventManager: JhiEventManager
    ) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmAction(login) {
        if (this.user.login === login) {
            if (this.user.deletionDate) {
                this.confirmRestore(login);
            } else {
                this.confirmDelete(login);
            }
        }
    }

    private confirmDelete(login) {
        this.userService.delete(login).subscribe((response) => {
            this.activeModal.dismiss('deleted');
            this.eventManager.broadcast({
                name: 'UserModified',
                content: { login, action: 'deleted' }
            });
        });
    }

    private confirmRestore(login) {
        this.userService.restore(login).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'userListModification',
                content: 'Restored a user'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-user-delete-dialog',
    template: ''
})
export class UserDeleteDialogComponent implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute, private userModalService: UserModalService) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.userModalService.open(
                UserMgmtDeleteDialogComponent as Component,
                params['login']
            );
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
