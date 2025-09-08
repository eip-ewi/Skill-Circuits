import {Graph} from "../circuit/graph";
import {getLevel} from "../circuit/level.svelte";
import type {Checkpoint} from "../../dto/checkpoint";
import {getCircuit, getPlacedBlocks, getVisibleBlocks} from "../circuit/circuit.svelte";
import type {Edition} from "../../dto/edition";
import moment from "moment";
import type {Module} from "../../dto/module";
import type {Path} from "../../dto/path";
import {isCompleted} from "../circuit/skill_state/completion";
import type {SkillBlock} from "../../dto/circuit/module/skill";

let edition: Edition | undefined = $state();

export async function fetchEdition(editionId: number) {
    const response = await fetch(`/api/editions/${editionId}`);
    edition = await response.json();
}

export function getEdition(): Edition {
    return edition!;
}

export function getCheckpoint(id: number): Checkpoint {
    return getCheckpoints().find(checkpoint => checkpoint.id === id)!;
}

export function getCheckpoints(): Checkpoint[] {
    return edition!.checkpoints;
}

export function getSortedCheckpoints(): Checkpoint[] {
    return getCheckpoints().toSorted((a, b) => moment(a.deadline).unix() - moment(b.deadline).unix());
}

export function getVisibleCheckpoints(): Checkpoint[] {
    let usedCheckpoints: Set<number> = new Set(getPlacedBlocks()
        .filter(block => block.blockType === "skill")
        .filter(block => block.checkpoint != null)
        .map(block => block.checkpoint!));
    return getSortedCheckpoints().filter(checkpoint => usedCheckpoints.has(checkpoint.id));
}

export function getNextCheckpoint(): Checkpoint | undefined {
    let uncompleted: Set<number> = new Set(getVisibleBlocks().filter(block => block.blockType === "skill" && block.checkpoint !== null)
        .filter(block => !isCompleted(block)).map(block => (block as SkillBlock).checkpoint!));
    return getVisibleCheckpoints().filter(c => moment(c.deadline).isAfter(moment())).find(c => uncompleted.has(c.id));
}

export function getFirstUncompletedPastCheckpoint(): Checkpoint | undefined {
    let uncompleted: Set<number> = new Set(getVisibleBlocks().filter(block => block.blockType === "skill" && block.checkpoint !== null && block.essential)
        .filter(block => !isCompleted(block)).map(block => (block as SkillBlock).checkpoint!));
    return getVisibleCheckpoints().filter(c => moment().isAfter(moment(c.deadline))).find(c => uncompleted.has(c.id));
}

export function getPath(id: number): Path {
    return getPaths().find(path => path.id === id)!;
}

export function getPaths(): Path[] {
    return edition!.paths;
}

export function getModule(id: number): Module {
    return getModules().find(path => path.id === id)!;
}

export function getModules(): Module[] {
    return edition!.modules;
}
