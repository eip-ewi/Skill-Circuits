import type {ItemData} from "./item";

export interface BlockData {
    id: number;
    name: string;
    column: number;
    parents: number[];
    children: number[];
    items: ItemData[];
}