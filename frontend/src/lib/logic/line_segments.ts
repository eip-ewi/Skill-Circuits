import type { LineSegments } from "../data/path";

export function generatePathString(path: LineSegments, radius: number) {
    let result = `M ${path.points[0]!.x} ${path.points[0]!.y}`;

    for (let i = 0; i < path.points.length; i++) {
        if (i === 0) {
            continue;
        }

        const point = path.points[i]!;

        if (i === path.points.length - 1) {
            result += ` L ${point.x} ${point.y}`;
            continue;
        }

        const previous = path.points[i - 1]!;
        const next = path.points[i + 1]!;

        const dxBefore = point.x - previous.x;
        const dyBefore = point.y - previous.y;
        const lengthBefore = Math.sqrt(dxBefore * dxBefore + dyBefore * dyBefore);
        const ratioBefore = Math.min(1.0, radius / lengthBefore);

        const dxAfter = next.x - point.x;
        const dyAfter = next.y - point.y;
        const lengthAfter = Math.sqrt(dxAfter * dxAfter + dyAfter * dyAfter);
        const ratioAfter = Math.min(1.0, radius / lengthAfter);

        const before = { x: point.x - dxBefore * ratioBefore, y: point.y - dyBefore * ratioBefore };
        const after = { x: point.x + dxAfter * ratioAfter, y: point.y + dyAfter * ratioAfter };

        result += ` L ${before.x} ${before.y} Q ${point.x} ${point.y}, ${after.x} ${after.y}`;
    }
    return result;
}
