import type {BlockData} from "./block";

export interface GroupData {
    id: number;
    name: string;
    blocks: BlockData[];
}