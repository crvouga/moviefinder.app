import * as elements from "typed-html";

function Home() {
  return (
    <div class="w-full h-full flex items-center justify-center bg-neutral-900 text-white">
      Home
      <a
        class="underline"
        href="/about"
        data-hx-get="/about"
        data-hx-swap="outerHTML"
        data-hx-target="#root"
        onclick="changePath('/about'); return false;">
        About
      </a>
    </div>
  );
}

function About() {
  return (
    <div class="w-full h-full flex items-center justify-center bg-neutral-900 text-white">
      About
      <a
        class="underline"
        href="/home"
        data-hx-get="/home"
        data-hx-swap="outerHTML"
        data-hx-target="#root"
        onclick="changePath('/home'); return false;">
        Home
      </a>
    </div>
  );
}

function Page({ req }: { req: Request }) {
  const pathname = new URL(req.url).pathname;

  if (pathname.startsWith("/home")) {
    return <Home></Home>;
  }

  if (pathname.startsWith("/about")) {
    return <About></About>;
  }

  return <div>404</div>;
}

function Html({ children }: elements.Attributes) {
  return (
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <meta
          name="viewport"
          content="width=device-width, initial-scale=1.0"></meta>
        <script src="https://cdn.tailwindcss.com"></script>
        <script
          src="https://unpkg.com/htmx.org@1.9.6"
          integrity="sha384-FhXw7b6AlE/jyjlZH5iHa/tTe9EpJ1Y55RjcgPbjeWMskSxZt1v9qkxLJWNJaGni"
          crossorigin="anonymous"></script>
      </head>
      <body>
        <div id="root">{children}</div>
      </body>
    </html>
  );
}

function handler(req: Request): Response {
  const isFullPageRequest = req.headers.get("HX-Request") !== "true";

  const content = <Page req={req} />;

  let root = isFullPageRequest ? <Html>{content}</Html> : content;

  const res = new Response(root);
  res.headers.set("Content-Type", "text/html");
  return res;
}

const server = Bun.serve({
  port: 3000,
  fetch: handler,
});

console.log(`Listening on localhost: ${server.port}`);
