(ns app.backend.config
  (:require [core.env :as env]
            [clojure.string :as str]))

(def api-key (-> "TMDB_API_READ_ACCESS_TOKEN" env/get-else-throw!))

(defn remove-quotes [s]
  (-> s (str/replace "\"" "") (str/replace "'" "")))

(def port (-> "PORT" env/get-else-throw! remove-quotes js/parseInt))

(def config {:tmdb/api-key api-key
             :http-server/port port
             :verify-sms/impl :verify-sms-impl/fake
             :db-conn/impl :db-conn-impl/pglite})

(defn assoc-config
  "assoc application configuration to map"
  [i]
  (merge config i))

(defn dissoc-config
  "dissoc application configuration from map so we don't leak secrets"
  [i]
  (apply dissoc i (keys config)))
