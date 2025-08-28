import type {SubmoduleBlock} from "./submodule";
import type {Group, IGroup} from "../group";
import type {Module} from "../../module";

export interface ModuleGroup extends IGroup, Module {
    blocks: SubmoduleBlock[];

    groupType: "module";
}