import { Component, OnInit, OnDestroy } from '@angular/core';
import { JhiEventManager } from 'ng-jhipster';
import { Subscription } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Organism } from '../organism.model';
import { OrganismComponent } from '../organism.component';
import { OrganismService } from '../organism.service';

@Component({
    templateUrl: 'organism-dialog-delete.component.html',
    styleUrls: ['organism-dialog-delete.component.scss']
})
export class OrganismDialogDeleteComponent implements OnInit {
    private eventSubscriber: Subscription;
    public organismo: Organism;

    constructor(
        private activeModal: NgbActiveModal,
        private eventManager: JhiEventManager,
        private organismService: OrganismService
    ) {}

    ngOnInit() {}

    public clear() {
        this.activeModal.dismiss('closed');
    }

    public delete() {
        this.organismService.deleteOrganism(this.organismo.id).subscribe((organismo) => {
            this.eventManager.broadcast({
                name: OrganismComponent.EVENT_NAME,
                content: organismo
            });
            this.activeModal.close('deleted');
        });
    }
}
