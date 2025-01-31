# Integration Tests - Test Overview

This file provides an overview of the integration tests. **If you add a test, please add it to this file for future reference following the formatting example.**

## Integration Tests

### ``ClassName`` (Formatting Example)
Description of what kind of tests the class contains.

#### ``methodName`` (Formatting Example)
Description of what the test does step by step and what the intended results are.

### ``HomePageTest``
This class contains all homepage related tests.

#### ``testLogInLogOut``
This test asserts on logging in and logging out behavior. It goes through the following steps:
1. Go to the login page via the button
2. Log in as a teacher
3. If necessary, close the changelog box
4. Check that the homepage was loaded
5. Log out again via user dropdown
6. Check that the homepage was loaded

#### ``testPublishUnpublishEdition``
This tests publishes and unpublishes an edition. The order of (un)publishing depends on whether the edition is in an (un)published state beforehand. It goes through the following steps:
1. Log in as teacher
2. Unpublish all other editions than the one considered in the test
3. Check if the edition is published or not by checking the edition setup pane
4. Log out
5. Then, depending on the publishing status of the edition:
   - If published:
     1. Assert that the edition is visible
     2. Log in as teacher
     3. Unpublish the edition
     4. Log out
     5. Assert that the edition is not visible
     6. Log in as teacher
     7. Publish the edition
     8. Log out
     9. Assert that the edition is visible
   - If not published:
       1. Assert that the edition is not visible
       2. Log in as teacher
       3. Publish the edition
       4. Log out
       5. Assert that the edition is visible
       6. Log in as teacher
       7. Unpublish the edition
       8. Log out
       9. Assert that the edition is not visible

### ``ItemCreationsTest``
This class contains all tests that assert on the creation of some item (e.g., module, submodule, skill).

#### ``testCreateModule``
This test creates a new module. It goes through the following steps:
1. Log in as teacher
2. Create a module in a preconfigured edition
3. Assert that the module is visible in the edition setup pane
4. Close edition setup pane
5. Open student view
6. Assert that the module is visible