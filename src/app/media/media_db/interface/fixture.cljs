(ns app.media.media-db.interface.fixture
  (:require [app.media.media-db.backend]
            [app.backend.config :as config]
            [core.db-conn.impl]
            [app.media.media-db.impl]
            [core.db-conn.interface :as db-conn]))

(def db-conn-pglite (db-conn/new! {:db-conn/impl :db-conn-impl/pglite}))

(def config-pglite (merge db-conn-pglite {:media-db/impl :media-db-impl/db-conn}))

(def config-tmdb-api {:media-db/impl :media-db-impl/tmdb-api
                      :tmdb/api-key (config/config :tmdb/api-key)
                      :query/limit 10
                      :query/offset 0})

(def config-fake {:media-db/impl :media-db-impl/fake})

(def configs-read-only
  [config-pglite
   config-tmdb-api
   #_config-fake])

(def configs [config-pglite])

(def test-media {:media/id "test-id"
                 :media/title "Test Movie"
                 :media/release-date "2023-01-01"
                 :media/overview "Test overview"
                 :media/poster-path "/test.jpg"
                 :media/vote-average 8.5
                 :media/vote-count 100
                 :media/popularity 50.0
                 :media/poster-url "http://test.com/poster.jpg"
                 :media/backdrop-url "http://test.com/backdrop.jpg"})
