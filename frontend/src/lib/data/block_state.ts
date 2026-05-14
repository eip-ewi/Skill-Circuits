import type { ModuleLevel } from "./level";

export namespace BlockStates {
    export let Inactive: "inactive" = "inactive";
    export let Hovering: "hovering" = "hovering";
    export let Dragging: "dragging" = "dragging";
    export let Editing: "editing" = "editing";
    export let Connecting: "connecting" = "connecting";
    export let WaitingForConnection: "waiting for connection" = "waiting for connection";
    export let AssigningPaths: "assigning paths" = "assigning paths";
    export let FocusMode: "focus mode" = "focus mode";
    export let VisibleInFocusMode: "visible in focus mode" = "visible in focus mode";
    export let DisabledInFocusMode: "disabled in focus mode" = "disabled in focus mode";
}

export type BlockState =
    | typeof BlockStates.Inactive
    | typeof BlockStates.Hovering
    | typeof BlockStates.Dragging
    | typeof BlockStates.Editing
    | typeof BlockStates.Connecting
    | typeof BlockStates.WaitingForConnection
    | typeof BlockStates.AssigningPaths
    | typeof BlockStates.FocusMode
    | typeof BlockStates.VisibleInFocusMode
    | typeof BlockStates.DisabledInFocusMode;
