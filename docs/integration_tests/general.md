# Integration Tests - General Information

This file provides some general guidelines and notes on creating the integration tests.

## State of the database
The integration tests should work independently of the state of the database. This means that if, for example, a skill is needed to test a behavior, that skill (and module, and submodule) first need to be created in the integration test as well.

## Dependency on LabraCore database
Currently, there are pre-configured data structures in the ``IntegrationTest`` class denoting the courses and editions. This should be changed in the future.

## Waiting for pages to be loaded
If a locator results in a page to (re-)load, the helper method ``clickAndWaitForPageLoad`` should be used, instead of a simple locator click. The method waits until the DOM is loaded. Otherwise, it is not guaranteed that the page is fully loaded before the next action, which can result in flaky tests.

## Making the browser visible for local testing/debugging
By default, the ``launchBrowser`` method in the ``IntegrationTest`` class does not visibly open the browser. For debugging integration tests, it is often useful to see the behavior. In this case, the line defining the browser can be changed to ``browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(150))`` (150 being the milliseconds between actions). However, the pushed version should remain ``browser = playwright.chromium().launch()`` since it is part of the pipeline.