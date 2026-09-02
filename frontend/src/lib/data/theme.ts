export const systemTheme = {
    name: "system",
    colourScheme: "system",
    displayName: "System default",
};

export const lightTheme = {
    name: "light",
    colourScheme: "light",
    displayName: "Light",
};

export const darkTheme = {
    name: "dark",
    colourScheme: "dark",
    displayName: "Dark",
};

export const nostalgiaTheme = {
    name: "nostalgia",
    colourScheme: "light",
    displayName: "Nostalgia",
};

export const lightHighContrastTheme = {
    name: "high_contrast_light",
    colourScheme: "light",
    displayName: "High-contrast (light)",
};

export const darkHighContrastTheme = {
    name: "high_contrast_dark",
    colourScheme: "dark",
    displayName: "High-contrast (dark)",
};

export const availableThemes: Theme[] = [
    systemTheme,
    lightTheme,
    darkTheme,
    nostalgiaTheme,
    lightHighContrastTheme,
    darkHighContrastTheme,
];

export type Theme =
    | typeof systemTheme
    | typeof lightTheme
    | typeof darkTheme
    | typeof nostalgiaTheme
    | typeof lightHighContrastTheme
    | typeof darkHighContrastTheme;
