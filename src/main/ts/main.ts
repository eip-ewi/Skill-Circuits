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

function toggleOverlay(id) {
    let overlay = document.getElementById(id) as HTMLDialogElement;
    if (overlay.open) {
        overlay.close();
    } else {
        overlay.showModal();
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
    }
}

function setOverlayState(id: string, open: boolean): void {
    let overlay = document.getElementById(id) as HTMLDialogElement;
    if (overlay.open && !open) {
        overlay.close();
    } else if (!overlay.open && open) {
        overlay.showModal();
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
    }
}

if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.toggleOverlay = toggleOverlay;
    module.exports.setOverlayState = setOverlayState;
}
