import type {ModuleLevel} from "./level";

export namespace BlockStates {
    export let Inactive: "inactive" = "inactive";
    export let Hovering: "hovering" = "hovering";
    export let Dragging: "dragging" = "dragging";
    export let Editing: "editing" = "editing";
    export let Connecting: "connecting" = "connecting";
    export let WaitingForConnection: "waiting for connection" = "waiting for connection";
    export let AssigningPaths: "assigning paths" = "assigning paths";
}

export type BlockState = typeof BlockStates.Inactive | typeof BlockStates.Hovering | typeof BlockStates.Dragging
    | typeof BlockStates.Editing | typeof BlockStates.Connecting | typeof BlockStates.WaitingForConnection
    | typeof BlockStates.AssigningPaths ;