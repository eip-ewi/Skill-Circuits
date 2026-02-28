export const systemTheme = {
    name: "system",
    colourScheme: "system",
    displayName: "System default",
}

export const lightTheme = {
    name: "light",
    colourScheme: "light",
    displayName: "Light",
}

export const darkTheme = {
    name: "dark",
    colourScheme: "dark",
    displayName: "Dark",
}

export const nostalgiaTheme = {
    name: "nostalgia",
    colourScheme: "light",
    displayName: "Nostalgia",
}

export const availableThemes: Theme[] = [
    systemTheme,
    lightTheme,
    darkTheme,
    nostalgiaTheme,
];

export type Theme = typeof systemTheme | typeof lightTheme | typeof darkTheme | typeof nostalgiaTheme;