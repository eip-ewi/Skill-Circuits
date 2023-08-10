// @ts-ignore
import {toggleOverlay} from "../../main/ts/main.ts";

import {openLinkOverlay, linkOverviewEventListeners, setVisibilityModuleInfos, createMsg, submitForm}
// @ts-ignore
    from "../../main/ts/linkOverview.ts";

// Load the HTML of the body for the tests
const fs = require("fs");
const path = require("path");
const htmlPath = path.join(path.join(__dirname, "html"), "link-overview.html");
const defaultInnerHTML = fs.readFileSync(htmlPath, "utf-8");

/**
 * Set up the dialog, and configure its functions.
 */
const setupDialog = function (): void {
    const dialog: JQuery = $("#link-overview-overlay");
    const dialogHTML: HTMLDialogElement = dialog[0] as HTMLDialogElement;
    dialogHTML.showModal = function (): void { this.open = true; }
    dialogHTML.close = function (): void { this.open = false; }
}

/**
 * Check that the correct option is selected, and that the remaining options are not selected.
 *
 * @param selected The value of the option that should be selected.
 */
const checkOptionCorrectlySelected = function (selected: string): void {
    $("#type-filter").children().each(function (): void {
        if ($(this).val() === selected) {
            expect($(this).attr("selected")).toBeDefined();
        } else {
            expect($(this).attr("selected")).toBeUndefined();
        }
    });
}

/**
 * Check the default values of the filter elements, and that all links/module boxes are visible.
 */
const checkDefaultFilterValues = function(): void {
    expect($("#type-filter").val()).toBe("All types");
    checkOptionCorrectlySelected("All types");
    expect($("#link-searchbar").val()).toBe("");

    expect($(".link_row").attr("hidden")).toBeUndefined();
    expect($(".wrapper").attr("hidden")).toBeUndefined();
}

/**
 * Check that a message pop-up was created, and is removed.
 *
 * @param message       The message.
 * @param classes       The icon class(es).
 */
const checkMessagePopUp = function(message: string, classes: string): void {
    const alert: JQuery = $("#link-form0").children(".link__alert");
    expect(alert.length).toBe(1);
    expect(alert.text()).toBe(message);
    const icon: JQuery = alert.first().children("i");
    expect(icon.length).toBe(1);
    expect(icon.attr("class")).toBe(classes);

    // Wait for the message to be removed, and assert that it does not exist anymore after timeout ms
    setTimeout(() => {
        expect($(".link__alert").length).toBe(0);
    }, 1500);
    jest.runAllTimers();
}

beforeEach(() => {
    document.body.innerHTML = defaultInnerHTML;
    linkOverviewEventListeners();
    setupDialog();
    openLinkOverlay(toggleOverlay);
});

test("Test open link overlay", () => {
    // Before each test the dialog is set up and opened, assert that the overlay is open
    expect($("#link-overview-overlay").attr("open")).toBeDefined();
});

test("Test search by substring filter", () => {
    checkDefaultFilterValues();

    // Set searchbar value to "test" and trigger input event
    const searchBar: JQuery = $("#link-searchbar");
    searchBar.val("test");
    searchBar.trigger("input");

    // Check tasks 0 and 1 visible, 2 not visible
    expect($("#link-row0").attr("hidden")).toBeUndefined();
    expect($("#link-row1").attr("hidden")).toBeUndefined();
    expect($("#link-row2").attr("hidden")).toBeDefined();

    // Check module 0 visible, module 1 not visible
    expect($("#wrapper0").attr("hidden")).toBeUndefined();
    expect($("#wrapper1").attr("hidden")).toBeDefined();
});

