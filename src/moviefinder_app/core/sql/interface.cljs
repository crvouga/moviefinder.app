(ns moviefinder-app.core.sql.interface)

(defmulti new
  "Create a new db connection"
  :sql/impl)

(defmulti query
  "Query the db and return the result"
  :sql/impl)

(defmulti watch
  "Watch the db and return a channel"
  :sql/impl)