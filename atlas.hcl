env "app" {
  url = getenv("DATABASE_URL")
  dev = "docker://postgres/17.6-alpine/dev"
  src = "file://atlas/schema.sql"
  migration {
    dir = "file://atlas/migrations"
    revisions_schema = "moviefinder"
  }
  schemas = ["moviefinder"]
  exclude = ["moviefinder.atlas_schema_revisions"]
}
