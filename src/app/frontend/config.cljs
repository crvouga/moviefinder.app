(ns app.frontend.config
  (:require
   ;;  [core.db-conn.interface :as db-conn]
   ;;  [core.db-conn.impl]
   [cljs.pprint :refer [pprint]]))

(def backend-url-dev "http://localhost:8888")
(def backend-url-prod "")
(defn dev? []
  (try
    (= (.-hostname js/window.location) "localhost")
    (catch js/Error _ false)))

(def backend-url (if (or false (dev?)) backend-url-dev backend-url-prod))


;; (def db (db-conn/new! {:db-conn/impl :db-conn-impl/pglite}))
(def db {})

(def config
  (merge db
         {:wire/backend-url backend-url
          :verify-sms/impl :verify-sms-impl/fake
          :media-db/impl :media-db-impl/rpc
          ;; :media-db/impl :media-db-impl/dual-source
          :media-db-impl-dual-source/primary (merge db {:media-db/impl :media-db-impl/db-conn})
          :media-db-impl-dual-source/secondary {:media-db/impl :media-db-impl/rpc}}))

(println "Config:")
(pprint config)