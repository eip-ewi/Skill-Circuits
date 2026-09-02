export const FocusModeBlockStates = {
    NotInFocusMode: "not in focus mode",
    FocusOnBlock: "focus on block",
    VisibleInFocusMode: "visible in focus mode",
    DisabledInFocusMode: "disabled in focus mode",
} as const;

export type FocusModeBlockState = (typeof FocusModeBlockStates)[keyof typeof FocusModeBlockStates];

export function isVisibleAndInFocusMode(state: FocusModeBlockState | undefined) {
    return (
        state === FocusModeBlockStates.FocusOnBlock ||
        state === FocusModeBlockStates.VisibleInFocusMode
    );
}
