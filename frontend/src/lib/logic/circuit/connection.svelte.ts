import type { LineSegments } from "../../data/path";
import type { Block } from "../../dto/circuit/block";
import type { Point } from "../../data/point";
import { getCircuit } from "./circuit.svelte";

export function createConnectionPath(from: Block, to: Block): LineSegments | undefined {
    const circuitRect = getCircuit().boundingRect!();
    const fromRect = from.boundingRect?.();
    const toRect = to.boundingRect?.();

    if (fromRect === undefined || toRect === undefined) {
        return undefined;
    }

    const scale = parseFloat(getComputedStyle(document.querySelector(".circuit")!).fontSize) / 16.0;

    const relativeFrom = {
        x1: fromRect.left - circuitRect.left,
        y1: fromRect.top - circuitRect.top,
        x2: fromRect.right - circuitRect.left,
        y2: fromRect.bottom - circuitRect.top,
    };
    const relativeTo = {
        x1: toRect.left - circuitRect.left,
        y1: toRect.top - circuitRect.top,
        x2: toRect.right - circuitRect.left,
        y2: toRect.bottom - circuitRect.top,
    };

    const start: Point = { x: relativeFrom.x1 + fromRect.width / 2, y: relativeFrom.y2 };
    const end: Point = { x: relativeTo.x1 + toRect.width / 2, y: relativeTo.y1 };

    const circuitWidth = getCircuit().width ?? 1;
    let gutterOffset = 0;
    if (circuitWidth > 1) {
        gutterOffset = (to.column! / (circuitWidth - 1) - 0.5) * 56 * scale;
    }

    const aboveChild: Point = { x: end.x, y: end.y - 64 * scale + gutterOffset };

    if (start.y > end.y) {
        let gutterStepSize = 0;
        if (circuitWidth > 1) {
            gutterStepSize = (56 * scale) / (circuitWidth - 1);
        }
        const horizontalDirection: "left" | "right" = end.x < start.x ? "left" : "right";

        const belowParent: Point = {
            x: start.x,
            y: start.y + 64 * scale + gutterStepSize / 2 - gutterOffset,
        };
        const belowParentBesideChild: Point = {
            x:
                horizontalDirection == "left"
                    ? relativeTo.x2 + 48 * scale
                    : relativeTo.x1 - 48 * scale,
            y: start.y + 64 * scale + gutterStepSize / 2 - gutterOffset,
        };
        const aboveChildBesideChild: Point = {
            x:
                horizontalDirection == "left"
                    ? relativeTo.x2 + 48 * scale
                    : relativeTo.x1 - 48 * scale,
            y: end.y - 64 * scale + gutterOffset,
        };

        return {
            points: [
                start,
                belowParent,
                belowParentBesideChild,
                aboveChildBesideChild,
                aboveChild,
                end,
            ],
        };
    }

    const belowParent: Point = { x: start.x, y: end.y - 64 * scale + gutterOffset };

    return { points: [start, belowParent, aboveChild, end] };
}