test("Test search by type filter", () => {
    checkDefaultFilterValues();

    // Set type filter value to "fa-pencil-ruler" and trigger change event
    const typeFilter: JQuery = $("#type-filter");
    typeFilter.val("fa-pencil-ruler");
    typeFilter.trigger("change");
    checkOptionCorrectlySelected("fa-pencil-ruler");

    // Check tasks 1 and 2 visible, 0 not visible
    const row0: JQuery = $("#link-row0");
    const row1: JQuery = $("#link-row1");
    const row2: JQuery = $("#link-row2");
    expect(row0.attr("hidden")).toBeDefined();
    expect(row1.attr("hidden")).toBeUndefined();
    expect(row2.attr("hidden")).toBeUndefined();

    // Check both module boxes visible
    expect($(".wrapper").attr("hidden")).toBeUndefined();

    // Set type filter value to "fa-book" and trigger change event
    typeFilter.val("fa-book");
    typeFilter.trigger("change");
    checkOptionCorrectlySelected("fa-book");

    // Check tasks 1 and 2 not visible, 0 visible
    expect(row0.attr("hidden")).toBeUndefined();
    expect(row1.attr("hidden")).toBeDefined();
    expect(row2.attr("hidden")).toBeDefined();

    // Check module 0 visible, module 1 not visible
    expect($("#wrapper0").attr("hidden")).toBeUndefined();
    expect($("#wrapper1").attr("hidden")).toBeDefined();
});

test("Test search by type filter and substring", () => {
    checkDefaultFilterValues();

    // Set type filter value to "fa-pencil-ruler", searchbar value to "test" and trigger events
    const typeFilter: JQuery = $("#type-filter");
    typeFilter.val("fa-pencil-ruler");
    typeFilter.trigger("change");
    checkOptionCorrectlySelected("fa-pencil-ruler");
    const searchBar: JQuery = $("#link-searchbar");
    searchBar.val("test");
    searchBar.trigger("input");

    // Check task 1 visible, 0 and 2 not visible (0 filtered out by type, 2 filtered out by substring)
    const row0: JQuery = $("#link-row0");
    const row1: JQuery = $("#link-row1");
    const row2: JQuery = $("#link-row2");
    expect(row0.attr("hidden")).toBeDefined();
    expect(row1.attr("hidden")).toBeUndefined();
    expect(row2.attr("hidden")).toBeDefined();

    // Check module 0 visible, module 1 not visible
    const wrapper0: JQuery = $("#wrapper0");
    const wrapper1: JQuery = $("#wrapper1");
    expect(wrapper0.attr("hidden")).toBeUndefined();
    expect(wrapper1.attr("hidden")).toBeDefined();

    // Set searchbar value to "different-url" and trigger event
    searchBar.val("different-url");
    searchBar.trigger("input");

    // Check tasks 0 and 1 not visible (0 filtered by both, 1 filtered by substring), 2 visible
    expect(row0.attr("hidden")).toBeDefined();
    expect(row1.attr("hidden")).toBeDefined();
    expect(row2.attr("hidden")).toBeUndefined();

    // Check module 0 not visible, module 1 visible
    expect(wrapper0.attr("hidden")).toBeDefined();
    expect(wrapper1.attr("hidden")).toBeUndefined();
});

test("Test visibility module boxes", () => {
    // Call setVisibilityModuleInfos, and check that both module boxes are visible
    setVisibilityModuleInfos();
    const wrapper0: JQuery = $("#wrapper0");
    const wrapper1: JQuery = $("#wrapper1");
    expect(wrapper0.attr("hidden")).toBeUndefined();
    expect(wrapper1.attr("hidden")).toBeUndefined();

    // Set one row in module 0 and one row in module 1 to be hidden
    // This should result in module 0 being visible, and module 1 being hidden
    $("#link-row0").attr("hidden", "");
    $("#link-row2").attr("hidden", "");
    setVisibilityModuleInfos();
    expect(wrapper0.attr("hidden")).toBeUndefined();
    expect(wrapper1.attr("hidden")).toBeDefined();
});

