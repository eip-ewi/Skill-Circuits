import type {SubmoduleGroup} from "./submodule";
import type {ICircuit} from "../circuit.js";
import type {ExternalSkillBlock} from "./skill";

export interface ModuleCircuit extends ICircuit {
    externalSkills: ExternalSkillBlock[];

    editionId: number;
    groups: SubmoduleGroup[];

    circuitType: "module";
}
