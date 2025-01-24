(ns moviefinder-app.core.tmdb-api.shared
  (:require [moviefinder-app.core.env :as env]))


(def api-read-access-token (env/get! "TMDB_API_READ_ACCESS_TOKEN"))
(def base-url "https://api.themoviedb.org/3")
(def base-headers
  {:Authorization (str "Bearer " api-read-access-token)})
(def base-params
  {:headers base-headers
   :as :json-strict})
