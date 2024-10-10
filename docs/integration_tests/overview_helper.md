# Integration Tests - Helper Method Overview

This file provides an overview of the helper methods in the ``IntegrationTest`` parent class. **If you add a helper method, please add it to this file for future reference following the formatting example.**

The description here can be close to the JavaDoc, but assumptions/navigations of the method should be explicitly stated.

## Helper Methods

###  ``(Formatting Example)``
- **Navigates to page**: If the method results in a page navigation, please add the page here.
- **Result**: Other results/actions of the method.
- **Assumptions**: E.g., whether the method assumes a certain page to be loaded, or a certain user to be logged in.

### ``navigateTo``
- **Navigates to page**: Navigates to a specified path prepending the configured base url (e.g., http://localhost:8084/ in dev).
- **Result**: -
- **Assumptions**: -

### ``clickAndWaitForPageLoad``
- **Navigates to page**: -
- **Result**: Clicks a given locator and waits until the DOM is loaded.
- **Assumptions**: -

###  ``getActiveEdition``
- **Navigates to page**: -
- **Result**: Returns an active edition of a given course, using the pre-configured data structures in the class.
- **Assumptions**: -

###  ``logInAs``
- **Navigates to page**: Homepage
- **Result**: Logs in as a given user. Waits until the page is loaded again.
- **Assumptions**: -

### ``logOutAs``
- **Navigates to page**: Homepage
- **Result**: Logs out as a given user. Waits until the page is loaded again.
- **Assumptions**: Logged in as the given user.

### ``closeChangelogBoxIfOpen``
- **Navigates to page**: -
- **Result**: Checks if the changelog box is open, and if it is, closes it.
- **Assumptions**: -

### ``ifCourseHiddenSwitchToOtherHomepageTab``
- **Navigates to page**: Homepage
- **Result**: If a given course is not visible in the current homepage tab, it navigates to the other tab. This method does not guarantee that the course is then visible in that (second) tab.
- **Assumptions**: -

### ``navigateToEditionSetup``
- **Navigates to page**: First opens the homepage, then further navigates to a given edition of a given course.
- **Result**: Opens the edition setup pane for that edition.
- **Assumptions**: Logged in as teacher for that edition.

### ``openEditionSetupOnCurrentPage``
- **Navigates to page**: -
- **Result**: Opens the edition setup on the currently loaded page.
- **Assumptions**: An edition page is loaded, and the user is logged in as teacher for that edition.

### ``setEditionState``
- **Navigates to page**: First opens the homepage, then further navigates to a given edition of a given course.
- **Result**: Given a desired end state, sets the publishing state of a given edition to be published/unpublished.
- **Assumptions**: Logged in as teacher for that edition.

### ``togglePublishUnpublish``
- **Navigates to page**: First opens the homepage, then further navigates to a given edition of a given course.
- **Result**: Toggles the publishing state of a given edition to be published/unpublished.
- **Assumptions**: Logged in as teacher for that edition.

### ``createModule``
- **Navigates to page**: First opens the homepage, then further navigates to a given edition of a given course.
- **Result**: Creates a new module in a given edition. The name will be a random UUID.
- **Assumptions**: Logged in as teacher for that edition.