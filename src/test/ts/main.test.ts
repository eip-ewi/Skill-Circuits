// @ts-ignore
import {toggleOverlay, setOverlayState} from "../../main/ts/main.ts";

const dialog: HTMLDialogElement = document.createElement("dialog");

beforeAll(() => {
    dialog.showModal = function () { this.open = true; }
    dialog.close = function () { this.open = false; }
    dialog.id = "overlay";
    document.body.append(dialog)
});

test("Test toggle overlay", () => {
    dialog.open = false;
    expect(dialog.open).toBe(false);

    // False to true
    toggleOverlay("overlay");
    expect(dialog.open).toBe(true);

    // True to false
    toggleOverlay("overlay");
    expect(dialog.open).toBe(false);
});

test("Test set overlay to opened", () => {
    dialog.open = false;
    expect(dialog.open).toBe(false);

    // False to true
    setOverlayState("overlay", true);
    expect(dialog.open).toBe(true);

    // True to true
    setOverlayState("overlay", true);
    expect(dialog.open).toBe(true);
});

test("Test set overlay to closed", () => {
    dialog.open = true;
    expect(dialog.open).toBe(true);

    // True to false
    setOverlayState("overlay", false);
    expect(dialog.open).toBe(false);

    // False to false
    setOverlayState("overlay", false);
    expect(dialog.open).toBe(false);
});