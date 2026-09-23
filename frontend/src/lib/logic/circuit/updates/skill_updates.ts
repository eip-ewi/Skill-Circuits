import { getLevel } from "../level.svelte";
import { withCsrf } from "../../csrf";
import type {
    ExternalSkillBlock,
    RegularSkillBlock,
    SkillBlock,
} from "../../../dto/circuit/module/skill";
import type { Checkpoint } from "../../../dto/checkpoint";
import { getCircuit } from "../circuit.svelte";
import { BlockStates } from "../../../data/block_state";
import type { ModuleCircuit } from "../../../dto/circuit/module/module";
import { setScrollTarget } from "../scroll_target.svelte";
import { fetchBookmarks } from "../../bookmarks.svelte";

export async function createExternalSkill(originalSkillId: number, column: number) {
    const response = await fetch(
        `/api/skills/external`,
        withCsrf({
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
        }),
    );

    if (response.ok) {
        const externalSkill: ExternalSkillBlock = await response.json();
        externalSkill.blockType = "skill";
        externalSkill.state = BlockStates.Inactive;
        (getCircuit() as ModuleCircuit).externalSkills.push(externalSkill);
        setScrollTarget({ kind: "block", id: externalSkill.id });
    }
}

export async function editSkillCheckpoint(skill: SkillBlock, newCheckpoint: Checkpoint | null) {
    const oldCheckpoint = skill.checkpoint;
    skill.checkpoint = newCheckpoint === null ? null : newCheckpoint.id;

    const patch = { checkpoint: { id: newCheckpoint?.id } };
    const response = await fetch(
        `/api/${getLevel().blocks}/${skill.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(patch),
        }),
    );

    if (!response.ok) {
        skill.checkpoint = oldCheckpoint;
    } else {
        setScrollTarget({ kind: "block", id: skill.id });
    }
}

export async function editSkillEssential(skill: SkillBlock, newEssential: boolean) {
    const oldEssential = skill.essential;
    skill.essential = newEssential;

    const response = await fetch(
        `/api/${getLevel().blocks}/${skill.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                essential: newEssential,
            }),
        }),
    );

    if (!response.ok) {
        skill.essential = oldEssential;
    }
}

export async function editSkillHidden(skill: RegularSkillBlock, newHidden: boolean) {
    const oldHidden = skill.hidden;
    const oldEssential = skill.essential;
    skill.hidden = newHidden;
    skill.essential = skill.essential && !newHidden;

    const response = await fetch(
        `/api/${getLevel().blocks}/${skill.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                hidden: newHidden,
                essential: skill.essential,
            }),
        }),
    );

    if (response.ok) {
        await fetchBookmarks();
    } else {
        skill.hidden = oldHidden;
        skill.essential = oldEssential;
    }
}
