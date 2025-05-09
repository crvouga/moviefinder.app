(ns lib.db.inter)

(defmulti init
  "Create a new db connection"
  :db/impl)

(defmulti query!
  "Query the db and return the result.
   
   Required parameters:
   - `:db/impl` - the implementation to use
   - `:db/query` - the SQL query to execute
   
   Optional parameters:
   - `:db/params` - parameters for the SQL query
   
   Returns a channel containing a map with:
   - `:db/rows` - the rows returned by the query
   - `:db/columns` - the columns returned by the query
   - `:db/error` - error message if query failed"
  :db/impl)
