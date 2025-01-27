(ns moviefinder-app.media.media-db.interface)

(defmulti query-result-chan! :media-db/impl)

