import { Organism } from '../../admin/organism';
import { BaseVersionedAndAuditingWithDeletionEntity } from '../../shared/model/base-versioned-auditing-with-deletion-entity';

export enum Type {
    TRANSFORMATION = 'TRANSFORMATION',
    JOB = 'JOB'
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
        public visibility?: boolean
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
    }
}
