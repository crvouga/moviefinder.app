(ns lib.tmdb-api.fixture
  (:require
   [lib.env :as env]))

(def ctx {:tmdb/api-key (-> "TMDB_API_READ_ACCESS_TOKEN" env/get-else-throw!)})
