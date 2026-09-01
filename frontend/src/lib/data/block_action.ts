export const BlockActions = {
    Goto: "go to",
    Expand: "expand",
    Move: "move",
    Edit: "edit",
    Delete: "delete",
    Link: "link",
    AddParent: "add parent",
    AddChild: "add child",
    RemoveParent: "remove parent",
    RemoveChild: "remove child",
    CancelLink: "cancel link",
    StopEdit: "stop edit",
    Bookmark: "bookmark",
    FocusMode: "focus mode",
    StopFocusMode: "stop focus mode",
} as const;

export type BlockAction = (typeof BlockActions)[keyof typeof BlockActions];
