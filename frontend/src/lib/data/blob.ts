import type { Block } from "../dto/circuit/block";
import type {Group} from "../dto/circuit/group";
import type {Point} from "./point";

export interface Blob {
    group: Group,
    min: Point,
    max: Point,
    allocations: Allocation[],
}

export interface Allocation {
    point: Point,
    neighbours: Neighbours,
    showName: boolean,
    block: Block | undefined,
}

export interface Neighbours {
    top: boolean;
    right: boolean;
    bottom: boolean;
    left: boolean;
    topRight: boolean;
    bottomRight: boolean;
    bottomLeft: boolean;
    topLeft: boolean;
}
