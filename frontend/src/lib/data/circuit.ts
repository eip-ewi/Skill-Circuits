import type {GroupData} from "./group";

export interface CircuitData {
    id: number;
    name: string;
    groups: GroupData[];
}
