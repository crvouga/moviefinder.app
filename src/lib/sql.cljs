(ns lib.sql
  (:require
   [clojure.string :as str]
   [honey.sql :as sql]))


(defn- escape-param-val [param]
  (if (string? param)
    (str "'" (str/replace param "'" "''") "'")
    (str param)))

(defn- replace-param [sql param]
  (str/replace-first sql "?" (escape-param-val param)))

(defn- params->sql [sql & params]
  (if (empty? params)
    sql
    (reduce replace-param sql params)))

(defn sql-query->raw-sql [query]
  (if (string? query)
    query
    (->> query sql/format (apply params->sql))))