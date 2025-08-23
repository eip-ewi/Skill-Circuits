import type { Block } from "../../dto/circuit/block";
import type {Circuit} from "../../dto/circuit/circuit";
import {Graph} from "./graph";
import type {Group} from "../../dto/circuit/group";
import type {Item} from "../../dto/circuit/item";
import {getLevel, isLevel} from "./level.svelte";
import {canEditCircuit, getAuthorisation} from "../authorisation.svelte";
import {ModuleLevel} from "../../data/level";
import {isSkillRevealed} from "./unlocked_skills.svelte";
import {BlockStates} from "../../data/block_state";

let circuit: Circuit | undefined = $state(undefined);
// @ts-ignore
let blocks: Block[] | undefined = $derived(circuit === undefined ? undefined : blocksFromCircuit(circuit));
let graph: Graph | undefined = $derived(circuit === undefined ? undefined : new Graph(blocks!.filter(block => isBlockVisible(block))));
// @ts-ignore
let blockToGroupMap: Map<number, Group> | undefined = $derived(circuit === undefined ? undefined : new Map(circuit!.groups.flatMap(group => group.blocks.map(block => [block.id, group]))));
// @ts-ignore
let itemToBlockMap: Map<number, Block> | undefined = $derived(circuit === undefined ? undefined : new Map(blocks.flatMap(block => block.items.map(item => [item.id, block]))));

function blocksFromCircuit(circuit: Circuit): Block[] {
    let blocks: Block[] = [];
    if (circuit.circuitType === "module") {
        blocks = [
            ...circuit.groups.flatMap(group => group.blocks),
            ...circuit.externalSkills,
        ];
    } else {
        blocks = circuit.groups.flatMap(group => group.blocks);
    }
    blocks.forEach(block => block.state = BlockStates.Inactive);
    return blocks;
}

export function circuitFetched(): boolean {
    return circuit !== undefined;
}

export function getCircuit<T extends Circuit["circuitType"]>(): Circuit & { circuitType: T } {
    return circuit! as Circuit & { circuitType: T };
}

export function getGroup(id: number): Group {
    return circuit!.groups.find(group => group.id === id)!;
}

export function getBlocks(): Block[] {
    return blocks!;
}

export function getPlacableBlocks(): Block[] {
    return blocks!.filter(block => block.column === null);
}

export function getPlacedBlocks(): Block[] {
    return blocks!.filter(block => block.column !== null);
}

export function getVisibleBlocks(): Block[] {
    return blocks!.filter(block => isBlockVisible(block));
}

export function isBlockVisible(block: Block) {
    return block.column !== null && (!isLevel(ModuleLevel) || canEditCircuit() || block.blockType !== "skill" || block.external || !block.hidden || isSkillRevealed(block));
}

export function getGroupForBlock(block: Block): Group {
    return blockToGroupMap!.get(block.id)!;
}

export function getBlock(id: number): Block {
    return blocks!.find(block => block.id === id)!;
}

export function getBlockForItem(item: Item): Block {
    return itemToBlockMap!.get(item.id)!;
}

export function getItem(id: number): Item {
    return blocks!.flatMap(block => block.items as Item[]).find(item => item.id === id)!;
}

export function getGraph(): Graph {
    return graph!;
}

export async function fetchCircuit(url: string) {
    let response = await fetch(url);
    circuit = await response.json();
}
