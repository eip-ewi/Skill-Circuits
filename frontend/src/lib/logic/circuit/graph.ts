import type { Block } from "../../dto/circuit/block";

export class Graph {

    private nodes: Map<number, Block>;
    private parents: Map<number, Block[]>;
    private children: Map<number, Block[]>;

    constructor(blocks: Block[]) {
        this.nodes = new Map();
        blocks.forEach(block => {
            this.nodes.set(block.id, block);
        });

        this.parents = new Map();
        this.children = new Map();

        blocks.forEach(block => {
            this.parents.set(block.id, block.parents.filter(parentId => this.nodes.has(parentId)).map(parentId => this.nodes.get(parentId)!));
            this.children.set(block.id, block.children.filter(childId => this.nodes.has(childId)).map(childId => this.nodes.get(childId)!));
        });
    }

    size(): number {
        return this.nodes.size;
    }

    getNode(id: number): Block {
        return this.nodes.get(id)!;
    }

    getParents<B extends Block>(block: B): B[] {
        return this.parents.get(block.id)! as B[];
    }

    isParent<B extends Block>(potentialParent: B, block: B): boolean {
        return this.getParents(block).some(parent => parent.id === potentialParent.id);
    }

    isAncestor<B extends Block>(potentialAncestor: B, block: B): boolean {
        return block.id === potentialAncestor.id || this.getParents(block).some(parent => this.isAncestor(potentialAncestor, parent));
    }

    getAncestors<B extends Block>(blocks: B[]): B[] {
        let visited: Set<number> = new Set();
        let queue: B[] = [...blocks];
        while (queue.length > 0) {
            let current: B = queue.shift()!;
            if (visited.has(current.id)) {
                continue;
            }
            visited.add(current.id);
            this.getParents(current).forEach(parent => queue.push(parent));
        }
        return this.getNodes().filter(block => visited.has(block.id)) as B[];
    }

    commonAncestors<B extends Block>(left: B, right: B): B[] {
        function ancestors(graph: Graph, current: number, visited: Set<number>) {
            if (visited.has(current)) {
                return;
            }
            visited.add(current);
            graph.parents.get(current)!.forEach(parent => ancestors(graph, parent.id, visited));
        }
        let leftAncestors: Set<number> = new Set();
        let rightAncestors: Set<number> = new Set();
        ancestors(this, left.id, leftAncestors);
        ancestors(this, right.id, rightAncestors);
        let commonAncestors: Set<number> = new Set([...leftAncestors.values()].filter(ancestor => rightAncestors.has(ancestor)));
        [...commonAncestors.values()].filter(ancestor => this.children.get(ancestor)!.some(childOfAncestor => commonAncestors.has(childOfAncestor.id))).forEach(ancestor => {
            commonAncestors.delete(ancestor);
        });
        return [...commonAncestors.values()].map(id => this.getNode(id)) as B[];
    }

    getShortestPathToAncestor<B extends Block>(descendant: B, ancestor: B): B[] | undefined {
        let queue: B[] = [descendant];
        let prev: Map<number, number> = new Map();
        let visited: Set<number> = new Set();

        while (queue.length > 0) {
            let current = queue.shift()!;

            if (visited.has(current.id)) {
                continue;
            }

            visited.add(current.id);

            if (current.id === ancestor.id) {
                break;
            }

            for (let parent of this.getParents(current)) {
                if (!prev.has(parent.id)) {
                    prev.set(parent.id, current.id);
                }
                queue.push(parent);
            }
        }

        if (!visited.has(ancestor.id)) {
            return undefined;
        }

        let path: B[] = [];
        let current = ancestor;
        while (current.id !== descendant.id) {
            path.push(current);
            current = this.getNode(prev.get(current.id)!) as B;
        }
        path.push(current);

        return path;
    }

    getChildren<B extends Block>(block: B): B[] {
        return this.children.get(block.id)! as B[];
    }

    isDescendant<B extends Block>(potentialDescendant: B, block: B): boolean {
        return block.id === potentialDescendant.id || this.getChildren(block).some(child => this.isDescendant(potentialDescendant, child));
    }

    getNodes(): Block[] {
        return [...this.nodes.values()];
    }

    getEdges(): {from: Block, to: Block}[] {
        let edges: {from: Block, to: Block}[] = [];
        this.nodes.forEach(block => {
            this.getChildren(block).forEach(child => {
                edges.push({from: block, to: child});
            });
        });
        return edges;
    }

}
