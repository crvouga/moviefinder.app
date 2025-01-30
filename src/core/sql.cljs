(ns core.sql
  (:require [honey.sql :as sql]
            [clojure.string :as str]))


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

(defn query->raw-sql [query]
  (if (string? query)
    query
    (->> query sql/format (apply params->sql))))