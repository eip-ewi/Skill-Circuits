import type { ModuleGroup } from "./module";
import type { ICircuit } from "../circuit.js";
import type { Checkpoint } from "../../checkpoint";
import type { Path } from "../../path";
import type { Edition } from "../../edition";

export interface EditionCircuit extends ICircuit, Edition {
    groups: ModuleGroup[];

    checkpoints: Checkpoint[];
    paths: Path[];

    circuitType: "edition";
}
