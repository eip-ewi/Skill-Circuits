// @ts-ignore
import {toggleOverlay} from "../../main/ts/main.ts";

// @ts-ignore
import {openLinkOverlay, linkOverviewEventListeners} from "../../main/ts/linkOverview.ts";

test("Test open link overlay", () => {
    document.body.innerHTML = "<dialog id='link-overview-overlay'>" +
        "<form>" +
        "    <select id='type-filter'>" +
        "        <option value='All types' id='option-a'>All types</option>" +
        "        <option value='Other' id='option-b' selected>Other</option>" +
        "    </select>" +
        "</form>" +
        "<form>" +
        "    <input id='link-searchbar' value='Some input'/>" +
        "</form></dialog>";
    linkOverviewEventListeners();

    const dialog: JQuery = $("#link-overview-overlay");
    const dialogHTML: HTMLDialogElement = dialog[0] as HTMLDialogElement;
    dialogHTML.showModal = function () { this.open = true; }
    dialogHTML.close = function () { this.open = false; }
    expect(dialog.attr("open")).toBe(undefined);

    openLinkOverlay(toggleOverlay);
    expect(dialog.attr("open")).toBeDefined();
    expect($("#type-filter").val()).toBe("All types");
    expect($("#option-b").attr("selected")).toBeUndefined();
    expect($("#option-a").attr("selected")).toBeDefined();
    expect($("#link-searchbar").val()).toBe("");
});