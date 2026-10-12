export type DeadlineDisplay = "EARLIEST" | "DELFT" | "LOCAL";

export const deadlineDisplayOptions: {
    value: DeadlineDisplay;
    label: string;
    description: string;
}[] = [
    {
        value: "EARLIEST",
        label: "Earliest",
        description: "Show whichever time is earlier.",
    },
    {
        value: "DELFT",
        label: "Delft",
        description: "Always show the deadline in Delft time.",
    },
    {
        value: "LOCAL",
        label: "Local",
        description: "Always show the deadline in your browser's timezone.",
    },
];
