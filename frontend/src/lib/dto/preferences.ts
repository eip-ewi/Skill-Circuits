import type { Theme } from "../data/theme";
import type { DeadlineDisplay } from "../data/deadline_display";

export interface Preferences {
    theme: Theme;
    blurBlocks: boolean;
    deadlineDisplay: DeadlineDisplay;
    additionalIcons: boolean;
}
