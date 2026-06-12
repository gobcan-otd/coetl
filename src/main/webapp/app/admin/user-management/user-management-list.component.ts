import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Etl } from '../../entities/etl/etl.model';
import { EtlListItem } from './etl-list-item.model';

@Component({
    selector: 'ac-user-management-list',
    templateUrl: 'user-management-list.component.html',
    styleUrls: ['user-management-list.component.scss']
})
export class UserMgmtListComponent implements OnInit, OnDestroy {
    public static EVENT_NAME = 'UserMgmtListComponent';

    @Input() allEtlAccess: boolean;
    @Output() allEtlAccessChange = new EventEmitter<boolean>();

    @Input() selectorList: EtlListItem[];

    @Input() selectedEtls: EtlListItem[];
    @Output() selectedEtlsChange = new EventEmitter<EtlListItem[]>();

    @Input() isEdit: boolean;

    constructor(private translateService: TranslateService) {}

    ngOnInit() {}

    ngOnDestroy() {}

    public onChange() {
        this.selectedEtlsChange.emit(this.selectedEtls);
    }

    public etlPermissionChange() {
        this.allEtlAccessChange.emit(this.allEtlAccess);
    }

    public hasSelectedEtls() {
        return !this.selectedEtls || this.selectedEtls.length === 0;
    }

    public hasEtls() {
        return !this.selectorList || this.selectorList.length === 0;
    }

    public getTranslationName(jsonToTranslate: string): string {
        return this.translateService.instant(jsonToTranslate);
    }

    public sort(list: any) {
        return list.slice().sort((a, b) => a.name.localeCompare(b.name));
    }
}
