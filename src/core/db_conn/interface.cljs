(ns core.db-conn.interface)

(defmulti new!
  "Create a new db connection"
  :sql/impl)

(defmulti query-chan!
  "Query the db and return the result
   :sql/impl is the implementation to use
   :sql/sql is the sql to execute
   Returns a channel containing the result 
   :sql/rows is the rows returned by the query
   :sql/columns is the columns returned by the query
   "
  :sql/impl)