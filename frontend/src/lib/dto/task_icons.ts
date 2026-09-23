const taskIcons = {
    READING: "book",
    VIDEO: "play",
    QUIZ: "clipboard-question",
    IMPLEMENTATION: "desktop",
    EXERCISE: "pencil-ruler",
    COLLABORATION: "people-carry",
    EXPERIMENT: "flask",
    CLASSROOM: "person-chalkboard",
} as const;

export const TaskIcons: Record<string, string> & typeof taskIcons = taskIcons;
