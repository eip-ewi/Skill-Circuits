import type {LineSegments} from "../../data/path";
import type { Block } from "../../dto/circuit/block";
import type {Point} from "../../data/point";
import {getCircuit} from "./circuit.svelte";

export function createConnectionPath(from: Block, to: Block): LineSegments {
    let circuitRect = getCircuit().boundingRect!();
    let fromRect = from.boundingRect!();
    let toRect = to.boundingRect!();
    let relativeFrom = {
        x1: fromRect.left - circuitRect.left,
        y1: fromRect.top - circuitRect.top,
        x2: fromRect.right - circuitRect.left,
        y2: fromRect.bottom - circuitRect.top,
    };
    let relativeTo = {
        x1: toRect.left - circuitRect.left,
        y1: toRect.top - circuitRect.top,
        x2: toRect.right - circuitRect.left,
        y2: toRect.bottom - circuitRect.top,
    };

    let start: Point = { x: relativeFrom.x1 + fromRect.width / 2, y: relativeFrom.y2 };
    let end: Point = { x: relativeTo.x1 + toRect.width / 2, y: relativeTo.y1 };

    let gutterOffset = (to.column! / (getCircuit().width! - 1) - 0.5) * 56;

    let aboveChild: Point = { x: end.x, y: end.y - 64 + gutterOffset };

    if (start.y > end.y) {
        let gutterStepSize = 56 / (getCircuit().width! - 1);
        let horizontalDirection: "left" | "right" = end.x < start.x ? "left" : "right";

        let belowParent: Point = { x: start.x, y: start.y + 64 + gutterStepSize / 2 - gutterOffset };
        let belowParentBesideChild: Point = { x: horizontalDirection == "left" ? relativeTo.x2 + 48 : relativeTo.x1 - 48, y: start.y + 64 + gutterStepSize / 2 - gutterOffset };
        let aboveChildBesideChild: Point = { x: horizontalDirection == "left" ? relativeTo.x2 + 48 : relativeTo.x1 - 48, y: end.y - 64 + gutterOffset };

        return { points: [start, belowParent, belowParentBesideChild, aboveChildBesideChild, aboveChild, end] };
    }

    let belowParent: Point = { x: start.x, y: end.y - 64 + gutterOffset };

    return { points: [start, belowParent, aboveChild, end] };
}