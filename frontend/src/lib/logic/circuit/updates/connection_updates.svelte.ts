import type { Block } from "../../../dto/circuit/block";
import {getLevel} from "../level.svelte";
import {withCsrf} from "../../csrf";
import type {SkillBlock} from "../../../dto/circuit/module/skill";

export async function connectSkills(from: SkillBlock, to: SkillBlock) {
    from.children.push(to.id);
    to.parents.push(from.id);
    let response = await fetch(`/api/skills/connections/${from.id}/${to.id}`, withCsrf({
        method: "POST",
    }));
    if (!response.ok) {
        from.children.pop();
        to.parents.pop();
    }
}

export async function disconnectSkills(from: SkillBlock, to: SkillBlock) {
    from.children.splice(from.children.indexOf(to.id), 1);
    to.parents.splice(to.parents.indexOf(from.id), 1);
    let response = await fetch(`/api/skills/connections/${from.id}/${to.id}`, withCsrf({
        method: "DELETE",
    }));
    if (!response.ok) {
        from.children.push(to.id);
        to.parents.push(from.id);
    }
}