test("Test create message",  () => {
    jest.useFakeTimers();

    const fromElement: JQuery = $("#link-form0");
    createMsg(fromElement, "message", "icon-class", 2);
    const alert: JQuery = fromElement.children(".link__alert");
    expect(alert.length).toBe(1);
    expect(alert.text()).toBe("message");
    const icon: JQuery = alert.first().children("i");
    expect(icon.length).toBe(1);
    expect(icon.hasClass("icon-class")).toBe(true);

    // Check that the message is still visible after 1ms
    setTimeout(() => {
        expect(alert.first().children("i").length).toBe(1);
        expect(alert.first().children("i").hasClass("icon-class")).toBe(true);
    }, 1);
    // Wait for the message to be removed, and assert that it does not exist anymore after 2ms
    setTimeout(() => {
        expect($(".link__alert").length).toBe(0);
    }, 2);
    jest.runAllTimers();
});

test("Test successful link deletion",  () => {
    // Set up ajax to call the success function
    $.ajax = jest.fn().mockImplementation((params) => {
        params.success();
    });

    expect($(".link_row").length).toBe(3);
    expect($("#link-row0").length).toBe(1);

    // Click button and assert that the row was removed
    $("#delete-0").trigger("click");
    expect($("#link-row0").length).toBe(0);
    expect($(".link_row").length).toBe(2);
});

test("Test failed link deletion",  () => {
    // Set up timers for the message pop up
    jest.useFakeTimers();
    // Set up ajax to call the error function
    $.ajax = jest.fn().mockImplementation((params) => {
        params.error();
    });

    expect($(".link_row").length).toBe(3);
    expect($("#link-row0").length).toBe(1);

    // Click button and assert that the row was *not* removed
    $("#delete-0").trigger("click");
    expect($(".link_row").length).toBe(3);
    expect($("#link-row0").length).toBe(1);
    expect($("#prev-link0").val()).toBe("https://test0.com");
    expect($("#link0").val()).toBe("https://test0.com");

    // Assert that the error pop up was created
    checkMessagePopUp("Error", "fa-sharp fa-regular fa-circle-xmark");
});

test("Test successful link change",  () => {
    // Set up timers for the message pop up
    jest.useFakeTimers();
    // Set up ajax to call the success function
    $.ajax = jest.fn().mockImplementation((params) => {
        params.success();
    });
    // Add the onblur function to the input element
    const linkInput: JQuery = $("#link0");
    linkInput.on("blur", () => submitForm("0"));

    // Change the value and trigger blur event
    linkInput.val("https://www.new-link.com");
    linkInput.trigger("blur");

    // Assert that the value of the input stayed changed, and the previous value input changed
    expect(linkInput.val()).toBe("https://www.new-link.com");
    expect($("#prev-link0").val()).toBe("https://www.new-link.com");

    // Assert that the success pop up was created
    checkMessagePopUp("Success", "fa-regular fa-circle-check");
});

test("Test failed link change",  () => {
    // Set up timers for the message pop up
    jest.useFakeTimers();
    // Set up ajax to call the error function
    $.ajax = jest.fn().mockImplementation((params) => {
        params.error();
    });
    // Add the onblur function to the input element
    const linkInput: JQuery = $("#link0");
    linkInput.on("blur", () => submitForm("0"));

    // Change the value and trigger blur event
    linkInput.val("https://www.new-link.com");
    linkInput.trigger("blur");

    // Assert that the value of the input, and the previous value input are the initial value again
    expect(linkInput.val()).toBe("https://test0.com");
    expect($("#prev-link0").val()).toBe("https://test0.com");

    // Assert that the error pop up was created
    checkMessagePopUp("Error", "fa-sharp fa-regular fa-circle-xmark");
});

test("Test malformed link change attempt",  () => {
    // Set up ajax
    $.ajax = jest.fn().mockImplementation(() => {});
    // Add the onblur function to the input element
    const linkInput: JQuery = $("#link0");
    linkInput.on("blur", () => submitForm("0"));

    // Change the value and trigger blur event
    linkInput.val("not a link");
    linkInput.trigger("blur");

    // Assert that the value of the input stayed changed, and the previous value input is the initial value
    expect(linkInput.val()).toBe("not a link");
    expect($("#prev-link0").val()).toBe("https://test0.com");

    // Assert that no pop up was created, and no request was sent via ajax
    expect($(".link__alert").length).toBe(0);
    expect($.ajax).not.toHaveBeenCalled();
});