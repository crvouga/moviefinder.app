(ns core.sql.impl-pglite
  (:require ["@electric-sql/pglite" :as pglite]
            [core.sql.interface :as sql]
            [core.promise :as promise]
            [cljs.core.async :refer [go <!]]))


(defn- result-js->clj [result]
  (-> result
      (js->clj :keywordize-keys true)))

(defn query-chan! [i]
  (let [raw-sql (-> i :sql/query)
        pglite-inst (-> i ::pglite-inst)
        result-promise (.query pglite-inst raw-sql)
        result-chan (promise/->chan result-promise)]
    (go
      (let [result-js (<! result-chan)
            result (result-js->clj result-js)
            mapped-result (merge i {:sql/rows (-> result :rows)})]
        (println "sql query" (select-keys mapped-result [:sql/impl :sql/query :sql/rows]))
        mapped-result))))

(defmethod sql/new! :sql-impl/pglite
  [i]
  (let [pglite-inst (pglite/PGlite.)]
    (assoc i
           :sql/impl :sql-impl/pglite
           ::pglite-inst pglite-inst)))

(defmethod sql/query-chan! :sql-impl/pglite [i]
  (query-chan! i))





