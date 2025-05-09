(ns app.media.media-db.impl-tmdb-api.query-plan-item)

(defmulti result-chan! (fn [query-plan-item _ctx] (first query-plan-item)))