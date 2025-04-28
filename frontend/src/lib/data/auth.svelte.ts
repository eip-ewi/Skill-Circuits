import type {Level} from "./level";

let auth: Auth | undefined = undefined;

export function getAuth(): Auth {
    return auth!;
}

let isFetched = $state(false);

export async function fetchAuth(level: Level, circuitId: number) {
    let response = await fetch(`/api/auth/${level.circuit}/${circuitId}`);
    auth = await response.json();
    isFetched = true;
}

export function hasAuth() {
    return isFetched;
}

interface Auth {
    canEditModule: boolean,
    canEditBlocks: boolean,
    canEditItems: boolean,
    canCompleteItems: boolean,
}