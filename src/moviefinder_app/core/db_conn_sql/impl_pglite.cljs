(ns moviefinder-app.core.db-conn-sql.impl-pglite
  (:require ["@electric-sql/pglite" :as pglite]
            [moviefinder-app.core.db-conn-sql.interface :as db-conn-sql]))

(defmethod db-conn-sql/new :db-conn-sql-impl/pglite
  [i]
  i
  #_(let [conn (js/PGlite.)]
      (assoc i
             :db-conn-sql/impl :db-conn-sql-impl/pglite
             :db-conn-sql/conn conn)))

(defmethod db-conn-sql/query :db-conn-sql-impl/pglite
  [i]
  (let [conn (:db-conn-sql/conn i)
        [result _] (pglite/query conn (:db-conn-sql/query i))]
    result))

(defmethod db-conn-sql/watch :db-conn-sql-impl/pglite
  [_i]
  (atom []))





