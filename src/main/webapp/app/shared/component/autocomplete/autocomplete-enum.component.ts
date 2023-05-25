import { Component, OnInit, Input } from '@angular/core';
import { AutocompleteComponent } from './autocomplete.component';
import { buildProvider } from '../../utils/build-provider.function';

/*
  Documentación en http://confluence.arte-consultores.com/display/INFRASTR/ac-autocomplete-enum
*/
@Component({
    selector: 'ac-autocomplete-enum',
    templateUrl: 'autocomplete.component.html',
    styleUrls: ['autocomplete.component.scss'],
    providers: [buildProvider(AutocompleteEnumComponent)]
})
export class AutocompleteEnumComponent extends AutocompleteComponent implements OnInit {
    public _suggestionsEnum;

    @Input()
    public translationPath: string;

    ngOnInit() {
        if (this.completeMethod.observers.length > 0) {
            throw new Error('completeMethod is not supported on ac-autocomplete-enum');
        }

        if (this.properties !== undefined) {
            throw new Error('properties is not supported on ac-autocomplete-enum');
        }

        if (this.suggestions !== undefined) {
            throw new Error('suggestions is not supported on ac-autocomplete-enum');
        }

        if (this.createNonFound) {
            throw new Error('createNonFound is not supported on ac-autocomplete-enum');
        }

        if (this._suggestionsEnum === undefined) {
            throw new Error('suggestionsEnum is required on ac-autocomplete-enum');
        }

        if (this.translationPath === undefined) {
            throw new Error('translationPath is required on ac-autocomplete-enum');
        }

        if (this.itemTemplate === undefined) {
            this.properties = ['value'];
        }

        this.mapSuggestions();
        super.ngOnInit();
    }

    @Input()
    get suggestionsEnum() {
        return this._suggestionsEnum;
    }

    set suggestionsEnum(suggestionsEnum: any) {
        this._suggestionsEnum = suggestionsEnum;
        if (this._suggestions) {
            this.mapSuggestions();
        }
    }

    private mapSuggestions() {
        this._suggestions = Object.keys(this._suggestionsEnum).map((key) =>
            Object.assign(
                {},
                {
                    id: key,
                    value: this.translateService.instant(this.translationPath + key)
                }
            )
        );
    }

    writeValue(value: any): void {
        if (value instanceof Array) {
            this._selectedSuggestions = value.map((element) => this.findSuggestionByEnum(element));
        } else {
            this._selectedSuggestions = this.findSuggestionByEnum(value);
        }
    }

    private findSuggestionByEnum(enumeration: any) {
        return this.suggestions.find((suggestion) => suggestion.id === enumeration);
    }

    @Input()
    get selectedSuggestions(): any {
        return this._selectedSuggestions;
    }

    set selectedSuggestions(value) {
        this._selectedSuggestions = value;
        if (this._selectedSuggestions === null || this._selectedSuggestions === undefined) {
            this.onModelChange(this._selectedSuggestions);
        } else if (value instanceof Array) {
            this.onModelChange(this._selectedSuggestions.map((suggestion) => suggestion.id));
        } else {
            this.onModelChange(this._selectedSuggestions.id);
        }
    }
}
