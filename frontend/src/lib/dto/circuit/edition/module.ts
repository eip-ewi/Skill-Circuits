import type {SubmoduleBlock} from "./submodule";
import type {IGroup} from "../group";
import type {Module} from "../../module";
import type {ModuleCircuit} from "../module/module";
import type {Graph} from "../../../logic/circuit/graph";

export interface ModuleGroup extends IGroup, Module {
    blocks: SubmoduleBlock[];

    groupType: "module";

    // Add a "second level" of circuits
    moduleCircuit: ModuleCircuit;

    // Computed on frontend
    moduleGraph: Graph | undefined;
}