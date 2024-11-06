import type {ItemPatch} from "./item_patch";

export interface BlockPatch {
    id: number,
    items: ItemPatch[],
}