import type {Preferences} from "../dto/preferences";
import {lightTheme, type Theme} from "../data/theme";
import {withCsrf} from "./csrf";

let preferences: Preferences = $state({theme: lightTheme, blurBlocks: true});

export function getTheme() : Theme {
    return preferences.theme;
}

export function getBlurBlocks() : boolean {
    return preferences.blurBlocks;
}

export async function setTheme(theme: Theme) {
    let response = await fetch(`/api/person/preferences/theme?theme=${theme.name.toUpperCase()}`, withCsrf({
        method: "PATCH",
    }));
    preferences = await response.json();
    setThemeProperties(preferences.theme);
}

export function setThemeProperties(theme: Theme) {
    const root = document.documentElement;

    root.setAttribute("data-theme", theme.name);
    root.setAttribute("data-colour-scheme", theme.colourScheme);
}

export async function setBlurBlocks(blurBlocksSetting: boolean) {
    let response = await fetch(`/api/person/preferences/blur?blurBlocks=${blurBlocksSetting}`, withCsrf({
        method: "PATCH",
    }));
    preferences = await response.json();
}

export async function fetchPreferences() {
    let response = await fetch("/api/person/preferences");
    preferences = await response.json();
}
