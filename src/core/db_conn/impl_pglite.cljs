(ns core.db-conn.impl-pglite
  (:require ["@electric-sql/pglite" :as pglite]
            [core.db-conn.interface :as db-conn]
            [core.promise :as promise]
            [cljs.core.async :refer [go <!]]
            [core.sql :as sql]))

(defn- result-js->clj [result]
  (-> result
      (js->clj :keywordize-keys true)))

(defn- print-result [result]
  (println "[db-conn]"
           (select-keys result [:result/type :error/data :error/message :db-conn/raw-sql :db-conn/query :db-conn/rows])
           "\n")
  result)

(defn query-chan! [i]
  (let [raw-sql (-> i :db-conn/query sql/query->raw-sql)
        pglite-inst (-> i ::pglite-inst)
        result-promise (.query pglite-inst raw-sql)
        result-chan (promise/->chan result-promise)]
    (go
      (let [result-js (<! result-chan)
            result (result-js->clj result-js)
            mapped-result (merge i result {:db-conn/raw-sql raw-sql
                                           :db-conn/rows (-> result :rows)})]
        (print-result mapped-result)
        mapped-result))))

(defmethod db-conn/new! :db-conn-impl/pglite
  [i]
  (let [pglite-inst (pglite/PGlite.)]
    (assoc i ::pglite-inst pglite-inst)))

(defmethod db-conn/query-chan! :db-conn-impl/pglite [i]
  (query-chan! i))





