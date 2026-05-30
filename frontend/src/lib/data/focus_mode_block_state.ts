export namespace FocusModeBlockStates {
    export let NotInFocusMode: "not in focus mode" = "not in focus mode";
    export let FocusOnBlock: "focus on block" = "focus on block";
    export let VisibleInFocusMode: "visible in focus mode" = "visible in focus mode";
    export let DisabledInFocusMode: "disabled in focus mode" = "disabled in focus mode";
}

export type FocusModeBlockState =
    | typeof FocusModeBlockStates.NotInFocusMode
    | typeof FocusModeBlockStates.FocusOnBlock
    | typeof FocusModeBlockStates.VisibleInFocusMode
    | typeof FocusModeBlockStates.DisabledInFocusMode;

export function isVisibleAndInFocusMode(state: FocusModeBlockState | undefined) {
    return (
        state === FocusModeBlockStates.FocusOnBlock ||
        state === FocusModeBlockStates.VisibleInFocusMode
    );
}
