import {availableThemes, lightTheme, type Theme} from "../data/theme";

let theme: Theme | undefined = $state();

function loadThemeFromLocalStorage() {
    let storedTheme = localStorage.getItem("theme");
    if (storedTheme == null) {
        localStorage.setItem("theme", JSON.stringify(lightTheme));
        theme = lightTheme;
    } else {
        theme = JSON.parse(storedTheme);
    }
}

export function getTheme(): Theme {
    if (theme === undefined) {
        loadThemeFromLocalStorage();
    }
    return theme!;
}

export function setTheme(newTheme: Theme) {
    theme = newTheme;
    localStorage.setItem("theme", JSON.stringify(newTheme));
}