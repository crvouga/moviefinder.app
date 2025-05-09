(ns app.frontend.ctx
  (:require
   [cljs.pprint :refer [pprint]]
   [app.frontend.env :as env]))

(def backend-url-dev "http://localhost:8888")

(def backend-url-prod "")

(def backend-url (if (env/dev?) backend-url-dev backend-url-prod))

(def rpc {:rpc/backend-url backend-url})

(def db {:db/impl :db/impl-better-sqlite3})

(def verify-sms {:verify-sms/impl :verify-sms-impl/fake})

(def media-db {:media-db/impl :media-db/impl-rpc})

(def kv {:kv/impl :kv/impl-rpc})

(def feed-db {:feed-db/impl :feed-db/impl-kv})

(def ctx
  (merge db
         rpc
         verify-sms
         media-db
         kv
         feed-db))

(println "Ctx:")
(pprint ctx)