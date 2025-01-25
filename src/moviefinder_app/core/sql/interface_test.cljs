(ns moviefinder-app.core.sql.interface-test
  (:require [moviefinder-app.core.sql.interface :as sql]
            [moviefinder-app.core.sql.impl]))

(def db-conn
  (sql/new {:sql/impl :sql-impl/pglite}))

(sql/query
 db-conn
 "CREATE TABLE IF NOT EXISTS movies (
  id INTEGER PRIMARY KEY, 
  title TEXT, 
  year INTEGER, 
  rating REAL)")

(sql/query
 db-conn
 [:insert-into [:movies]
  :columns [:title :year :rating]
  :values [["The Godfather" 1972 9.2]
           ["The Dark Knight" 2008 9.0]
           ["The Godfather: Part II" 1974 9.0]
           ["The Lord of the Rings: The Return of the King" 2003 8.9]
           ["Pulp Fiction" 1994 8.9]
           ["Schindler's List" 1993 8.9]]])

