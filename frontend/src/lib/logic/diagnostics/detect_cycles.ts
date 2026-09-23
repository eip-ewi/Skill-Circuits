import type { Block } from "../../dto/circuit/block";
import type { Graph } from "../circuit/graph";

export function hasCycle(graph: Graph) {
    const toVisit: Block[] = graph.getNodes();
    const visited: Set<number> = new Set();
    while (toVisit.length > 0) {
        const current = toVisit.pop()!;
        if (visited.has(current.id)) {
            continue;
        }
        if (dfs(graph, current, visited, new Set())) {
            return true;
        }
    }
    return false;
}

function dfs(graph: Graph, current: Block, visited: Set<number>, stack: Set<number>): boolean {
    if (stack.has(current.id)) {
        return true;
    }

    if (visited.has(current.id)) {
        return false;
    }

    visited.add(current.id);
    stack.add(current.id);

    for (const child of graph.getChildren(current)) {
        if (dfs(graph, child, visited, stack)) {
            return true;
        }
    }

    stack.delete(current.id);

    return false;
}
