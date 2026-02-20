import type {Theme} from "../data/theme";

let systemDefaultColorScheme: "light" | "dark" = $state(window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");

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