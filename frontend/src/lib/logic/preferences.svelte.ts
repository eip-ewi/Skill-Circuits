import type {Preferences} from "../dto/preferences";
import {lightTheme, type Theme} from "../data/theme";
import {withCsrf} from "./csrf";

// TODO: use default or add an "isFetched" function?
let preferences: Preferences = $state({theme: lightTheme, blurSkills: true});

export function getTheme() : Theme {
    return preferences.theme;
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

export async function setBlurSkills(blurSkillsSetting: boolean) {
    let response = await fetch(`/api/person/preferences/blur?blurSkills=${blurSkillsSetting}`, withCsrf({
        method: "PATCH",
    }));
    preferences = await response.json();
}

export async function fetchPreferences() {
    let response = await fetch("/api/person/preferences");
    preferences = await response.json();
}
