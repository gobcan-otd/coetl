import { Component, OnInit } from '@angular/core';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { Organism } from '../organism.model';
import { OrganismComponent } from '../organism.component';
import { OrganismService } from '../organism.service';

@Component({
    selector: 'ac-organism-dialog',
    templateUrl: 'organism-dialog.component.html',
    styleUrls: ['organism-dialog.component.scss']
})
export class OrganismDialogComponent implements OnInit {
    public organismo: Organism;

    constructor(
        private activeModal: NgbActiveModal,
        private eventManager: JhiEventManager,
        private organismService: OrganismService
    ) {}

    ngOnInit() {}

    public save() {
        let parameterEditObservable = null;
        if (this.organismo && this.organismo.id) {
            parameterEditObservable = this.organismService.updateOrganism(this.organismo);
        } else {
            parameterEditObservable = this.organismService.createOrganism(this.organismo);
        }
        this.subscribeToSaveResponse(parameterEditObservable);
    }

    public clear() {
        this.activeModal.dismiss('closed');
    }

    private subscribeToSaveResponse(observable: Observable<Organism>) {
        observable.subscribe((organismo) => {
            this.eventManager.broadcast({
                name: OrganismComponent.EVENT_NAME,
                content: organismo
            });
            this.activeModal.close('saved');
        });
    }
}
