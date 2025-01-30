(ns moviefinder-app.media.media-db.interface.fixture
  (:require [moviefinder-app.media.media-db.backend]
            [moviefinder-app.backend.config :as config]))

(def configs [{:tmdb/api-key (config/config :tmdb/api-key)
               :media-db/impl :media-db-impl/tmdb-api
               :query/limit 10
               :query/offset 0}])
