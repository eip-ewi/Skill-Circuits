import type { Level } from "../../data/level";

let level: Level | undefined = $state();

export function getLevel(): Level {
    if (level === undefined) throw new Error("The level of the circuit is not known!");
    return level;
}

export function isOnCircuit(): boolean {
    return level !== undefined;
}

export function setLevel(newLevel: Level): void {
    level = newLevel;
}

export function clearLevel(): void {
    level = undefined;
}

export function isLevel(checkLevel: Level): boolean {
    return level?.circuit === checkLevel.circuit;
}
