-- Create "sync_checkpoint" table
CREATE TABLE "moviefinder"."sync_checkpoint" (
  "id" text NOT NULL,
  "payload" jsonb NOT NULL,
  "updated_at" timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY ("id")
);
