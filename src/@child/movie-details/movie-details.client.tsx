import { useCurrentPage } from "../../@shared/page";

export function MoveDetailsPage() {
  const currentPage = useCurrentPage();

  return (
    <div>
      <h1 className="text-3xl font-bold underline">Movie details</h1>;
      <button onClick={() => currentPage.push({ t: "feed" })}>go feed</button>
    </div>
  );
}
