import type {Course} from "./course";
import type {Checkpoint} from "./checkpoint";
import type {Module} from "./module";
import type {Path} from "./path";
import type {Person} from "./person";

export interface Edition {
    id: number;
    name: string;
    course: Course,
    published: boolean,
    checkpoints: Checkpoint[];
    modules: Module[];
    paths: Path[];
    teachers: Person[],
    editors: Person[],
}

export interface EditionCard {
    id: number;
    name: string;
    course: {
        id: number;
        name: String;
    },
}

export interface ManagedEdition {
    id: number;
    name: string;
    hasCircuit: boolean;
    course: {
        id: number;
        name: string;
    }
}