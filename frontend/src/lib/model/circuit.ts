import type {BlockData} from "../data/block";
import type {ItemData} from "../data/item";
import type {CircuitData} from "../data/circuit";
import type {GroupData} from "../data/group";
import {GroupModel} from "./group";
import type {BlockModel} from "./block";

export class CircuitModel implements CircuitData {

    id: number;
    name: string;
    groups: GroupModel[];

    constructor(data: CircuitData) {
        this.id = data.id;
        this.name = data.name;
        this.groups = data.groups.map(g => new GroupModel(g, this));
    }

    getBlock(id: number): BlockModel | undefined {
        return this.groups.flatMap(g => g.blocks).find(b => b.id === id);
    }

}