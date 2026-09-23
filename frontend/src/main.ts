import { mount } from "svelte";
import "./style/main.css";
import App from "./App.svelte";
import { closeReloginWindowWhenLoggedIn, installSessionFetchHandler } from "./lib/logic/session";

//All fetches in the app should check if the session is still valid
installSessionFetchHandler();

//The login pop-up page should auto close
void closeReloginWindowWhenLoggedIn();

const app = mount(App, {
    target: document.getElementById("app")!,
});

export default app;
