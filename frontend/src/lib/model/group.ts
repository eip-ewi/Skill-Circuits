import type {BlockData} from "../data/block";
import type {ItemData} from "../data/item";
import type {CircuitData} from "../data/circuit";
import type {GroupData} from "../data/group";
import {BlockModel} from "./block";
import type {CircuitModel} from "./circuit";

export class GroupModel implements GroupData {

    id: number;
    name: string;
    blocks: BlockModel[];

    constructor(data: GroupData, circuit: CircuitModel) {
        this.id = data.id;
        this.name = data.name;
        this.blocks = data.blocks.map(b => new BlockModel(b, circuit));
    }
}