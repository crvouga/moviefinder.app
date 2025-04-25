(ns core.backend.db-conn.interface)

(defmulti new!
  "Create a new db connection"
  :db-conn/impl)

(defmulti query-chan!
  "Query the db and return the result.
   
   Required parameters:
   - `:db-conn/impl` - the implementation to use
   - `:db-conn/query` - the SQL query to execute
   
   Optional parameters:
   - `:db-conn/params` - parameters for the SQL query
   
   Returns a channel containing a map with:
   - `:db-conn/rows` - the rows returned by the query
   - `:db-conn/columns` - the columns returned by the query
   - `:db-conn/error` - error message if query failed"
  :db-conn/impl)

(defmethod new! :default [config]
  (let [config-new (merge config {:db-conn/impl :db-conn-impl/better-sqlite3})]
    (new! config-new)))

(defmethod query-chan! :default [conn query params]
  (let [conn-new (merge conn {:db-conn/impl :db-conn-impl/better-sqlite3})]
    (query-chan! conn-new query params)))