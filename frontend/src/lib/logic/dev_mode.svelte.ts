let devMode: boolean = $state(false)

export function getDevMode() {
    return devMode;
}

export async function fetchDevMode() {
    let response = await fetch("/dev-mode");
    devMode = await response.text() === "true";
}