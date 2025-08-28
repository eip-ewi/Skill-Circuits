import type {LineSegments} from "../data/path";

export function generatePathString(path: LineSegments, radius: number) {
    let result = `M ${path.points[0]!.x} ${path.points[0]!.y}`;

    for (let i = 0; i < path.points.length; i++) {
        if (i === 0) {
            continue;
        }

        let point = path.points[i]!;

        if (i === path.points.length - 1) {
            result += ` L ${point.x} ${point.y}`;
            continue;
        }

        let previous = path.points[i - 1]!;
        let next = path.points[i + 1]!;

        let dxBefore = (point.x - previous.x);
        let dyBefore = (point.y - previous.y);
        let lengthBefore = Math.sqrt(dxBefore * dxBefore + dyBefore * dyBefore);
        let ratioBefore = Math.min(1.0, radius / lengthBefore);

        let dxAfter = (next.x - point.x);
        let dyAfter = (next.y - point.y);
        let lengthAfter = Math.sqrt(dxAfter * dxAfter + dyAfter * dyAfter);
        let ratioAfter = Math.min(1.0, radius / lengthAfter);

        let before = { x: point.x - dxBefore * ratioBefore, y: point.y - dyBefore * ratioBefore };
        let after = { x: point.x + dxAfter * ratioAfter, y: point.y + dyAfter * ratioAfter };

        result += ` L ${before.x} ${before.y} Q ${point.x} ${point.y}, ${after.x} ${after.y}`;
    }
    return result;
}