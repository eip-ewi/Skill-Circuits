import { mount } from "svelte";
import "./style/main.css";
import App from "./App.svelte";
import { installSessionFetchHandler } from "./lib/logic/session";

//All fetches in the app should check if the session is still valid
installSessionFetchHandler();

const app = mount(App, {
    target: document.getElementById("app")!,
});

export default app;
