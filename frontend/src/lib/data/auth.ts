import type {Level} from "./level";

let auth: Auth | undefined = undefined;

export function getAuth(): Auth {
    return auth!;
}

export async function fetchAuth(level: Level, circuitId: number) {
    let response = await fetch(`/api/auth/${level.circuit}/${circuitId}`);
    auth = await response.json();
}

interface Auth {
    canEditBlocks: boolean,
    canEditItems: boolean,
    canCompleteItems: boolean,
}