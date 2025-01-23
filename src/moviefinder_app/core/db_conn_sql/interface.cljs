(ns moviefinder-app.core.db-conn-sql.interface)

(defmulti new
  "Create a new db connection"
  :db-conn-sql/impl)

(defmulti query
  "Query the db and return the result"
  :db-conn-sql/impl)

(defmulti watch
  "Watch the db and return a channel"
  :db-conn-sql/impl)