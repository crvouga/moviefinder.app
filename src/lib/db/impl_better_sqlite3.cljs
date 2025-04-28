(ns lib.db.impl-better-sqlite3
  (:require [clojure.core.async :as async]
            [lib.db.inter :as db]
            ["better-sqlite3" :as sqlite3]
            [lib.sql :as sql]))

(defn- new-db-instance [config]
  (let [db-path (or (:db/path config) ":memory:")
        db (sqlite3 db-path)]
    db))

(defmethod db/new! :db/impl-better-sqlite3
  [config]
  (let [db (new-db-instance config)]
    (merge config {::sqlite-instance db})))

(defmethod db/query-chan! :db/impl-better-sqlite3
  [conn {:keys [db/query db/params] :or {params []}}]
  (let [sqlite-instance (::sqlite-instance conn)
        raw-sql (sql/sql-query->raw-sql query)
        chan (async/chan 1)]
    (try
      (let [stmt (.prepare ^js sqlite-instance raw-sql)
            is-select-query (re-find #"(?i)^SELECT" raw-sql)
            result (try
                     (if (empty? params)
                       (if is-select-query
                         (.all stmt)
                         (.run stmt))
                       (if is-select-query
                         (.all stmt (clj->js params))
                         (.run stmt (clj->js params))))
                     (catch js/Error e
                       (throw (js/Error. (str "Statement error: " (.-message e))))))
            rows (if is-select-query
                   (js->clj result :keywordize-keys true)
                   [])
            columns (when (and is-select-query (pos? (count rows)))
                      (vec (keys (first rows))))]
        (async/put! chan {:db/rows rows
                          :db/columns columns})
        (async/close! chan))
      (catch js/Error e
        (println "SQL error:" e " raw-sql:" raw-sql " params:" params)
        (async/put! chan {:db/error (.-message e)})
        (async/close! chan)))
    chan))
