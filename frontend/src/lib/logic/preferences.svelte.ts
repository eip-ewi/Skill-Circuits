import type {Preferences} from "../dto/preferences";
import {lightTheme, systemTheme, type Theme} from "../data/theme";
import {withCsrf} from "./csrf";

let preferences: Preferences = $state({theme: systemTheme, blurBlocks: true});
let systemDefaultColorScheme: "light" | "dark" = $state(window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");

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

export function addSystemColorSchemeEventListener() {
    const darkColorScheme = window.matchMedia("(prefers-color-scheme: dark)");
    darkColorScheme.addEventListener("change", setSystemDefault);
}

export function setSystemDefault(event: MediaQueryListEvent) {
    systemDefaultColorScheme = event.matches ? "dark" : "light";
}

export function getThemeName(theme: Theme) {
    return theme.name === "system" ? systemDefaultColorScheme : theme.name;
}

export function getThemeColorScheme(theme: Theme) {
    return theme.name === "system" ? systemDefaultColorScheme : theme.colourScheme;
}

export function setThemeProperties(theme: Theme) {
    const root = document.documentElement;
    root.setAttribute("data-theme", getThemeName(theme));
    root.setAttribute("data-colour-scheme", getThemeColorScheme(theme));
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
