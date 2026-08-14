import type { Preferences } from "../dto/preferences";
import {
    darkHighContrastTheme,
    lightHighContrastTheme,
    systemTheme,
    type Theme,
} from "../data/theme";
import { withCsrf } from "./csrf";
import { setThemeProperties } from "./theme.svelte";

let preferences: Preferences = $state({
    theme: systemTheme,
    blurBlocks: true,
    additionalIcons: false,
});

export async function fetchPreferences() {
    const response = await fetch("/api/person/preferences");
    preferences = await response.json();
}

export function getTheme(): Theme {
    return preferences.theme;
}

export function getBlurBlocks(): boolean {
    return preferences.blurBlocks;
}

export function getAdditionalIcons(): boolean {
    return preferences.additionalIcons;
}

export async function setTheme(theme: Theme) {
    const response = await fetch(
        `/api/person/preferences/theme?theme=${theme.name.toUpperCase()}`,
        withCsrf({
            method: "PATCH",
        }),
    );
    preferences = await response.json();
    setThemeProperties(preferences.theme);
}

export async function setBlurBlocks(blurBlocksSetting: boolean) {
    const response = await fetch(
        `/api/person/preferences/blur?blurBlocks=${blurBlocksSetting}`,
        withCsrf({
            method: "PATCH",
        }),
    );
    preferences = await response.json();
}

export async function setAdditionalIcons(additionalIconsSetting: boolean) {
    const response = await fetch(
        `/api/person/preferences/additional-icons?additionalIcons=${additionalIconsSetting}`,
        withCsrf({
            method: "PATCH",
        }),
    );
    preferences = await response.json();
}

export function isHighContrastThemeSet(): boolean {
    return (
        getTheme().name === lightHighContrastTheme.name ||
        getTheme().name === darkHighContrastTheme.name
    );
}
