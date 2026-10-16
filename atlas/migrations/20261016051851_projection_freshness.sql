-- Modify "source_entity" table
ALTER TABLE "moviefinder"."source_entity" ADD COLUMN "detail_fetched_at" timestamptz NULL;
