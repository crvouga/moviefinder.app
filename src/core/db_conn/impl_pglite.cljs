(ns core.db-conn.impl-pglite
  (:require ["@electric-sql/pglite" :as pglite]
            [core.db-conn.interface :as db-conn]
            [core.promise :as promise]
            [cljs.core.async :refer [go <!]]
            [honey.sql :as sql]))


(defn- result-js->clj [result]
  (-> result
      (js->clj :keywordize-keys true)))

(defn- get-sql [query]
  (if (string? query)
    query
    (let [[sql & params] (sql/format query)]
      (if (empty? params)
        sql
        (reduce (fn [sql param]
                  (.replace sql "?"
                            (if (string? param)
                              (str "'" param "'")
                              (str param))))
                sql
                params)))))

(defn query-chan! [i]
  (let [raw-sql (-> i :sql/query get-sql)
        pglite-inst (-> i ::pglite-inst)
        result-promise (.query pglite-inst raw-sql)
        result-chan (promise/->chan result-promise)]
    (go
      (let [result-js (<! result-chan)
            result (result-js->clj result-js)
            mapped-result (merge i result {:sql/raw-sql raw-sql
                                           :sql/rows (-> result :rows)})]
        (println "[db-conn]"
                 (select-keys mapped-result [:result/type :error/data :error/message :sql/raw-sql :sql/query :sql/rows])
                 "\n")
        mapped-result))))

(defmethod db-conn/new! :sql-impl/pglite
  [i]
  (let [pglite-inst (pglite/PGlite.)]
    (assoc i
           :sql/impl :sql-impl/pglite
           ::pglite-inst pglite-inst)))

(defmethod db-conn/query-chan! :sql-impl/pglite [i]
  (query-chan! i))





