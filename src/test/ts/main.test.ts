// @ts-ignore
import {toggleOverlay} from "../../main/ts/main.ts";

test("Test overlay", () => {
    const dialog = document.createElement("dialog");
    dialog.showModal = function () { this.open = true; }
    dialog.close = function () { this.open = false; }
    dialog.id = "overlay";

    document.body.append(dialog)

    expect(dialog.open).toBe(false);
    toggleOverlay("overlay");
    expect(dialog.open).toBe(true);
    toggleOverlay("overlay");
    expect(dialog.open).toBe(false);
});