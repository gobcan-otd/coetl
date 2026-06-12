import { Organism } from '../../admin/organism';
import { BaseVersionedAndAuditingWithDeletionEntity } from '../../shared/model/base-versioned-auditing-with-deletion-entity';

export enum PentahoType {
    TRANSFORMATION = 'TRANSFORMATION',
    JOB = 'JOB'
}

export enum HopType {
    WORKFLOW = 'WORKFLOW',
    PIPELINE = 'PIPELINE'
}

export const Type = { ...PentahoType, ...HopType };
export type Type = typeof Type;

export enum ExecutionPlatform {
    PENTAHO = 'PENTAHO',
    APACHE_HOP = 'APACHE_HOP'
}

export enum LogLevel {
    ERROR = 'ERROR',
    BASIC = 'BASIC',
    DEBUG = 'DEBUG'
}

export class EtlBase extends BaseVersionedAndAuditingWithDeletionEntity {
    constructor(
        public id?: number,
        public code?: string,
        public name?: string,
        public organizationInCharge?: Organism,
        public type?: Type,
        public executionPlanning?: string,
        public nextExecution?: Date,
        public lastExecution?: Date,
        public visibility?: boolean,
        public executionPlatform?: ExecutionPlatform,
        public logLevel?: LogLevel
    ) {
        super();
    }

    isDeleted(): boolean {
        return !!this.deletionDate;
    }

    isPlanning(): boolean {
        return !!this.executionPlanning;
    }
}

export class Etl extends EtlBase {
    constructor(
        public purpose?: string,
        public functionalInCharge?: string,
        public technicalInCharge?: string,
        public comments?: string,
        public executionDescription?: string,
        public nextExecution?: Date,
        public etlFile?: File,
        public uriRepository?: string,
        public isAttachedFilesChanged?: boolean
    ) {
        super();
        this.logLevel = LogLevel.ERROR;
    }
}
