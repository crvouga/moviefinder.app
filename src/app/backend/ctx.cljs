(ns app.backend.ctx
  (:require [lib.env :as env]
            [lib.kv.inter :as kv]
            [lib.kv.impl]
            [lib.db.inter :as db]
            [lib.fs.impl]
            [lib.db.impl]
            [lib.str :as str]
            [app.auth.session.session-db.inter :as session-db]
            [app.auth.session.session-db.impl]
            [app.user.user-db.inter :as user-db]
            [app.user.user-db.impl]))

(def api-key {:tmdb/api-key (-> "TMDB_API_READ_ACCESS_TOKEN" env/get-else-throw!)})

(def port {:http-server/port (-> "PORT" env/get-else-throw! str/remove-quotes js/parseInt)})

(def db (db/init! {:db/impl :db/impl-better-sqlite3}))

(def kv (kv/init {:kv/impl :kv/impl-fs}))

(def session-db (session-db/init! (merge kv {:session-db/impl :session-db/impl-kv})))

(def user-db (user-db/init! (merge kv {:user-db/impl :user-db/impl-kv})))

(def verify-sms {:verify-sms/impl :verify-sms-impl/fake})

(def ctx
  (merge api-key port kv db session-db user-db verify-sms))

(defn assoc-ctx
  " assoc application configuration to map "
  [i]
  (merge ctx i))

(defn dissoc-ctx
  " dissoc application configuration from map so we don't leak secrets "
  [i]
  (apply dissoc i (keys ctx)))
