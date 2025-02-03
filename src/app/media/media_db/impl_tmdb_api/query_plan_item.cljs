(ns app.media.media-db.impl-tmdb-api.query-plan-item)

(defmulti query-result-chan! (fn [query-plan-item] (first query-plan-item)))