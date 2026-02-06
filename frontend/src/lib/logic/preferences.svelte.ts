import type {Preferences} from "../dto/preferences";
import {lightTheme, systemTheme, type Theme} from "../data/theme";
import {withCsrf} from "./csrf";
import {setThemeProperties} from "./theme.svelte";

let preferences: Preferences = $state({theme: systemTheme, blurBlocks: true});

export async function fetchPreferences() {
    let response = await fetch("/api/person/preferences");
    preferences = await response.json();
}

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

export async function setBlurBlocks(blurBlocksSetting: boolean) {
    let response = await fetch(`/api/person/preferences/blur?blurBlocks=${blurBlocksSetting}`, withCsrf({
        method: "PATCH",
    }));
    preferences = await response.json();
}

