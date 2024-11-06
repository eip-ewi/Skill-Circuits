import type {GroupData} from "../data/group";

export interface BackgroundGroup {
    group: GroupData;
    row: number;
    column: number;
    showLabel: boolean;

    connectLeft: boolean;
    connectRight: boolean;
    connectTop: boolean;
    connectBottom: boolean;

    connectTopLeft: boolean;
    connectTopRight: boolean;
    connectBottomRight: boolean;
    connectBottomLeft: boolean;
}

export function createBackgroundGroup(group: GroupData, column: number, row: number): BackgroundGroup {
    return {
        group: group,
        row: row,
        column: column,
        showLabel: false,

        connectLeft: false,
        connectRight: false,
        connectTop: false,
        connectBottom: false,

        connectTopLeft: false,
        connectTopRight: false,
        connectBottomRight: false,
        connectBottomLeft: false,
    }
}