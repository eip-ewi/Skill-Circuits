import type {Course} from "./course";
import type {Checkpoint} from "./checkpoint";
import type {Module} from "./module";
import type {Path} from "./path";
import type {Person} from "./person";

export interface Edition {
    id: number;
    name: string;
    course: Course,
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