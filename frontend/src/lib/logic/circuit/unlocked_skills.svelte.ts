import type {SkillBlock} from "../../dto/circuit/module/skill";
import type {SkillItem} from "../../dto/circuit/edition/skill";
import {getBlocks, getVisibleBlocks} from "./circuit.svelte";
import {isCompleted} from "./skill_state/completion";
import type {Block} from "../../dto/circuit/block";

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

export async function scrollToFirstIncomplete() {
    const skillBlocks = getVisibleBlocks().filter(a => !isCompleted(a) && a.blockType === "skill" && a.essential) as SkillBlock[];
    const firstIncomplete = getFirstByYPosition(skillBlocks);

    if (firstIncomplete) {
        const element = document.querySelector(`#block-${firstIncomplete.skillId}`);
        element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

export function getFirstByYPosition(skills: SkillBlock[]): { skillId: number, y: number } | null {
    const skillsWithPositions: Array<{ skillId: number, y: number }> = [];

    skills.forEach(skill => {
        const element = document.querySelector(`#block-${skill.id}`);

        if (element) {
            const rect = element.getBoundingClientRect();
            const yPos = rect.top + window.scrollY;
            skillsWithPositions.push({
                skillId: skill.id,
                y: yPos
            });
        }

    });

    skillsWithPositions.sort((a, b) => a.y - b.y);
    return skillsWithPositions[0] ?? null;
}