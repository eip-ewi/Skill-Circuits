import type {ModuleLevel} from "./level";

export namespace BlockActions {
    export let Goto: "go to" = "go to";
    export let Expand: "expand" = "expand";
    export let Move: "move" = "move";
    export let Edit: "edit" = "edit";
    export let Delete: "delete" = "delete";
    export let Link: "link" = "link";
    export let AddParent: "add parent" = "add parent";
    export let AddChild: "add child" = "add child";
    export let RemoveParent: "remove parent" = "remove parent";
    export let RemoveChild: "remove child" = "remove child";
    export let CancelLink: "cancel link" = "cancel link";
    export let StopEdit: "stop edit" = "stop edit";
}

export type BlockAction = typeof BlockActions.Goto | typeof BlockActions.Expand
    | typeof BlockActions.Move | typeof BlockActions.Edit | typeof BlockActions.Delete
    | typeof BlockActions.Link | typeof BlockActions.AddParent | typeof BlockActions.AddChild
    | typeof BlockActions.RemoveParent | typeof BlockActions.RemoveChild | typeof BlockActions.CancelLink
    | typeof BlockActions.StopEdit ;