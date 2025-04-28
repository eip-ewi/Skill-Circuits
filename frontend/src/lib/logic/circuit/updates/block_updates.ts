import type { Block } from "../../../dto/circuit/block";
import {getLevel} from "../level.svelte";
import {withCsrf} from "../../csrf";
import type {SkillBlock} from "../../../dto/circuit/module/skill";
import type {Group} from "../../../dto/circuit/group";
import {getBlocks, getCircuit, getGroupForBlock} from "../circuit.svelte";
import {BlockStates} from "../../../data/block_state";
import type {Checkpoint} from "../../../dto/checkpoint";

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
    }
}

export async function editBlockCheckpoint(skill: SkillBlock, newCheckpoint: Checkpoint | null) {
    let oldCheckpoint = skill.checkpoint;
    skill.checkpoint = newCheckpoint === null ? null : newCheckpoint.id;

    let patch: any = {};
    if (newCheckpoint !== null) {
        patch.checkpoint = { id: newCheckpoint.id };
    }
    let response = await fetch(`/api/${getLevel().blocks}/${skill.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(patch),
    }));

    if (!response.ok) {
        skill.checkpoint = oldCheckpoint;
    }
}

export async function editBlockEssential(skill: SkillBlock, newEssential: boolean) {
    let oldEssential = skill.essential;
    skill.essential = newEssential;

    let response = await fetch(`/api/${getLevel().blocks}/${skill.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            essential: newEssential,
        }),
    }));

    if (!response.ok) {
        skill.essential = oldEssential;
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
        let group = getGroupForBlock(block);
        group.blocks.splice(group.blocks.findIndex(b => b.id === block.id), 1);
        getBlocks().filter(b => b.parents.includes(block.id)).forEach(b => b.parents.splice(b.parents.indexOf(block.id), 1));
        getBlocks().filter(b => b.children.includes(block.id)).forEach(b => b.children.splice(b.children.indexOf(block.id), 1));
    }
}
