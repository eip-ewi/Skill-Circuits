import type {Block} from "../../dto/circuit/block";
import {Graph} from "./graph";
import type {Warning} from "../../data/warning";
import type {Checkpoint} from "../../dto/checkpoint";
import moment from "moment";
import {getBlocks} from "./circuit.svelte";
import type {SkillBlock} from "../../dto/circuit/module/skill";

export function placeBlocks(graph: Graph) {
    let placement = new Placement();

    let sortedBlocks = topologicalSort(graph, graph.getNodes());

    let rows: Map<number, number> = new Map();
    graph.getNodes().forEach(block => {
        rows.set(block.id, 0);
    });

    sortedBlocks.forEach(block => {
        let minRow = Math.max(0, ...graph.getParents(block).map(parent => rows.get(parent.id)! + 1));
        let freeRow = placement.firstFreeSpotInColumnFromRow(block.column!, minRow);
        block.row = freeRow;
        rows.set(block.id, freeRow);
        placement.place(block);
    });
}

export function placeBlocksWithCheckpoints(graph: Graph, checkpoints: Checkpoint[]) {
    let placement = new Placement();

    let skills = graph.getNodes() as SkillBlock[];

    let rows: Map<number, number> = new Map();
    skills.forEach(skill => {
        rows.set(skill.id, 0);
    });

    let placed: Set<number> = new Set();

    function placeSkills(skills: SkillBlock[], minHeight: number){
        let sortedSkills = topologicalSort(graph, skills);

        sortedSkills.forEach(skill => {
            let minRow = Math.max(minHeight, ...graph.getParents(skill).filter(parent => rows.has(parent.id)).map(parent => rows.get(parent.id)! + 1));
            let freeRow = placement.firstFreeSpotInColumnFromRow(skill.column!, minRow);
            skill.row = freeRow;
            rows.set(skill.id, freeRow);
            placement.place(skill);
            placed.add(skill.id);
        });
    }

    let sortedCheckpoints: Checkpoint[] = checkpoints.toSorted((a, b) => moment(a.deadline).unix() - moment(b.deadline).unix());

    let minHeight = 0;
    for (let checkpoint of sortedCheckpoints) {
        let skillsInCheckpoint = skills.filter(skill => checkpoint.id === skill.checkpoint);
        let skillsToPlace = graph.getAncestors(skillsInCheckpoint).filter(skill => !placed.has(skill.id));
        placeSkills(skillsToPlace, minHeight);
        minHeight = Math.max(minHeight, ...skillsToPlace.map(skill => rows.get(skill.id)!)) + 1;
    }

    placeSkills(skills.filter(skill => !placed.has(skill.id)), minHeight);
}

function topologicalSort<B extends Block>(graph: Graph, blocks: B[]): B[] {
    let result: B[] = [];

    let toInclude: Set<number> = new Set(blocks.map(block => block.id));

    let noDependencies: Set<number> = new Set();
    blocks.filter(block => graph.getParents(block).filter(parent => toInclude.has(parent.id)).length === 0).forEach(block => {
        noDependencies.add(block.id);
    });

    let removed: Set<number> = new Set();

    function nonRemovedParents(block: B): B[] {
        return graph.getParents(block).filter(parent => !removed.has(parent.id) && toInclude.has(parent.id));
    }

    while (removed.size < blocks.length) {

        if (noDependencies.size === 0) {
            // Cycle
            let blockWithFewestParents = blocks.filter(block => !removed.has(block.id)).toSorted((a, b) => nonRemovedParents(a).length - nonRemovedParents(b).length)[0]!;
            noDependencies.add(blockWithFewestParents.id);
        }

        while (noDependencies.size > 0) {
            let current = graph.getNode(noDependencies.keys().next().value!) as B;
            noDependencies.delete(current.id);

            result.push(current);

            removed.add(current.id);
            graph.getChildren(current).filter(child => !removed.has(child.id) && toInclude.has(child.id)).filter(child => nonRemovedParents(child).length === 0).forEach(child => {
                noDependencies.add(child.id);
            });
        }

    }

    return result;
}

class Placement {

    private placement: Map<string, Block>;

    constructor() {
        this.placement = new Map();
    }

    private makeKey(column: number, row: number): string {
        return `${column};${row}`
    }

    place(block: Block) {
        this.placement.set(this.makeKey(block.column!, block.row!), block);
    }

    firstFreeSpotInColumnFromRow(column: number, row: number): number {
        let result = row;
        while (this.placement.has(this.makeKey(column, result))) {
            result++;
        }
        return result;
    }

}