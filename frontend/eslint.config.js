import eslint from "@eslint/js";
import prettier from "eslint-config-prettier";
import { defineConfig } from "eslint/config";
import svelte from "eslint-plugin-svelte";
import globals from "globals";
import tseslint from "typescript-eslint";

import svelteConfig from "./svelte.config.js";

export default defineConfig(
    eslint.configs.recommended,
    tseslint.configs.recommended,
    svelte.configs.recommended,
    {
        files: ["src/**/*.{ts,svelte}"],
        languageOptions: {
            globals: globals.browser,
        },
    },
    {
        files: ["*.{js,ts}"],
        languageOptions: {
            globals: globals.node,
        },
    },
    {
        files: ["**/*.svelte", "**/*.svelte.js", "**/*.svelte.ts"],
        languageOptions: {
            parserOptions: {
                projectService: true,
                extraFileExtensions: [".svelte"],
                parser: tseslint.parser,
                svelteConfig,
            },
        },
    },
    {
        rules: {
            // The TypeScript-aware extension below replaces the core rule.
            "no-unused-vars": "off",
            "@typescript-eslint/no-unused-vars": [
                "error",
                {
                    args: "all",
                    argsIgnorePattern: "^_",
                    caughtErrors: "all",
                    caughtErrorsIgnorePattern: "^_",
                    destructuredArrayIgnorePattern: "^_",
                    varsIgnorePattern: "^_",
                    ignoreRestSiblings: true,
                    enableAutofixRemoval: {
                        imports: true,
                    },
                },
            ],
        },
    },
    {
        files: ["**/*.svelte"],
        rules: {
            // Assignments to bindable/state values can be observed by templates or parents even
            // when generic control-flow analysis cannot see a subsequent script-level read.
            "no-useless-assignment": "off",
        },
    },
    svelte.configs.prettier,
    prettier,
);
