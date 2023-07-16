import "./app.css";
import App from "./App.svelte";
// import function to register Swiper custom elements
import { register } from "swiper/element/bundle";
// register Swiper custom elements
register();

const app = new App({
  target: document.getElementById("app"),
});

export default app;
