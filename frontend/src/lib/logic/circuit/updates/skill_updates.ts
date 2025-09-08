import {getLevel} from "../level.svelte";
import {withCsrf} from "../../csrf";
import type {ExternalSkillBlock, RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
import type {Checkpoint} from "../../../dto/checkpoint";
import {getCircuit} from "../circuit.svelte";
import type {Block} from "../../../dto/circuit/block";
import {BlockStates} from "../../../data/block_state";
import type {ModuleCircuit} from "../../../dto/circuit/module/module";

export async function createExternalSkill(originalSkillId: number, column: number) {
    let response = await fetch(`/api/skills/external`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            module: {
                id: getCircuit().id,
            },
            skill: {
                id: originalSkillId,
            },
            column: column,
        }),
    }));

    if (response.ok) {
        let externalSkill: ExternalSkillBlock = await response.json();
        // @ts-ignore
        externalSkill.blockType = "skill";
        externalSkill.state = BlockStates.Editing;
        (getCircuit() as ModuleCircuit).externalSkills.push(externalSkill);
    }
}

export async function editSkillCheckpoint(skill: SkillBlock, newCheckpoint: Checkpoint | null) {
    let oldCheckpoint = skill.checkpoint;
    skill.checkpoint = newCheckpoint === null ? null : newCheckpoint.id;

    let patch: any = {};
    patch.checkpoint = { id: newCheckpoint?.id };
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

export async function editSkillEssential(skill: SkillBlock, newEssential: boolean) {
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

export async function editSkillHidden(skill: RegularSkillBlock, newHidden: boolean) {
    let oldHidden = skill.hidden;
    let oldEssential = skill.essential;
    skill.hidden = newHidden;
    skill.essential = skill.essential && !newHidden;

    let response = await fetch(`/api/${getLevel().blocks}/${skill.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            hidden: newHidden,
            essential: skill.essential,
        }),
    }));

    if (!response.ok) {
        skill.hidden = oldHidden;
        skill.essential = oldEssential;
    }
}

