(ns core.tmdb-api.fixture
  (:require
   [core.env :as env]))

(def ctx {:tmdb/api-key (-> "TMDB_API_READ_ACCESS_TOKEN" env/get-else-throw!)})
