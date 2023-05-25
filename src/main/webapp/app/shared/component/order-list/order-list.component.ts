import { Component, EventEmitter, forwardRef, Input, Output, HostBinding } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export const AC_ORDER_LIST_VALUE_ACCESSOR: any = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => OrderListComponent),
    multi: true
};

type QueryEmitter = { query: string; originalEvent: Event };

// IMPORTANTE : La lista a ordenar debe ser un List en el servidor y no un set, o no guardará el orden
@Component({
    selector: 'ac-order-list',
    templateUrl: 'order-list.component.html',
    styleUrls: ['./order-list.component.scss'],
    providers: [AC_ORDER_LIST_VALUE_ACCESSOR]
})
export class OrderListComponent implements ControlValueAccessor {
    private _orderedList: any;

    @Input()
    public suggestions: any[];

    // TODO: INFRASTR-113 - Revisar la implementación del componente order-list
    @Input()
    public propertiesToQuery;

    public dragAndDropScope: string;

    @Input()
    public canReorder = true;

    public hasControls = false;

    @Input()
    @HostBinding('class.disabled')
    public disabled = false;

    @Input()
    public required = false;

    @Input()
    public minLength = 3;

    @Input()
    public debouncedMode: boolean;

    @Input()
    public customIcon = 'fa-pencil-square-o';

    @Output()
    public onClick: EventEmitter<any> = new EventEmitter();

    @Output()
    public onComplete: EventEmitter<QueryEmitter> = new EventEmitter();

    @Output()
    private onSelect: EventEmitter<any> = new EventEmitter();

    // TODO: INFRASTR-113 - Revisar la implementación del componente order-list
    @Input()
    public itemTemplate: Function;

    private onModelChange: Function = () => {};

    private onModelTouched: Function = () => {};

    @Input()
    private compareWith: Function = (selectedItem, existingItem) => {
        if (selectedItem && existingItem) {
            if (!selectedItem.id) {
                console.error(`selectedItem don't have defined id`, selectedItem);
            }
            if (!existingItem.id) {
                console.error(`existingItem don't have defined id`, existingItem);
            }
            return selectedItem.id === existingItem.id;
        } else {
            return selectedItem === existingItem;
        }
    };

    constructor() {
        this.dragAndDropScope = this.generateRandomDragAndDropScope();
    }

    onReorder() {
        this.onModelChange(this._orderedList);
    }

    onBlurMethod() {
        this.onModelTouched();
    }

    generateRandomDragAndDropScope() {
        return 'order-list-scope-' + Math.trunc(Math.random() * 10000);
    }

    removeItem(selectedItem: any) {
        this.orderedList = this.orderedList.filter((existingItem) => {
            return !this.compareWith(selectedItem, existingItem);
        });
    }

    public onClickMethod($event): void {
        this.onClick.emit($event);
    }

    public onCompleteMethod(queryEmitter: QueryEmitter): void {
        this.onComplete.emit(queryEmitter);
    }

    /* ControlValueAccessor */

    writeValue(value: any): void {
        this._orderedList = value;
    }

    @Input()
    get orderedList(): any {
        return this._orderedList;
    }

    set orderedList(value) {
        this._orderedList = value;
        this.onModelChange(this._orderedList);
    }

    registerOnChange(fn: Function): void {
        this.onModelChange = fn;
    }

    registerOnTouched(fn: Function): void {
        this.onModelTouched = fn;
    }

    onSelectMethod($event) {
        this.onSelect.emit($event);
    }
}
