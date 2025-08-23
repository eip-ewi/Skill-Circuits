import type {Group} from "../../dto/circuit/group";
import type {Point} from "../../data/point";
import type {Allocation, Blob, Neighbours} from "../../data/blob";
import type {Block} from "../../dto/circuit/block";
import {isBlockVisible} from "./circuit.svelte";

export function createBlobs(groups: Group[]): Blob[] {
    function visibleBlocks(group: Group): Block[] {
        return group.blocks.filter(block => isBlockVisible(block));
    }

    let blobs: Blob[] =
        groups.filter(group => visibleBlocks(group).length > 0).map(group => { return {
            group: group,
            min: {
                x: Math.min(...visibleBlocks(group).map(block => block.column!)),
                y: Math.min(...visibleBlocks(group).map(block => block.row!)),
            },
            max: {
                x: Math.max(...visibleBlocks(group).map(block => block.column!)),
                y: Math.max(...visibleBlocks(group).map(block => block.row!)),
            },
            allocations: visibleBlocks(group).map(block => { return { point: { x: block.column!, y: block.row! }, neighbours: emptyNeighbours(), showName: false, block: block }; }),
        }; })
            .toSorted((a, b) => area(a) - area(b));

    if (!blobs.some(blob => blob.allocations.length > 0)) {
        return [];
    }

    let width = Math.max(...groups.flatMap(group => group.blocks.map(block => block.column!))) + 1;
    let grid = new Grid(width);

    blobs.forEach(blob => blob.allocations.forEach(alloc => grid.occupy(alloc.point, blob.group)));

    for (let blob of blobs) {
        for (let x = blob.min.x; x <= blob.max.x; x++) {
            for (let y = blob.min.y; y <= blob.max.y; y++) {
                let p: Point = { x, y };
                if (grid.isOccupied(p)) {
                    continue;
                }
                grid.occupy(p, blob.group);
                blob.allocations.push({ point: p, neighbours: emptyNeighbours(), showName: false, block: undefined });
            }
        }
    }

    for (let blob of blobs) {
        for (let alloc of blob.allocations) {
            if (alloc.point.y > 0 && grid.getOccupant({ x: alloc.point.x, y: alloc.point.y - 1 }) === blob.group.id) {
                alloc.neighbours.top = true;
            }
            if (alloc.point.x < width - 1 && grid.getOccupant({ x: alloc.point.x + 1, y: alloc.point.y }) === blob.group.id) {
                alloc.neighbours.right = true;
            }
            if (grid.getOccupant({ x: alloc.point.x, y: alloc.point.y + 1 }) === blob.group.id) {
                alloc.neighbours.bottom = true;
            }
            if (alloc.point.x > 0 && grid.getOccupant({ x: alloc.point.x - 1, y: alloc.point.y }) === blob.group.id) {
                alloc.neighbours.left = true;
            }
            if (alloc.point.x < width - 1 && alloc.point.y > 0 && grid.getOccupant({ x: alloc.point.x + 1, y: alloc.point.y - 1 }) === blob.group.id) {
                alloc.neighbours.topRight = true;
            }
            if (alloc.point.x < width - 1 && grid.getOccupant({ x: alloc.point.x + 1, y: alloc.point.y + 1 }) === blob.group.id) {
                alloc.neighbours.bottomRight = true;
            }
            if (alloc.point.x > 0 && grid.getOccupant({ x: alloc.point.x - 1, y: alloc.point.y + 1 }) === blob.group.id) {
                alloc.neighbours.bottomLeft = true;
            }
            if (alloc.point.x > 0 && alloc.point.y > 0 && grid.getOccupant({ x: alloc.point.x - 1, y: alloc.point.y - 1 }) === blob.group.id) {
                alloc.neighbours.topLeft = true;
            }
        }
    }

    blobs.forEach(blob => {
        let separated = separateBlobs(blob, grid);
        separated.forEach(connected => {
            let minY = Math.min(...connected.map(alloc => alloc.point.y));
            let top = connected.filter(alloc => alloc.point.y === minY);
            let topLeft = top.toSorted((a, b) => a.point.x - b.point.x)[0]!;
            topLeft.showName = true;
        });

        let loose: Set<number> = new Set(separated.filter(connected => !connected.some(alloc => alloc.block !== undefined)).flat().map(alloc => grid.encode(alloc.point)));
        blob.allocations = blob.allocations.filter(alloc => !loose.has(grid.encode(alloc.point)));
    });

    return blobs;

}

function separateBlobs(blob: Blob, grid: Grid): Allocation[][] {
    let result: Allocation[][] = [];

    let todo = blob.allocations;

    while (todo.length > 0) {
        let start = todo[0]!;
        let connected = bfs(blob, start, grid);
        result.push(connected);
        todo = todo.filter(a => !connected.some(b => a.point.x === b.point.x && a.point.y === b.point.y));
    }

    return result;
}

function bfs(blob: Blob, start: Allocation, grid: Grid): Allocation[] {
    let queue: Allocation[] = [start];
    let visited: Set<number> = new Set();
    let result: Allocation[] = [];

    while (queue.length > 0) {
        let current = queue.shift()!;

        if (visited.has(grid.encode(current.point))) {
            continue;
        }

        visited.add(grid.encode(current.point));
        result.push(current);

        let neighbours: Point[] = [
            { x: current.point.x, y: current.point.y - 1 },
            { x: current.point.x + 1, y: current.point.y },
            { x: current.point.x, y: current.point.y + 1 },
            { x: current.point.x - 1, y: current.point.y },
        ].filter(p => p.x >= 0 && p.y >= 0 && p.x < grid.width);

        for (let neighbour of neighbours) {
            if (grid.getOccupant(neighbour) === blob.group.id) {
                queue.push(findAllocationAt(blob, neighbour)!);
            }
        }
    }

    return result;
}

function findAllocationAt(blob: Blob, point: Point): Allocation | undefined {
    return blob.allocations.find(alloc => alloc.point.x === point.x && alloc.point.y === point.y);
}

function area(rect: { min: Point, max: Point }): number {
    return (rect.max.x - rect.min.x + 1) * (rect.max.y - rect.min.y + 1);
}

function emptyNeighbours(): Neighbours {
    return {
        top: false,
        right: false,
        bottom: false,
        left: false,
        topRight: false,
        bottomRight: false,
        bottomLeft: false,
        topLeft: false,
    };
}

class Grid {

    readonly width: number;
    private occupied: Map<number, number> = new Map();

    constructor(width: number) {
        this.width = width;
        this.occupied = new Map();
    }

    isOccupied(p: Point): boolean {
        return this.occupied.has(this.encode(p));
    }

    getOccupant(p: Point): number | undefined {
        return this.occupied.get(this.encode(p));
    }

    occupy(p: Point, group: Group) {
        this.occupied.set(this.encode(p), group.id);
    }

    encode(p: Point): number {
        return p.x + p.y * this.width;
    }

}

/*
 * Alg 1:
 *  - Assign the smallest fitting rectangle to every group
 *  - Starting with the smallest rectangle, allocate slots
 *  - Remove blobs with no blocks
 */

/*
 * Alg 2:
 *  - Assign every block its own blob
 *  - Constrained to the smallest fitting rectangle of its group, bfs from every blob simultaneously
 *  - Once a group visits a slot, it is added to the queue
 *  - Assign slots according to claim order
 *  - Claims are rescinded if no block can be found
 */