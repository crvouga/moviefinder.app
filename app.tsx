import * as elements from "typed-html";

export async function handler(request: Request) {
  const url = new URL(request.url);

  const res = new Response(<Home></Home>);
  res.headers.set("Content-Type", "text/html");
  return res;
}

function Home() {
  return (
    <div class="w-full h-full flex items-center justify-center bg-neutral-900 text-white">
      Home
    </div>
  );
}
