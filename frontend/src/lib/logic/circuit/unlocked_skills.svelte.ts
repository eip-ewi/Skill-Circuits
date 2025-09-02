import type {SkillBlock} from "../../dto/circuit/module/skill";
import type {SkillItem} from "../../dto/circuit/edition/skill";

let unlockedSkills: number[] = $state([]);
let unlockedSkillSet: Set<number> = $derived(new Set(unlockedSkills));

export function isSkillRevealed(skill: SkillBlock): boolean {
    return unlockedSkillSet.has(skill.id);
}

export function isSkillItemRevealed(skill: SkillItem): boolean {
    return unlockedSkillSet.has(skill.id);
}

export function addRevealedSkills(skillIds: number[]) {
    unlockedSkills.push(...skillIds);
}

export async function fetchRevealedSkills() {
    let response = await fetch(`/api/skills/unlocked`);
    if (response.ok) {
        unlockedSkills = await response.json();
    }
}