(ns app.media.media-db.inter.fixture
  (:require [app.media.media-db.backend]
            [app.backend.ctx :as config]
            [lib.db.impl]
            [app.media.media-db.impl]))


(def config-tmdb-api {:media-db/impl :media-db/impl-tmdb-api
                      :tmdb/api-key (config/ctx :tmdb/api-key)
                      :q/limit 10
                      :q/offset 0})

(def configs-read-only
  [config-tmdb-api])

(def configs [config-tmdb-api])

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
