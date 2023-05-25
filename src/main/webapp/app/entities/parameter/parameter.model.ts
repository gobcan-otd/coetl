export enum Type {
    AUTO = 'AUTO',
    MANUAL = 'MANUAL',
    GLOBAL = 'GLOBAL'
}

export enum Typology {
    GENERIC = 'GENERIC',
    PASSWORD = 'PASSWORD'
}

export class Parameter {
    constructor(
        public id?: number,
        public key?: string,
        public value?: string,
        public type?: Type,
        public etlId?: number,
        public typology?: Typology,
        public description?: string
    ) {}
}
