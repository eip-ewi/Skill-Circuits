import type {Point} from "../util/point";

export interface LineModel {
    from: Point,
    to: Point,
    fromBlock: number,
    toBlock: number,
    animated: boolean,
    locked: boolean,
}