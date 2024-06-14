// @ts-ignore
import {toggleOverlay, setOverlayState} from "../../main/ts/main.ts";

test("Test toggle overlay", () => {
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

test("Test set overlay to opened", () => {
    const dialog = document.createElement("dialog");
    dialog.showModal = function () { this.open = true; }
    dialog.close = function () { this.open = false; }
    dialog.id = "overlay";

    document.body.append(dialog)

    expect(dialog.open).toBe(false);
    setOverlayState("overlay", true);
    expect(dialog.open).toBe(true);
    setOverlayState("overlay", true);
    expect(dialog.open).toBe(true);
});

test("Test set overlay to closed", () => {
    const dialog = document.createElement("dialog");
    dialog.showModal = function () { this.open = true; }
    dialog.close = function () { this.open = false; }
    dialog.open = true;
    dialog.id = "overlay";

    document.body.append(dialog)

    expect(dialog.open).toBe(true);
    setOverlayState("overlay", false);
    expect(dialog.open).toBe(false);
    setOverlayState("overlay", false);
    expect(dialog.open).toBe(false);
});