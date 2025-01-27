(ns moviefinder-app.backend.ctx
  (:require
   [core.env :as env]
   [clojure.string :as str]))

(def api-key (str/trim (env/get! "TMDB_API_READ_ACCESS_TOKEN")))

(def port-env (env/get! "PORT"))

(when-not port-env
  (throw (js/Error. "PORT environment variable is not set")))

(def port (js/parseInt port-env))

(def secrets {:tmdb/api-key api-key})

(def ctx {:http-server/port port
          :verify-sms/impl :verify-sms-impl/fake
          :sql/impl :sql-impl/pglite})
