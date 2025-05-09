(ns app.media.media-id
  (:require
   [clojure.string :as str]))

(defn from-tmdb-id [tmdb-id]
  (str "media:tmdb:" tmdb-id))

(defn to-tmdb-id [media-id]
  (-> media-id (str/split #":") (nth 2)))


