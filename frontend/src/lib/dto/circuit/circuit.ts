import type {Group} from "./group";
import type {EditionCircuit} from "./edition/edition";
import type {ModuleCircuit} from "./module/module";

export interface ICircuit {
    id: number;
    name: string;
    groups: Group[];

    width: number | undefined;
    boundingRect: (() => DOMRect) | undefined;
}

export type Circuit = EditionCircuit | ModuleCircuit;