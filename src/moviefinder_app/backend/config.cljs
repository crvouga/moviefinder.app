(ns moviefinder-app.backend.config
  (:require
   [core.env :as env]
   [clojure.string :as str]))

(def api-key (str/trim (env/get! "TMDB_API_READ_ACCESS_TOKEN")))

(def port-env (env/get! "PORT"))

(when-not port-env
  (throw (js/Error. "PORT environment variable is not set")))

(def port (js/parseInt port-env))

(def config {:tmdb/api-key api-key
             :http-server/port port
             :verify-sms/impl :verify-sms-impl/fake
             :sql/impl :sql-impl/pglite})

(defn assoc-config
  "assoc application configuration to map"
  [i]
  (merge config i))

(defn dissoc-config
  "dissoc application configuration from map so we don't leak secrets"
  [i]
  (apply dissoc i (keys config)))
