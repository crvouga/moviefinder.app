(ns core.sql.impl-pglite
  (:require #_["@electric-sql/pglite" :as pglite]
   [core.sql.interface :as sql]))

(defmethod sql/new :sql-impl/pglite
  [i]
  i
  #_(let [conn (js/PGlite.)]
      (assoc i
             :sql/impl :sql-impl/pglite
             :sql/conn conn)))

(defmethod sql/query :sql-impl/pglite
  [_i]
  [])

(defmethod sql/watch :sql-impl/pglite
  [_i]
  (atom []))





