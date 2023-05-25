import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { Autosize } from 'ng-autosize';
import { NgJhipsterModule } from 'ng-jhipster';
import { CookieModule } from 'ngx-cookie';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { MarkdownModule, MarkedOptions } from 'ngx-markdown';
import {
    AccordionModule,
    AutoCompleteModule,
    ButtonModule,
    CalendarModule,
    CheckboxModule,
    ChipsModule,
    DataTableModule,
    FileUploadModule,
    InputTextareaModule,
    ListboxModule,
    OrderListModule,
    SelectButtonModule
} from 'primeng/primeng';

import { DEFAULT_LANGUAGE } from './service/language/language.constants';

@NgModule({
    declarations: [Autosize],
    imports: [
        NgbModule.forRoot(),
        NgJhipsterModule.forRoot({
            // set below to true to make alerts look like toast
            alertAsToast: true,
            i18nEnabled: true,
            defaultI18nLang: DEFAULT_LANGUAGE,
            sortIconSelector: 'span.order-by-class'
        }),
        InfiniteScrollModule,
        CookieModule.forRoot(),
        MarkdownModule.forRoot({
            provide: MarkedOptions,
            useValue: {
                gfm: true,
                tables: true,
                breaks: false,
                pedantic: false,
                sanitize: false,
                smartLists: true,
                smartypants: false
            }
        }),
        BrowserAnimationsModule,
        CalendarModule,
        AutoCompleteModule,
        ChipsModule,
        ButtonModule,
        SelectButtonModule,
        ListboxModule,
        OrderListModule,
        CheckboxModule,
        InputTextareaModule,
        FileUploadModule,
        DataTableModule,
        AccordionModule
    ],
    exports: [
        FormsModule,
        HttpModule,
        CommonModule,
        NgbModule,
        NgJhipsterModule,
        InfiniteScrollModule,
        MarkdownModule,
        AutoCompleteModule,
        ChipsModule,
        CalendarModule,
        ButtonModule,
        SelectButtonModule,
        ListboxModule,
        OrderListModule,
        CheckboxModule,
        InputTextareaModule,
        Autosize,
        FileUploadModule,
        DataTableModule,
        AccordionModule
    ]
})
export class CoetlSharedLibsModule {}
