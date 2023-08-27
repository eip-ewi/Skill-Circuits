/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Toggles the visibility of the copy edition form which displays the select element.
 */
function toggleEditionFormVisibility(): void {
    const copyEditionForm: JQuery = $("#copy-edition-form");
    const copyEdition: JQuery = $("#copy-edition");

    if (copyEditionForm.hasClass("hidden")) {
        copyEditionForm.removeClass("hidden");
        copyEdition.addClass("hidden");
    } else {
        copyEdition.removeClass("hidden");
        copyEditionForm.addClass("hidden");
    }
}

/**
 * Handle the submission of the copy edition form (selecting which edition to copy).
 *
 * @param event The form submission event.
 */
function handleCopyEditionFormSubmission(event: Event): void {
    event.preventDefault();
    $("#copy-edition-from").text($("#copy-edition-select").children(":selected").text());
    toggleOverlay("copy-confirmation-overlay");
}

/**
 * Handle the submission of the confirmation pop up form for copying editions.
 *
 * @param event The form submission event.
 */
function handleCopyEditionConfirmation(event: Event): void {
    event.preventDefault();

    const editionTo: number = $("#copy-checkpoint-id").val() as number;
    const editionFrom: number = $("#copy-edition-select").val() as number;
    $.ajax({
        type: "post",
        url: `/edition/${editionTo}/copy/${editionFrom}`,
        processData: false,
        contentType: "application/json",
        success: () => {
            location.reload();
        },
        error: () => {
            toggleOverlay("copy-confirmation-overlay");
        },
    });
}

/**
 * Adds the event listeners to elements for the copying of editions.
 */
function editionCopyEventListeners(): void {
    $("#copy-edition-form").on("submit", handleCopyEditionFormSubmission);
    $("#copy-confirmation-form").on("submit", handleCopyEditionConfirmation);
}

if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.toggleEditionFormVisibility = toggleEditionFormVisibility;
    module.exports.handleCopyEditionFormSubmission = handleCopyEditionFormSubmission;
    module.exports.handleCopyEditionConfirmation = handleCopyEditionConfirmation;
} else {
    // For the browser view, add the event listeners
    document.addEventListener("DOMContentLoaded", function () {
        editionCopyEventListeners();
    });
}
