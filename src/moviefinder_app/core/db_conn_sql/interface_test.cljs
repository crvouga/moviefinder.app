(ns moviefinder-app.core.db-conn-sql.interface-test
  (:require [moviefinder-app.core.db-conn-sql.interface :as db-conn-sql]
            [moviefinder-app.core.db-conn-sql.impl]))

(def db-conn
  (db-conn-sql/new {:db-conn-sql/impl :db-conn-sql-impl/pglite}))

(db-conn-sql/query
 db-conn
 "CREATE TABLE IF NOT EXISTS movies (
  id INTEGER PRIMARY KEY, 
  title TEXT, 
  year INTEGER, 
  rating REAL)")

(db-conn-sql/query
 db-conn
 [:insert-into [:movies]
  :columns [:title :year :rating]
  :values [["The Godfather" 1972 9.2]
           ["The Dark Knight" 2008 9.0]
           ["The Godfather: Part II" 1974 9.0]
           ["The Lord of the Rings: The Return of the King" 2003 8.9]
           ["Pulp Fiction" 1994 8.9]
           ["Schindler's List" 1993 8.9]]])

