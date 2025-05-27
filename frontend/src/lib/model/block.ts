import type {BlockData} from "../data/block";
import type {ItemData} from "../data/item";
import type {CircuitModel} from "./circuit";

export class BlockModel implements BlockData {

    id: number;
    name: string;
    column: number;
    row: number;
    items: ItemData[];
    parents: number[];
    children: number[];
    essential: boolean;

    private circuit: CircuitModel;

    constructor(data: BlockData, circuit: CircuitModel) {
        this.id = data.id;
        this.name = data.name;
        this.column = data.column;
        this.row = 0;
        this.items = data.items;
        this.parents = data.parents;
        this.children = data.children;
        this.essential = data.essential;

        this.circuit = circuit;
    }

    get locked(): boolean {
        return !this.items.some(item => item.completed) &&
            this.parents.map(pId => this.circuit.getBlock(pId)!)
                .some(parent => !parent.completed)
    }

    get completed(): boolean {
        return !this.items.some(item => !item.completed);
    }

}