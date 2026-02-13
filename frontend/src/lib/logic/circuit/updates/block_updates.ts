import type { Block } from "../../../dto/circuit/block";
import {getLevel} from "../level.svelte";
import {withCsrf} from "../../csrf";
import type {RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
import type {Group} from "../../../dto/circuit/group";
import {getBlocks, getCircuit, getGroupForBlock} from "../circuit.svelte";
import {BlockStates} from "../../../data/block_state";
import type {Checkpoint} from "../../../dto/checkpoint";
import type {ModuleCircuit} from "../../../dto/circuit/module/module";
import {setScrollTarget} from "../scroll_target.svelte";

export async function createBlock(column: number) {
    let firstGroup = getCircuit().groups[0]!;

    let create: any = {
        name: `New ${getLevel().block}`,
        column: column,
    };
    create[getLevel().group] = { id: firstGroup.id };

    let response = await fetch(`/api/${getLevel().blocks}`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(create),
    }));

    if (response.ok) {
        let block: Block = await response.json();
        // @ts-ignore
        block.blockType = getLevel().block;
        block.state = BlockStates.Editing;
        (firstGroup.blocks as Block[]).push(block);
        setScrollTarget({ kind: "block", id: block.id });
    }
}

export async function editBlockName(block: Block, newName: string) {
    let oldName = block.name;
    block.name = newName;

    let response = await fetch(`/api/${getLevel().blocks}/${block.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName,
        }),
    }));

    if (!response.ok) {
        block.name = oldName;
    }
}

export async function editBlockGroup(block: Block, newGroup: Group) {
    let oldGroup = getGroupForBlock(block);
    oldGroup.blocks.splice(oldGroup.blocks.findIndex(b => b.id === block.id), 1);
    (newGroup.blocks as Block[]).push(block);

    let patch: any = {};
    patch[getLevel().group] = { id: newGroup.id };
    let response = await fetch(`/api/${getLevel().blocks}/${block.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(patch),
    }));

    if (!response.ok) {
        newGroup.blocks.pop();
        (oldGroup.blocks as Block[]).push(block);
    } else {
        setScrollTarget({ kind: "block", id: block.id });
    }
}

export async function deleteBlock(block: Block) {
    let response = await fetch(`/api/${getLevel().blocks}/${block.id}`, withCsrf({
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
        },
    }));

    if (response.ok) {
        if (block.blockType === "skill" && block.external) {
            let circuit = getCircuit() as ModuleCircuit;
            circuit.externalSkills.splice(circuit.externalSkills.findIndex(s => s.id === block.id)!, 1);
        } else {
            let group = getGroupForBlock(block);
            group.blocks.splice(group.blocks.findIndex(b => b.id === block.id), 1);
        }
        getBlocks().filter(b => b.parents.includes(block.id)).forEach(b => b.parents.splice(b.parents.indexOf(block.id), 1));
        getBlocks().filter(b => b.children.includes(block.id)).forEach(b => b.children.splice(b.children.indexOf(block.id), 1));
    }
}

export function editingBlocks() {
    return getBlocks().filter(b => b.state === BlockStates.Editing).length > 0;
}


export function makeBlocksInactive() {
    getBlocks().forEach(b => {
        if (b.state === BlockStates.Editing) {
            b.state = BlockStates.Inactive;
        }
    });
}
