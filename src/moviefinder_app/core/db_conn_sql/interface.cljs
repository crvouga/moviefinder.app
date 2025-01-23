(ns moviefinder-app.core.db-conn-sql.interface)

(defn- dispatch [i] (-> i :db-conn-sql/impl (or :db-conn-sql-impl/pglite)))

(defmulti new
  "Create a new db connection"
  dispatch)

(defmulti query
  "Query the db and return the result"
  dispatch)

(defmulti watch
  "Watch the db and return a channel"
  dispatch)