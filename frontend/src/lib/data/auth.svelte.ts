import type {Level} from "./level";

let auth: Auth | undefined = undefined;

export function getAuth(): Auth {
    return auth!;
}

export const authState: {isFetched: boolean} = $state({
    isFetched: false
});

export async function fetchAuth(level: Level, circuitId: number) {
    let response = await fetch(`/api/auth/${level.circuit}/${circuitId}`);
    auth = await response.json();
    authState.isFetched = true;
}

interface Auth {
    canEditModule: boolean,
    canEditBlocks: boolean,
    canEditItems: boolean,
    canCompleteItems: boolean,
}