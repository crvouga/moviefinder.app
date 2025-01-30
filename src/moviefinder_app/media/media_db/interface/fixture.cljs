(ns moviefinder-app.media.media-db.interface.fixture
  (:require [moviefinder-app.media.media-db.backend]
            [moviefinder-app.backend.config :as config]
            [core.db-conn.impl]
            [moviefinder-app.media.media-db.impl]
            [core.db-conn.interface :as db-conn]))

(def db-conn-pglite (db-conn/new! {:db-conn/impl :db-conn-impl/pglite}))

(def config-pglite (merge db-conn-pglite {:media-db/impl :media-db-impl/db-conn}))

(def configs-read-only
  [config-pglite
   #_{:media-db/impl :media-db-impl/fake}
   {:tmdb/api-key (config/config :tmdb/api-key)
    :media-db/impl :media-db-impl/tmdb-api
    :query/limit 10
    :query/offset 0}])

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
