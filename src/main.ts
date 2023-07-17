import "./app.css";
import App from "./App.svelte";
import { register } from "swiper/element/bundle";
register();

const app = new App({
  target: document.getElementById("app"),
});

export default app;
