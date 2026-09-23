export const BlockStates = {
    Inactive: "inactive",
    Hovering: "hovering",
    Dragging: "dragging",
    Editing: "editing",
    Connecting: "connecting",
    WaitingForConnection: "waiting for connection",
    AssigningPaths: "assigning paths",
} as const;

export type BlockState = (typeof BlockStates)[keyof typeof BlockStates];
