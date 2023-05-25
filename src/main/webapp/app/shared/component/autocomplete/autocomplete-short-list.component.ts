import { Component, OnInit } from '@angular/core';
import { AutocompleteComponent } from './autocomplete.component';
import { buildProvider } from '../../utils/build-provider.function';

/*
  Documentación en http://confluence.arte-consultores.com/display/INFRASTR/ac-autocomplete-short-list
*/
@Component({
    selector: 'ac-autocomplete-short-list',
    templateUrl: 'autocomplete.component.html',
    styleUrls: ['autocomplete.component.scss'],
    providers: [buildProvider(AutocompleteShortListComponent)]
})
export class AutocompleteShortListComponent extends AutocompleteComponent implements OnInit {
    ngOnInit() {
        if (this.completeMethod.observers.length > 0) {
            throw new Error('completeMethod is not supported on ac-autocomplete-short-list');
        }

        super.ngOnInit();
    }
}
