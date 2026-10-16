import { fieldText } from "../../shared/contracts";
import { useEffect, useState } from "react";
import { Link } from "@tanstack/react-router";
import {
  Film,
  Heart,
  Eye,
  Meh,
  ThumbsDown,
  Plus,
  Check,
  Bookmark,
  Star,
  ArrowUpRight,
} from "lucide-react";
import type { Entity } from "../../shared/contracts";
import { useEntities, send, report, authClient } from "../state";
import { Button } from "./ui/button";
import { Sheet } from "./ui/dialog";
export const imageUrl = (path: string | null | undefined, size = "w500") =>
  path
    ? path.startsWith("https://")
      ? path
      : `https://image.tmdb.org/t/p/${size}${path}`
    : undefined;
export const titleHref = (id: string) => `/title/${encodeURIComponent(id)}`;
export function Poster({
  entity,
  priority = false,
  className = "",
}: {
  entity?: Entity;
  priority?: boolean;
  className?: string;
}) {
  const [failed, setFailed] = useState(false);
  useEffect(() => setFailed(false), [entity?.poster]);
  return (
    <div className={`poster ${className}`}>
      {entity?.poster && !failed ? (
        <img
          src={imageUrl(entity.poster)}
          srcSet={
            entity.poster.startsWith("https://")
              ? undefined
              : `${imageUrl(entity.poster, "w342")} 342w, ${imageUrl(entity.poster, "w500")} 500w, ${imageUrl(entity.poster, "w780")} 780w`
          }
          sizes="(max-width: 600px) 90vw, 420px"
          alt={`${entity.title} poster`}
          loading={priority ? "eager" : "lazy"}
          fetchPriority={priority ? "high" : "auto"}
          decoding="async"
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="poster-fallback">
          <Film size={44} />
          <span>{entity?.title ?? "Your next great watch"}</span>
        </div>
      )}
    </div>
  );
}
export function TitleCard({ entity }: { entity: Entity }) {
  const userId = authClient.useSession().data?.user.id,
    statuses = useEntities("status"),
    status = statuses.find((s) => s.data.userId === userId && s.data.titleId === entity.id);
  return (
    <Link to="/title/$id" params={{ id: entity.id }} className="title-card">
      <Poster entity={entity} />
      {(status?.data.seen || status?.data.sentiment === "liked") && (
        <span className="card-status">
          {status?.data.seen === true && <Eye size={12} aria-label="Seen" />}
          {status?.data.sentiment === "liked" && <Heart size={12} aria-label="Liked" />}
        </span>
      )}
      <h3>{entity.title}</h3>
      <p>
        {entity.year ?? "—"} <span>·</span>{" "}
        {entity.kind === "tv" ? "Series" : entity.kind === "person" ? "Person" : "Film"}
        {!!entity.rating && (
          <>
            <Star size={11} />
            {entity.rating.toFixed(1)}
          </>
        )}
      </p>
    </Link>
  );
}
export function Empty({
  title,
  children,
  icon = "✦",
}: {
  title: string;
  children: React.ReactNode;
  icon?: string;
}) {
  return (
    <div className="empty">
      <span className="empty-icon">{icon}</span>
      <h2>{title}</h2>
      <p>{children}</p>
    </div>
  );
}
export function Tags({
  entity,
  userId,
  compact = false,
}: {
  entity: Entity;
  userId: string;
  compact?: boolean;
}) {
  const statuses = useEntities("status"),
    status = statuses.find((s) => s.data.userId === userId && s.data.titleId === entity.id),
    [add, setAdd] = useState(false);
  const selected = fieldText(status?.data.sentiment ?? "unrated");
  const toggle = (value: "liked" | "meh" | "disliked") =>
    void send(
      { type: "status", titleId: entity.id, sentiment: selected === value ? "unrated" : value },
      userId,
    ).catch(report);
  return (
    <>
      <div className={`tag-controls ${compact ? "compact" : ""}`}>
        <button
          aria-label="Mark as seen"
          aria-pressed={!!status?.data.seen}
          className={status?.data.seen ? "selected" : ""}
          onClick={() =>
            void send(
              { type: "status", titleId: entity.id, seen: !status?.data.seen },
              userId,
            ).catch(report)
          }
        >
          <Eye />
          <span>Seen</span>
        </button>
        <button
          aria-label="Like"
          aria-pressed={selected === "liked"}
          className={selected === "liked" ? "selected liked" : ""}
          onClick={() => toggle("liked")}
        >
          <Heart />
          <span>Love it</span>
        </button>
        <button
          aria-label="Meh"
          aria-pressed={selected === "meh"}
          className={selected === "meh" ? "selected" : ""}
          onClick={() => toggle("meh")}
        >
          <Meh />
          <span>Meh</span>
        </button>
        <button
          aria-label="Dislike"
          aria-pressed={selected === "disliked"}
          className={selected === "disliked" ? "selected" : ""}
          onClick={() => toggle("disliked")}
        >
          <ThumbsDown />
          <span>Not for me</span>
        </button>
        <button aria-label="Add to list" onClick={() => setAdd(true)}>
          <Plus />
          <span>Add to list</span>
        </button>
      </div>
      <AddToList entity={entity} userId={userId} open={add} setOpen={setAdd} />
    </>
  );
}
export function AddToList({
  entity,
  userId,
  open,
  setOpen,
}: {
  entity: Entity;
  userId: string;
  open: boolean;
  setOpen: (v: boolean) => void;
}) {
  const lists = useEntities("list"),
    members = useEntities("collaborator"),
    items = useEntities("item");
  const editable = lists.filter((l) =>
    members.some((m) => m.data.listId === l.id && m.data.userId === userId),
  );
  return (
    <Sheet
      open={open}
      onOpenChange={setOpen}
      title="Save for a good night"
      description={entity.title}
    >
      <div className="sheet-body list-picker">
        {editable.map((list) => {
          const exists = items.some(
            (i) => i.data.listId === list.id && i.data.titleId === entity.id,
          );
          return (
            <button
              key={list.id}
              disabled={exists}
              onClick={() =>
                void send({ type: "list.add", listId: list.id, titleId: entity.id }, userId)
                  .then(() => setOpen(false))
                  .catch(report)
              }
            >
              <span className="list-icon">
                <Bookmark />
              </span>
              <span>
                {list.title}
                <small>{items.filter((i) => i.data.listId === list.id).length} titles</small>
              </span>
              {exists ? <Check /> : <Plus />}
            </button>
          );
        })}
        <Button
          variant="outline"
          onClick={() => {
            const title = window.prompt("Name your new list");
            if (title)
              void send({ type: "list.create", title, description: "" }, userId)
                .then(async (result) => {
                  const list = result.entities.find((e) => e.kind === "list");
                  if (list)
                    await send({ type: "list.add", listId: list.id, titleId: entity.id }, userId);
                  setOpen(false);
                })
                .catch(report);
          }}
        >
          <Plus size={16} />
          Create a list
        </Button>
      </div>
    </Sheet>
  );
}
export function PageHeading({
  eyebrow,
  title,
  children,
}: {
  eyebrow: string;
  title: string;
  children?: React.ReactNode;
}) {
  return (
    <header className="page-heading">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h1>{title}</h1>
      </div>
      {children}
    </header>
  );
}
export function External({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <a href={href} target="_blank" rel="noreferrer" className="external-link">
      {children}
      <ArrowUpRight size={15} />
    </a>
  );
}
